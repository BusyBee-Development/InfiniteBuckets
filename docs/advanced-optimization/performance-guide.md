---
id: performance-guide
title: Performance Guide
sidebar_position: 1
---

Placing buckets cost about as much as a vanilla bucket. Drain buckets are the ones to think about:
one use checks every block in a cube and removes the matching liquid in one go.

## How big a drain is

| `drain-radius` | Blocks checked | Typical use                          |
|:---------------|:---------------|:-------------------------------------|
| `3` (default)  | 343            | Clearing a small pool                |
| `5`            | 1,331          | Lava sponge for a nether build       |
| `8`            | 4,913          | Clearing a room or a lake            |
| `16` (max)     | 35,937         | Ocean monuments, big build areas     |

Checking blocks is cheap. What costs more is removing them, since each removed block updates its
neighbours. A radius-16 drain in open ocean can remove tens of thousands of water blocks in one
tick.

## Capping a drain

If big drains cause lag spikes on your server, cap the number of blocks one use may remove:

```yaml
performance:
  drain-block-limit: 2000
```

`0` (the default) means no cap. With a cap, the blocks nearest the click are drained first and the
rest are left, so the player can click again to continue.

## Tips

- Prefer a moderate `drain-radius` with more `usage-limit` over one huge radius.
- Give large drain buckets a `cooldown` (milliseconds) so they can't be spammed.
- On Folia, a drain only touches blocks in the
  region that owns the click; see [Folia Support](folia-support.md).
