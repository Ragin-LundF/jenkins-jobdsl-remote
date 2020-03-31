import model.JobsModel
import parser.Json2ModelParser

/**
 * Jenkins JobDSL Remote Script
 */
class JenkinsJobDslRemote {
    private static final String DEFAULT_PIPELINE_SCRIPT_JSON_PATH = "../jobdefinition/jenkins-dsl-jobs.json"
    /**
     * Main method.
     * This method will be executed, when Jenkins calls this as script.
     *
     * @param args  arguments (not used)
     */
    static void main(String[] args) {
        // Load model
        JobsModel jobsModel
        if (args != null && args.size() > 0) {
            jobsModel = loadAndParseModel(args[0])
        } else {
            jobsModel = loadAndParseModel(DEFAULT_PIPELINE_SCRIPT_JSON_PATH)
        }

        println(jobsModel)
    }

    static JobsModel loadAndParseModel(String path) {
        return Json2ModelParser.parseJobJsonToModel(path)
    }
}
