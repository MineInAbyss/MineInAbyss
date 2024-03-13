rootProject.name = "mineinabyss"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://repo.papermc.io/repository/maven-public/") //Paper
        mavenLocal()
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
        maven("https://repo.mineinabyss.com/snapshots")
        mavenLocal()
    }

    versionCatalogs {
        create("idofrontLibs"){
            from("com.mineinabyss:catalog:$idofrontVersion")
            version("modelengine", "R4.0.4")
        }
        create("miaLibs").from(files("gradle/miaLibs.versions.toml"))
    }
}

include(
    "mineinabyss-components",
    "mineinabyss-features"
)
