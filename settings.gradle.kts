rootProject.name = "mineinabyss"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
    }

    plugins {
        val kotlinVersion: String by settings
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }

    val miaConventionsVersion: String by settings
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.mineinabyss.conventions"))
                useVersion(miaConventionsVersion)
        }
    }
}
