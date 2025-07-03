# InfiniteBuckets

A lightweight Spigot plugin that provides players with infinite water and lava buckets, perfect for survival, creative, or utility purposes. No external dependencies are required, and a custom GUI is in development for future releases.

## Plugin Details

- **Name**: InfiniteBuckets
- **Version**: 1.4.8
- **API Version**: 1.21
- **Folia Supported**: True (Supports Folia)
- **Authors**: [djtmk]
- **Description**: Infinite Water and Lava Buckets

## Features

- Obtain infinite-use water and lava buckets via intuitive commands.
- Fully customizable bucket display names and lore.
- Standalone plugin—no external dependencies like Vault needed.
- Permission-based access for fine-tuned control.
- Quick configuration reloads without server restarts.

## Commands

- **` /infb [water|lava|reload|give] `**  
  The main command for InfiniteBuckets.
    - `water`: Grants the player an Infinite Water Bucket.
    - `lava`: Grants the player an Infinite Lava Bucket.
    - `reload`: Reloads the plugin’s configuration file (requires `infb.admin`).
    - `give`: Grants buckets to other players (requires `infb.admin`).
    - `debug <on|off>`: Toggles debug logging for bucket interactions (requires `infb.admin`).
- **Aliases**: `/infinitebuckets`, `/ib`
- **Usage**: `/<command> [water|lava|reload|give]`

> **Note**: Commands have been updated in version 1.1 to streamline access and remove economy dependencies.

## Permissions

- **`infb.use.water`**
    - *Description*: Allows use of the inf water bucket.
    - *Default*: `true`
- **`infb.use.lava`**
    - *Description*: Allows use of the inf lava bucket.
    - *Default*: `true`
- **`infb.admin`**
    - *Description*: Allows use of `/infb reload` and `/infb give` commands.
    - *Default*: `op`

## Configuration (`config.yml`)

Below is the default configuration file. Customize the display names and lore to fit your server’s theme!

```
#=======================================================
# InfiniteBuckets Configuration
#=======================================================
# A lightweight Spigot plugin that provides players with
# infinite water and lava buckets.
#
# Color codes:
# &c = Red      &b = Aqua     &7 = Gray
# &o = Italic   &l = Bold     &n = Underline
#=======================================================

#-------------------------------------------------------
# General Settings
#-------------------------------------------------------
# Enable debug mode to see detailed logs in console
debug: false

#-------------------------------------------------------
# Lava Bucket Settings
#-------------------------------------------------------
lava:
  # Display name shown on the bucket item
  display: "&cInfinite Lava Bucket"

  # Lore text shown when hovering over the bucket
  # DO NOT CHANGE the formatting or order of these lines
  lore:
    - "&7&oLegends murmur of a molten treasure"
    - "&7&oforged in the shadow of Vesuvius' rage,"
    - "&7&otouched by the pleas of Pompeii's lost souls"
    - "&7&oto defy the ash that choked their fields,"
    - "&7&oa spark of mercy spared from ruin."
    - " "
    - "&cThis is an Infinite Lava Bucket"

#-------------------------------------------------------
# Water Bucket Settings
#-------------------------------------------------------
water:
  # Display name shown on the bucket item
  display: "&bInfinite Water Bucket"

  # Whether the infinite water bucket works in the Nether
  # true = works in Nether, false = doesn't work in Nether
  work_in_nether: false

  # Lore text shown when hovering over the bucket
  # DO NOT CHANGE the formatting or order of these lines
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

### Soft-Dependencies
- Yes - SuperiorSkyBlock2 
  * This plugin works with SSB2 to listen to the protection made by the plugin. Players can not place on other peoples island with out permission. This plugin can work without SSB2 as well. 

### Installation
1. Download the latest InfiniteBuckets .jar file from one of these sources:
   - [Modrinth](https://modrinth.com/plugin/infinitebuckets)
   - [BuiltByBit](https://builtbybit.com/resources/infinitebuckets.61863/)
2. Place the .jar file in your server's plugins folder.
3. Restart your server or use `/infb reload` to load the plugin.
4. Customize the config.yml file in plugins/InfiniteBuckets/ as desired.

### Credits
- **Original Developer**: ZedBear
  - Last Updated by ZedBear: 4 years ago
  - Original GitHub Repository: [ZedBear Github](https://github.com/ZedBear/InfiniteBuckets)
- **Current Developer**: djtmk
