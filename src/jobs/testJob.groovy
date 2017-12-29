folder(basePath) {
    description 'This example shows basic folder/job creation.'
}

listView("Release") {
    pipelineJob("testdsl-calls-other-pipeline") {
        logRotator{
            numToKeep 30
        }
        definition {
            cps {
                sandbox()
                script("""
                     try {
                  stage('Checkout') {
                steps {
                         echo 'Hello World'
              			 script {
                         git credentialsId: '062dee70-e83b-4843-ab77-443e5fa6c7ab', url: 'ssh://git@git.swisscom.ch:7999/rst/bonita-adapter.git'
                         def props = readProperties file: 'gradle.properties'
                         echo "${props['version']}"
                         sshagent(['062dee70-e83b-4843-ab77-443e5fa6c7ab']) {
                          sh "git push origin HEAD:props['version'])"
                              }
                    	}
                	  }	
				}
                  stage ('Build') {
                          node{
                              sh "echo 'shell scripts to run static tests...'"
                          }
                      }
                  stage ('Tests') {
                      parallel 'static': {
                           node{
                                sh "echo 'shell scripts to run static tests...'"
                           }
                      },
                      'unit': {
                           node{
                          sh "echo 'shell scripts to run unit tests...'"
                           }
                      },
                      'integration': {
                           node{
                                  sh "echo 'shell scripts to run integration tests...'"
                           }
                      }
                  }
                  stage ('Deploy') {
                     node{  
                         sh "echo 'shell scripts to deploy to server...'"
                     }
                  }
              } catch (err) {
                  currentBuild.result = 'FAILED'
                  throw err
              }
                """.stripIndent())
            }
        }
    }
}