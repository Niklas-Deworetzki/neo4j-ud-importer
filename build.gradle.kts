plugins {
    kotlin("jvm") version "2.1.10"
    id("com.github.gmazzo.buildconfig") version "5.6.5"
}

group = "se.gu"
version = "0.1"

buildConfig {
    buildConfigField("APP_VERSION", project.version.toString())
    buildConfigField("BUILD_TIME", System.currentTimeMillis())
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.neo4j.driver:neo4j-java-driver:5.28.4")
    implementation("info.picocli:picocli:4.7.6")
    implementation("org.fusesource.jansi:jansi:2.4.1")
    implementation("me.tongfei:progressbar:0.10.1")
    implementation("io.github.sttk:stringcase:0.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "se.gu.UDImporter"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from(
        {
            configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
        }
    )
}

kotlin {
    jvmToolchain(21)
}