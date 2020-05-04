import groovy.test.GroovyTestCase
import model.JobsModel
import model.MultibranchModel
import model.PipelineJobModel
import org.junit.jupiter.api.Test
import parser.Json2ModelParser

class Json2ModelParserTest extends GroovyTestCase {
    @Test
    void testParseJobJsonToModel() {
        // read model
        JobsModel jenkinsJobModel = readJobsModelWithJson2ModelParser()

        // assert basic structure
        assertNotNull(jenkinsJobModel.multiBranchJobs)
        assertNotNull(jenkinsJobModel.pipelineJobs)
        assertEquals(3, jenkinsJobModel.multiBranchJobs.size())
        assertEquals(1, jenkinsJobModel.pipelineJobs.size())

        // validate multibranchModel
        assertEqualsMultiBranchJobModel(
                "firstMultiBranchJob",
                "First job",
                "mainView",
                "Jenkinsfile",
                "firstMultiBranchJob",
                "https://github.com/myProjects/firstMultiBranchJob.git",
                "* * * * *",
                "myGitCredentialsId",
                null,
                null,
                jenkinsJobModel.multiBranchJobs.get(0) as MultibranchModel
        )
        assertEqualsMultiBranchJobModel(
                "secondMultiBranchJob",
                "Second job",
                null,
                "Jenkinsfile",
                "secondMultiBranchJob",
                "https://github.com/myProjects/secondMultiBranchJob.git",
                "* * * * *",
                "myGitCredentialsId",
                10,
                15,
                jenkinsJobModel.multiBranchJobs.get(1) as MultibranchModel
        )
        assertEqualsMultiBranchJobModel(
                "thirdMultiBranchJob",
                "Third job",
                null,
                "Jenkinsfile",
                "thirdMultiBranchJob",
                "https://github.com/myProjects/thirdMultiBranchJob.git",
                "* * * * *",
                "myGitCredentialsId",
                5,
                10,
                jenkinsJobModel.multiBranchJobs.get(2) as MultibranchModel
        )

        // validate pipelineJob
        assertEqualsPipelineJobModel(
                "myPipelineJob",
                "This is a test job",
                "pipelineView",
                "Jenkinsfile.groovy",
                "2 H * * *",
                "618f1dae-9475-41c3-9d17-381ff3c8684e",
                "master",
                "myPipelineJobId",
                "https://github.com/myProjects/myPipelineJobProject.git",
                "* * * * *",
                "myGitCredentialsId",
                10,
                15,
                jenkinsJobModel.pipelineJobs.get(0) as PipelineJobModel
        )
    }

    @Test
    void testCreateViewModelMap() {
        // read jobs
        JobsModel jobsModel = readJobsModelWithJson2ModelParser()
        assertNotNull(jobsModel)

        // create view map
        Map<String, List<String>> viewMap = Json2ModelParser.createViewModelMap(jobsModel)
        assertNotNull(viewMap)
        assertEquals(2, viewMap.size())
        assertTrue(viewMap.find {it.key == "mainView"}.value.contains("firstMultiBranchJob"))
        assertTrue(viewMap.find {it.key == "pipelineView"}.value.contains("myPipelineJob"))
    }

