# KysPlugin

KysPlugin is a Minecraft plugin that adds a custom command for players to test their luck with a chance of death or various effects.
It has been made specifically for a private Minecraft server with keepInventory on

## Features

- **Russian Roulette Mode**: Players have a 1/6 chance of dying, with a guaranteed death on the 6th attempt.
- **50/50 Mode**: Players have a 50% chance of dying or receiving a random negative effect.
- **Persistent Data**: Tracks successful and unsuccessful attempts for each player.
- **Customizable Effects**: Configure which effects can be applied in 50/50 mode.

## Commands

- `/kys`: Executes the Kys command.
- `/kyssetting`: Configures the Kys settings.
- `/kyssetting countreset`: Resets the count of successful and unsuccessful attempts.

## Installation

1. Download the latest release of the plugin.
2. Place the plugin jar file in the `plugins` directory of your Minecraft server.
3. Start the server to load the plugin.

## Configuration

The plugin does not require any additional configuration files. All settings are managed through the `/kyssetting` command.

## Building from Source

To build the plugin from source, you need to have Maven installed. Run the following command in the project directory:

```sh
mvn clean package
```
The compiled jar file will be located in the target directory.


## Changelog

1.1\
Improved the command to have less Java Errors (fuck java)\
Added the inabililty to use the command in the nether\

1.2\
Added the inability to run the command in the End