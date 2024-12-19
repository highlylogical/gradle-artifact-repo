plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = "com.highlylogical.oss"
version = "0.1.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}


dependencies {
    testImplementation(gradleTestKit())
    testImplementation("org.spockframework:spock-core:2.3-groovy-3.0")

    testImplementation(kotlin("test"))
    implementation(gradleApi())
//    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")

}

gradlePlugin {
    website = "https://github.com/highlylogical/gradle-artifact-repo"
    vcsUrl = "https://github.com/highlylogical/gradle-artifact-repo"
    plugins {
        create("artifactRepoPlugin") {
            id = "com.highlylogical.artifactrepo"
            displayName = "Artifact Repository Plugin"
            implementationClass = "com.highlylogical.oss.ArtifactRepoPlugin"
            description = "Plugin for configuring artifact repositories from configuration file"
            tags = listOf("artifact", "repository", "config")
        }

    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}