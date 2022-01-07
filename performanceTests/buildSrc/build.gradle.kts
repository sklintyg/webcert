import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.3.31"
}

repositories {
  maven("https://nexus.drift.inera.se/repository/maven-releases/")
  gradlePluginPortal()
  mavenCentral()
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
  }
}
