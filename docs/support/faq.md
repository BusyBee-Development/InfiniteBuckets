---
id: faq
title: FAQ
sidebar_position: 1
---

**Does this work on Spigot, or only Paper?**
Paper, Spigot, and Folia 1.20.1+ are all supported. See [Requirements](../getting-started/requirements.md).

**Do I need to restart after editing a config?**
No. `/infb reload` (permission `infinitebuckets.admin`) reloads every file.

**Will updating wipe my config?**
No. New keys are added to `config.yml`, `messages.yml` and `guis.yml` with your values kept and a
backup saved first. `buckets.yml` is never touched by updates. See
[Installation → Updating](../getting-started/installation.md#updating).

**How do I make a sponge that drains lava?**
Make a `DRAIN_AREA` bucket with `liquid-type: LAVA` and `icon: SPONGE`. Add `place-block: true` to
place it like a real sponge. Full examples are on [Drain Buckets & Sponges](../features-modules/drain-buckets-sponges.md).

**Why does a sponge bucket come back with one use fewer?**
Placing it is a use. Otherwise a 16-use sponge could be placed and mined forever.

**Can players get infinite water by putting a bucket in a dispenser?**
Only if you set `allow-automation: true` on an unlimited placing bucket. Off by default. See
[Hoppers & Dispensers](../features-modules/hoppers-dispensers.md).

**Can buckets be used to grief claims?**
No, as long as your protection plugin is one of the [supported ones](../integrations/protection-plugins.md).
Every placement and every drained block is checked. With CoreProtect, bucket changes can also be
rolled back.

**Why is drain-radius capped at 16?**
A radius-16 drain already checks almost 36,000 blocks in one tick. See the
[Performance Guide](../advanced-optimization/performance-guide.md).

**Is this plugin open source?**
No. The source is publicly viewable on [GitHub](https://github.com/BusyBee-Development/InfiniteBuckets),
but it's licensed "All Rights Reserved" by Busy Bee Development. See the repository's `LICENSE`
file for exact terms.
