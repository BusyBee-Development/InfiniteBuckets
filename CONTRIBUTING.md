# Contributing to InfiniteBuckets

Thank you for your interest in improving InfiniteBuckets! Contributions of all kinds are welcome — bug reports, feature requests, documentation, and code.

This document explains how to get set up, the standards we follow, and how to propose changes.


## Ways to contribute
- Report bugs and regressions
- Suggest features or improvements
- Improve documentation (README, config comments, messages)
- Submit code (fixes, refactors, integrations, new capabilities)


## Quick start (developer setup)
- Requirements
  - Java 17
  - Maven 3.8+
  - IntelliJ IDEA (recommended) or any Java IDE
- Build
  - `mvn package`
  - Output: `target/InfiniteBuckets-v<version>.jar`
- Run locally
  1. Download a Paper 1.21+ server (Folia supported as well).
  2. Copy the built jar into the server `plugins` folder.
  3. Start the server to generate `plugins/InfiniteBuckets/config.yml` and `messages.yml`.
  4. Set `debug-mode: true` while developing to see diagnostic logs.


## Coding standards and plugin-specific guidelines
- General
  - Target Java 17 and Paper API 1.21.
  - Keep the plugin Folia-friendly: avoid blocking operations on the main thread.
  - Prefer the Bukkit/Paper API; avoid NMS unless absolutely necessary (we currently do not use NMS).
  - Any networking or I/O must be done asynchronously (see VersionCheck which uses `CompletableFuture`).
- Messages and localization
  - Do not hardcode player-facing strings.
  - Add keys to `src/main/resources/messages.yml` and fetch via `MessageManager`.
  - Use MiniMessage formatting and placeholders via `Placeholder` resolvers.
- Permissions
  - Follow the existing namespace: `infb.*`.
  - Per-bucket usage perms default to `infb.use.<bucketId>` unless configured otherwise.
- Configuration
  - New features that need config should be added to `src/main/resources/config.yml` with clear comments.
  - Provide sensible defaults; avoid breaking existing configs.
  - If you add a new config option, update README and this file where relevant.
- Logging & debugging
  - Use `DebugLogger` for debug messages; gate them behind `debug-mode`.
  - Keep logs concise and informative.
- Hooks / protections
  - If adding or enhancing a protection hook, implement `ProtectionHook` in `hooks.protectionhook` and register it in `HookManager` when the dependency plugin is detected.
  - Do not assume a hook is present; always check via `PluginManager.isPluginEnabled` and handle null APIs defensively.
- Commands & permissions checks
  - Validate sender permissions before executing admin-level actions.
  - Provide informative feedback using messages.yml entries.
- Threading
  - Interactions with the Bukkit world (blocks, entities) should occur on the main thread unless using Folia-appropriate scheduler constructs.


## Branching and workflow
1. Fork the repository and create your feature branch:
   - `feature/<short-description>` for new features
   - `fix/<short-description>` for bug fixes
   - `docs/<short-description>` for documentation
2. Keep PRs focused and as small as reasonably possible.
3. If your change relates to an issue, reference it in the PR description (e.g., "Closes #123").


## Commit convention (Conventional Commits)
Use Conventional Commits to make history and automation clearer.

Common types:
- `feat:` a new feature
- `fix:` a bug fix
- `docs:` documentation only changes
- `refactor:` code change that neither fixes a bug nor adds a feature
- `perf:` performance improvement
- `test:` adding or fixing tests
- `build:` build system or dependencies changes
- `ci:` CI configuration changes
- `chore:` maintenance tasks

Examples:
- `feat: add PlotSquared region check`
- `fix: prevent water placement on non-passable blocks`
- `docs: clarify nether usage rules in README`


## Pull request checklist
Before opening a PR, please ensure:
- The project builds: `mvn package`.
- Code follows guidelines above (messages via MessageManager, no blocking on main thread, proper permission checks).
- New config keys are documented in `config.yml` comments and README.
- User-facing changes are reflected in `README.md`.
- You tested on a Paper 1.21 server (and, if applicable, Folia).
- The PR description explains the motivation and what was changed.


## Reporting bugs
When filing an issue, include:
- Server software and version (Paper/Folia + version)
- Java version
- Plugin version (`/plugins` output or jar version)
- Steps to reproduce
- Expected vs. actual behavior
- Any console errors and logs (use `debug-mode: true` if relevant)
- Other relevant plugins (especially protection plugins like WorldGuard, Towny, etc.)


## Proposing features
Describe the problem you’re trying to solve, proposed behavior/config, and any potential impact on permissions or integrations. Mocked config snippets are appreciated.


## Licensing of contributions
This project is licensed under the Apache License 2.0 (see LICENSE). By submitting a contribution, you agree that your work will be licensed under the same license.


## Code of Conduct (short)
Be respectful and constructive. Harassment, discrimination, or personal attacks are not tolerated. Maintainers may moderate discussions and contributions that violate these principles.


## Maintainers
- Project author: @djtmk

If you’re unsure about anything, feel free to open an issue to discuss before investing significant time. Thanks for contributing!