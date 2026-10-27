---
id: placing-buckets
title: Placing Buckets
sidebar_position: 1
---

A placing bucket (`mode: VANILLA_LIKE`) works like a vanilla bucket that never empties: right-click
places its liquid and the bucket stays in your hand.

## Liquids

`liquid-type` can be `WATER`, `LAVA`, or `POWDER_SNOW`. A bucket with any other value is skipped
with a warning in the console.

## How placing works

- Like vanilla, the liquid goes into the block you click if it can be replaced (grass, flowers,
  torches, which drop as items), or against the face you click otherwise. Water also fills a
  waterloggable block you click, such as a slab or stairs.
- Right-clicking a container, door, trapdoor, fence gate, button, lever, bed, anvil, crafting table
  or enchanting table uses that block, as with a vanilla bucket. Sneak to place liquid instead.
- The bucket never turns into an empty bucket, and it can't pick liquid up. Main hand and off hand
  both work.
- Liquid that washes away a plant is handled the vanilla way, and [CoreProtect](../integrations/coreprotect.md) logs it.

## Where it's allowed

Every placement is checked against:

- The bucket's `permission` (see [Commands & Permissions](../core-reference/commands-permissions.md)).
- [World rules](../configuration/main-configuration.md#world-rules): disabled worlds, per-world
  water/lava rules, and water in the Nether.
- Your [protection plugins](../integrations/protection-plugins.md).

## Example

```yaml
buckets:
  water_infinite:
    display-name: "<aqua>Infinite Water Bucket</aqua>"
    lore:
      - "<gray>Never runs out!</gray>"
    liquid-type: WATER
    mode: VANILLA_LIKE
    usage-limit: -1
    permission: "infinitebuckets.use.water"
    glowing: true
    icon: WATER_BUCKET
```

For a bucket that runs out, see [Limited Uses & Cooldowns](limited-uses-cooldowns.md).
