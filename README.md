# InfiniteBuckets

Customizable infinite buckets for modern Paper servers. Give your players endless water or lava in a safe, configurable way — with beautiful names/lore using MiniMessage, per‑bucket permissions, optional Nether restrictions, and smart behavior like waterlogging support. Folia compatible.

Modrinth: https://modrinth.com/plugin/infinitebuckets

## Why server owners like it
- Player‑friendly: Right‑click to place flowing water or lava; automatically waterlogs blocks when appropriate.
- Fully customizable: Define any number of infinite buckets in config (material, name, lore, permission, nether rules).
- Permission‑driven: Gate access per bucket (e.g., water vs. lava vs. your custom buckets).
- Modern & safe:
  - Paper API 1.21
  - Java 17
  - Folia supported
  - Async update notifier for admins
- Integrations: Hooks included for popular protection ecosystems so usage can respect build rules on your server.

> Note: Explicit build‑permission enforcement is implemented for SuperiorSkyblock2 island BUILD privilege. Hooks for WorldGuard, GriefPrevention, Towny, Lands, PlotSquared, and Residence are initialized and ready for extended protection handling.

---

## Installation
1. Requirements:
   - Paper/Paper‑fork 1.21+ (Folia supported)
   - Java 17+
2. Download the latest jar from Modrinth and place it into your server's `plugins` folder.
3. Start the server to generate the default configuration.
4. (Optional) Edit `plugins/InfiniteBuckets/config.yml` and `messages.yml` to customize buckets and messages.
5. Use permissions/commands below to manage access.

## Commands
- `/infinitebuckets` (aliases: `/infb`, `/ib`)
  - `help` — Show help menu.
  - `reload` — Reloads config, messages, buckets, and hooks.
  - `give <player> <bucketId> [amount]` — Gives an infinite bucket item.

Examples:
- `/infb help`
- `/infb reload`
- `/infb give Steve water 1`
- `/infb give Alex lava 16`

## Permissions
- `infb.admin`
  - Default: OP
  - Allows `/infb reload`, `/infb give`, and tab completion.
- `infb.use.water`
  - Default: true
  - Use the Infinite Water Bucket.
- `infb.use.lava`
  - Default: true
  - Use the Infinite Lava Bucket.
- Custom buckets: `infb.use.<bucketId>`
  - If you define a bucket with id `milk`, by default it uses `infb.use.milk` unless you set a custom permission in the bucket definition.

## Configuration
Config path: `plugins/InfiniteBuckets/config.yml`

Key options:
- `debug-mode` — Enable detailed debug messages in console (useful for troubleshooting).
- `buckets` — Define each infinite bucket:
  - `material` — Must be a bucket type (e.g., `WATER_BUCKET`, `LAVA_BUCKET`, `MILK_BUCKET`, ...)
  - `display-name` — MiniMessage‑formatted name.
  - `lore` — MiniMessage‑formatted list of lore lines.
  - `permission` — Permission required to use this bucket (defaults to `infb.use.<bucketId>` if omitted).
  - `works-in-nether` — Whether this bucket can be used in the Nether.

Default snippet:
```yml
buckets:
  water:
    material: "WATER_BUCKET"
    display-name: "<gradient:#00A6FF:#00E1FF>Infinite Water Bucket</gradient>"
    lore:
      - "<gray>An endless supply of pure, refreshing water.</gray>"
    permission: "infb.use.water"
    works-in-nether: false

  lava:
    material: "LAVA_BUCKET"
    display-name: "<gradient:#FF8C00:#FF4500>Infinite Lava Bucket</gradient>"
    lore:
      - "<gray>A bucket filled with an endless supply of molten rock.</gray>"
    permission: "infb.use.lava"
    works-in-nether: true

# Example custom bucket (uncomment & adjust to use)
#  milk:
#    material: "MILK_BUCKET"
#    display-name: "<white>Infinite Milk Bucket</white>"
#    lore:
#      - "<gray>Clears all potion effects upon use.</gray>"
#    permission: "infb.use.milk"
#    works-in-nether: true
```

MiniMessage formatting docs: https://docs.advntr.dev/minimessage/format.html

## How it behaves in‑game
- Players right‑click with the infinite bucket in hand to place fluid.
- Water buckets will waterlog compatible blocks when possible.
- If a bucket is disabled in the Nether via config, it will refuse to place there.
- Permissions are checked per bucket before use.
- SuperiorSkyblock2: Player must have the island `BUILD` privilege where they are standing.

## Integrations
Automatically detects and prepares hooks for:
- WorldGuard
- GriefPrevention
- Towny
- Lands
- PlotSquared
- Residence
- SuperiorSkyblock2 (explicit build privilege check implemented)

These integrations help ensure infinite bucket use can align with your server’s protection rules. Keep them installed and updated for best results.

## Update notifications
Admins with `infb.admin` will be notified in‑game on join if a new version is available (clickable link to Modrinth). Version checks are performed asynchronously.

## Troubleshooting
- Players can’t use a bucket:
  - Ensure they have the correct `infb.use.<bucketId>` permission.
  - Check `works-in-nether` if they are in the Nether.
  - If using SuperiorSkyblock2, verify they have `BUILD` privilege at their location.
- Debugging:
  - Set `debug-mode: true` in `config.yml` and watch console for detailed logs.
- Still stuck? Visit the Modrinth page for updates and issue reporting.

## License
This project includes a [LICENSE](https://github.com/BusyBee-Development/ClearLaggEnhanced/edit/main/LICENSE) file in the repository root. Please review it for usage terms.
