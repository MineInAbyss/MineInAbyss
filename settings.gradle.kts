rootProject.name = "mineinabyss"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://papermc.io/repo/repository/maven-public/") //Paper
    }

    plugins {
        val kotlinVersion: String by settings
        val composeVersion: String by settings
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("org.jetbrains.compose") version composeVersion
    }

    val idofrontVersion: String by settings
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.mineinabyss.conventions"))
                useVersion(idofrontVersion)
        }
    }
}

dependencyResolutionManagement {
    val idofrontVersion: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
    }

    versionCatalogs {
        create("libs").from("com.mineinabyss:catalog:$idofrontVersion")
        create("mialibs").from(files("gradle/mialibs.versions.toml"))
    }
}

include(
    "mineinabyss-core",
    "mineinabyss-components",
    "mineinabyss-features"
)
