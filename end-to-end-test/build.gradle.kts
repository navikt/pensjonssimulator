val logback_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.2"
    kotlin("plugin.serialization") version "2.1.10"
}

group = "no.nav.pensjon"

application {
    mainClass = "no.nav.pensjon.IntegrasjonstestApplicationKt"
}

repositories {
    mavenCentral()
}
//end-to-end-test.jar
ktor {
    fatJar {
        archiveFileName.set("fat.jar")
    }
}

dependencies {
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")

    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-client-logging:3.1.3")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation("com.nimbusds:nimbus-jose-jwt:10.2")
    implementation("com.typesafe:config:1.4.2")

}
