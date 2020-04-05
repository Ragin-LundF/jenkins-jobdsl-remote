import jenkins.JenkinsCleanupTask
import jobs.JenkinsJobConstants
import model.BaseJobDslPipelineModel
import model.JobsModel
import model.MultibranchModel
import model.PipelineJobModel
import parser.Json2ModelParser
import validator.ModelValidator

// define variables
final String DEFAULT_PIPELINE_SCRIPT_JSON_PATH = "jobdefinition/jenkins-dsl-jobs.json"
ArrayList<String> definedJobs = [JenkinsJobConstants.SEED_JOB_NAME]

// first get workspace directory
hudson.FilePath workspace = hudson.model.Executor.currentExecutor().getCurrentWorkspace()
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
        // Validating
        validateModel(jobsModel)
        // process jobs
        processJobs(jobsModel)
        // cleanup jobs
        JenkinsCleanupTask.cleanupJobs(definedJobs)
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
            createMultibranchPipelineJob(multibranchJob, jobsModel.getJobDslJobsMap().get(JobsModel.JOB_TYPE.MULTIBRANCH_JOB))
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
            createPipelineJob(pipelineJobModel, jobsModel.getJobDslJobsMap().get(JobsModel.JOB_TYPE.PIPELINE_JOB))
        }
        println "[INFO][Job processor]  Processing pipeline jobs finished..."
    }
    println "[INFO][Job processor] Finished..."
}

void createMultibranchPipelineJob(final BaseJobDslPipelineModel model) {
    if (model instanceof MultibranchModel) {
        MultibranchModel multibranchModel = model as MultibranchModel
        // define the job with JobDSL closure
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
                cron(multibranchModel.getGit().getRepositoryTrigger())
            }
        }
    } else {
        println "[ERROR][create multibranch pipeline job] provided model was not a MultibranchModel"
        println "[ERROR][create multibranch pipeline job] ${model}"
    }
}

void createPipelineJob(final BaseJobDslPipelineModel model) {
    if (model instanceof PipelineJobModel) {
        PipelineJobModel pipelineJobModel = model as PipelineJobModel
        pipelineJob(pipelineJobModel.getJobName()) {
            description(pipelineJobModel.getJobDescription())
            triggers {
                scm(pipelineJobModel.getGit().getRepositoryTrigger())
                cron(pipelineJobModel.getCronTrigger())
            }
            definition {
                cpsScm {
                    scm {
                        git {
                            remote {
                                name(pipelineJobModel.getGit().getRepositoryId())
                                url(pipelineJobModel.getGit().getRepositoryUrl())
                                String credentials = (pipelineJobModel.getGit().getCredentialsId() != null) ? pipelineJobModel.getGit().getCredentialsId() : System.getenv("GIT_CREDENTIALS_ID")
                                credentials(credentials)
                            }
                            scriptPath(pipelineJobModel.getPipelineScriptPath())
                        }
                    }
                }
            }
        }
    } else {
        println "[ERROR][create pipeline job] provided model was not a PipelineJobModel"
        println "[ERROR][create multibranch pipeline job] ${model}"
    }
}