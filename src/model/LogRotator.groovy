package model

import groovy.transform.ToString

/**
 * Represents the log rotation of the jobs
 */
@ToString(includeNames = true, includeFields = true, ignoreNulls = true)
class LogRotator {
    /**
     * represents the days to keep the logfiles.
     * Default: 10
     */
    Integer daysToKeep = 10
    /**
     * represents the number of artifacts to keep
     * Default: 5
     */
    Integer numToKeep = 5
}
