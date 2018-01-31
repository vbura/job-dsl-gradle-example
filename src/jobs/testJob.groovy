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
    pipelineJob('taifun-core-build-'+versionRelease) {
        description()
        parameters {
            stringParam('master', 'master', 'test',)
        }

        logRotator {
            numToKeep 10
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
                        extensions { }  // required as otherwise it may try to tag the repo, which you may not want
                    }

                    // the single line below also works, but it
                    // only covers the 'master' branch and may not give you
                    // enough control.
                    // git(repo, 'master', { node -> node / 'extensions' << '' } )
                }
            }
        }

    }
}