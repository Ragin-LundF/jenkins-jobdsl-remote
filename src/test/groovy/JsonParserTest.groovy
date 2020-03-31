import model.BaseJobDslPipelineModel
import model.GitModel
import model.JobsModel
import model.MultibranchModel
import model.PipelineJobModel
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import parser.Json2ModelParser

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull

class JsonParserTest {
    @Test
    @DisplayName(value = "JSON file with one multibranch")
    void testMultiBranch() {
        JobsModel jobsModel = Json2ModelParser.parseJobJsonToModel(getClass().getResource('jobs-multibranch-single.json').getPath())

        // validate, that everything was set
        assertNotNull(jobsModel)
        assertNotNull(jobsModel.getMultiBranchJobs())
        assertNull(jobsModel.getPipelineJobs())
        assertEquals(1, jobsModel.getMultiBranchJobs().size())

        // validate multibranchJobs
        MultibranchModel multibranchModel = jobsModel.getMultiBranchJobs().get(0)
        validateBaseJobDslPipelineModel(multibranchModel)
    }

    @Test
    @DisplayName(value = "JSON file with one pipelineJob")
    void testPipelineJob() {
        JobsModel jobsModel = Json2ModelParser.parseJobJsonToModel(getClass().getResource('jobs-pipeline-single.json').getPath())

        // validate, that everything was set
        assertNotNull(jobsModel)
        assertNull(jobsModel.getMultiBranchJobs())
        assertNotNull(jobsModel.getPipelineJobs())
        assertEquals(1, jobsModel.getPipelineJobs().size())

        // validate pipelineJobs
        PipelineJobModel pipelineJobModel = jobsModel.getPipelineJobs().get(0)
        validateBaseJobDslPipelineModel(pipelineJobModel)
    }

    @Test
    @DisplayName(value = "JSON file with one multiBranchJob and one pipelineJob")
    void testMultiBranchAndPipelineJob() {
        JobsModel jobsModel = Json2ModelParser.parseJobJsonToModel(getClass().getResource('jobs-multi-jobs.json').getPath())

        // validate, that everything was set
        assertNotNull(jobsModel)
        assertNotNull(jobsModel.getMultiBranchJobs())
        assertNotNull(jobsModel.getPipelineJobs())
        assertEquals(1, jobsModel.getPipelineJobs().size())
        assertEquals(1, jobsModel.getPipelineJobs().size())

        // validate multibranchJobs
        MultibranchModel multibranchModel = jobsModel.getMultiBranchJobs().get(0)
        validateBaseJobDslPipelineModel(multibranchModel)
        // validate pipelineJobs
        PipelineJobModel pipelineJobModel = jobsModel.getPipelineJobs().get(0)
        validateBaseJobDslPipelineModel(pipelineJobModel)
    }

    /**
     * Method to test the data of BaseJobDslPipelineModel
     *
     * @param baseJobDslPipelineModel   parsed BasePipelineModel
     */
    static void validateBaseJobDslPipelineModel(BaseJobDslPipelineModel baseJobDslPipelineModel) {
        assertNotNull(baseJobDslPipelineModel)
        assertEquals("myJob", baseJobDslPipelineModel.getJobName())
        assertEquals("This is a test job", baseJobDslPipelineModel.getJobDescription())
        assertEquals("Jenkinsfile", baseJobDslPipelineModel.getPipelineScriptPath())

        // Validate Git object
        assertNotNull(baseJobDslPipelineModel.getGit())
        GitModel gitModel = baseJobDslPipelineModel.getGit()
        assertEquals("my-repo", gitModel.getRepositoryId())
        assertEquals("https://github.com/myrepo.git", gitModel.getRepositoryUrl())
        assertEquals("* * * * *", gitModel.getRepositoryTrigger())
    }
}
