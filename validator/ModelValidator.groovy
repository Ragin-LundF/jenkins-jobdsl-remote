package validator

import model.BaseJobDslPipelineModel
import model.JobsModel
import model.MultibranchModel
import model.PipelineJobModel

final class ModelValidator {
    /**
     * Validate the complete model for required fields
     *
     * @param jobsModel     JobsModel instance
     */
    static void validateModel(final JobsModel jobsModel) throws AssertionError {
        if (jobsModel != null) {
            validateMultiBranchJobs(jobsModel.getMultiBranchJobs())
            validatePipelineJobs(jobsModel.getPipelineJobs())
        }
    }

    /**
     * If multibranch job definitions are present, iterate over them and validate entries
     *
     * @param multibranchModelList      list of MultiBranchModel entries
     */
    private static void validateMultiBranchJobs(final List<MultibranchModel> multibranchModelList) {
        if (multibranchModelList != null) {
            for (MultibranchModel multibranchModel in multibranchModelList) {
                validateMultiBranchJob(multibranchModel)
            }
        }
    }

    /**
     * Validate a multibranch model definition
     *
     * @param multibranchModel  MultiBranchModel entry
     */
    private static void validateMultiBranchJob(final MultibranchModel multibranchModel) {
        assert multibranchModel != null : "Multibranch pipeline detected, but no entry found!"
        assert multibranchModel.getJobName() != null : "Found multibranch pipeline without definition of a jobName."
        validateGitModel(multibranchModel)
    }

    /**
     * If pipeline job definitions are present, iterate over them and validate entries
     *
     * @param pipelineJobModelList      list of PipelineJobModel entries
     */
    private static void validatePipelineJobs(final List<PipelineJobModel> pipelineJobModelList) {
        if (pipelineJobModelList != null) {
            for (PipelineJobModel pipelineJobModel in pipelineJobModelList) {
                validatePipelineJob(pipelineJobModel)
            }
        }
    }

    /**
     * Validate a pipelineJob model definition
     *
     * @param pipelineJob  PipelineJobModel entry
     */
    private static void validatePipelineJob(final PipelineJobModel pipelineJob) {
        assert pipelineJob != null : "Pipeline job detected, but no entry found!"
        assert pipelineJob.getJobName() != null : "Found pipeline job without definition of a jobName."
        validateGitModel(pipelineJob)
    }

    /**
     * Validate Git definition inside of a base job definition
     *
     * @param pipelineModel     BaseJobDslPipelineModel
     */
    private static void validateGitModel(final BaseJobDslPipelineModel pipelineModel) {
        assert pipelineModel.getGit() != null: "No Git definition for the job (${pipelineModel?.getJobName()}) found."
        assert pipelineModel.getGit().getRepositoryUrl() != null: "No Git repository definition for the job (${pipelineModel?.getJobName()}) found."
        assert pipelineModel.getGit().getRepositoryId() != null: "No Git repository ID definition for the job (${pipelineModel?.getJobName()}) found."
    }
}
