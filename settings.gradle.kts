@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://repo.c7x.dev/repository/maven-public/")
            credentials {
                username = System.getenv("CRI_REPO_LOGIN") ?: System.getenv("CRISTALIX_REPO_USERNAME") ?: System.getenv("REPO_C7X_USERNAME")
                password = System.getenv("CRI_REPO_PASSWORD") ?: System.getenv("CRISTALIX_REPO_PASSWORD") ?: System.getenv("REPO_C7X_PASSWORD")
            }
        }
    }

    plugins {
        kotlin("jvm") version "1.6.21"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://repo.c7x.dev/repository/maven-public/")
            credentials {
                username = System.getenv("CRI_REPO_LOGIN") ?: System.getenv("CRISTALIX_REPO_USERNAME") ?: System.getenv("REPO_C7X_USERNAME")
                password = System.getenv("CRI_REPO_PASSWORD") ?: System.getenv("CRISTALIX_REPO_PASSWORD") ?: System.getenv("REPO_C7X_PASSWORD")
            }
        }
    }

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

rootProject.name = "metaworld-api"
include("core")


