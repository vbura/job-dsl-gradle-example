import hudson.model.*

String basePath = 'Release'
String repo = 'sheehan/grails-example'

folder(basePath) {
    description 'This example shows basic folder/job creation.'
}

Build build = Executor.currentExecutor().currentExecutable as Build
ParametersAction parametersAction = build.getAction(ParametersAction)

println parametersAction

listView("$basePath") {
    pipelineJob("/test-release") {
        description()
        parameters {
            stringParam('Branch', "${branch}", 'test',)
        }
        logRotator {
            numToKeep 10
        }

        definition {
            cps {
                sandbox()
                script("""
               node {
                     stage("Checkout") {
                            echo 'Hello World'
                            script {
                                git branch:$branch credentialsId: '062dee70-e83b-4843-ab77-443e5fa6c7ab', url: 'ssh://git@git.swisscom.ch:7999/rst/bonita-adapter.git'
                                def props = readProperties file: 'gradle.properties'
                                sh "./gradlew clean"
                            }    
                     }
                    stage ('Build') {
                    
                         sshagent(['062dee70-e83b-4843-ab77-443e5fa6c7ab']) {
                                sh "git add ."
                                sh "git commit -am 'test'"
                                sh "git push origin HEAD:{$branch}"
                          }
                    }
                    stage ('Tests') {
                        sh "echo 'shell scripts to run integration tests...'"
                    }
                    stage ('Deploy') {
                            sh "echo 'shell scripts to deploy to server...'"
                    }
               }
                """.stripIndent())
            }
        }
    }
}