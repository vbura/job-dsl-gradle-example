import groovy.io.FileType
import hudson.model.*


def file = new File('vlad/gradle.properties')

if (file.exists()) {
    def fileFromWorkspace = streamFileFromWorkspace('vlad/gradle.properties')
    Properties props = new Properties()
    props.load(fileFromWorkspace)
    def property = props.getProperty('version')

    println property
    def versionRelease = property.substring(0, property.indexOf('-'))



    pipelineJob('taifun-core-build-' + versionRelease) {
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
                            url('ssh://git@git.swisscom.ch:7999/rst/bonita-adapter.git')
                            credentials('062dee70-e83b-4843-ab77-443e5fa6c7ab')
                        }
                        branches('master')
                        scriptPath('Jenkinsfile')
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
                                git credentialsId: '062dee70-e83b-4843-ab77-443e5fa6c7ab', url: 'ssh://git@git.swisscom.ch:7999/rst/bonita-adapter.git'
                                sh " sed -i '/version=/ s/=.*/=$versionRelease.0/' gradle.properties"
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
} else {
    println("Unable to read properties file.")
}
