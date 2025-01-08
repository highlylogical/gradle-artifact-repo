plugins {
    id("maven-publish")
}

tasks.register("listRepos") {
    doLast {
        println("Configured Repositories:")
        project.repositories.forEach { repo ->
            when (repo) {
                is MavenArtifactRepository -> println("Maven repo: ${repo.name}, URL: ${repo.url}")
                else -> println("Other repo: ${repo.name}, type: ${repo.javaClass.name}")
            }
        }
        project.publishing.repositories.forEach { repo ->
            when (repo) {
                is MavenArtifactRepository -> println("Maven repo: ${repo.name}, URL: ${repo.url}")
                else -> println("Other repo: ${repo.name}, type: ${repo.javaClass.name}")
            }
        }
    }
}