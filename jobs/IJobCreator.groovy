package jobs

import model.BaseJobDslPipelineModel

/**
 * Interface for Job Creator classes
 */
interface IJobCreator {
    /**
     * Create a Jenkins job with Jenkins DSL
     *
     * @param model  an instance of BaseJobDslPipelineModel with the parameter to create the job
     */
    void createJob(final BaseJobDslPipelineModel model, def job)
}
