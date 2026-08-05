# Double Chest Buyer Mod

A beautiful Fabric mod for Minecraft 1.21.4 featuring a double chest buyer GUI with hot items, categories, and balance system.

## Features

- 🎁 **54-slot double chest interface** with smooth rendering
- 🏷️ **Category system** for organizing items (Blocks, Ores, Materials)
- 🔥 **Hot items section** at the top with special pricing
- 💰 **Balance system** with Wither skull icon (☠)
- 🎨 **Glassmorphism-style GUI** with intuitive layout
- 📦 **Persistent JSON data** for items and balance
- ⌨️ **Keybind** (B by default) to open the buyer

## Installation

1. Download the latest `.jar` from releases
2. Place in `mods/` folder
3. Open with Fabric Loader 1.21.4+

## Building

```bash
./gradlew build
```

Output: `build/libs/doublechest-buyer-mod-1.0.0.jar`

## Configuration

Edit `config/doublechest/items.json` to customize:
- Item categories and prices
- Hot items
- Starting balance

## Command

- **/buyer** - Open the Double Chest Buyer GUI

## Credits

Made for Minecraft 1.21.4 with Fabric Loom & Yarn mappings.
