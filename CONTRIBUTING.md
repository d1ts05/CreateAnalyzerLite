# Contributing to CreateAnalyzerLite

Thank you for considering contributing! This guide will help you get started.

## 🚀 Quick Start

### Prerequisites
- **Java 21 JDK** ([Adoptium](https://adoptium.net/) recommended)
- **Git**
- **IntelliJ IDEA** or **Eclipse** (optional)

### Setup

```bash
# Fork and clone
git clone https://github.com/YOUR_USERNAME/CreateAnalyzerLite.git
cd CreateAnalyzerLite

# Generate IDE run configurations
./gradlew genIntellijRuns  # IntelliJ IDEA
./gradlew genEclipseRuns   # Eclipse

# Build
./gradlew build

# Run client
./gradlew runClient