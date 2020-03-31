package model

import groovy.transform.ToString

@ToString(includeNames = true, includeFields = true)
class JobsModel {
    List<MultibranchModel> multiBranchJobs
    List<PipelineJobModel> pipelineJobs
}
