---
id: commands-permissions
title: Commands & Permissions
sidebar_position: 1
---

The base command is `/infinitebuckets`, with the aliases `/infb` and `/ib`.

## Commands

| Command                                 | Description                                                         | Permission              |
|:----------------------------------------|:--------------------------------------------------------------------|:------------------------|
| `/infb help`                            | Shows the help menu.                                                | None                    |
| `/infb give <player> <bucket> [amount]` | Gives a player `amount` (default 1) of a bucket, by its ID in `buckets.yml`. Items that don't fit are dropped at the player's feet. Works from the console. | `infinitebuckets.admin` |
| `/infb list`                            | Opens the bucket list, where you create, edit and delete buckets, or take one for yourself. | `infinitebuckets.admin` |
| `/infb builder`                         | Same as `/infb list`.                                               | `infinitebuckets.admin` |
| `/infb reload`                          | Reloads `config.yml`, `buckets.yml`, `messages.yml`, `guis.yml` and the protection hooks. | `infinitebuckets.admin` |

`/infb list` and `/infb builder` are player-only. See [Bucket Builder](bucket-builder.md).

## Permissions

| Permission                  | Default | Description                                                        |
|:----------------------------|:--------|:-------------------------------------------------------------------|
| `infinitebuckets.admin`     | op      | All commands, the Bucket Builder, and update notifications on join. |
| `infinitebuckets.use.water` | true    | Use `water_infinite` (the permission set on it in `buckets.yml`).  |
| `infinitebuckets.use.lava`  | true    | Use `lava_infinite` (the permission set on it in `buckets.yml`).   |

### Per-bucket permissions

Each bucket's `permission` in `buckets.yml` is the node a player needs to **use** it. It can be any
node you like; a node that isn't one of the two above only works for ops until you grant it. Leave
`permission` out and anyone holding the bucket can use it.

Players without it get the `no-permission-use` message. Giving buckets is always admin-only.
