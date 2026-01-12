# InfiniteBuckets

A flexible and powerful Spigot/Paper plugin that provides fully customizable infinite buckets with advanced features, protection plugin integration, and extensive configuration options.

## Features

### Bucket Types
- **Vanilla-like Buckets**: Infinite water and lava buckets that behave like vanilla Minecraft buckets
  - Works with dispensers for automated fluid placement
  - Configurable Nether restrictions
  - World-specific rules
- **Drain Area Buckets**: Sponge-like buckets that can drain fluids in a configurable radius
  - Customizable drain radius and block limits
  - Removes waterlogged blocks
  - Per-use cooldown system
- **Limited Use Buckets**: Buckets with a specific number of uses before depletion
  - Dynamic lore showing remaining uses
  - Fully customizable behavior

### Protection Plugin Integration
The plugin automatically hooks into popular protection plugins to prevent unauthorized bucket usage:
- **WorldGuard** (v7+)
- **Lands**
- **SuperiorSkyblock2**
- **BentoBox**

### Customization
- **MiniMessage Support**: Full support for gradients, hex colors, and rich text formatting
- **Custom Bucket Creation**: Define unlimited custom bucket types with unique properties
- **Permission-based Access**: Fine-grained permission control for each bucket type
- **World Restrictions**: Disable buckets in specific worlds or dimensions
- **Update Notifications**: Automatic update checking with in-game notifications for admins

### Performance & Safety
- Async processing for large drain operations
- Configurable safety limits to prevent server lag
- Global cooldown system between bucket uses
- Debug mode for troubleshooting

## Commands

All commands use the `/infinitebuckets` base command (aliases: `/infb`, `/ib`):

| Command | Permission | Description |
|---------|-----------|-------------|
| `/infb help` | None | Display the help menu |
| `/infb reload` | `infb.admin` | Reload all configuration files |
| `/infb give <player> <bucket> [amount]` | `infb.admin` | Give a player infinite buckets |

## Permissions

| Permission | Default | Description |
|-----------|---------|-------------|
| `infb.admin` | op | Access to admin commands (reload, give) |
| `infb.use.water` | true | Use infinite water buckets |
| `infb.use.lava` | true | Use infinite lava buckets |

Additional permissions can be configured per-bucket in `buckets.yml`.

## Configuration Files

### config.yml
Main plugin settings including:
- Debug mode toggle
- Update checker settings
- Performance and safety limits
- World restrictions and rules
- Protection plugin integration settings

### buckets.yml
Defines all bucket types with full customization:
- Built-in presets (water, lava, sponge buckets)
- Custom bucket definitions
- Bucket behavior modes (vanilla_like, drain_area)
- Uses, capacity, lore, and display names
- Per-bucket permissions and world rules

### messages.yml
All player-facing messages with MiniMessage formatting support:
- Custom plugin prefix
- Command responses
- Error messages
- Update notifications

## Technical Details

- **Minecraft Version**: 1.20+
- **Server Software**: Spigot, Paper (recommended), or Folia
- **Java Version**: 17+
- **API Version**: 1.20

### Dependencies (Soft)
All protection plugins are optional soft dependencies:
- WorldGuard (v7.0.14+)
- Lands API (v7.17.2+)
- SuperiorSkyblock2 API (2025.1+)
- BentoBox (v2.7.0)

### Bundled Libraries
- FoliaLib (v0.5.1) - For Folia and Paper scheduler compatibility

