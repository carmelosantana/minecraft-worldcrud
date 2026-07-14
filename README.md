# WorldCRUD Plugin

A powerful world management utility plugin for Minecraft servers. Supports world generation, teleportation, CRUD operations, world borders, and admin debugging commands.

## Features

### 🌍 World Generation
- Supports `NORMAL` world type with reliable generation
- World border support for creating intentionally small worlds
- Easy world creation with simple commands

### 🌎 World Borders
- Set world borders during creation or for existing worlds
- Support for radius-based border sizing (e.g., 1000 blocks = 1000 block radius)
- Automatic center positioning at world spawn

### ✈️ Teleportation
- Instantly teleport to any loaded or generated world
- Command: `/worldcrud teleport <world_name>`
- Permission-based access control

### 🛠️ World Management (CRUD)
- Create, rename, delete, save, reset, and load worlds
- Full command suite with feedback and error handling
- Permission checks for all operations
- Custom spawn setting and entity clearing

### 🔐 Permissions
- `worldcrud.admin` → Full access to all commands
- `worldcrud.teleport` → Allows teleporting to registered worlds

### 🧪 Debug & Admin Tools
- `/worldcrud debug on|off` - Toggle debug logging
- `/worldcrud info <world_name>` - Get detailed world information including border info
- `/worldcrud list` - List all available worlds
- `/worldcrud version`, `/worldcrud reload`, `/worldcrud help` - Admin utilities

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/worldcrud create <name> <type> [size]` | Create a new world with optional border size | `worldcrud.admin` |
| `/worldcrud delete <name>` | Delete the specified world | `worldcrud.admin` |
| `/worldcrud rename <old> <new>` | Rename an existing world | `worldcrud.admin` |
| `/worldcrud teleport <name>` | Teleport to a world | `worldcrud.teleport` |
| `/worldcrud setspawn <name>` | Set spawn point | `worldcrud.admin` |
| `/worldcrud reset <name>` | Reset to original state | `worldcrud.admin` |
| `/worldcrud clear <name>` | Remove entities and drops | `worldcrud.admin` |
| `/worldcrud save <name>` | Save the world state | `worldcrud.admin` |
| `/worldcrud load <name>` | Load the world | `worldcrud.admin` |
| `/worldcrud border <name> <size>` | Set world border for existing world | `worldcrud.admin` |
| `/worldcrud info [name]` | Get info on world (including border) | All |
| `/worldcrud list` | List all worlds | All |
| `/worldcrud help` | Show command help | All |
| `/worldcrud version` | Show plugin version | All |
| `/worldcrud reload` | Reload config | `worldcrud.admin` |
| `/worldcrud permissions` | View plugin permissions | All |
| `/worldcrud debug <on\|off>` | Toggle debug logs | `worldcrud.admin` |

## World Types

- **NORMAL** - Standard overworld generation with all biomes

> **Note:** Previous versions supported FLAT, ISLAND, and VOID world types, but they have been removed for stability. Only the NORMAL type is now supported, ensuring reliable world generation across all Paper server versions.

## World Borders

You can set world borders in two ways:

1. **During world creation**: `/worldcrud create myworld NORMAL 1000`
2. **For existing worlds**: `/worldcrud border myworld 1000`

The size parameter represents the radius in blocks from the center. For example:
- `1000` = 1000 blocks radius (2000x2000 total area)
- `5000` = 5000 blocks radius (10000x10000 total area)

## Installation

1. Download the latest `worldcrud-1.0.0.jar` from the releases
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. The plugin will create a default configuration file

## Configuration

Edit `plugins/WorldCRUD/config.yml`:

```yaml
default-world-type: NORMAL
debug-mode: false
allowed-types:
  - NORMAL
```

## Development

### Requirements
- Java 25+
- Maven 3.6+
- Paper 26.1.2+ server

### Building
```bash
# Clone the repository
git clone <repository-url>
cd worldcrud

# Build the plugin
make build

# Set up development server
make setup

# Start the server
make start

# Install and test
make test-commands
```

### Testing
```bash
# Run all tests
make test

# Test in Docker container
make docker-test

# Interactive debugging
make debug
```

### Quick Development Cycle
```bash
# Build, install, and restart server
make dev
```

## Docker Support

The plugin includes Docker support for easy testing:

```bash
# Build and test in Docker
make docker-build
make docker-test

# Or use docker-compose directly
docker-compose up -d
```

## Permissions

The plugin uses two main permissions:

- `worldcrud.admin` - Full access to all commands (default: op)
- `worldcrud.teleport` - Allows teleporting to worlds (default: op)

## Support

For support, bug reports, or feature requests:
- Visit: https://xp.farm
- Author: Carmelo Santana

## License

This project is licensed under the [GNU Affero General Public License v3.0 or later](LICENSE).
