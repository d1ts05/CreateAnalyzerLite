# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Planned
- HUD overlay rendering system
- Config screen integration (YACL/Cloth)
- Theme system implementation
- Create integration layer
- Network estimator with BFS
- Target selector (raycast)
- Layout engine for positioning

## [0.3.3] - 2025-01-17

### Added
- Initial project structure
- Mod entrypoint (`CreateAnalyzerLite.java`)
- Client config system with TOML backend
  - UI settings (theme, scale, anchor, offsets)
  - Content toggles (RPM, stress, nodes)
  - Behaviour options (hide in menus, goggles-only)
  - Performance tuning (sampling, BFS limits, cache TTL)
- Config data snapshot POJO for efficient reads
- Create mod presence detection
- NeoForge client bus registration
- Keybind definitions (toggle, cycle, lock)
- TTL cache utility for performance optimization
- Localization (en_us)
- Build system (Gradle + NeoForge 21.1.x)
- Documentation
  - Comprehensive README with usage guide
  - Blueprint technical design document
  - Contributing guidelines
  - MIT License

### Technical Details
- **Target**: Minecraft 1.21.1, NeoForge 21.1.62, Java 21
- **Package**: `com.zivalez.createanalyzerlite.*`
- **Config**: `config/createanalyzerlite-client.toml`
- **Optional Dependency**: Create 0.5.1.i+
- **Architecture**: Client-only, event-driven, performance-first

### Known Limitations
- Overlay rendering not yet implemented (foundation only)
- Config screen requires manual TOML editing for now
- Create integration stubs present but not functional

[Unreleased]: https://github.com/d1ts05/CreateAnalyzerLite/compare/v0.3.3...HEAD
[0.3.3]: https://github.com/d1ts05/CreateAnalyzerLite/releases/tag/v0.3.3