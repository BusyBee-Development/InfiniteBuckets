---
id: troubleshooting
title: Troubleshooting
sidebar_position: 4
---

## A bucket does nothing when right-clicked

- **No message at all:** you may be clicking a chest, door, button or other usable block; sneak to
  use the bucket on it. A drain bucket with nothing to drain in range also does nothing.
- **`no-permission-use`:** the player lacks the bucket's `permission`.
- **`world-disabled`, `bucket-disabled-world`, `nether-disabled`:** a [world rule](../configuration/main-configuration.md#world-rules) blocks it.
- **Cooldown message:** the bucket's `cooldown` (or `global-cooldown`) hasn't run out.
- **Silently in a claim:** the [protection plugin](../integrations/protection-plugins.md) denies it.

## A sponge won't go down as a block

`place-block: true` must be set on the bucket, and it must be `mode: DRAIN_AREA`. Without it, a
sponge drains from your hand and never places. Then run `/infb reload`.

The block also needs room: it won't go into a solid block or inside a player or mob.

## A placed sponge broke into a plain sponge

It was placed on its last use, so it's used up. Or it was broken in creative mode (no drops), or
removed by something that doesn't drop items, such as WorldEdit.

## A drain stops short of its radius

- `performance.drain-block-limit` in `config.yml` caps it.
- Blocks the player can't break (a neighbouring claim) are skipped.
- `drain-radius` above 16 is lowered to 16; check the console for the warning.
- On Folia, the drain stays inside the region that owns the click.

## Changes to buckets.yml don't show up

Run `/infb reload`. Name, lore, icon and glint are set when an item is created, so hand out new
items to see those change; behaviour changes apply to existing items right away. A bucket with an
invalid `liquid-type` is skipped with a warning in the console.

## The console says a file was corrupted

`config.yml`, `messages.yml` or `guis.yml` had a YAML mistake. The broken file is saved as
`backups/<file>.corrupted-<timestamp>` and a fresh default file is created in its place. Copy your
settings back from the backup, fixing the mistake.
