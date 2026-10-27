---
id: main-configuration
title: Main Configuration
sidebar_position: 2
---

`config.yml` holds the plugin-wide settings. Run `/infb reload` after editing.

## General

| Setting           | Description                                                                                 | Default |
|:------------------|:--------------------------------------------------------------------------------------------|:--------|
| `debug-mode`      | Extra console lines about protection hook detection and admin commands. Leave off unless troubleshooting. | `false` |
| `global-cooldown` | Milliseconds between uses for every bucket whose own `cooldown` is `0`. `0` means none.     | `0`     |

## Performance

| Setting                         | Description                                                                                                   | Default |
|:--------------------------------|:--------------------------------------------------------------------------------------------------------------|:--------|
| `performance.drain-block-limit` | Optional cap on blocks one drain-bucket use removes, on top of its `drain-radius`. `0` means no cap. When set, blocks nearest the click go first. | `0` |
| `performance.async-processing`  | Reserved; currently has no effect.                                                                            | `true`  |

See the [Performance Guide](../advanced-optimization/performance-guide.md) for when to set a drain limit.

## Integrations

| Setting                               | Description                                                                         | Default |
|:--------------------------------------|:------------------------------------------------------------------------------------|:--------|
| `integrations.auto-detect-hooks`      | Hook into the protection plugins and CoreProtect below when they're installed. `false` turns every hook off. | `true` |
| `integrations.hooks.worldguard`       | WorldGuard (and claim plugins built on it, such as LandClaim).                     | `true`  |
| `integrations.hooks.lands`            | Lands.                                                                              | `true`  |
| `integrations.hooks.bentobox`         | BentoBox.                                                                           | `true`  |
| `integrations.hooks.superiorskyblock` | SuperiorSkyblock2.                                                                  | `true`  |
| `integrations.hooks.coreprotect`      | Log bucket placements and drains to CoreProtect.                                   | `true`  |

See [Protection Plugins](../integrations/protection-plugins.md) and [CoreProtect](../integrations/coreprotect.md).

## Database

Cooldowns are stored in a database: SQLite by default, or MySQL to share them across servers. See
[Database Setup](database-setup.md).

## World rules

| Setting                                     | Description                                                                 | Default |
|:--------------------------------------------|:----------------------------------------------------------------------------|:--------|
| `world-settings.disabled-worlds`            | Worlds where no infinite bucket works at all.                               | `[]`    |
| `world-settings.default-nether-restriction` | Blocks water buckets in Nether worlds that have no rule of their own below. | `true`  |
| `world-settings.world-rules.<world>`        | Per-world `allow-water` and `allow-lava` (both `true` if left out).         | `world_nether` rule |

A world listed under `world-rules` follows only its own rule; `default-nether-restriction` applies
to Nether worlds without one. `allow-water` also covers powder snow buckets. Both placing and drain
buckets follow these rules, as do dispensers.

```yaml
world-settings:
  disabled-worlds:
    - event_world
  default-nether-restriction: true
  world-rules:
    world_nether:
      allow-water: false
      allow-lava: true
    resource_world:
      allow-lava: false
```

Players are told which rule stopped them with the `world-disabled`, `bucket-disabled-world` or
`nether-disabled` message.
