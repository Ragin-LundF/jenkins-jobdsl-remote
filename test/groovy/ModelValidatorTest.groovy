import groovy.test.GroovyTestCase
import model.BaseJobDslPipelineModel
import model.GitModel
import model.JobsModel
import model.MultibranchModel
import model.PipelineJobModel
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import validator.ModelValidator

class ModelValidatorTest extends GroovyTestCase {
    @Test
    @DisplayName(value = "Accept null JobsModel")
    void testValidateModelNullModel() {
        ModelValidator.validateModel(null)
    }

    @Test
    @DisplayName(value = "Accept empty JobsModel")
    void testValidateModelEmptyModel() {
        JobsModel jobsModel = new JobsModel()
        ModelValidator.validateModel(jobsModel)
    }

    @Test
    @DisplayName(value = "MultibranchModel list with null element")
    void testValidateModelEmptyMultibranchModelEntry() {
        List<MultibranchModel> multibranchModelList = new ArrayList<MultibranchModel>()
        // add wrong empty entry
        multibranchModelList.add(null)

        // create jobs model and add wrong list with null entry
        JobsModel jobsModel = new JobsModel()
        jobsModel.setMultiBranchJobs(multibranchModelList)

        // validate
        try {
            ModelValidator.validateModel(jobsModel)
            throw new Error()
        } catch (AssertionError ae) {
            // it should fail, so if an assertion error was thrown, then everything was fine and expected
            assertEquals("java.lang.AssertionError", ae.getClass().getName())
        } catch (Error e) {
            fail()
        }
    }

    @Test
    @DisplayName(value = "MultibranchModel with entry with null jobName")
    void testValidateModelEmptyMultibranchModelJobName() {
        // create valid multibranch model
        MultibranchModel multibranchModel = new MultibranchModel()
        createValidModel(multibranchModel)

        // manipulate jobName to wrong null entry
        multibranchModel.jobName = null

        // create multibranch model list and add wrong entry
        List<MultibranchModel> multibranchModelList = new ArrayList<MultibranchModel>()
        multibranchModelList.add(multibranchModel)

        // create jobs model and add wrong multibranch job list
        JobsModel jobsModel = new JobsModel()
        jobsModel.setMultiBranchJobs(multibranchModelList)

        // validate
        try {
            ModelValidator.validateModel(jobsModel)
            throw new Error()
        } catch (AssertionError ae) {
            // it should fail, so if an assertion error was thrown, then everything was fine and expected
            assertEquals("java.lang.AssertionError", ae.getClass().getName())
        } catch (Error e) {
            fail()
        }
    }

    @Test
    @DisplayName(value = "PipelineJobModel list with null element")
    void testValidateModelEmptyPipelineJobModelEntry() {
        List<PipelineJobModel> pipelineJobModelList = new ArrayList<PipelineJobModel>()
        // add wrong empty entry
        pipelineJobModelList.add(null)

        // create jobs model and add wrong pipeline job list
        JobsModel jobsModel = new JobsModel()
        jobsModel.setPipelineJobs(pipelineJobModelList)

        // validate
        try {
            ModelValidator.validateModel(jobsModel)
            throw new Error()
        } catch (AssertionError ae) {
            // it should fail, so if an assertion error was thrown, then everything was fine and expected
            assertEquals("java.lang.AssertionError", ae.getClass().getName())
        } catch (Error e) {
            fail()
        }
    }

    @Test
    @DisplayName(value = "PipelineJobModel with entry with null jobName")
    void testValidateModelEmptyPipelineJobModelJobName() {
        // create valid model
        PipelineJobModel pipelineJobModel = new PipelineJobModel()
        createValidModel(pipelineJobModel)

        // add wrong jobName
        pipelineJobModel.jobName = null

        // create list of pipeline jobs and add wrong entry
        List<PipelineJobModel> pipelineJobModelList = new ArrayList<PipelineJobModel>()
        pipelineJobModelList.add(pipelineJobModel)

        // create jobs model and add pipeline jobs
        JobsModel jobsModel = new JobsModel()
        jobsModel.setPipelineJobs(pipelineJobModelList)

        // validate
        try {
            ModelValidator.validateModel(jobsModel)
            throw new Error()
        } catch (AssertionError ae) {
            // it should fail, so if an assertion error was thrown, then everything was fine and expected
            assertEquals("java.lang.AssertionError", ae.getClass().getName())
        } catch (Error e) {
            fail()
        }
    }

    @Test
    void testValidateModelEmptyGitModel() {
        // create valid model
        PipelineJobModel pipelineJobModel = new PipelineJobModel()
        createValidModel(pipelineJobModel)

        // manipulate model with wrong git entry
        pipelineJobModel.git = null

        // create new pipeline job list and add wrong entry
        List<PipelineJobModel> pipelineJobModelList = new ArrayList<PipelineJobModel>()
        pipelineJobModelList.add(pipelineJobModel)

        // create jobs model and add wrong list
        JobsModel jobsModel = new JobsModel()
        jobsModel.setPipelineJobs(pipelineJobModelList)

        // validate
        try {
            ModelValidator.validateModel(jobsModel)
            throw new Error()
        } catch (AssertionError ae) {
            // it should fail, so if an assertion error was thrown, then everything was fine and expected
            assertEquals("java.lang.AssertionError", ae.getClass().getName())
        } catch (Error e) {
            fail()
        }
    }

    /**
     * Create a valid model
     *
     * @return filled model, that is valid
     */
    private void createValidModel(BaseJobDslPipelineModel model) {
        model.jobName = "A job"
        model.jobDescription = "A description"
        model.pipelineScriptPath = "Jenkinsfile"
        model.view = "myView"
        model.git = createValidGitModel()
    }

    /**
     * Create a valid gitmodel
     * @return
     */
    private GitModel createValidGitModel() {
        GitModel gitModel = new GitModel()
        gitModel.repositoryId = "repoId"
        gitModel.repositoryUrl = "http://repo.com/myrepo.git"
        gitModel.repositoryTrigger = "* * * * *"

        return gitModel
    }
}
