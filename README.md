# InfiniteBuckets

A lightweight Spigot plugin that provides players with infinite water and lava buckets, perfect for survival, creative, or utility purposes. No external dependencies are required, and a custom GUI is in development for future releases.

## Plugin Details

- **Name**: InfiniteBuckets
- **Version**: 1.1
- **API Version**: 1.21
- **Authors**: [djtmk]
- **Description**: Infinite Water and Lava Buckets

## Features

- Obtain infinite-use water and lava buckets via intuitive commands.
- Fully customizable bucket display names and lore.
- Standalone plugin—no external dependencies like Vault needed.
- Permission-based access for fine-tuned control.
- Quick configuration reloads without server restarts.

## Commands

- **` /inf [water|lava|reload|give] `**  
  The main command for InfiniteBuckets.
    - `water`: Grants the player an Infinite Water Bucket.
    - `lava`: Grants the player an Infinite Lava Bucket.
    - `reload`: Reloads the plugin’s configuration file (requires `inf.admin`).
    - `give`: Grants buckets to other players (requires `inf.admin`).
- **Aliases**: `/infinitebuckets`, `/ib`
- **Usage**: `/<command> [water|lava|reload|give]`

> **Note**: Commands have been updated in version 1.1 to streamline access and remove economy dependencies.

## Permissions

- **`inf.use.water`**
    - *Description*: Allows use of the `/inf water` command.
    - *Default*: `true`
- **`inf.use.lava`**
    - *Description*: Allows use of the `/inf lava` command.
    - *Default*: `true`
- **`inf.admin`**
    - *Description*: Allows use of `/inf reload` and `/inf give` commands.
    - *Default*: `op`

## Configuration (`config.yml`)

Below is the default configuration file. Customize the display names and lore to fit your server’s theme!

```
# InfiniteBuckets Spigot Plugin!
# Create infinite Lava and Water buckets for all your needs!
#
lava:
  display: "&cInfinite Lava Bucket"
  lore:
    - "&7&oLegends murmur of a molten treasure"
    - "&7&oforged in the shadow of Vesuvius’ rage,"
    - "&7&otouched by the pleas of Pompeii’s lost souls"
    - "&7&oto defy the ash that choked their fields,"
    - "&7&oa spark of mercy spared from ruin."
    - " "
    - "&cThis is an Infinite Lava Bucket"

water:
  display: "&bInfinite Water Bucket"
  lore:
    - "&7&oLegend says this water bucket"
    - "&7&owas enchanted by a nice young fairy"
    - "&7&oto solve the drought that a tiny village"
    - "&7&owas suffering"
    - " "
    - "&bThis is an Infinite Water Bucket"
  ```
### Dependencies
- None!
  * As of version 1.1, InfiniteBuckets operates independently, with Vault and economy plugin support removed for simplicity.

### Installation
* Download the latest InfiniteBuckets .jar file from the GitHub Releases page.
    Place the .jar file in your server’s plugins folder.
    Restart your server or use /inf reload to load the plugin.
    Customize the config.yml file in plugins/InfiniteBuckets/ as desired.

### Credits
- Original Developer: ZedBear
    Last Updated by ZedBear: 4 years ago
-    Current Developer: djtmk
-    GitHub Repository: [ZedBear Github](https://github.com/ZedBear/InfiniteBuckets)
-    Recent Updates:
       - Version 1.1: Removed Vault dependency, updated commands, added permissions, and modernized for Spigot 1.21.

### Future Plans
- Develop a custom GUI for bucket acquisition.
