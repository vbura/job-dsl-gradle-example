package com.dslexample.util

import hudson.model.Build
import hudson.model.Executor

class StepsUtil {

    static void proxiedGradle(context, String gradleTasks) {
        context.with {
            gradle {
                useWrapper true
                tasks gradleTasks
                switches '''
                    -Dhttp.proxyHost=xxx
                    -Dhttps.proxyHost=xxx
                    -Dhttp.proxyPort=xxx
                    -Dhttps.proxyPort=xxx
                '''.stripIndent().trim()
            }
        }
    }

    static String getJobParameter(String PARAMETER) {
        Build buildEnv = Executor.currentExecutor().currentExecutable as Build
        def resolver = buildEnv.buildVariableResolver
        def project = resolver.resolve(PARAMETER)
        project
    }

    static String getGitUrl(String PROJECT) {
        def gitProject = "https://git.swisscom.ch/scm/rst/" + PROJECT + ".git"
        return gitProject
    }
    static String getSshUrl(String PROJECT) {
        def gitProject = " ssh://git@git.swisscom.ch:7999/rst/" + PROJECT + ".git"
        return gitProject
    }



}
