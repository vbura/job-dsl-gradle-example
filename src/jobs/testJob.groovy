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

job{
    name("releases/"+versionRelease)
    description('This is a Test Job')
}

listView(basePath) {
     jobs{
         name("releases/"+versionRelease)
     }
}