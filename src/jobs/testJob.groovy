String basePath = 'example1'
String repo = 'sheehan/grails-example'

folder(basePath) {
    description 'This example shows basic folder/job creation.'
}


job("$basePath/grails example build") {

    parameters {
        runParam( 'master', 'master','test')
    }

    triggers {
        scm 'H/5 * * * *'
    }
    pipelineJob("pipeline-calls-other-pipeline") {
        logRotator{
            numToKeep 30
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

