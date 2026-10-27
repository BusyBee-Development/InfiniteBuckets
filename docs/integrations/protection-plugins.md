---
id: protection-plugins
title: Protection Plugins
sidebar_position: 1
---

InfiniteBuckets checks your protection plugin before every bucket use, so an infinite bucket can't
grief a claim that a vanilla bucket couldn't. Supported plugins are hooked automatically; the
console shows `Hooked into <plugin>.` for each one found on startup.

## Supported plugins

| Plugin                     | What's checked                                                                                       |
|:---------------------------|:-----------------------------------------------------------------------------------------------------|
| **WorldGuard 7**           | The same checks as a vanilla bucket: placing liquid or a block needs `build` and `block-place`, draining needs `build` and `block-break`. Players with region bypass (`worldguard.region.bypass.<world>`, or op) are let through. |
| **WorldGuard-based claim plugins** (such as LandClaim) | Their claims are WorldGuard regions, so the WorldGuard hook covers them: members can use buckets in a claim, others can't, and the claim's `block-place` / `block-break` flags apply. |
| **Lands**                  | The land's block-place flag for the player.                                                          |
| **SuperiorSkyblock2**      | The island's `BUILD` privilege.                                                                      |
| **BentoBox**               | The island's bucket flag (BSkyBlock, AcidIsland and other BentoBox gamemodes).                       |

## What gets checked

- **Placing liquid:** the block the liquid goes into.
- **Click-to-drain:** every block that would be drained, one by one. Blocks the player can't break
  are skipped, so a drain next to a claim stops at its edge.
- **Placing a sponge block** (`place-block`): the block it goes into, then each drained block as
  above.
- **Dispensers:** the block in front of the dispenser, checked without a player.

## Turning hooks off

Every hook is on by default. Turn one off in `config.yml`, or set `auto-detect-hooks: false` to
turn them all off (CoreProtect included):

```yaml
integrations:
  auto-detect-hooks: true
  hooks:
    worldguard: true
    lands: true
    bentobox: true
    superiorskyblock: true
    coreprotect: true
```

Run `/infb reload` after changing them.

## Troubleshooting

If players can use buckets where they shouldn't:

1. Check the console for `Hooked into <plugin>.` on startup. No line means the plugin wasn't found
   or its hook is off in `config.yml`.
2. Check whether the player has WorldGuard's region bypass (`worldguard.region.bypass.<world>`);
   op includes it.
3. Turn on `debug-mode` and run `/infb reload` to see which hooks are skipped and why.
