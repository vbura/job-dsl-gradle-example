import static com.dslexample.util.StepsUtil.*

String project = getJobParameter("project")
String version = getVersionFromPropertiesFile(project)
String gitUrl = getGitUrl(project)
String gitSshUrl= getSshUrl(project)

def versionRelease = version.substring(0, version.indexOf('-'))
def tag = 'T-' + new Date().format('yy.MM') + '-' + versionRelease

println tag


pipelineJob(project + '-build-' + versionRelease) {
    description('Jenkins Release JOB for ' + versionRelease + ' branch.' +
                'Build application when a commit is made on ' + versionRelease + ' branch')
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
                        url(gitUrl)
                        credentials('7ccc73cf-51af-4f1b-802c-2dad7c63857d')
                    }
                    extensions {
                        localBranch('**')
                        cloneOptions{
                            timeout(50)
                        }
                    }
                    branches('**/releases/' + versionRelease)
                    scriptPath('Jenkinsfile')
                }

            }
        }
    }

}

pipelineJob(project + '-release-' + versionRelease) {
    description('Jenkins Release JOB for ' + versionRelease + ' branch')
    logRotator {
        numToKeep 10
    }
    definition {
        cpsScm {
            scm {
                git {

                    remote {
                        url(gitSshUrl)
                        credentials('062dee70-e83b-4843-ab77-443e5fa6c7ab')
                    }
                    extensions {
                        localBranch('**')
                        cloneOptions{
                            timeout(50)
                        }
                    }
                    branches('**/releases/' + versionRelease)
                    scriptPath('Jenkins/Nexus/Jenkinsfile')
                }

            }
        }
    }

}

pipelineJob('git-branch-and-build-trigger') {
    description('This is an automatic created job that trigger by RELEASE job. Is  creating new release branch, ' +
            'new release manual job, new release build job and trigger first time Jenkins release build job ' )
    logRotator {
        numToKeep 10
    }
    definition {
        cps {
            sandbox()
            script("""
               node {
                     stage("Checkout") {
                            script {
                                git credentialsId: '062dee70-e83b-4843-ab77-443e5fa6c7ab', url: '$gitSshUrl'
                            }    
                     }

                    stage('Trigger Release JOB'){
                        build job: '${project}-release', parameters: [credentials(description: '', name: 'Nexus repository', value: 'nexusPassword')]
                    }    

                    stage ('Create Release Branch') {
                         sshagent(['062dee70-e83b-4843-ab77-443e5fa6c7ab']) {
                                sh "sed -i '/version=/ s/=.*/=$versionRelease.1-SNAPSHOT/' gradle.properties"
                                sh "git add ."
                                sh "git commit -am 'Create branch $versionRelease by Jenkins'"
                                sh "git push origin HEAD:releases/$versionRelease"
                          }
                    }
                    
                    stage('Trigger Release Build'){
                        build '${project}-build-${versionRelease}'
                    }
                    
                    stage ('Tests') {
                        sh "echo 'Cleaning...'"
                    }
               }
                """.stripIndent())
        }
    }
}


private String getVersionFromPropertiesFile(String PROJECT) {
    if (PROJECT == 'taifun-core')
        PROJECT = PROJECT + "/master"
    def fileFromWorkspace = streamFileFromWorkspace(PROJECT + '/gradle.properties')
    Properties props = new Properties()
    props.load(fileFromWorkspace)
    def version = props.getProperty('version')
    version
}