#!groovy

def javaEnv() {
    def javaHome = tool 'JDK8u66'
    ["PATH=${env.PATH}:${javaHome}/bin", "JAVA_HOME=${javaHome}"]
}

stage('checkout') {
    node {
        try {
            checkout scm
        } catch (e) {
            currentBuild.result = "FAILED"
            notifyFailed()
            throw e
        }
    }
}

stage('build') {
    node {
        try {
            withEnv(javaEnv()) {
                sh './gradlew --refresh-dependencies clean build sonarqube -PcodeQuality -DgruntColors=false'
            }
        } catch (e) {
            currentBuild.result = "FAILED"
            notifyFailed()
            throw e
        }
    }
}

stage('deploy') {
    node {
        try {
            ansiblePlaybook extraVars: [version: "5.0.$BUILD_NUMBER", ansible_ssh_port: "22", deploy_from_repo: "false"], \
                installation: 'ansible-yum', \
                inventory: 'ansible/hosts_test', \
                playbook: 'ansible/deploy.yml', \
                sudoUser: null
        } catch (e) {
            currentBuild.result = "FAILED"
            notifyFailed()
            throw e
        }
    }
}

stage('integration tests') {
    node {
        try {
            wrap([$class: 'Xvfb']) {
                withEnv(javaEnv()) {
                    sh './gradlew restAssuredTest -DbaseUrl=http://webcert.inera.nordicmedtest.se/'

                    sh './gradlew fitnesseTest -Dgeb.env=firefoxRemote -Dweb.baseUrl=https://webcert.inera.nordicmedtest.se/ \
                        -DbaseUrl=https://webcert.inera.nordicmedtest.se/ -Dlogsender.baseUrl=https://webcert.inera.nordicmedtest.se/log-sender/ \
                        -Dcertificate.baseUrl=https://intygstjanst.inera.nordicmedtest.se/inera-certificate/ -PfileOutput'

                    sh './gradlew protractorTests -Dprotractor.env=build-server'
                }
            }
        } catch (e) {
            currentBuild.result = "FAILED"
            notifyFailed()
            throw e
        }
    }
}

stage('tag and upload') {
    node {
        try {
            withEnv(javaEnv()) {
                sh './gradlew uploadArchives tagRelease -DnexusUsername=$NEXUS_USERNAME -DnexusPassword=$NEXUS_PASSWORD \
                    -DgithubUser=$GITHUB_USERNAME -DgithubPassword=$GITHUB_PASSWORD'
            }
        }
        catch (e) {
            currentBuild.result = "FAILED"
            notifyFailed()
            throw e
        }
    }
}

def notifyFailed() {
    emailext (subject: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
              body: """FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':\n\nCheck console output at ${env.BUILD_URL}""",
              recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'DevelopersRecipientProvider']])

}
