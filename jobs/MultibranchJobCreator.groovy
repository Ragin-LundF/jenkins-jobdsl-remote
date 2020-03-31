package jobs

import model.BaseJobDslPipelineModel
import model.MultibranchModel

class MultibranchJobCreator implements IJobCreator {
    /**
     * Create a multibranchPipelineJob with Jenkins DSL
     *
     * @param model  an instance of MultibranchModel with the parameter to create the job
     */
    @Override
    void createJob(final BaseJobDslPipelineModel model) {
        if (model instanceof MultibranchModel) {
            MultibranchModel multibranchModel = model as MultibranchModel
            // define the job with JobDSL closure
            multibranchPipelineJob(multibranchModel.getJobName()) {
                factory {
                    workflowBranchProjectFactory {
                        scriptPath(multibranchModel.getPipelineScriptPath())
                    }
                }
                branchSources {
                    git {
                        id(multibranchModel.getGit().getRepositoryId())
                        remote(multibranchModel.getGit().getRepositoryUrl())
                        // as an alternative it is possible to set the credentials id via system environment variable
                        String credentials = (multibranchModel.getGit().getCredentialsId() != null) ? multibranchModel.getGit().getCredentialsId() : System.getenv("GIT_CREDENTIALS_ID")
                        credentialsId(credentials)
                    }
                }
                description(multibranchModel.getJobDescription())
                triggers {
                    cron(multibranchModel.getGit().getRepositoryTrigger())
                }
            }
        } else {
            println "[ERROR][create multibranch pipeline job] provided model was not a MultibranchModel"
            println "[ERROR][create multibranch pipeline job] ${model}"
        }
    }
}
