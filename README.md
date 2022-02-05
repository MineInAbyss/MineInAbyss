<div align="center">

[![Mine in Abyss](https://user-images.githubusercontent.com/16233018/116150394-4e6b2900-a6b1-11eb-8efb-ac5542c4d8d0.png)](https://mineinabyss.com)
[![Java CI with Gradle](https://github.com/MineInAbyss/MineInAbyss/actions/workflows/gradle-ci.yml/badge.svg)](https://github.com/MineInAbyss/MineInAbyss/actions/workflows/gradle-ci.yml)
[![Maven](https://img.shields.io/maven-metadata/v?metadataUrl=https://repo.mineinabyss.com/releases/com/mineinabyss/mineinabyss/maven-metadata.xml)](https://repo.mineinabyss.com/#/releases/com/mineinabyss/mineinabyss)
[![Discord](https://badgen.net/discord/members/QXPCk2y)](https://discord.gg/QXPCk2y)
[![Contribute](https://shields.io/badge/Contribute-e57be5?logo=github%20sponsors&style=flat&logoColor=white)](https://github.com/MineInAbyss/MineInAbyss/wiki/Setup-and-Contribution-Guide)
</div>

Mine In Abyss is a Spigot plugin made for the Mine In Abyss Minecraft server. The server aims to recreate the world
of [Made in Abyss](https://en.wikipedia.org/wiki/Made_in_Abyss) within Minecraft. You can join
our [Discord](https://discord.gg/qWAMBSK) for more information about the server. Visit our
site [mineinabyss.com](https://mineinabyss.com) for news and our social media links.

## Features

* (WIP) Custom Item/Artifact system using [Geary ECS](https://github.com/MineInAbyss/Geary)
  and [Looty](https://github.com/MineInAbyss/Looty).
* Extremely deep world using [DeeperWorld](https://github.com/MineInAbyss/DeeperWorld)
    * Stacked sections automatically and seamlessly teleported betwen
* The Curse of Ascension
    * Varying curse effects mimicking the curses in the manga/anime as close as possible
* Using [Idofront](https://github.com/MineInAbyss/Idofront) API
    * Shares commonly used code between our projects
    * Many extension functions and Kotlin specific features
    * Command API (which we aren't using here yet!)
* GUIs made with [Guiy-compose](https://github.com/MineInAbyss/guiy-compose)

## Setup and contributions

Please read
our [Setup and Contribution guide](https://github.com/MineInAbyss/MineInAbyss/wiki/Setup-and-Contribution-Guide).

### Plugin dependencies

- [Our shared platform](https://github.com/MineInAbyss/Idofront/releases/latest)
- [DeeperWorld](https://github.com/MineInAbyss/DeeperWorld/releases/latest)
- [Guiy-compose](https://github.com/MineInAbyss/guiy-compose/releases/latest)
- [Geary](https://github.com/MineInAbyss/Geary/releases/latest)

#### Optional:

- [Vault](https://www.spigotmc.org/resources/vault.34315/)
    - Any economy plugin to go with it, ex [EssentialsX](https://www.spigotmc.org/resources/essentialsx.9089/)

#### Recommended for development:
- [PlugMan](https://www.spigotmc.org/resources/plugmanx.88135/)
### Gradle

```groovy
repositories {
    maven("https://repo.mineinabyss.com/releases")
}

dependencies {
    compileOnly("com.mineinabyss:mineinabyss:<version>")
}
```
