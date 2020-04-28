package model

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true, ignoreNulls = true)
class PipelineJobModel extends BaseJobDslPipelineModel {
    // cron trigger to execute the job independent of scm
    String cronTrigger
    String remoteTriggerUuid
}
