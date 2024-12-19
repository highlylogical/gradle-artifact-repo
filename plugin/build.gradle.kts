plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    //id("com.gradle.plugin-publish") version "1.2.0"
    //kotlin("jvm") version "2.0.21"
}

group = "com.highlylogical.oss"
version = "1.0-SNAPSHOT"

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
    plugins {
        create("artifactRepoPlugin") {
            id = "com.highlylogical.artifactrepo"
            implementationClass = "com.highlylogical.oss.ArtifactRepoPlugin"
        }
    }
}

//pluginBundle {
//    website = "https://github.com/your-repo/gradle-artifactory-plugin"
//    vcsUrl = "https://github.com/your-repo/gradle-artifactory-plugin"
//    tags = listOf("artifactory", "publishing", "repositories")
//}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}