[![Mine in Abyss](https://user-images.githubusercontent.com/16233018/75004708-02cc4800-543a-11ea-8bb3-a9184d9311a0.png)](https://mineinabyss.com)

[![Java CI with Gradle](https://github.com/MineInAbyss/MineInAbyss/actions/workflows/gradle-ci.yml/badge.svg)](https://github.com/MineInAbyss/MineInAbyss/actions/workflows/gradle-ci.yml)
[![Maven](https://badgen.net/maven/v/metadata-url/repo.mineinabyss.com/releases/com/derongan/minecraft/MineInAbyss/maven-metadata.xml)](https://repo.mineinabyss.com/releases/com/mineinabyss/mineinabyss)
[![Discord](https://badgen.net/discord/members/QXPCk2y)](https://discord.gg/QXPCk2y)
[![Contribute](https://shields.io/badge/Contribute-e57be5?logo=github%20sponsors&style=flat&logoColor=white)](https://github.com/MineInAbyss/MineInAbyss/wiki/Setup-and-Contribution-Guide)

# Mine In Abyss

Mine In Abyss is a Spigot plugin made for the Mine In Abyss Minecraft server.
The server aims to recreate the world of [Made in Abyss](https://en.wikipedia.org/wiki/Made_in_Abyss) within Minecraft.
You can join our [Discord](https://discord.gg/qWAMBSK) for more information about the server. Visit our site [mineinabyss.com](https://mineinabyss.com) for news and our social media links.

## Features
* (WIP) Custom Item/Artifact system using [Geary ECS](https://github.com/MineInAbyss/Geary) and [Looty](https://github.com/MineInAbyss/Looty).
* Extremely deep world using [DeeperWorld](https://github.com/MineInAbyss/DeeperWorld)
    * Stacked sections automatically and seamlessly teleported betwen
* The Curse of Ascension
    * Varying curse effects mimicking the curses in the manga/anime as close as possible
* Using [Idofront](https://github.com/MineInAbyss/Idofront) API
   * Shares commonly used code between our projects
   * Many extension functions and Kotlin specific features
   * Command API (which we aren't using here yet!)
* GUIs made with [Guiy](https://github.com/MineInAbyss/guiy)
   * Lots of features and nice to code with

## Setup and Contributions

Please read our [Setup and Contribution guide](https://github.com/MineInAbyss/MineInAbyss/wiki/Setup-and-Contribution-Guide).

### Additional plugin dependencies
- [DeeperWorld](https://github.com/MineInAbyss/DeeperWorld)
- [Vault](https://www.spigotmc.org/resources/vault.34315/)

#### Optional:
- [Multiverse](https://www.spigotmc.org/resources/multiverse-core.390/) (or other multi-world plugins if needed)
- [Geary](https://github.com/MineInAbyss/Geary) and [Looty](https://github.com/MineInAbyss/Looty) (to use relics)

### Gradle

```groovy
repositories {
    maven { url 'https://repo.mineinabyss.com/releases' }
}

dependencies {
    implementation 'com.mineinabyss:mineinabyss:<version>'
}
```
