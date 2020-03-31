package model

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
class GitModel {
    // unique repository ID
    String repositoryId
    // URL to the repository
    String repositoryUrl
    // Trigger in Cron format. Defines how often the scm should be checked
    String repositoryTrigger
}
