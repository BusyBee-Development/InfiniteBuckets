---
id: message-customization
title: Message Customization
sidebar_position: 3
---

Every message players see is in `messages.yml`. Messages use
[MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting (gradients, hex colors,
hover and click), and legacy `&` color codes still work. Run `/infb reload` after editing.

`plugin-prefix` is put in front of every single-line message. Set a message to `""` to turn it off.

## Messages

| Key                          | When it's sent                                                   | Placeholders                     |
|:-----------------------------|:-----------------------------------------------------------------|:---------------------------------|
| `no-permission-command`      | A player runs an admin command without `infinitebuckets.admin`. |                                  |
| `no-permission-use`          | A player uses a bucket without its `permission`.                 | `<bucket_name>`                  |
| `world-disabled`             | The world is in `disabled-worlds`.                               | `<bucket_name>`                  |
| `bucket-disabled-world`      | The world's rule blocks this liquid.                             | `<bucket_name>`                  |
| `nether-disabled`            | Water in the Nether with `default-nether-restriction` on.        | `<bucket_name>`                  |
| `reload-success`             | After `/infb reload`.                                            |                                  |
| `bucket-no-uses`             | A limited bucket with no uses left is used.                      | `<bucket_name>`                  |
| `bucket-depleted`            | A limited bucket's last use is spent.                            | `<bucket_name>`                  |
| `dispenser-no-limited-uses`  | A player puts a limited bucket in a dispenser or dropper.        |                                  |
| `gui.cooldown.active`        | A bucket is used while on cooldown.                              | `<bucket_name>`, `<seconds>`     |
| `give.sender`                | To whoever ran `/infb give`.                                     | `<player>`, `<amount>`, `<bucket_name>` |
| `give.receiver`              | To the player who got the buckets.                               | `<amount>`, `<bucket_name>`      |
| `give.player-not-found`      | `/infb give` names a player who isn't online.                    | `<player>`                       |
| `give.invalid-bucket`        | `/infb give` names a bucket that doesn't exist.                  | `<bucket>`                       |
| `give.invalid-amount`        | `/infb give` with an amount that isn't a positive number.        |                                  |
| `gui.prompts.*`              | Chat prompts while editing a bucket's name or lore in the builder. | `<name>` in `name-updated`     |
| `gui.status.saved`           | A bucket is saved from the builder.                              |                                  |
| `gui.status.deleted`         | A bucket is deleted from the list.                               |                                  |

## Multi-line messages

`help-menu` (shown by `/infb help`) and `update-notifier` (shown to admins on join when a new
version is out, with `<current_version>` and `<new_version>`) are lists of lines and don't get the
prefix.

## Menus

Menu titles and button text are in `guis.yml`; see [Bucket Builder](../core-reference/bucket-builder.md).
