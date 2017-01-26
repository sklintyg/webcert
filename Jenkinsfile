#!groovy

def buildVersion = "5.2.${BUILD_NUMBER}"
def commonVersion = "3.2.+"
def infraVersion = "3.2.+"

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

stage('deploy') {
    node {
        util.run {
            ansiblePlaybook extraVars: [version: buildVersion, ansible_ssh_port: "22", deploy_from_repo: "false"], \
                installation: 'ansible-yum', inventory: 'ansible/inventory/webcert/test', playbook: 'ansible/deploy.yml'
            util.waitForServer('https://webcert.inera.nordicmedtest.se/version.jsp')
        }
    }
}

stage('restAssured') {
   node {
       try {
           shgradle "restAssuredTest -DbaseUrl=http://webcert.inera.nordicmedtest.se/ \
                 -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DinfraVersion=${infraVersion}"
       } finally {
           publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'web/build/reports/tests/restAssuredTest', \
               reportFiles: 'index.html', reportName: 'RestAssured results'
       }
   }
}

stage('protractor') {
   node {
       try {
           wrap([$class: 'Xvfb']) {
               shgradle "protractorTests -Dprotractor.env=build-server \
                     -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DinfraVersion=${infraVersion}"
           }
       } finally {
           publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'test/dev/report', \
               reportFiles: 'index.html', reportName: 'Protractor results'
       }
   }
}

stage('fitnesse') {
   node {
       try {
           wrap([$class: 'Xvfb']) {
               shgradle "fitnesseTest -PfileOutput -PoutputFormat=html -Dgeb.env=firefoxRemote -Dweb.baseUrl=https://webcert.inera.nordicmedtest.se/ \
                     -DbaseUrl=https://webcert.inera.nordicmedtest.se/ -Dlogsender.baseUrl=https://webcert.inera.nordicmedtest.se/log-sender/ \
                     -Dcertificate.baseUrl=https://webcert.inera.nordicmedtest.se/inera-certificate/ \
                     -DbuildVersion=${buildVersion} -DcommonVersion=${commonVersion} -DinfraVersion=${infraVersion}"
           }
       } finally {
           publishHTML allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'specifications/', \
               reportFiles: 'fitnesse-results.html', reportName: 'Fitnesse results'
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
