---
id: folia-support
title: Folia Support
sidebar_position: 2
---

InfiniteBuckets runs on Folia out of the box: the plugin declares `folia-supported: true` and does
all of its work through region-aware scheduling.

## How it behaves on Folia

- **Placing liquid and blocks** runs on the region that owns the target block; the player's
  inventory is updated on the player's own region.
- **Drains** remove liquid only in the region that owns the clicked block. A drain that crosses a
  region border leaves the far side alone; click again from the other side.
- **Cooldowns** are read from the database off the main threads, then applied on the player's region.
- **`/infb give`** from the console hands out items on the player's own region.

Everything else (protection hooks, CoreProtect logging, placed sponges coming back when mined, world
rules) works the same as on Paper.
