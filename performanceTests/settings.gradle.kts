import se.inera.webcert.performance.Dependencies.gatlingPluginVersion

pluginManagement {
  repositories {
    maven("https://nexus.drift.inera.se/repository/maven-releases/")
    gradlePluginPortal()
    mavenCentral()
  }

  resolutionStrategy {
    eachPlugin {
      if (requested.id.id.startsWith("com.github.lkishalmi.gatling")) {
        useVersion(gatlingPluginVersion)
      }
    }
  }
}

rootProject.name = "webcert-performance-test"