    /**
     * Check the MultibranchModel
     *
     * @param jobName               expected name of the multibranch job
     * @param jobDescription        expected description of the multibranch job
     * @param view                  expected view of the multibranch job
     * @param pipelineScriptPath    expected pipeline scriptPath of the multibranch job
     * @param gitRepositoryId       expected repositoryId of the Git object of the multibranch job
     * @param gitRepositoryUrl      expected repositoryUrl of the Git object of the multibranch job
     * @param gitRepositoryTrigger  expected repositoryTrigger of the Git object of the multibranch job
     * @param gitCredentialsId      expected credentialsId of the Git object of the multibranch job
     * @param logRotatorNumToKeep   expected logRotator.numToKeep of the multibranch job
     * @param logRotatorDaysToKeep  expected logRotator.daysToKeep of the multibranch job
     * @param multibranchModel      filled instance of MultiBranchModel which should be compared (actual)
     */
    private void assertEqualsMultiBranchJobModel(
            String jobName,
            String jobDescription,
            String view,
            String pipelineScriptPath,
            String gitRepositoryId,
            String gitRepositoryUrl,
            String gitRepositoryTrigger,
            String gitCredentialsId,
            Integer logRotatorNumToKeep,
            Integer logRotatorDaysToKeep,
            MultibranchModel multibranchModel
    ) {
        // basic data
        assertEquals(jobName, multibranchModel.jobName)
        assertEquals(jobDescription, multibranchModel.jobDescription)
        assertEquals(view, multibranchModel.view)
        assertEquals(pipelineScriptPath, multibranchModel.pipelineScriptPath)

        // log rotate data
        assertNotNull(multibranchModel.logRotator)
        assertEquals(logRotatorDaysToKeep, multibranchModel.logRotator.daysToKeep)
        assertEquals(logRotatorNumToKeep, multibranchModel.logRotator.numToKeep)

        // git data
        assertNotNull(multibranchModel.git)
        assertEquals(gitRepositoryId, multibranchModel.git.repositoryId)
        assertEquals(gitRepositoryUrl, multibranchModel.git.repositoryUrl)
        assertEquals(gitRepositoryTrigger, multibranchModel.git.repositoryTrigger)
        assertEquals(gitCredentialsId, multibranchModel.git.credentialsId)
    }
    /**
     * Check the PipelineJobModel
     *
     * @param jobName               expected name of the pipeline job
     * @param jobDescription        expected description of the pipeline job
     * @param view                  expected view of the pipeline job
     * @param pipelineScriptPath    expected pipeline scriptPath of the pipeline job
     * @param cronTrigger           expected cronTrigger of the pipeline job
     * @param remoteTriggerUuid     expected remoteTriggerUuid of the pipeline job
     * @param remoteBranchName      expected remoteBranchName of the pipeline job
     * @param gitRepositoryId       expected repositoryId of the Git object of the pipeline job
     * @param gitRepositoryUrl      expected repositoryUrl of the Git object of the pipeline job
     * @param gitRepositoryTrigger  expected repositoryTrigger of the Git object of the pipeline job
     * @param gitCredentialsId      expected credentialsId of the Git object of the pipeline job
     * @param logRotatorNumToKeep   expected logRotator.numToKeep of the pipeline job
     * @param logRotatorDaysToKeep  expected logRotator.daysToKeep of the pipeline job
     * @param pipelineJobModel      filled instance of MultiBranchModel which should be compared (actual)
     */
    private void assertEqualsPipelineJobModel(
            String jobName,
            String jobDescription,
            String view,
            String pipelineScriptPath,
            String cronTrigger,
            String remoteTriggerUuid,
            String remoteBranchName,
            String gitRepositoryId,
            String gitRepositoryUrl,
            String gitRepositoryTrigger,
            String gitCredentialsId,
            Integer logRotatorNumToKeep,
            Integer logRotatorDaysToKeep,
            PipelineJobModel pipelineJobModel
    ) {
        // basic job data
        assertEquals(jobName, pipelineJobModel.jobName)
        assertEquals(jobDescription, pipelineJobModel.jobDescription)
        assertEquals(view, pipelineJobModel.view)
        assertEquals(pipelineScriptPath, pipelineJobModel.pipelineScriptPath)
        assertEquals(cronTrigger, pipelineJobModel.cronTrigger)
        assertEquals(remoteTriggerUuid, pipelineJobModel.remoteTriggerUuid)
        assertEquals(remoteBranchName, pipelineJobModel.remoteBranchName)

        // log rotator data
        assertNotNull(pipelineJobModel.logRotator)
        assertEquals(logRotatorDaysToKeep, pipelineJobModel.logRotator.daysToKeep)
        assertEquals(logRotatorNumToKeep, pipelineJobModel.logRotator.numToKeep)

        // git data
        assertNotNull(pipelineJobModel.git)
        assertEquals(gitRepositoryId, pipelineJobModel.git.repositoryId)
        assertEquals(gitRepositoryUrl, pipelineJobModel.git.repositoryUrl)
        assertEquals(gitRepositoryTrigger, pipelineJobModel.git.repositoryTrigger)
        assertEquals(gitCredentialsId, pipelineJobModel.git.credentialsId)
    }

    /**
     * Read jobs with Json2ModelParser
     *
     * @return parsed JobsModel object from jenkins-jobdsl-jobs.json
     */
    private JobsModel readJobsModelWithJson2ModelParser() {
        JobsModel jobsModel = Json2ModelParser.parseJobJsonToModel(getClass().getResource("jenkins-jobdsl-jobs.json").toURI().path)
        assertNotNull(jobsModel)

        return jobsModel
    }
}
