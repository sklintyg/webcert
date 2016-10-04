#!groovy

def javaEnv() {
  def javaHome = tool 'JDK8u66'
  ["PATH=${env.PATH}:${javaHome}/bin", "JAVA_HOME=${javaHome}"]
}

stage 'checkout'

node {
  checkout scm
}

stage 'build'

node {
  withEnv(javaEnv()) {
    sh './gradlew clean install'
  }
}

stage 'test'

node {
  withEnv(javaEnv()) {
    sh './gradlew integrationTest'
  }
}
