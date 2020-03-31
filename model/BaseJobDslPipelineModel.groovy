package model

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true, ignoreNulls = true)
class BaseJobDslPipelineModel {
    String jobName
    String jobDescription
    String pipelineScriptPath
    GitModel git
}
