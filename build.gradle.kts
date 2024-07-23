plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.+"
    kotlin("plugin.serialization") version "2.0.0"
}

group = "org.cubewhy.patch"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
//    implementation("org.ow2.asm:asm:9.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
}

kotlin {
    jvmToolchain(17)
}

tasks.shadowJar {
    archiveClassifier.set("exec")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    exclude("native-binaries/**")

    exclude("LICENSE.txt")

    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")

    exclude("org/junit/**")
}

tasks.jar {
    archiveClassifier.set("library")
    dependsOn("shadowJar")

    manifest {
        attributes(
            "Main-Class" to "org.cubewhy.patch.MainKt",
        )
        attributes(
            "Charset" to "UTF-8"
        )
    }
}
