package model

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true, ignoreNulls = true)
class BaseJobDslPipelineModel {
    /**
     * Is this job disabled (optional; default: false)
     */
    String disabled = false
    /**
     * Name of the job (mandatory)
     */
    String jobName
    /**
     * Description of the job (mandatory)
     */
    String jobDescription
    /**
     * Path to the Jenkins pipeline script like Jenkinsfile (optional)
     * Default: Jenkinsfile
     */
    String pipelineScriptPath = "Jenkinsfile"
    /**
     * View to which this job should be assigned (optional)
     */
    String view
    /**
     * Log rotators for the job (optional)
     */
    LogRotator logRotator = new LogRotator()
    /**
     * Git Model for the repository (mandatory)
     */
    GitModel git
}
