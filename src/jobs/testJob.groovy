import hudson.FilePath;
import jenkins.model.Jenkins;
import hudson.model.*

String basePath = 'Release'

folder(basePath) {
    description 'This example shgows basic folder/job creation.'
}


println "WOW-->" + new File(".").getAbsolutePath()

Build build = Executor.currentExecutor().currentExecutable as Build
def resolver = build.buildVariableResolver
def branchName = resolver.resolve("Branch")
println "${branchName}"

listFiles(createFilePath(pwd()));



new File("${WORKSPACE}").eachFile() { file->
    println file.getName()
}


def createFilePath(path) {
    if (env['NODE_NAME'] == null) {
        error "envvar NODE_NAME is not set, probably not inside an node {} or running an older version of Jenkins!";
    } else if (env['NODE_NAME'].equals("master")) {
        return new FilePath(path);
    } else {
        return new FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), path);
    }
}


def listFiles(rootPath) {
    print "Files in ${rootPath}:";
    for (subPath in rootPath.list()) {
        echo "  ${subPath.getName()}";
    }
}
