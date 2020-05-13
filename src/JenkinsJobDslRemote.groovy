import com.google.common.base.Throwables
import groovy.transform.Field
import hudson.model.TopLevelItem
import hudson.model.View
import jenkins.model.Jenkins
import jobs.JenkinsJobConstants
import model.JobsModel
import model.MultibranchModel
import model.PipelineJobModel
import parser.Json2ModelParser
import validator.ModelValidator
import hudson.model.Executor
import hudson.model.Result

// define variables
@Field
final String DEFAULT_PIPELINE_SCRIPT_JSON_PATH = "jobdefinition/jenkins-dsl-jobs.json"
@Field
ArrayList<String> definedJobs = [JenkinsJobConstants.SEED_JOB_NAME]
@Field
Executor jenkinsExecutor = hudson.model.Executor.currentExecutor()
@Field
Jenkins jenkinsInstance = Jenkins.instanceOrNull

// first get workspace directory
hudson.FilePath workspace = jenkinsExecutor.getCurrentWorkspace()
println "Workspace: ${workspace?.toURI()?.getPath()}"

// start the script
execute(workspace?.toURI()?.getPath() + DEFAULT_PIPELINE_SCRIPT_JSON_PATH)


/**
 * Execute the job creator
 *
 * @param jobsFile  Path to the jobs file
 */
void execute(String jobsFile) {
    // Load model
    println "[INFO][JSON Parser] Try to parse JSON file from ${jobsFile}"
    JobsModel jobsModel = loadAndParseModel(jobsFile)
    println "[INFO][JSON Parser] Finished..."

    if (jobsModel != null) {
        try {
            // Validating
            validateModel(jobsModel)
            // process jobs
            processJobs(jobsModel)
            // cleanup jobs
            cleanupJobs(definedJobs)
            // set views
            createViewsFromJobs(jobsModel)
            cleanupEmptyViews()
        } catch (Exception e) {
            println Throwables.getStackTraceAsString(e)
            jenkinsExecutor.interrupt(Result.FAILURE)
        }
    }
}

/**
 * Method to load and parse the model.
 * This is outsourced into a method, to allow to add more file formats later
 *
 * @param path  Path to the job definition file
 * @return
 */
protected static JobsModel loadAndParseModel(String path) {
    return Json2ModelParser.parseJobJsonToModel(path)
}

/**
 * Validate the model, that it is basically correct.
 *
 * @param jobsModel     parsed jobs file as JobsModel
 */
protected static void validateModel(JobsModel jobsModel) {
    println "[INFO][Model Validation] Validating the model..."
    ModelValidator.validateModel(jobsModel)
    println "[INFO][Model Validation] Everything looks good so far..."
}

/**
 * Process the jobs to CRUD the jobs in Jenkins
 *
 * @param jobsModel     parsed jobs file as JobsModel
 */
private void processJobs(JobsModel jobsModel) {
    // first create multibranch pipelines
    println "[INFO][Job processor] Processing the jobs..."
    if (jobsModel.getMultiBranchJobs() != null && ! jobsModel.getMultiBranchJobs().isEmpty()) {
        println "[INFO][Job processor]  Processing multibranch jobs..."
        // iterate over the multibranch jobs
        for (MultibranchModel multibranchJob in jobsModel.getMultiBranchJobs()) {
            // first add the job to the list of valid jobs
            definedJobs << multibranchJob.getJobName()
            createMultibranchPipelineJob(multibranchJob)
        }
        println "[INFO][Job processor]  Processing multibranch jobs finished..."
    }

    // second create pipeline jobs
    if (jobsModel.getPipelineJobs() != null && ! jobsModel.getPipelineJobs().isEmpty()) {
        println "[INFO][Job processor]  Processing pipeline jobs..."
        // iterate over the multibranch jobs
        for (PipelineJobModel pipelineJobModel in jobsModel.getPipelineJobs()) {
            // first add the job to the list of valid jobs
            definedJobs << pipelineJobModel.getJobName()
            createPipelineJob(pipelineJobModel)
        }
        println "[INFO][Job processor]  Processing pipeline jobs finished..."
    }
    println "[INFO][Job processor] Finished..."
}

