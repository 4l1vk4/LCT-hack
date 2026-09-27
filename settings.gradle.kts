// Подгрузка локальных свойств из .env
val envFile = file(".env")
if (envFile.exists()) {
    envFile.forEachLine { line ->
        val trimmed = line.trim()
        if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
            val parts = trimmed.split("=", limit = 2)
            val key = parts[0].trim()
            val value = parts[1].trim().trim('"', '\'')
            if (key == "org.gradle.java.home" || key == "ORG_GRADLE_JAVA_HOME") {
                System.setProperty("org.gradle.java.home", value)
            }
        }
    }
}

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FinPet"
include(":app")
