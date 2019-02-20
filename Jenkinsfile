#!groovy

def buildVersion = "6.4.0.${BUILD_NUMBER}"

def commonVersion = "3.10.0.+"
def infraVersion = "3.10.0.+"
def logsenderBaseVersion = "6.4.0.*" // Star is needed as this is a regexp
def refDataVersion = "1.0.0.+"
def versionFlags = "-DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DinfraVersion=${infraVersion} -DrefDataVersion=${refDataVersion}"

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
                  ${versionFlags}"
        } finally {
            publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'build/reports/allTests', \
                reportFiles: 'index.html', reportName: 'JUnit results'

        }
    }
}

stage('deploy') {
    node {
        def logsenderVersion = util.latestVersion("se/inera/intyg/logsender/logsender", logsenderBaseVersion)
        util.run {
            ansiblePlaybook extraVars: [version: buildVersion, ansible_ssh_port: "22", deploy_from_repo: "false", logsender_version: "${logsenderVersion}"], \
                installation: 'ansible-yum', inventory: 'ansible/inventory/webcert/test', playbook: 'ansible/deploy.yml'
            util.waitForServer('https://webcert.inera.nordicmedtest.se/version.jsp')
        }
    }
}

stage('restAssured') {
   node {
       try {
           shgradle "restAssuredTest -DbaseUrl=http://webcert.inera.nordicmedtest.se/ \
                 ${versionFlags}"
       } finally {
           publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'web/build/reports/tests/restAssuredTest', \
               reportFiles: 'index.html', reportName: 'RestAssured results'
       }
   }
}

stage('protractor') {
   node {
       try {
           shgradle "protractorTests -Dprotractor.env=build-server ${versionFlags}"
       } finally {
           publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'test/reports', \
               reportFiles: 'index.html', reportName: 'Protractor results'
       }
   }
}

stage('tag and upload') {
    node {
        shgradle "uploadArchives tagRelease ${versionFlags}"
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
                [$class: 'StringParameterValue', name: 'REF_DATA_VERSION', value: refDataVersion],
                [$class: 'StringParameterValue', name: 'GIT_REF', value: gitRef],
                [$class: 'StringParameterValue', name: 'RELEASE_FLAG', value: releaseFlag]
        ]
    }
}
