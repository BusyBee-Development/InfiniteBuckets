---
id: bucket-builder
title: Bucket Builder
sidebar_position: 2
---

The Bucket Builder lets you create and edit buckets in-game. Open it with `/infb list` (or
`/infb builder`), permission `infinitebuckets.admin`.

## Bucket list

Every bucket in `buckets.yml` is shown, a page at a time.

| Click on a bucket | Does                                              |
|:------------------|:--------------------------------------------------|
| Left-click        | Opens it in the builder.                          |
| Right-click       | Asks to confirm, then deletes it from `buckets.yml`. |
| Shift-click       | Puts one in your inventory.                       |

**Create New Bucket** (the nether star) opens the builder on a new bucket named `bucket_<n>`: an
unlimited water bucket that places water.

## Builder buttons

| Button              | Does                                                                                   |
|:--------------------|:---------------------------------------------------------------------------------------|
| Select Liquid Type  | Pick water, lava, or powder snow.                                                      |
| Edit Display Name   | Closes the menu; type the new name in chat (MiniMessage). Type `cancel` to stop.       |
| Edit Lore           | Closes the menu; type the lore in chat, with `\|` between lines. Type `cancel` to stop. |
| Usage Limit         | Cycles Infinite, 100 uses, 500 uses. Other values go in `buckets.yml`.                 |
| Mode                | Switches between placing liquid and draining an area.                                  |
| Drain Radius        | Drain buckets only. Left-click +1, right-click -1, from 1 to 16.                       |
| Place as Block      | Drain buckets only. Turns `place-block` on or off.                                     |
| Toggle Glowing      | Turns the enchantment glint on or off.                                                 |
| Save Bucket         | Writes the bucket to `buckets.yml` and goes back to the list.                          |
| Cancel              | Goes back to the list without saving.                                                  |

Changing the liquid or mode also changes the icon (water bucket, lava bucket, powder snow bucket,
or an empty bucket for drain mode), unless the bucket has an icon you picked yourself, such as a
sponge.

## Set in buckets.yml only

The builder doesn't edit `icon`, `permission`, `cooldown`, `custom-model-data`, the sounds,
`allow-automation`, or a usage limit other than 100 or 500. Set those in
[`buckets.yml`](../configuration/buckets-configuration.md) and run `/infb reload`. Saving from the
builder keeps them.

## Customizing the menus

Titles, slots, materials, and text of every menu are in `guis.yml`, with MiniMessage formatting.
