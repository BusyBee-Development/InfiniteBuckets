---
id: quick-start
title: Quick Start
sidebar_position: 3
---

## 1. Give yourself a bucket

InfiniteBuckets ships with two buckets:

| Bucket ID        | What it does                    | Permission to use            |
|:-----------------|:--------------------------------|:-----------------------------|
| `water_infinite` | Places water and never empties. | `infinitebuckets.use.water`  |
| `lava_infinite`  | Places lava and never empties.  | `infinitebuckets.use.lava`   |

```
/infb give <player> water_infinite
/infb give <player> lava_infinite 3
```

Both permissions are given to everyone by default, so any player holding one of these buckets can
use it. Control who *gets* buckets through `/infb give` (admins only).

## 2. Build your own

Run `/infb list`, click **Create New Bucket**, and set it up in the
[Bucket Builder](../core-reference/bucket-builder.md): liquid, name, lore, uses, mode, drain
radius. Click **Save Bucket** and it's written to `buckets.yml`.

Prefer YAML? Every option is on [Buckets Configuration](../configuration/buckets-configuration.md),
with ready-to-paste examples.

## 3. Make a sponge

A sponge is a drain bucket: one right-click clears all the lava (or water) around it. Paste this
under `buckets:` in `buckets.yml`, run `/infb reload`, then `/infb give <you> hot_sponge`:

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
    icon: SPONGE
```

It goes down as a block, drains lava within 5 blocks, and comes back when mined with one use fewer.
More in [Drain Buckets & Sponges](../features-modules/drain-buckets-sponges.md).

> A bucket's `permission` is checked when it's used. A new node like
> `infinitebuckets.use.hotsponge` only works for ops until you grant it through your permissions
> plugin. Leave `permission` out to let everyone holding the sponge use it.
