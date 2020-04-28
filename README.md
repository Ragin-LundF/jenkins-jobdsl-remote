# Jenkins JobDSL Creation Script #

This script can be used to load Jenkins job definitions from a remote version control system (VCS).

It can be used in a `seed` job, which does a checkout of this repository and the job description.

When this script is triggered by such a seed job, it will load a simple JSON file, which can be used by developers to create/delete/update their jobs.

In combination with the [k8s-jcasc-management](https://github.com/Ragin-LundF/k8s-jcasc-management) it offers the possibility to save everything in your VCS as a backup and to restore a Jenkins completely from code.
It also helps to have a history of jobs (when was a job created, when deleted...).

## Example Jenkins seed job definition ##

A seed job can look like this:

```groovy
job('seed_job') {
  label('jenkins-master')
  multiscm {
      git {
          remote {
              url('https://github.com/Ragin-LundF/jenkins-jobdsl-remote.git')
          }
          extensions {
            relativeTargetDirectory('jobdslscript')
          }
      }
      git {
          remote {
              url('https://github.com/myProjects/jenkins-job-definition.git')
              credentials("myCredentialsIdForMyPrivateRepository")
          }
          extensions {
            relativeTargetDirectory('jobdefinition')
          }
      }
  }
  steps {
      dsl {
          lookupStrategy('SEED_JOB')
          external('jobdslscript/src/JenkinsJobDslRemote.groovy')
      }
  }
  triggers {
      scm('* * * * *')
  }
}
```

# Usage #
First you have to create a seed job like in the example above.

Now you can define a job in a repository. The JSON must have the name `jenkins-dsl-jobs.json`.
You can define as much jobs, as your Jenkins can handle inside one JSON file.

To have a better overview, it is recommended, that different teams have own job repositories.

The structure of the JSON looks like described in the example on buttom.

| element name | type | subtype of | description |
| --- | --- | --- | --- | 
| `multiBranchJobs` | array | *none* | main element for multibranch jobs. It contains a list of multibranch objects, which are defining the jobs. |
| `pipelineJobs` | array | *none* | main element for pipeline jobs. It contains a list of pipelineJob objects, which are defining the jobs. |
| `jobName` | string | *MultibranchJob* or *PipelineJob* | defines the name of the job. (`required`) |
| `jobDescription` | string | *MultibranchJob* or *PipelineJob* | defines the description of the job, which will be shown in Jenkins. |
| `view` | string | *none* | defines the view in which the job will be shown in Jenkins. |
| `pipelineScriptPath` | string | *MultibranchJob* or *PipelineJob* | defines the name of the `Jenkinsfile` script. (`required`) |
| `cronTrigger` | string | *PipelineJob* | defines cron trigger, to force a build at defined times. |
| `remoteTriggerUuid` | string | *PipelineJob* | defines a UUID that can be used to trigger a job from external scripts. |
| `git` | object | *MultibranchJob* or *PipelineJob* | object, that contains the GIT parameter. (`required`) |
| `repositoryId`| string | *MultibranchJob* or *PipelineJob* | defines a GIT repository ID. This is required by the SCM plugin. (`required`) |
| `repositoryUrl`| string | *MultibranchJob* or *PipelineJob* | defines the URL to the GIT repository. (`required`) |
| `repositoryTrigger`| string | *MultibranchJob* or *PipelineJob* | defines a trigger in cron format at which time the SCM plugin should check for new commits. |
| `credentialsId`| string | *MultibranchJob* or *PipelineJob* | defines the credentialsId, which are available through Jenkins to access a private repository. It is also possible to set a `GIT_CREDENTIALS_ID` environment variable at the build slave. If this is defined in the JSON, it overwrites the env variable. |

## Example of the JSON for the job definition ##

```json
{
  "multiBranchJobs": [
    {
      "jobName": "myMultibranchJob",
      "jobDescription": "This is a test job",
      "view": "myJobs",
      "pipelineScriptPath": "Jenkinsfile",
      "git": {
        "repositoryId": "myMultibranchJobId",
        "repositoryUrl": "https://github.com/myProjects/myMultibranchProject.git",
        "repositoryTrigger": "* * * * *",
        "credentialsId": "myGitCredentialsId"
      }
    }
  ],
  "pipelineJobs": [
    {
      "jobName": "myPipelineJob",
      "jobDescription": "This is a test job",
      "pipelineScriptPath": "Jenkinsfile.groovy",
      "cronTrigger": "2 H * * *",
      "remoteTriggerUuid": "618f1dae-9475-41c3-9d17-381ff3c8684e",
      "git": {
        "repositoryId": "myPipelineJobId",
        "repositoryUrl": "https://github.com/myProjects/myPipelineJobProject.git",
        "repositoryTrigger": "* * * * *",
        "credentialsId": "myGitCredentialsId"
      }
    }
  ]
}
```