/**
 * Method, that reads current jobs from Jenkins, compares them with the
 * defined jobs and deletes remaining Jenkins jobs.
 *
 * @param definedPipelineJobItemsList   list of all jobs, which are defined in the JSON job description including seed job
 */
void cleanupJobs(ArrayList<String> definedPipelineJobItemsList) {
    println "[INFO][Cleanup] Starting cleanup of jobs..."

    if (jenkinsInstance != null) {
        // get all items from Jenkins
        List<TopLevelItem> currentJenkinsJobItemsList = jenkinsInstance.items

        // Iterate over the Jenkins items to find out which item exists
        ArrayList<TopLevelItem> currentPipelineItems = []
        for (TopLevelItem currentJenkinsJobItem in currentJenkinsJobItemsList) {
            for (String definedPipelineJobItem in definedPipelineJobItemsList) {
                if (currentJenkinsJobItem.fullName.startsWith(definedPipelineJobItem) || currentJenkinsJobItem.fullName.startsWith(JenkinsJobConstants.SEED_JOB_NAME)) {
                    currentPipelineItems << currentJenkinsJobItem
                }
            }
        }

        // remove the pipeline items from the Jenkins job items and store them into a list
        List<TopLevelItem> undefinedJenkinsJobsList = currentJenkinsJobItemsList.minus(currentPipelineItems)

        // if we have jobs in Jenkins, which are not defined in the job model, delete them
        if (!undefinedJenkinsJobsList.isEmpty()) {
            println "[INFO][Cleanup] ---------------- Job items to delete ----------------"
            println undefinedJenkinsJobsList
            println "[INFO][Cleanup] ----------------                     ----------------"

            for (TopLevelItem undefinedJenkinsItem in undefinedJenkinsJobsList) {
                // if it is not a folder, delete the job!
                if ('com.cloudbees.hudson.plugins.folder.Folder' != undefinedJenkinsItem.class.canonicalName) {
                    println "[INFO][Cleanup] Deleting job [${undefinedJenkinsItem.fullName}] from Jenkins"
                    TopLevelItem item = jenkinsInstance.getItem(undefinedJenkinsItem.fullName)
                    item.delete()
                }
            }
        }
    } else {
        println("[WARNING] Jenkins instance was null at JenkinsCleanupTask")
    }
    println "[INFO][Cleanup] Cleanup of jobs finished successful..."
}

/**
 * Cleanup empty views
 */
void cleanupEmptyViews() {
    List<View> jenkinsViewList = jenkinsInstance.getViews()
    if (jenkinsViewList != null && !jenkinsViewList.isEmpty()) {
        for (View jenkinsView : jenkinsViewList) {
            if (jenkinsView.getAllItems() == null || jenkinsView.getAllItems().isEmpty()) {
                jenkinsInstance.deleteView(jenkinsView)
            }
        }
    }
}

/**
 * Create Views for the jobs
 * @param jobsModel JobsModel which should be used to create the view
 */
void createViewsFromJobs(final JobsModel jobsModel) {
    Map<String, List<String>> viewMap = Json2ModelParser.createViewModelMap(jobsModel)
    if (viewMap != null && ! viewMap.isEmpty()) {
        viewMap.each {
            String viewName = it.key
            List<String> jobsList = it.value
            listView(viewName) {
                jobs {
                    for (String jobName : jobsList) {
                        name(jobName)
                    }
                }
                columns {
                    status()
                    weather()
                    name()
                    lastSuccess()
                    lastFailure()
                    lastDuration()
                    buildButton()
                }
            }
        }
    }
}

/**
 * Create multibranchPipelineJob with Jenkins JobDSL language
 *
 * @param model Pipeline model for multibranch pipeline job
 */
