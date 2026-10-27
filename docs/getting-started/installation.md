---
id: installation
title: Installation
sidebar_position: 2
---

1. Check the [Requirements](requirements.md): Java 21+, Paper/Spigot/Folia 1.20.1+.
2. Download the latest jar from [Modrinth](https://modrinth.com/plugin/infinitebuckets).
3. Drop the jar into your server's `plugins/` folder.
4. Start (or restart) the server. On first run, InfiniteBuckets creates `plugins/InfiniteBuckets/`:
   - `config.yml`: performance, cooldown, protection hooks, database, and world rules.
   - `buckets.yml`: your buckets. Ships with `water_infinite` and `lava_infinite`.
   - `messages.yml`: every player-facing message.
   - `guis.yml`: the layout and text of the Bucket Builder menus.
   - `database.db`: the SQLite database that stores cooldowns (unless you switch to MySQL).
5. Edit whichever files you need (see [Configuration](../configuration/buckets-configuration.md)).
6. Apply changes without restarting: `/infb reload` (permission `infinitebuckets.admin`).

## Verifying it's working

Run `/infb give <yourname> water_infinite`. You should get an **Infinite Water Bucket** that places
water and stays full. If you run a protection plugin, the console shows `Hooked into <plugin>.` on
startup for each one InfiniteBuckets found.

## Updating

Replace the jar and restart. `config.yml`, `messages.yml`, and `guis.yml` get any new keys an update
adds, with their default values. Your values are kept, and a copy of the old file is saved first as
`backups/<file>.backup-<timestamp>`. A file with nothing new is left alone.

`buckets.yml` is never merged with the defaults: it holds your own buckets, and a bucket you deleted
stays deleted. Coming from a version before 26.9? See the
[Migration Guide](../advanced-optimization/migration-guide.md).

## Uninstalling

Remove the jar from `plugins/` and restart. The `plugins/InfiniteBuckets/` folder stays on disk;
delete it for a clean removal. Buckets already handed out turn back into ordinary items.
