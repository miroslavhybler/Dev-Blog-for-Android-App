@file:Suppress("UnstableApiUsage")

val snapshotVersion: String? = System.getenv("COMPOSE_SNAPSHOT_ID")


pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        snapshotVersion?.let {
            println("https://androidx.dev/snapshots/builds/$it/artifacts/repository/")
            maven { url = uri("https://androidx.dev/snapshots/builds/$it/artifacts/repository/") }
        }
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://androidx.dev/snapshots/builds/13508953/artifacts/repository")
    }
}

rootProject.name = "DevBlog for Android"
include(":app")
include(":tests-names")
include(":baselineprofile")
include(":benchmark")
include(":tests-shared")
