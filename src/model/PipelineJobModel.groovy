package model

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true, ignoreNulls = true)
class PipelineJobModel extends BaseJobDslPipelineModel {
    // cron trigger to execute the job independent of scm
    String cronTrigger
    // authentication token for external script access to trigger a build
    String remoteTriggerUuid
    // name of a specific branch, that should be handled in this job
    String remoteBranchName
}
