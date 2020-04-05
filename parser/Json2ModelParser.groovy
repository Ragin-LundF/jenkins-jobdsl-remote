package parser

import groovy.json.JsonSlurper
import model.JobsModel

final class Json2ModelParser {

    /**
     * Parse the Jenkins JobDSL JSON abstraction to the model
     *
     * @return  JobDslPipelineModel with mapped data from JSON file
     */
    static JobsModel parseJobJsonToModel(String jobDescriptionPath) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        JobsModel jobsModel = jsonSlurper.parse(new File(jobDescriptionPath))

        return jobsModel
    }
}
