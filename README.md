# CreateAnalyzerLite (Lite + Config)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green.svg)](https://minecraft.net)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.x-orange.svg)](https://neoforged.net)
[![Create](https://img.shields.io/badge/Create-6.0.x-blue.svg)](https://github.com/Creators-of-Create/Create)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-0.3.3-blue.svg)](../../releases)

> **Client-only HUD overlay** for displaying Create mod metrics (RPM, stress, network nodes) with comprehensive in-game configuration.

---

## ✨ Features

### 📊 Real-Time Metrics Display
- **Speed/RPM** - Rotational speed and direction of kinetic components
- **Stress Analysis** - Consumption vs. capacity with color-coded indicators (🟢 Safe / 🟡 Warning / 🔴 Overloaded)
- **Network Nodes** - Count of connected kinetic components in the network
- **Status Visualization** - Minimalist bars, badges, and icons

### 🎨 Customizable UI
- **Display Modes** - Toggle between **Compact** (1-2 lines) and **Expanded** (detailed panel)
- **Theme System** - Light, Dark, or Auto theme following system preferences
- **Flexible Layout** - Scale (50%-200%), anchor positioning (corners), custom offsets
- **Visual Styling** - Adjustable opacity, padding, and corner radius

### ⚙️ Configuration
- **In-Game Config Screen** - Access via `Mods → CreateAnalyzerLite → Config`
- **TOML File Support** - Manual editing at `config/createanalyzerlite-client.toml`
- **Granular Control** - Toggle individual metrics, adjust performance settings
- **Persistent Settings** - Configuration survives game restarts

### 🎮 Intuitive Controls
- **Toggle Overlay** - Default: `O`
- **Cycle Display Mode** - Default: `Shift + O`
- **Lock Target** - Default: `Alt + O` (optional feature)

### ⚡ Performance Optimized
- **Smart Caching** - TTL-based sample caching to reduce overhead
- **Throttled Sampling** - Configurable tick intervals (`sampleEveryTicks`)
- **BFS Limiter** - Capped network traversal (`maxBfsNodes`) for large networks
- **Zero Allocations** - Render loop optimized to avoid GC pressure

### 🛡️ Graceful Degradation
- **Works Without Create** - Silently disables if Create mod is not installed (no crashes)
- **Safe for Multiplayer** - 100% client-side, works on any server
- **Modpack Friendly** - No mixins/ASM, compatible with Sodium, ModernFix, Connector bridges

---

## 📦 Installation

### Requirements
- **Minecraft** 1.21.1 (Java Edition)
- **NeoForge** 21.1.x or higher ([Download](https://neoforged.net/))
- **Create** 6.0.x or higher (optional, recommended) ([CurseForge](https://www.curseforge.com/minecraft/mc-mods/create) | [Modrinth](https://modrinth.com/mod/create))

### Steps
1. Download the latest `.jar` from [Releases](../../releases)
2. Place the file in your `.minecraft/mods/` folder
3. Launch Minecraft with NeoForge profile
4. (Optional) Install Create mod for full functionality

---

## 🎮 Usage Guide

### Getting Started

1. **Launch the game** and load into a world
2. **Look at a Create kinetic component** (e.g., shaft, cogwheel, motor)
3. **Press `O`** to toggle the overlay - metrics will appear on-screen

### Keybinds (Default)

| Key | Action | Description |
|-----|--------|-------------|
| `O` | Toggle Overlay | Show/hide the HUD |
| `Shift + O` | Cycle Mode | Switch between Compact and Expanded views |
| `Alt + O` | Lock Target | Keep displaying metrics for the current component |

> **Note:** Keybinds can be customized in Minecraft's Controls menu under "CreateAnalyzerLite"

### Configuration

#### In-Game Config Screen
1. From the **Main Menu** or **Pause Menu**, click **Mods**
2. Find **CreateAnalyzerLite** in the list
3. Click **Config** button to open the configuration screen
4. Adjust settings and click **Save**

#### Manual TOML Editing
Config file location: `config/createanalyzerlite-client.toml`

```toml
[ui]
    theme = "AUTO"          # AUTO, LIGHT, or DARK
    scale = 1.0             # 0.5 to 2.0
    anchor = "TOP_LEFT"     # TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    offsetX = 10
    offsetY = 10

[content]
    showRPM = true
    showStress = true
    showNodes = true

[behaviour]
    hideInMenus = true
    onlyWhenHoldingGoggles = false  # Requires Engineer's Goggles (optional)

[perf]
    sampleEveryTicks = 5    # Lower = more responsive, higher = better performance
    maxBfsNodes = 256       # Maximum nodes to scan for network estimation
```

---

## 🔧 For Developers

### Building from Source

#### Prerequisites
- **Java 21 JDK** ([Adoptium](https://adoptium.net/) recommended)
- **Git**

#### Clone & Build

```bash
git clone https://github.com/d1ts05/CreateAnalyzerLite.git
cd CreateAnalyzerLite
./gradlew build
```

Build output: `build/libs/createanalyzerlite-0.3.3.jar`

### Development Environment Setup

```bash
# IntelliJ IDEA
./gradlew genIntellijRuns

# Eclipse
./gradlew genEclipseRuns

# Run client in development
./gradlew runClient
```

### Architecture Overview

```
com.zivalez.createanalyzerlite
├─ CreateAnalyzerLite.java      // @Mod entrypoint
├─ config/
│  ├─ ClientConfig.java         // ModConfigSpec (TOML)
│  └─ ConfigData.java           // Snapshot POJO
├─ hud/
│  ├─ OverlayRenderer.java      // Main render loop
│  ├─ LayoutEngine.java         // Position/sizing logic
│  ├─ Theme.java                // Color palettes
│  └─ Widgets.java              // UI components (bars, badges, icons)
├─ input/
│  └─ Keybinds.java             // Key mapping registration
├─ integration/create/
│  ├─ CreatePresent.java        // Mod availability check
│  ├─ KineticQuery.java         // Read speed/stress from BlockEntities
│  └─ NetworkEstimator.java     // BFS network traversal
├─ probe/
│  └─ TargetSelector.java       // Raycast crosshair target
├─ util/
│  └─ Cache.java                // TTL caching utility
└─ platform/
   └─ NeoForgeClientBus.java    // Event registration
```

See **[BLUEPRINT](CreateAnalyzerLite-BLUEPRINT.md)** for detailed technical design.

---

## 🐛 Troubleshooting

### Overlay Not Showing

1. **Verify Create is installed** (if you want metrics; overlay auto-disables without Create)
2. **Check keybind** - Make sure you pressed `O` (default toggle key)
3. **Look at a kinetic component** - Overlay only appears when targeting Create blocks
4. **Check config** - Ensure `hideInMenus = false` if testing from pause menu

### Performance Issues

Adjust these settings in `config/createanalyzerlite-client.toml`:

```toml
[perf]
    sampleEveryTicks = 10   # Increase to reduce CPU usage
    maxBfsNodes = 128       # Lower to speed up network scans
```

### Mod Conflicts

**CreateAnalyzerLite uses NO mixins/ASM.** If you experience crashes:
- Check your crash log for the actual culprit mod
- Known conflicts: Some Connector bridge mods may have issues (not caused by CreateAnalyzerLite)
- Try isolating by temporarily removing other mods

---

## 📚 Documentation

- **[Blueprint](CreateAnalyzerLite-BLUEPRINT.md)** - Complete technical design document
- **[Changelog](CHANGELOG.md)** - Version history and release notes
- **[License](LICENSE)** - MIT License terms

---

## 🤝 Contributing

We welcome contributions! Please:

1. **Fork** this repository
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Follow the coding standards** in `CreateAnalyzerLite-BLUEPRINT.md` section 6
4. **Test thoroughly** (with and without Create mod installed)
5. **Submit a Pull Request**

### Coding Guidelines
- Java 21 with explicit imports
- No Lombok or IDE-specific plugins
- Use `final` where applicable
- Avoid per-frame allocations in render loops
- Client-safe code only (no server-side logic)

---

## 🐞 Bug Reports & Feature Requests

Use [GitHub Issues](../../issues) to:
- 🐛 Report bugs (include crash logs and mod list)
- 💡 Suggest features
- 📖 Request documentation improvements

**Before opening an issue:**
- Search existing issues to avoid duplicates
- Verify the issue occurs with ONLY CreateAnalyzerLite + Create installed
- Include Minecraft version, NeoForge version, and Create version

---

## 📄 License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) for details.

```
Copyright (c) 2025 Zivalez

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software...
```

---

## 🙏 Acknowledgments

- **[Zivalez](https://github.com/d1ts05)** - Original developer
- **[Create Team](https://github.com/Creators-of-Create/Create)** - For the amazing Create mod
- **[NeoForge Team](https://neoforged.net/)** - For the mod loader
- **Community Contributors** - Thank you for feedback and testing

---

## 🔗 Links

- **GitHub Repository**: [d1ts05/CreateAnalyzerLite](https://github.com/d1ts05/CreateAnalyzerLite)
- **Issue Tracker**: [GitHub Issues](../../issues)
- **Create Mod**: [GitHub](https://github.com/Creators-of-Create/Create) | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/create)

---

<div align="center">

**Made with ❤️ for the Create community**

[![GitHub stars](https://img.shields.io/github/stars/d1ts05/CreateAnalyzerLite?style=social)](../../stargazers)
[![GitHub forks](https://img.shields.io/github/forks/d1ts05/CreateAnalyzerLite?style=social)](../../network/members)

</div>