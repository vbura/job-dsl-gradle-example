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
                        stage 'Hello world'
                        echo 'Hello World 1'
                        stage "invoke another pipeline"
                        echo 'Hello World 1'
                        stage 'Goodbye world'
                        echo "Goodbye world"
                    }
            """.stripIndent())
            }
        }
    }
}