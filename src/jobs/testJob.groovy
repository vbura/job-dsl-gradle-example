import groovy.io.FileType
import hudson.model.*

Build buildEnv = Executor.currentExecutor().currentExecutable as Build
def resolver = buildEnv.buildVariableResolver
def project = resolver.resolve("project")

println "${project}"



def fileFromWorkspace = streamFileFromWorkspace('vlad/gradle.properties')
Properties props = new Properties()
props.load(fileFromWorkspace)
def property = props.getProperty('version')

println property
def versionRelease = property.substring(0, property.indexOf('-'))



pipelineJob(project + '-build-' + versionRelease) {
    description('Build aplication when a commit is made on ' + versionRelease + ' branch')
    logRotator {
        numToKeep 10
    }
    triggers {
        bitbucketPush()
        pollSCM {
            scmpoll_spec('')
        }
    }

    definition {
        cpsScm {
            scm {
                git {

                    remote {
                        url('https://git.swisscom.ch/scm/rst/'+project+'.git')
                        credentials('7ccc73cf-51af-4f1b-802c-2dad7c63857d')
                    }
                    branches('master')
                    scriptPath('Jenkinsfile')
                    extensions {}  // required as otherwise it may try to tag the repo, which you may not want
                }

            }
        }
    }

}

pipelineJob(project + '-release-' + versionRelease) {
    description('Build aplication when a commit is made on ' + versionRelease + ' branch')
    logRotator {
        numToKeep 10
    }
    definition {
        cpsScm {
            scm {
                git {

                    remote {
                        url('https://git.swisscom.ch/scm/rst/'+project+'.git')
                        credentials('7ccc73cf-51af-4f1b-802c-2dad7c63857d')
                    }
                    branches('master')
                    scriptPath('Jenkins/Nexus/Jenkinsfile')
                    extensions {}  // required as otherwise it may try to tag the repo, which you may not want
                }

            }
        }
    }

}

pipelineJob('git-duplicate') {
    description('Build aplication when a commit is made on ' + versionRelease + ' branch')
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
                                git credentialsId: '7ccc73cf-51af-4f1b-802c-2dad7c63857d', url: 'https://git.swisscom.ch/scm/rst/'+project+'.git'
                                sh " sed -i '/version=/ s/=.*/=$versionRelease.1-SNAPSHOT/' gradle.properties"
                            }    
                     }
                    stage ('Build') {
                         sshagent(['062dee70-e83b-4843-ab77-443e5fa6c7ab']) {
                                sh "git add ."
                                sh "git commit -am 'Create branch $versionRelease by Jenkins'"
                                sh "git push origin HEAD:releases/$versionRelease"
                          }
                    }
                    stage ('Tests') {
                        sh "echo 'Cleaning...'"
                    }
               }
                """.stripIndent())
        }
    }
}
