# InfiniteBuckets

This plugin provides highly customizable "infinite" buckets for your Minecraft server. Go beyond simple infinite water and lava to create unique items with special behaviors.

## Features

*   **Multiple Bucket Modes:**
    *   `VANILLA_LIKE`: Functions like a standard bucket but never runs out.
    *   `DRAIN_AREA`: Drains a configurable area of specified fluids.
    *   `EFFECT`: Applies a potion effect or other action when used.
*   **Extensive Configuration:** Customize every aspect of your buckets, including:
    *   Name, lore, and item appearance.
    *   Custom permissions for using and crafting.
    *   Limited or unlimited uses.
    *   Custom crafting recipes.
    *   Specify which fluids can be picked up or drained.
    *   Configure area-draining behavior (radius, cooldown, etc.).
*   **PlaceholderAPI Support:** Use placeholders in lore to display remaining uses.
*   **Soft Dependencies:** Integrates with various land-protection plugins to prevent abuse.

## Commands

*   `/infinitebuckets` (aliases: `/infb`, `/ib`): Main command for the plugin (currently placeholder).

## Permissions

*   `infb.admin`: Grants access to all admin commands.
*   `infb.use.<bucket_id>`: Allows a player to use the specified infinite bucket.
*   `infb.craft.<bucket_id>`: Allows a player to craft the specified infinite bucket.

## Configuration

You can create custom buckets in the `buckets.yml` file. Here is an example of a configuration for a few different buckets:

```yaml
buckets:
  - id: "infinite_water"
    displayName: "<blue>Infinite Water Bucket</blue>"
    lore:
      - "<gray>A bucket that never runs out of water.</gray>"
      - "<gray>Uses: <white><uses></white></gray>"
    icon: "minecraft:water_bucket"
    mode: "VANILLA_LIKE"
    fluids:
      - "minecraft:water"
    uses: -1 # Infinite uses
    crafting:
      enabled: true
      recipe: "WWW,WUW,WWW" # Example recipe shape
    permissions:
      use: "infb.use.water"
      craft: "infb.craft.water"

  - id: "lava_drainer"
    displayName: "<red>Lava Drainer</red>"
    lore:
      - "<gray>Drains a large area of lava.</gray>"
    icon: "minecraft:bucket"
    mode: "DRAIN_AREA"
    behavior:
      radius: 3
      maxBlocksPerUse: 100
      cooldown: 30
      fluids:
        - "minecraft:lava"
    permissions:
      use: "infb.use.lavadrainer"

  - id: "cleansing_milk"
    displayName: "<white>Cleansing Milk</white>"
    lore:
      - "<gray>Cures all negative effects.</gray>"
    icon: "minecraft:milk_bucket"
    mode: "EFFECT"
    action: "CURE_EFFECTS"
    permissions:
      use: "infb.use.milk"

customBuckets:
  - id: "limited_water_bucket"
    displayName: "<aqua>Limited Water Bucket</aqua>"
    lore:
      - "<!i><blue>• Has a limited number of uses</blue>"
      - "<!i><gray>• Right-click to place water</gray>"
      - "<!i><gray>• Uses Remaining: <uses></gray>" # Placeholder for remaining uses
    icon: "minecraft:water_bucket"
    mode: "vanilla_like"
    fluids: [ "minecraft:water" ]
    capacity: 1
    uses: 5
    crafting:
      enabled: false
    permissions:
      use: "buckets.use.limited_water"
      craft: ""
    enabled: true
```

## Soft Dependencies

InfiniteBuckets integrates with land protection plugins to prevent unauthorized bucket usage in protected regions. Players must have build permissions in a region to use infinite buckets there. Land owners automatically have permission to use buckets on their own land.

Supported protection plugins:

*   SuperiorSkyblock2
*   WorldGuard (v6 & v7)
*   GriefPrevention
*   Towny
*   Lands (v6.37+ & v7+)
*   PlotSquared
*   Residence

**Note:** Protection checks apply to all bucket placement locations, including:
- Direct block placement (water/lava)
- Waterlogging blocks
- Area draining operations (DRAIN_AREA mode)
