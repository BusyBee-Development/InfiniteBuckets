---
id: migration-guide
title: Migration Guide
sidebar_position: 3
---

## Upgrading from a version before 26.9

Older versions stored buckets in `buckets.yml` under `presets:` and `customBuckets:`. On first start,
26.9 and newer convert them to the current `buckets:` format automatically:

- A copy of the old file is saved first as `backups/buckets.yml.pre-migration-<timestamp>`.
- Each old bucket becomes a `buckets.<id>` entry, using its old `id`. The console shows
  `Migrated <n> bucket(s) from the old buckets.yml format.`
- Disabled old buckets (`enabled: false`) are left out. A bucket whose ID is already under
  `buckets:` is skipped with a warning, as is one without an ID or with a mode that no longer exists.
- Permissions carry over: an old bucket's `permissions.use`, or `infb.use.<id>` if it had none.
- Uses carry over, and lore keeps its old look (no italics).

Buckets players already have keep working: an old item is still recognized as its bucket and keeps
counting down its own uses.

Check the converted entries afterwards. Fields the old format didn't have get their defaults
(`cooldown: 0`, `glowing: true`, `allow-automation: false`).

## max-drain-blocks is now drain-block-limit

`performance.max-drain-blocks` was replaced by `performance.drain-block-limit`. The old default of
100 stopped drain buckets well short of their radius, so it's dropped on upgrade and the new default
is `0` (no cap). A value you'd changed yourself is carried over.

## Updating between current versions

Replace the jar and restart. See [Installation → Updating](../getting-started/installation.md#updating).