void createMultibranchPipelineJob(final MultibranchModel multibranchModel) {
    // define the job with JobDSL closure
    println("[INFO] creating multibranch job (${multibranchModel.getJobName()})...")
    multibranchPipelineJob(multibranchModel.getJobName()) {
        factory {
            workflowBranchProjectFactory {
                scriptPath(multibranchModel.getPipelineScriptPath())
            }
        }
        branchSources {
            git {
                id(multibranchModel.getGit().getRepositoryId())
                remote(multibranchModel.getGit().getRepositoryUrl())
                // as an alternative it is possible to set the credentials id via system environment variable
                String credentials = (multibranchModel.getGit().getCredentialsId() != null) ? multibranchModel.getGit().getCredentialsId() : System.getenv("GIT_CREDENTIALS_ID")
                credentialsId(credentials)
            }
        }
        description(multibranchModel.getJobDescription())
        triggers {
            (multibranchModel.getGit().getRepositoryTrigger() != null) ? cron(multibranchModel.getGit().getRepositoryTrigger()) : ""
        }
        if (multibranchModel.getLogRotator() != null && (multibranchModel.getLogRotator().getDaysToKeep() != null || multibranchModel.getLogRotator().getNumToKeep() != null)) {
            orphanedItemStrategy {
                discardOldItems {
                    (multibranchModel.getLogRotator().getDaysToKeep() != null) ? daysToKeep(multibranchModel.getLogRotator().getDaysToKeep()) : ""
                    (multibranchModel.getLogRotator().getNumToKeep() != null) ? numToKeep(multibranchModel.getLogRotator().getNumToKeep()) : ""
                }
            }
        }
    }
    println("[INFO] finished creating multibranch job (${multibranchModel.getJobName()})")
}

/**
 * Create pipelineJob with Jenkins JobDSL language
 *
 * @param model Pipeline model for pipeline job
 */
void createPipelineJob(final PipelineJobModel pipelineJobModel) {
    println("[INFO] creating pipeline job (${pipelineJobModel.getJobName()})...")
    pipelineJob(pipelineJobModel.getJobName()) {
        description(pipelineJobModel.getJobDescription())
        (pipelineJobModel.disabled === true) ? disabled() : ""
        triggers {
            (pipelineJobModel.getGit().getRepositoryTrigger() != null) ? scm(pipelineJobModel.getGit().getRepositoryTrigger()) : ""
            (pipelineJobModel.getCronTrigger() != null) ? cron(pipelineJobModel.getCronTrigger()) : ""
        }
        (pipelineJobModel.getRemoteTriggerUuid() != null) ? authenticationToken(pipelineJobModel.getRemoteTriggerUuid()) : ""
        definition {
            cpsScm {
                scm {
                    git {
                        remote {
                            name(pipelineJobModel.getGit().getRepositoryId())
                            url(pipelineJobModel.getGit().getRepositoryUrl())
                            String credentialsIdString = (pipelineJobModel.getGit().getCredentialsId() != null) ? pipelineJobModel.getGit().getCredentialsId() : System.getenv("GIT_CREDENTIALS_ID")
                            credentials(credentialsIdString)
                        }
                        (pipelineJobModel.getRemoteBranchName() != null) ? branch("${pipelineJobModel.getRemoteBranchName()}") : ""
                    }
                }
                scriptPath(pipelineJobModel.getPipelineScriptPath())
            }
        }
        if (pipelineJobModel.getLogRotator() != null && (pipelineJobModel.getLogRotator().getDaysToKeep() != null || pipelineJobModel.getLogRotator().getNumToKeep() != null)) {
            logRotator {
                (pipelineJobModel.getLogRotator().getDaysToKeep() != null) ? daysToKeep(pipelineJobModel.getLogRotator().getDaysToKeep()) : ""
                (pipelineJobModel.getLogRotator().getNumToKeep() != null) ? artifactNumToKeep(pipelineJobModel.getLogRotator().getNumToKeep()) : ""
            }
        }
    }
    println("[INFO] finished creating pipeline job (${pipelineJobModel.getJobName()})")
}