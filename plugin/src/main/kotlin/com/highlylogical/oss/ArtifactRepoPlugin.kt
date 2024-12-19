package com.highlylogical.oss

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import java.io.File
import java.net.URI
import java.util.Properties

class ArtifactRepoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val artifactRepoFile =
            File(System.getProperty("user.home")).listFiles()
                .find { it.extension == "artifactrepo"  }

        if (artifactRepoFile != null && !artifactRepoFile.exists()) {
            project.logger.warn("Artifactory properties file not found in user home directory: ${artifactRepoFile.absolutePath}")
            return
        }

        val properties = Properties().apply {
            load(artifactRepoFile!!.inputStream())
        }

        val host = properties.getProperty("host")
        val user = properties.getProperty("user")
        val password = properties.getProperty("password")
        val publishRepo = properties.getProperty("publish-repo")
        val pullRepo = properties.getProperty("pull-repo")

        if (host.isNullOrBlank() || user.isNullOrBlank() || password.isNullOrBlank() || publishRepo.isNullOrBlank() || pullRepo.isNullOrBlank()) {
            project.logger.warn("Missing required artifact repository properties. Ensure 'host', 'user', 'password', 'publish-repo', and 'pull-repo' are set in a '.artifactrepo' file")
            return
        }

        // Configure repository for downloading artifacts
        project.repositories.maven {

            name = "pull-$host-$pullRepo".replace(Regex("[^\\w\\d]"), "-")

            url = URI("https://$host/$pullRepo")
            credentials {
                username = user
                this.password = password
            }
            project.logger.info("Added artifact repo to project!")
            project.logger.lifecycle("Configured artifact repository for downloading from $pullRepo")
        }

        // configure publishing repository
        project.plugins.withId("maven-publish") {
            project.extensions.configure<PublishingExtension>("publishing") {
                repositories.maven {
                    name = "publish-$host-$publishRepo".replace(Regex("[^\\w\\d]"), "-")
                    url = URI("https://$host/$publishRepo")
                    credentials {
                        username = user
                        this.password = password
                    }
                }
                project.logger.lifecycle("Configured artifact repository for publishing to $publishRepo")
            }
        }

    }
}
