String basePath = 'example1'
String repo = 'sheehan/grails-example'

folder(basePath) {
    description 'This example shows basic folder/job creation.'
}

job("$basePath/grails example build") {
    scm {
        github repo
    }

    triggers {
        scm 'H/5 * * * *'
    }
    steps {
        scm {
            git{
                remote{
                   url ('ssh://git@git.swisscom.ch:7999/rst/bonita-adapter.git')
                    credentials('osmionica')
                }
            }
        }
    }
}
