---
id: requirements
title: Requirements
sidebar_position: 1
---

## Server

- **Minecraft 1.20.1 or newer.**
- **Paper** (recommended), **Spigot**, or **Folia**. On Folia every bucket action runs on the region
  that owns the block or player. See [Folia Support](../advanced-optimization/folia-support.md).

## Java

- **Java 21** or newer.

## Dependencies

None. InfiniteBuckets runs on its own. These plugins are optional and hooked automatically when
present:

| Plugin              | What InfiniteBuckets does with it                                  |
|:--------------------|:-------------------------------------------------------------------|
| WorldGuard 7        | Checks build, place and break flags, and region bypass.            |
| Lands               | Checks the land's block-place flag for the player.                 |
| SuperiorSkyblock2   | Checks the island's `BUILD` privilege.                             |
| BentoBox            | Checks the island's bucket flag.                                   |
| CoreProtect         | Logs every bucket placement and drain, so they can be rolled back. |

See [Protection Plugins](../integrations/protection-plugins.md) and
[CoreProtect](../integrations/coreprotect.md) for details.
