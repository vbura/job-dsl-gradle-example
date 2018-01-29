import groovy.io.FileType
import hudson.model.*
import org.apache.tools.ant.Executor

String basePath = 'Release'

folder(basePath) {
    description 'This example shgows basic folder/job creation.'
}

Build build = Executor.currentExecutor().currentExecutable as Build
def resolver = build.buildVariableResolver
def workspace = pwd()
def branchName = resolver.resolve("${workspace}")
println "${branchName}"


def listfiles(dir) {
    dlist = []
    flist = []
    new File(dir).eachDir {dlist << it.name }
    dlist.sort()
    new File(dir).eachFile(FileType.FILES, {flist << it.name })
    flist.sort()
    return (dlist << flist).flatten()
}

fs = listfiles(".")
fs.each {
    println it
}

File propertiesFile = new File('/agent/workspace/dsl-test-vlad/vlad/gradle.properties')
propertiesFile.withInputStream {
    properties.load(propertiesFile)
}

println "${properties["version"]}"


//listView("$basePath") {
//    pipelineJob("/test-release") {
//        description()
//        parameters {
//            stringParam('Branch', "$branchName", 'test',)
//        }
//        logRotator {
//            numToKeep 10
//        }
//
//        definition {
//            cps {
//                sandbox()
//                script("""
//               node {
//                     stage("Checkout") {
//                            echo 'Hello World'
//                            script {
//                                git branch:$branchName credentialsId: '062dee70-e83b-4843-ab77-443e5fa6c7ab', url: 'ssh://git@git.swisscom.ch:7999/rst/bonita-adapter.git'
//                                def props = readProperties file: 'gradle.properties'
//                                sh "./gradlew clean"
//                            }
//                     }
//                    stage ('Build') {
//
//                         sshagent(['062dee70-e83b-4843-ab77-443e5fa6c7ab']) {
//                                sh "git add ."
//                                sh "git commit -am 'test'"
//                                sh "git push origin HEAD:{$branchName}"
//                          }
//                    }
//                    stage ('Tests') {
//                        sh "echo 'shell scripts to run integration tests...'"
//                    }
//                    stage ('Deploy') {
//                            sh "echo 'shell scripts to deploy to server...'"
//                    }
//               }
//                """.stripIndent())
//            }
//        }
//    }
//}