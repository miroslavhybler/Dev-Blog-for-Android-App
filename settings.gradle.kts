@file:Suppress("UnstableApiUsage")

val snapshotVersion: String? = System.getenv("COMPOSE_SNAPSHOT_ID")


pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "DevBlog for Android"
include(":app")
include(":tests-names")
include(":baselineprofile")
include(":benchmark")
include(":tests-shared")
