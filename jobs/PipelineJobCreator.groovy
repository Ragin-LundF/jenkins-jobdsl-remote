package jobs

import model.BaseJobDslPipelineModel
import model.PipelineJobModel

class PipelineJobCreator implements IJobCreator {
    /**
     * create a pipeline job by using the Jenkins DSL language
     *
     * @param model an instance of PipelineJobModel with the parameter to create the job
     */
    @Override
    void createJob(final BaseJobDslPipelineModel model) {
        if (model instanceof PipelineJobModel) {
            PipelineJobModel pipelineJobModel = model as PipelineJobModel
            pipelineJob(pipelineJobModel.getJobName()) {
                description(pipelineJobModel.getJobDescription())
                triggers {
                    scm(pipelineJobModel.getGit().getRepositoryTrigger())
                    cron(pipelineJobModel.getCronTrigger())
                }
                definition {
                    cpsScm {
                        scm {
                            git {
                                remote {
                                    name(pipelineJobModel.getGit().getRepositoryId())
                                    url(pipelineJobModel.getGit().getRepositoryUrl())
                                    String credentials = (pipelineJobModel.getGit().getCredentialsId() != null) ? pipelineJobModel.getGit().getCredentialsId() : System.getenv("GIT_CREDENTIALS_ID")
                                    credentials(credentials)
                                }
                                scriptPath(pipelineJobModel.getPipelineScriptPath())
                            }
                        }
                    }
                }
            }
        } else {
            println "[ERROR][create pipeline job] provided model was not a PipelineJobModel"
            println "[ERROR][create multibranch pipeline job] ${model}"
        }
    }
}
