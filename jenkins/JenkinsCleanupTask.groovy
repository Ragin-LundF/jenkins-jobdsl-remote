package jenkins

import jenkins.model.*
import hudson.model.*
import jobs.JenkinsJobConstants

class JenkinsCleanupTask {
    /**
     * Method, that reads current jobs from Jenkins, compares them with the
     * defined jobs and deletes remaining Jenkins jobs.
     *
     * @param definedPipelineJobItemsList   list of all jobs, which are defined in the JSON job description including seed job
     */
    static void cleanupJobs(ArrayList<String> definedPipelineJobItemsList) {
        println "[INFO][Cleanup] Starting cleanup of jobs..."

        // get all items from Jenkins
        List<String> currentJenkinsJobItemsList = Jenkins.instance.items

        // Iterate over the Jenkins items to find out which item exists
        ArrayList<String> currentPipelineItems = []
        for (currentJenkinsJobItem in currentJenkinsJobItemsList) {
            for (String definedPipelineJobItem in definedPipelineJobItemsList) {
                if (currentJenkinsJobItem.fullName.startsWith(definedPipelineJobItem) || currentJenkinsJobItem.fullName.startsWith(JenkinsJobConstants.SEED_JOB_NAME)) {
                    currentPipelineItems << currentJenkinsJobItem
                }
            }
        }

        // remove the pipeline items from the Jenkins job items and store them into a list
        List undefinedJenkinsJobsList = currentJenkinsJobItemsList.minus(currentPipelineItems)

        // if we have jobs in Jenkins, which are not defined in the job model, delete them
        if (! undefinedJenkinsJobsList.isEmpty()) {
            println "[INFO][Cleanup] ---------------- Job items to delete ----------------"
            println undefinedJenkinsJobsList
            println "[INFO][Cleanup] ----------------                     ----------------"

            for (undefinedJenkinsItem in undefinedJenkinsJobsList) {
                // if it is not a folder, delete the job!
                if ('com.cloudbees.hudson.plugins.folder.Folder' != undefinedJenkinsItem.class.canonicalName) {
                    println "[INFO][Cleanup]    Deleting job [${undefinedJenkinsItem.fullName}] from Jenkins"
                    undefinedJenkinsItem.delete()
                }
            }
        }
        println "[INFO][Cleanup] Cleanup of jobs finished..."
    }
}
