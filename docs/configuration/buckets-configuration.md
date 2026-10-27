---
id: buckets-configuration
title: Buckets Configuration
sidebar_position: 1
---

`buckets.yml` holds every bucket on your server, each under `buckets:` with its own ID. The ID is
what you use in `/infb give <player> <id>`. Edit the file and run `/infb reload`, or use the
in-game [Bucket Builder](../core-reference/bucket-builder.md), which writes to the same file.

## Bucket fields

Every field has a default, so a bucket only needs the ones you want to change.

| Field               | Description                                                                                                  | Default          |
|:--------------------|:-------------------------------------------------------------------------------------------------------------|:-----------------|
| `display-name`      | Item name. MiniMessage and legacy `&` colors.                                                                | the bucket ID    |
| `lore`              | Item lore lines. MiniMessage and legacy `&` colors.                                                          | none             |
| `liquid-type`       | `WATER`, `LAVA`, or `POWDER_SNOW`. A bucket with anything else is skipped.                                   | `WATER`          |
| `mode`              | `VANILLA_LIKE` places the liquid; `DRAIN_AREA` removes it around the click. See [Placing Buckets](../features-modules/placing-buckets.md) and [Drain Buckets & Sponges](../features-modules/drain-buckets-sponges.md). | `VANILLA_LIKE` |
| `drain-radius`      | `DRAIN_AREA` only: blocks the drained cube reaches on each side, 1 to 16. `3` drains 7x7x7.                  | `3`              |
| `place-block`       | `DRAIN_AREA` only: right-click places the `icon` as a block, like a vanilla sponge, and drains around it. Mine it to get the bucket back. | `false` |
| `usage-limit`       | Uses per item; `-1` for unlimited. See [Limited Uses & Cooldowns](../features-modules/limited-uses-cooldowns.md). | `-1`        |
| `cooldown`          | Milliseconds between uses per player. `0` uses `global-cooldown` from `config.yml`.                         | `0`              |
| `permission`        | Permission needed to use the bucket. Leave it out to let anyone use it.                                      | none             |
| `glowing`           | Enchantment glint on the item.                                                                               | `false`          |
| `custom-model-data` | Custom model data for resource packs. `0` for none.                                                          | `0`              |
| `place-sound`       | Sound played at the block on each use (an XSeries sound name, such as `ITEM_BUCKET_EMPTY_LAVA`). | `ITEM_BUCKET_EMPTY` |
| `refill-sound`      | Kept for compatibility; not played.                                                                          | `ITEM_BUCKET_FILL` |
| `icon`              | Item material, such as `SPONGE`. For `place-block`, also the block placed. | `WATER_BUCKET` |
| `allow-automation`  | Lets the bucket into dispensers and droppers. See [Hoppers & Dispensers](../features-modules/hoppers-dispensers.md). | `false` |

> Changing a bucket's `display-name`, `lore`, `icon` or `glowing` doesn't update items already
> handed out; give new ones. Changes to how the bucket behaves (`mode`, `drain-radius`,
> `cooldown`, `permission` and so on) apply to existing items right away.

## Examples

Paste any of these under `buckets:` and run `/infb reload`.

### Infinite Water Bucket

```yaml
  water_infinite:
    display-name: "<aqua>Infinite Water Bucket</aqua>"
    lore:
      - "<gray>Never runs out!</gray>"
      - "<gray>Right-click to place water</gray>"
    liquid-type: WATER
    mode: VANILLA_LIKE
    usage-limit: -1
    cooldown: 0
    permission: "infinitebuckets.use.water"
    glowing: true
    custom-model-data: 0
    place-sound: ITEM_BUCKET_EMPTY
    icon: WATER_BUCKET
    allow-automation: false
```

### Lava Bucket with 50 uses and a cooldown

```yaml
  lava_limited:
    display-name: "<red>Lava Bucket <gray>(50 uses)</gray></red>"
    lore:
      - "<gray>Right-click to place lava</gray>"
    liquid-type: LAVA
    mode: VANILLA_LIKE
    usage-limit: 50
    cooldown: 2000
    permission: "infinitebuckets.use.lavalimited"
    place-sound: ITEM_BUCKET_EMPTY_LAVA
    icon: LAVA_BUCKET
```

### Lava Sponge (click to drain)

Drains lava within 5 blocks of the click, 16 uses.

```yaml
  lava_sponge:
    display-name: "<red>Hot Lava Sponge</red>"
    lore:
      - "<gray>Right-click lava to soak it up</gray>"
    liquid-type: LAVA
    mode: DRAIN_AREA
    drain-radius: 5
    usage-limit: 16
    permission: "infinitebuckets.use.lavasponge"
    glowing: true
    place-sound: ITEM_BUCKET_FILL_LAVA
    icon: SPONGE
```

### Placeable Lava Sponge

Goes down as a block in lava, drains within 5 blocks, comes back with one use fewer when mined.

```yaml
  hot_sponge:
    display-name: "<red>HOT LAVA SPONGE</red>"
    lore:
      - "<gray>Place it in lava to soak it up</gray>"
    liquid-type: LAVA
    mode: DRAIN_AREA
    place-block: true
    drain-radius: 5
    usage-limit: 16
    permission: "infinitebuckets.use.hotsponge"
    glowing: true
    place-sound: ITEM_BUCKET_FILL_LAVA
    icon: SPONGE
```

### Placeable Water Sponge

The biggest drain: a 33x33x33 cube of water per placement, 8 placements.

```yaml
  super_sponge:
    display-name: "<gold>SUPER SPONGE</gold>"
    lore:
      - "<gray>Place it to drain a 33x33 area of water</gray>"
    liquid-type: WATER
    mode: DRAIN_AREA
    place-block: true
    drain-radius: 16
    usage-limit: 8
    permission: "infinitebuckets.use.supersponge"
    glowing: true
    place-sound: ITEM_BUCKET_FILL
    icon: SPONGE
```

### Dispenser Water

Can be loaded into a dispenser, which then places water forever.

```yaml
  water_dispenser:
    display-name: "<aqua>Dispenser Water</aqua>"
    liquid-type: WATER
    mode: VANILLA_LIKE
    usage-limit: -1
    allow-automation: true
    icon: WATER_BUCKET
```

### Infinite Powder Snow

```yaml
  powder_snow_infinite:
    display-name: "<white>Infinite Powder Snow</white>"
    liquid-type: POWDER_SNOW
    mode: VANILLA_LIKE
    usage-limit: -1
    place-sound: ITEM_BUCKET_EMPTY_POWDER_SNOW
    icon: POWDER_SNOW_BUCKET
```
