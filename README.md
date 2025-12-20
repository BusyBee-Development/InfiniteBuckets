# InfiniteBuckets

## Overview

InfiniteBuckets is a Spigot plugin that introduces a variety of configurable, permission-based infinite buckets. These buckets can have limited uses, custom names, and unique abilities, providing a flexible and engaging experience for server administrators and players.

## Features

- **Multiple Bucket Types**: Create and customize different types of infinite buckets, including:
  - **Vanilla-like**: Infinite water and lava buckets that behave like their vanilla counterparts.
  - **Drain Area**: Buckets that can drain a specified radius of fluids, with configurable settings for radius, max blocks per use, and fluid types.
  - **Effect**: Buckets that can apply effects to players, such as clearing all active potion effects.
- **Limited Uses**: Configure buckets to have a limited number of uses. When a bucket is depleted, it can be configured to disappear or be replaced with a regular bucket.
- **Permissions**: Each bucket has its own set of permissions for crafting, usage, and administration, allowing for fine-grained control over who can use which buckets.
- **Protection Hooks**: The plugin integrates with various land protection plugins to ensure that buckets cannot be used to bypass build restrictions. Supported plugins include:
  - WorldGuard
  - Lands
  - SuperiorSkyblock
- **Customization**: Customize bucket names, lore, and other attributes to create unique and thematic items for your server.
- **Dispenser Support**: Vanilla-like infinite buckets can be used in dispensers, allowing for automated fluid placement without consuming the bucket.

## Commands

The plugin provides a single command, `/infinitebuckets`, with the following subcommands:

- `/infinitebuckets reload`: Reloads the plugin's configuration files.
- `/infinitebuckets give <player> <bucket> [amount]`: Gives a specified amount of a custom bucket to a player.
- `/infinitebuckets list`: Lists all available bucket types.

## Configuration

The plugin's configuration is split into three files:

- `config.yml`: The main configuration file, where you can enable or disable features, configure debug logging, and manage other general settings.
- `buckets.yml`: This file contains the definitions for all custom buckets. You can create new buckets, modify existing ones, and configure their properties, such as name, lore, uses, and special abilities.
- `messages.yml`: All player-facing messages can be customized in this file, allowing you to translate or rephrase any message to fit your server's theme.

## Technical Details

- **Java Version**: 17
- **API**: Paper 1.21
- **Dependencies**:
  - FoliaLib
  - WorldGuard
  - WorldEdit
  - Lands
  - SuperiorSkyblock
## Building from Source

To build the plugin from source, you will need to have Maven and JDK 17 installed.

1. Clone the repository: `git clone https://github.com/djtmk/InfiniteBuckets.git`
2. Navigate to the project directory: `cd InfiniteBuckets`
3. Run the Maven build command: `mvn clean package`

The compiled JAR file will be located in the `target` directory.
