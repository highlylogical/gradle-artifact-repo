package com.highlylogical.oss

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.publish.PublishingExtension
import java.io.File
import java.net.URI
import java.util.Properties


class ArtifactRepoPlugin : Plugin<Any> {

    private val logger = Logging.getLogger(ArtifactRepoPlugin::class.java)

    private data class ArtifactRepoConfig(
        val host: String,
        val user: String,
        val password: String,
        val publishRepo: String,
        val pullRepo: String
    )
    private fun getArtifactRepoProperties(logger: Logger): Result<ArtifactRepoConfig> {
        val artifactRepoFile = File(System.getenv("ARTIFACTREPO_CONFIG_DIR") ?: System.getProperty("user.home"))
            .listFiles()
            ?.find { it.extension == "artifactrepo" }

        if (artifactRepoFile == null || !artifactRepoFile.exists()) {
            val errorMessage = "Artifactory properties file not found in directory: ${artifactRepoFile?.absolutePath}"
            logger.warn(errorMessage)
            return Result.failure(Exception(errorMessage))
        }

        val properties = Properties().apply {
            load(artifactRepoFile.inputStream())
        }

        val requiredProps = listOf("host", "user", "password", "publish-repo", "pull-repo")
        val missingProps = requiredProps.filter { properties.getProperty(it).isNullOrBlank() }

        if (missingProps.isNotEmpty()) {
            val errorMessage = "Missing required artifact repository properties: ${missingProps.joinToString(", ")}"
            logger.warn(errorMessage)
            return Result.failure(Exception(errorMessage))
        }

        val host = properties.getProperty("host")
        val user = properties.getProperty("user")
        val password = properties.getProperty("password")
        val publishRepo = properties.getProperty("publish-repo")
        val pullRepo = properties.getProperty("pull-repo")

        if (host.isNullOrBlank() || user.isNullOrBlank() || password.isNullOrBlank() || publishRepo.isNullOrBlank() || pullRepo.isNullOrBlank()) {
            val errorMessage = "Invalid property values detected."
            logger.warn(errorMessage)
            return Result.failure(Exception(errorMessage))
        }
        val config = ArtifactRepoConfig(host, user, password, publishRepo, pullRepo)
        return Result.success(config)
    }

    override fun apply(target: Any) {
        when (target) {
            is Project -> {
                logger.info("Configuring project repositories")
                configureProjectRepositories(target)
            }
            is Settings -> {
                logger.info("Configuring settings repositories")
                configureSettingsRepositories(target)
                logger.info("Add artifact repo to plugins")
                target.gradle.settingsEvaluated {
                    target.gradle.allprojects() {
                        project.plugins.apply("com.highlylogical.artifactrepo")
                    }
                }
            }
            else -> {
                logger.warn("ArtifactRepoPlugin is not applicable to target: $target")
            }
        }
    }

    private fun configureProjectRepositories(project: Project) {
        getArtifactRepoProperties(project.logger).onSuccess { config ->
            project.logger.lifecycle("Configuring artifact repositories for project ${project.name}")
            configureMavenPullRepo(project, config, project.logger)
            configureMavenPublishRepo(project, config, project.logger)
        }
    }

    private fun configureSettingsRepositories(settings: Settings) {
        getArtifactRepoProperties(logger).onSuccess { config ->
            logger.lifecycle("Configuring artifact repositories for settings")
            configureMavenPullRepo(settings, config, logger)
            settings.pluginManagement.repositories.gradlePluginPortal()
        }
    }

    private fun configureMavenPullRepo(repoSource: Any, config: ArtifactRepoConfig, logger: Logger) {
        fun configureDownloadRepository(repositoryHandler: RepositoryHandler, configName: String) {
            repositoryHandler.maven {
                name = "pull-${config.host}-${config.pullRepo}".replace(Regex("[^\\w\\d]"), "-")
                url = URI("https://${config.host}/${config.pullRepo}")
                credentials {
                    username = config.user
                    this.password = config.password
                }
                logger.info("Configured $configName for downloading from ${config.pullRepo}")
            }

        }

        when (repoSource) {
            is Settings -> {
                configureDownloadRepository(repoSource.pluginManagement.repositories, "build settings")
            }
            is Project -> {
                configureDownloadRepository(repoSource.repositories, repoSource.name)
            }
            else -> {
                logger.warn("Unsupported repository source type: ${repoSource.javaClass.name}")
            }
        }
    }

    private fun configureMavenPublishRepo(project: Project, config: ArtifactRepoConfig, logger: Logger) {
        // configure publishing repository
        project.plugins.withId("maven-publish") {
            project.extensions.configure<PublishingExtension>("publishing") {
                repositories.maven {
                    name = "publish-${config.host}-${config.publishRepo}".replace(Regex("[^\\w\\d]"), "-")
                    url = URI("https://${config.host}/${config.publishRepo}")
                    credentials {
                        username = config.user
                        this.password = config.password
                    }
                }
                logger.info("Configured ${project.name} for publishing to ${config.publishRepo}")
            }
        }
    }
}
