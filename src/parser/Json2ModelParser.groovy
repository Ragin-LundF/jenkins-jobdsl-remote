package parser

import groovy.json.JsonSlurper
import model.BaseJobDslPipelineModel
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

    /**
     * Create a map with the views from parsed JobModel
     * Map looks like: key=viewName, value=List<String> with jobsName
     *
     * @param jobsModel     parsed jobs model
     * @return              map with views
     */
    static Map<String, List<String>> createViewModelMap(JobsModel jobsModel) {
        // parse Jobs
        Map<String, List<String>> viewMap = parseJobsFromModelToView(null, jobsModel.getPipelineJobs())
        viewMap = parseJobsFromModelToView(viewMap, jobsModel.getMultiBranchJobs())

        return viewMap
    }

    /**
     * Parse the jobs and check if a view and jobName was set.
     * If both are set, then add the view name and the job to the map.
     *
     * If the map was empty, then it creates a new one
     *
     * @param viewMap   Map that can be null/empty or contains already views and jobs.
     * @param baseJobDslPipelineModelList   Model which contains the jobs
     * @return  Map with syntax: key=viewName, value=List with jobName
     */
    static Map<String, List<String>> parseJobsFromModelToView(Map<String, List<String>> viewMap, List<BaseJobDslPipelineModel> baseJobDslPipelineModelList) {
        // check map to be sure that everything is ok
        if (viewMap == null) {
            viewMap = new HashMap<String, List<String>>()
        }
        // check the models and add the view if it is not empty
        if (baseJobDslPipelineModelList != null && ! baseJobDslPipelineModelList.isEmpty()) {
            for (BaseJobDslPipelineModel baseJobDslPipelineModel : baseJobDslPipelineModelList) {
                if (baseJobDslPipelineModel.getView()?.trim() && baseJobDslPipelineModel.getJobName()?.trim()) {
                    if (viewMap.containsKey(baseJobDslPipelineModel.getView())) {
                        viewMap.get(baseJobDslPipelineModel.getView()).add(baseJobDslPipelineModel.getJobName())
                    } else {
                        List<String> jobNameList = new ArrayList<>()
                        jobNameList.add(baseJobDslPipelineModel.getJobName())
                        viewMap.put(baseJobDslPipelineModel.getView(), jobNameList)
                    }
                }
            }
        }

        return viewMap
    }
}
