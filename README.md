![Mine in Abyss](https://user-images.githubusercontent.com/1530104/46649702-e2566300-cb4e-11e8-9837-57a656d4d492.png)

# Mine In Abyss

Mine In Abyss is a plugin for spigot that is made for the Mine In Abyss minecraft server.
The server aims to recreate the world of [Made in Abyss](https://en.wikipedia.org/wiki/Made_in_Abyss) within Minecraft.
You can join our [Discord](https://discord.gg/qWAMBSK) for more information about the server.

## Features
* Custom Item/Artifact system using [Looty](https://github.com/MineInAbyss/Looty)
    * A number of premade artifacts such as [blaze reap](http://madeinabyss.wikia.com/wiki/Blaze_Reap)
    * API for creating own artifacts
    * Custom loot spawning as entities on map
* Extremely deep world using [DeeperWorld](https://github.com/MineInAbyss/DeeperWorld)
    * Stacked sections automatically and seamlessly teleported betwen
* The Curse of Ascension (planned to be moved to a separate plugin called [AbyssialCurse](https://github.com/MineInAbyss/Abysscurse))
    * Varying curse effects mimicking the curses in the manga/anime as close as possible

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

You will need a working Spigot/Paper server. Get the [Paper jar](https://papermc.io/downloads#Paper-1.13), then install Paper with [this guide](https://paper.readthedocs.io/en/stable/server/getting-started.html). 
We are currently running a `1.13.2` server, and our plugins are designed to work with this version of Spigot. 

### Setup

We recommend using IntelliJ as your IDE. You can look at the quick guide below, or find a full guide [here](https://github.com/MineInAbyss/MineInAbyss/wiki/Spigot-Plugin-Setup-Guide-and-contributing-to-this-project) for a detailed installation walkthrough.

1. Clone this repository
2. Clone [DeeperWorld](https://github.com/Derongan/DeeperWorld) into the same directory
3. Enter the MineInAbyss directory
3. Build the project with gradle
    * Linux/OSX: `./gradlew build`
    * Windows: `gradlew.bat build`
4. Copy the jar from `MineInAbyss/MineInAbyss/build/libs` to the spigot plugin directory
5. Copy the jar from `DeeperWorld/build/libs` to the spigot plugin directory
7. Update the config files based on the samples to work for your use case

## Contributing

Talk to us on the Discord if you want to help. 