---
id: drain-buckets-sponges
title: Drain Buckets & Sponges
sidebar_position: 2
---

A drain bucket (`mode: DRAIN_AREA`) removes liquid instead of placing it. Give it a `SPONGE` icon
and it's a sponge. It works one of two ways:

- **Click to drain** (default): right-click and every block of the bucket's `liquid-type` within
  `drain-radius` is removed. The bucket stays in your hand and takes one use.
- **Place as a block** (`place-block: true`): right-click puts the bucket down as a real block,
  like a vanilla sponge, and drains around it. Mine it to pick it back up.

## Drain radius

`drain-radius` is how far the drained cube reaches on each side, from 1 to 16 (default 3):

| `drain-radius` | Cube drained  |
|:---------------|:--------------|
| `3`            | 7x7x7         |
| `5`            | 11x11x11      |
| `8`            | 17x17x17      |
| `16`           | 33x33x33      |

A value above 16 is lowered to 16, with a warning in the console when the plugin loads. Only blocks
of the bucket's own `liquid-type` are removed: a lava sponge leaves water alone. Blocks nearest the
centre go first.

## Click to drain

Right-click a block in or next to the liquid, or aim at a liquid surface and right-click: the drain
is centred on the block you clicked, or the liquid source you're looking at. Nothing drained means
no use is taken.

### Example: Lava Sponge (click to drain)

Clears lava in a 5-block radius and lasts 16 uses.

```yaml
buckets:
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

## Place as a block

With `place-block: true`, right-click puts the bucket's `icon` down as a block (a sponge if the icon
isn't a full block) and drains around it.

- **Where it goes:** against the face you click, or straight into a liquid source you're looking
  at. It won't go inside a player or mob, or into a block that isn't replaceable.
- **Uses:** each placement takes one use. Mine the block and you get the bucket back with the uses
  it has left, so a 16-use sponge comes back with 15.
- **Last use:** a bucket placed on its last use breaks into a plain block, and the player is told
  it's used up.
- **Pistons and explosions:** a pushed or blown-up placed bucket still drops as the bucket. This is
  stored on the block, so it survives restarts.
- **Water sponges stay dry:** the drain runs before the block goes down, so a placed water sponge
  doesn't turn into a wet sponge.

### Example: Placeable Lava Sponge

```yaml
buckets:
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

### Example: Placeable Water Sponge

The largest drain there is: a 33x33x33 cube of water per placement, 8 placements per sponge.

```yaml
buckets:
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

## Protection

Placing a block checks your protection plugin's place permission. Each drained block checks break,
so a drain next to a claim stops at the claim's edge instead of failing outright. With CoreProtect
installed, the placed block and every drained block are logged under the player. See
[Protection Plugins](../integrations/protection-plugins.md) and [CoreProtect](../integrations/coreprotect.md).

## Limiting how much one use drains

The radius alone decides the size. To cap the number of blocks one use can remove, set
`performance.drain-block-limit` in `config.yml`; see the
[Performance Guide](../advanced-optimization/performance-guide.md).
