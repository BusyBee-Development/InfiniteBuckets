---
id: coreprotect
title: CoreProtect
sidebar_position: 2
---

With CoreProtect installed, every bucket change is logged the way CoreProtect logs a vanilla bucket,
so `/co lookup` shows bucket use and `/co rollback` takes it back. The console shows
`Hooked into CoreProtect.` on startup.

## What's logged

| Action                                   | Shows in CoreProtect as                                         |
|:-----------------------------------------|:----------------------------------------------------------------|
| A player places liquid                   | `placed water` (or lava, powder snow) under the player's name. A plant the liquid washed away is logged as broken. |
| A drain bucket removes liquid            | `broke water` (or lava) for each block, under the player.       |
| A player places a sponge block (`place-block`) | `placed sponge` (or the bucket's `icon`) under the player, plus each drained block. |
| A dispenser places liquid                | `placed water` under `#dispenser`.                              |

## Rolling back

```
/co rollback u:<player> t:1h r:20
```

takes back that player's bucket placements and puts back the liquid their drain buckets removed.

## Requirements

CoreProtect Community Edition and forks that keep its API both work. If CoreProtect is installed but
its API is turned off or too old, the console says so and buckets work without logging.

Turn logging off with `integrations.hooks.coreprotect: false` in `config.yml`.
