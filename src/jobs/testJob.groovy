import groovy.io.FileType
import hudson.model.*

String basePath = 'Releases'


Build build = Executor.currentExecutor().currentExecutable as Build
def resolver = build.buildVariableResolver
def branchName = resolver.resolve("Branch")

println "${branchName}"

def fileFromWorkspace = streamFileFromWorkspace('vlad/gradle.properties')


Properties props = new Properties()
props.load(fileFromWorkspace)


def property = props.getProperty('version')

println property
def versionRelease =  property.substring(0, property.indexOf('-'))


listView('Releases') {
    pipelineJob('releases/'+versionRelease) {
        description()
        parameters {
            stringParam('master', 'master', 'test',)
        }

        logRotator {
            numToKeep 30
        }

        definition {
            cps {
                sandbox()
                script("""
               node {
                     stage("Checkout") {
                            script {
                                git credentialsId: '062dee70-e83b-4843-ab77-443e5fa6c7ab', url: 'ssh://git@git.swisscom.ch:7999/rst/bonita-adapter.git'
                                sh "./gradlew clean"
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