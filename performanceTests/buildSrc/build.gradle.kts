import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.3.31"
}

repositories {
  maven("https://build-inera.nordicmedtest.se/nexus/repository/releases/")
  gradlePluginPortal()
  jcenter()
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
  }
}
