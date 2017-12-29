String basePath = 'Release'
String repo = 'sheehan/grails-example'

folder(basePath) {
    description 'This example shows basic folder/job creation.'
}

listView("$basePath") {
    pipelineJob("$basePath/pipeline-calls-other-pipeline") {
        description()
        parameters {
            stringParam('master', 'master', 'test',)
        }

        logRotator {
            numToKeep 30
        }
        scm {
            git {
                remote {
                    url('ssh://git@git.swisscom.ch:7999/rst/bonita-adapter.git')
                    credentials('062dee70-e83b-4843-ab77-443e5fa6c7ab')

                }
            }
        }

        definition {
            cps {
                sandbox()
                script("""
               node {
                     stage("Checkout") {
                            echo 'Hello World'
                            script {
                                git credentialsId: '062dee70-e83b-4843-ab77-443e5fa6c7ab', url: 'ssh://git@git.swisscom.ch:7999/rst/bonita-adapter.git'
                                def props = readProperties file: 'gradle.properties'
                                sh "./gradlew release"
                            }    
                     }
                    stage ('Build') {
                    
                         sshagent(['062dee70-e83b-4843-ab77-443e5fa6c7ab']) {
                                sh "git add ."
                                sh "git commit -am 'test'"
                                sh "git push origin HEAD:test"
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