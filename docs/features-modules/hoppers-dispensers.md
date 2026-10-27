---
id: hoppers-dispensers
title: Hoppers & Dispensers
sidebar_position: 4
---

## Hoppers

Every bucket moves through hoppers like any other item, so hopper-fed chests, shop chests and
storage systems work with infinite buckets and sponges. Players can also put buckets into hoppers
by hand.

## Dispensers and droppers

By default buckets can't go into a dispenser or dropper, whether a player puts them in or a hopper
feeds them. A player who tries to put a limited-use bucket in gets the `dispenser-no-limited-uses`
message.

Set `allow-automation: true` on a bucket to let it in. A dispenser then places the bucket's liquid
in front of it, never runs out, and keeps the bucket. Only an unlimited (`usage-limit: -1`) placing
bucket (`mode: VANILLA_LIKE`) can be dispensed:

- Limited-use buckets are always kept out, even with `allow-automation: true`.
- A drain bucket in a dispenser does nothing.

A dispenser's liquid follows the [world rules](../configuration/main-configuration.md#world-rules)
and protection at the target block, and [CoreProtect](../integrations/coreprotect.md) logs it under
`#dispenser`.

```yaml
buckets:
  water_dispenser:
    display-name: "<aqua>Dispenser Water</aqua>"
    liquid-type: WATER
    mode: VANILLA_LIKE
    usage-limit: -1
    allow-automation: true
    icon: WATER_BUCKET
```
