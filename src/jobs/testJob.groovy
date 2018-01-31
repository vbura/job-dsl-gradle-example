import groovy.io.FileType
import hudson.model.*

String basePath = 'Release'

folder(basePath) {
    description 'This example shgows basic folder/job creation.'
}

Build build = Executor.currentExecutor().currentExecutable as Build
def resolver = build.buildVariableResolver
def branchName = resolver.resolve("Branch")

println "${branchName}"

def fileFromWorkspace = streamFileFromWorkspace('vlad/gradle.properties')


Properties props = new Properties()
props.load(fileFromWorkspace)


def property = props.getProperty('version')

println property
println property.substring(0, property.indexOf('-'))

listView("$property") {

}