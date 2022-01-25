import com.github.lkishalmi.gradle.gatling.GatlingPluginExtension
import se.inera.webcert.performance.Dependencies.gatlingVersion
import se.inera.webcert.performance.Dependencies.scalajHttpVersion

plugins {
  scala
  id("com.github.lkishalmi.gatling")
}

ext {
  set("gatlingBaseUrl", System.getProperty("certificate.baseUrl") ?: "http://localhost:8020")
}

val gatlingBaseUrlArg = "-DbaseUrl=${extra.get("gatlingBaseUrl")}"

sourceSets {
  getByName("gatling") {
    withConvention(ScalaSourceSet::class) {
      scala {
        srcDirs("src/gatling/scala")
        setOutputDir(file("build/classes/scala/gatling"))
      }
      resources {
        srcDirs("src/gatling/resources")
      }
    }
  }
}

configure<GatlingPluginExtension> {
  toolVersion = gatlingVersion
  jvmArgs = listOf(gatlingBaseUrlArg)

  dataDir = "src/gatling/resources/data"
  confDir = "src/gatling/resources/conf"

  simulationsDir = "src/gatling/scala/se/inera/webcert/simulations"
  simulations = listOf(
     "se.inera.webcert.simulations.Arende",
     "se.inera.webcert.simulations.FragaSvar",
     "se.inera.webcert.simulations.InjiceraFraga",
     "se.inera.webcert.simulations.SkrivSigneraSkickaIntyg",
     "se.inera.webcert.simulations.TaBortFraga"
  )
}

repositories {
  mavenCentral()
}

dependencies {
  gatling("io.gatling:gatling-app:$gatlingVersion")
  gatling("io.gatling:gatling-recorder:$gatlingVersion")
  gatling("io.gatling.highcharts:gatling-charts-highcharts:$gatlingVersion")
  gatling("org.scalaj:scalaj-http_2.11:$scalajHttpVersion")
}

