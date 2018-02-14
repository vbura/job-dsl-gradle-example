import static com.dslexample.util.StepsUtil.*

String project = getJobParameter( "project")
String version = getVersionFromPropertiesFile(project)
String gitUrl = getGitUrl(project)

def versionRelease = version.substring(0, version.indexOf('-'))

def tag =  'T-'+new Date().format('yy.MM')+'-${version}'

println tag


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
                        url(gitUrl)
                        credentials('7ccc73cf-51af-4f1b-802c-2dad7c63857d')
                    }
                    branches('**/releases'+versionRelease)
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
                        url(gitUrl)
                        credentials('7ccc73cf-51af-4f1b-802c-2dad7c63857d')
                    }
                    branches('**/releases'+versionRelease)
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
                            script {
                                git credentialsId: '7ccc73cf-51af-4f1b-802c-2dad7c63857d', url: '$gitUrl'
                            }    
                     }

                    stage('Trigger Release JOB'){
                        build job: '${project}-release', parameters: [credentials(description: '', name: 'Nexus repository', value: 'nexusPassword')]
                    }    

                    stage ('Create Branch $versionRelease') {
                         sshagent(['062dee70-e83b-4843-ab77-443e5fa6c7ab']) {
                                sh "git checkout $tag"
                                sh "sed -i '/version=/ s/=.*/=$versionRelease.1-SNAPSHOT/' gradle.properties"
                                sh "git add ."
                                sh "git commit -am 'Create branch $versionRelease by Jenkins'"
                                sh "git push origin HEAD:releases/$versionRelease"
                          }
                    }
                    
                    stage('Initial Build $versionRelease'){
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
        PROJECT = PROJECT +"/master"
    def fileFromWorkspace = streamFileFromWorkspace(PROJECT + '/gradle.properties')
    Properties props = new Properties()
    props.load(fileFromWorkspace)
    def version = props.getProperty('version')
    version
}