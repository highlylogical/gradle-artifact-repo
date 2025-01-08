# gradle-artifact-repo

This plugin configures artifact repositories for gradle projects using a configuration file. The configuration file is intentionally generic to allow for use with other build systems, such as sbt using the [sbt-artifact-repo](https://github.com/highlylogical/sbt-artifact-repo) plugin. 

## Usage

To use the `gradle-artifact-repo` plugin, follow these steps:

1. Add the plugin to your `settings.gradle.kts` file:
    ```kotlin
    plugins {
        id("com.highlylogical.artifactrepo") version "0.2.0"
    }
    ```

2. Create a `.artifactrepo` file in your home directory with the following properties:
    ```
    host=your.artifact.repo.host
    user=your-username
    password=your-password
    publish-repo=your/publish/repo
    pull-repo=your/pull/repo
    ```

    The default location of the `.artifactrepo` file can be overridden by setting the `ARTIFACT_REPO_CONFIG` environment variable.

3. Configure the `maven-publish` plugin if you want to publish artifacts:
    ```kotlin
    plugins {
        id("maven-publish")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
            }
        }
    }
    ```

4. Run your Gradle tasks as usual. The plugin will automatically configure the repositories based on the properties in the `.artifactrepo` file.