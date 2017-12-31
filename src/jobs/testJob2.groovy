import hudson.model.*

String basePath = 'Release'
String repo = 'sheehan/grails-example'

folder(basePath) {
    description 'This example shows basic folder/job creation.'
}

Build build = Executor.currentExecutor().currentExecutable as Build
def resolver = build.buildVariableResolver
def branch = resolver.resolve("Branch")


listView("$basePath") {
    job('ci') {
        scm{
            git {
                remote('https://github.com/jenkinsci/job-dsl-plugin.git')
                credentialsId('github-ci')
                includes('JENKINS-*')
            }
        }
    }

}