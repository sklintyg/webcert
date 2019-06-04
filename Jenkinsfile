#!groovy

def buildVersion = "6.3.1.${BUILD_NUMBER}"
def commonVersion = "3.9.0.+"
def infraVersion = "3.9.0.+"

stage('checkout') {
    node {
        git url: "https://github.com/sklintyg/webcert.git", branch: GIT_BRANCH
        util.run { checkout scm }
    }
}

stage('build') {
    node {
        try {
            shgradle "--refresh-dependencies clean build camelTest testReport sonarqube -PcodeQuality -PcodeCoverage -DgruntColors=false \
                  -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DinfraVersion=${infraVersion}"
        } finally {
            publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/reports/allTests', \
                reportFiles: 'index.html', reportName: 'JUnit results'

        }
    }
}

stage('tag and upload') {
    node {
        shgradle "uploadArchives tagRelease -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DinfraVersion=${infraVersion}"
    }
}

stage('notify') {
    node {
        util.notifySuccess()
    }
}

stage('propagate') {
    node {
        gitRef = "v${buildVersion}"
	    releaseFlag = "${GIT_BRANCH.startsWith("release")}"
        build job: "webcert-dintyg-build", wait: false, parameters: [
                [$class: 'StringParameterValue', name: 'WEBCERT_BUILD_VERSION', value: buildVersion],
                [$class: 'StringParameterValue', name: 'COMMON_VERSION', value: commonVersion],
                [$class: 'StringParameterValue', name: 'INFRA_VERSION', value: infraVersion],
                [$class: 'StringParameterValue', name: 'GIT_REF', value: gitRef],
                [$class: 'StringParameterValue', name: 'RELEASE_FLAG', value: releaseFlag]
        ]
    }
}
