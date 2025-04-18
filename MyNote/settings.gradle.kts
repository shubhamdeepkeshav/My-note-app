pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")  // Android-related plugins
                includeGroupByRegex("com\\.google.*")   // Google-related plugins
                includeGroupByRegex("androidx.*")       // AndroidX-related plugins
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)  // Fail if repositories are added in build.gradle
    repositories {
        google()  // Google's repository for dependencies
        mavenCentral()  // Maven Central repository for dependencies
    }
}

rootProject.name = "MyNote"
include(":app")
