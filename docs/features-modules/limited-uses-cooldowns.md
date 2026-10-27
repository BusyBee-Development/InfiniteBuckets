---
id: limited-uses-cooldowns
title: Limited Uses & Cooldowns
sidebar_position: 3
---

## Limited uses

`usage-limit` is how many times one bucket item can be used. `-1` (the default) means unlimited.

- Each use takes one: one liquid placed, one drain, or one [placement as a block](drain-buckets-sponges.md#place-as-a-block).
- When the last use is spent, that item is used up and the player gets the `bucket-depleted` message.
- Buckets with the same uses left stack. Each item in a stack keeps its own uses: using one splits
  it off into your hand and counts it down, and only that one is used up at the end. The rest keep
  their full count.
- Lowering `usage-limit` in `buckets.yml` doesn't change items already handed out; they keep the
  uses they have.

```yaml
buckets:
  limited_water:
    display-name: "<aqua>Water Bucket <gray>(5 uses)</gray></aqua>"
    liquid-type: WATER
    mode: VANILLA_LIKE
    usage-limit: 5
    icon: WATER_BUCKET
```

## Cooldowns

`cooldown` is the wait, in milliseconds, between uses of a bucket by the same player. `1000` is one
second.

- A bucket with `cooldown: 0` uses `global-cooldown` from `config.yml` instead (also `0` by default,
  meaning no cooldown).
- The cooldown is per player and per bucket type: waiting on a water bucket doesn't block a lava
  bucket.
- Cooldowns are stored in the [database](../configuration/database-setup.md), so they survive
  restarts and, with MySQL, carry across servers.
- A player on cooldown gets the `gui.cooldown.active` message with the seconds left.

```yaml
buckets:
  lava_slow:
    display-name: "<red>Slow Lava Bucket</red>"
    liquid-type: LAVA
    mode: VANILLA_LIKE
    cooldown: 5000   # 5 seconds between uses
    icon: LAVA_BUCKET
```
