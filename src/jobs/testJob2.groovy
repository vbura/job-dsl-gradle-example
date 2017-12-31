import hudson.model.*

String basePath = 'Release'
String repo = 'sheehan/grails-example'

folder(basePath) {
    description 'This example shows basic folder/job creation.'
}

Build build = Executor.currentExecutor().currentExecutable as Build
def resolver = build.buildVariableResolver
def branchNew = resolver.resolve("Branch")


listView("$basePath") {
    job('ci') {
        scm {
            git {
                remote {
                    name('origin')
                    url('ssh://git@git.swisscom.ch:7999/rst/bonita-adapter.git')
                    credentials('062dee70-e83b-4843-ab77-443e5fa6c7ab')
                }
                branch('master')
            }
        }
        steps {
            shell("git push origin HEAD:test123")

        }
    }
}