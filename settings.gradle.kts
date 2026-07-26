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
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    val miaLibs: String by settings

    repositories {
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")
        mavenLocal()
    }

    versionCatalogs {
        create("miaLibs") {
            from("com.mineinabyss:catalog:$miaLibs")
            version("idofront", "2.0-SNAPSHOT")
            version("sqlite-kt", "0.1.4-dev.1")
        }
    }
}

include(
    "mineinabyss-components",
    "mineinabyss-features"
)
