import jenkins.JenkinsCleanupTask
import jobs.IJobCreator
import jobs.JenkinsJobConstants
import jobs.MultibranchJobCreator
import jobs.PipelineJobCreator
import model.JobsModel
import model.MultibranchModel
import model.PipelineJobModel
import parser.Json2ModelParser
import validator.ModelValidator

JenkinsJobDslRemoteScript jenkinsJobDslRemote = new JenkinsJobDslRemoteScript()
jenkinsJobDslRemote.execute(System.getenv("WORKDIR") + "/${JenkinsJobDslRemoteScript.DEFAULT_PIPELINE_SCRIPT_JSON_PATH}")

/**
 * Jenkins JobDSL Remote Script
 */
class JenkinsJobDslRemoteScript {
    public final static String DEFAULT_PIPELINE_SCRIPT_JSON_PATH = "../jobdefinition/jenkins-dsl-jobs.json"
    private ArrayList<String> definedJobs = [JenkinsJobConstants.SEED_JOB_NAME]

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
            JenkinsCleanupTask.cleanupJobs(this.definedJobs)
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
        assert new File(path).exists() : "ERROR: Unable to load job definition file (${path})"
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
            IJobCreator multibranchJobCreator = new MultibranchJobCreator()
            for (MultibranchModel multibranchJob in jobsModel.getMultiBranchJobs()) {
                // first add the job to the list of valid jobs
                this.definedJobs << multibranchJob.getJobName()
                multibranchJobCreator.createJob(multibranchJob)
            }
            println "[INFO][Job processor]  Processing multibranch jobs finished..."
        }

        // second create pipeline jobs
        if (jobsModel.getPipelineJobs() != null && ! jobsModel.getPipelineJobs().isEmpty()) {
            println "[INFO][Job processor]  Processing pipeline jobs..."
            // iterate over the multibranch jobs
            IJobCreator pipelineJobCreator = new PipelineJobCreator()
            for (PipelineJobModel pipelineJobModel in jobsModel.getPipelineJobs()) {
                // first add the job to the list of valid jobs
                this.definedJobs << pipelineJobModel.getJobName()
                pipelineJobCreator.createJob(pipelineJobModel)
            }
            println "[INFO][Job processor]  Processing pipeline jobs finished..."
        }
        println "[INFO][Job processor] Finished..."
    }
}
