# Contributing to WorldCRUD Plugin

Thank you for your interest in contributing to the WorldCRUD plugin! This document provides guidelines for developing and contributing to this Minecraft plugin.

## Development Environment

### Requirements
- Java 21+
- Maven 3.6+
- Minecraft Java Edition 1.21+
- Paper 1.21.6+ (recommended)
- ViaVersion plugin (for Geyser compatibility)

### Setup

1. Clone the repository
2. Run `make setup` to prepare the development environment
3. Run `make build` to build the plugin
4. Run `make start` to start the test server

## Building the Plugin

The plugin uses Maven for build management. Key commands:

```bash
# Build the plugin
make build

# Run tests
make test

# Install to test server
make install

# Quick development cycle
make dev
```

## Testing

### Local Testing
```bash
# Start test server
make start

# Run debug tests
make debug

# View test commands
make test-commands
```

### Docker Testing
```bash
# Test in Docker container
make docker-test

# Build Docker image
make docker-build
```

## Code Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Add comments for complex logic
- Use `NamespacedKey` for custom identifiers
- Implement proper error handling

## Plugin Architecture

The plugin follows a modular architecture:

- `WorldCRUDPlugin` - Main plugin class
- `WorldManager` - Handles world operations
- `WorldCRUDCommand` - Command handling and tab completion
- `PermissionHandler` - Permission management
- `WorldTypes` - World type enumeration

## Adding New Features

1. Create a new branch for your feature
2. Implement the feature following existing patterns
3. Add appropriate tests
4. Update documentation
5. Submit a pull request

## Commands and Permissions

When adding new commands:

1. Add the command to `WorldCRUDCommand`
2. Add tab completion support
3. Add appropriate permission checks
4. Update the help system
5. Add to the README documentation

## Configuration

The plugin uses a simple YAML configuration. When adding new config options:

1. Add default values to `config.yml`
2. Handle the configuration in the appropriate manager class
3. Support configuration reloading

## Testing Commands

The plugin includes several test commands for development:

```bash
# Create test worlds
/worldcrud create testworld NORMAL
/worldcrud create voidworld VOID

# Test world management
/worldcrud list
/worldcrud info testworld
/worldcrud teleport testworld

# Test admin commands
/worldcrud debug on
/worldcrud permissions
```

## Debugging

Enable debug mode for detailed logging:

```bash
/worldcrud debug on
```

Use the debug script for interactive testing:

```bash
make debug
```

## Docker Support

The plugin includes Docker support for testing:

- `docker-compose.yml` - Container configuration
- `docker-test.sh` - Automated testing script

## Version Compatibility

- **Geyser/Floodgate**: Requires Minecraft 1.21.5+ or ViaVersion
- **ViaVersion**: Automatically downloaded for cross-version support
- **Paper**: Latest 1.21.6 builds recommended

## Server Management

The included server manager provides:

- Automatic Paper server download
- Plugin installation
- World management
- Log viewing
- Network configuration

## Performance Considerations

- Use cached `ItemStack` instances where possible
- Implement event-driven architecture
- Minimize database/file I/O operations
- Use async operations for heavy tasks

## Error Handling

- Always validate input parameters
- Provide meaningful error messages
- Log errors appropriately
- Handle edge cases gracefully

## Documentation

- Update README.md for new features
- Add inline code comments
- Update command help text
- Include examples in documentation

## Submission Guidelines

1. Test your changes thoroughly
2. Ensure all tests pass
3. Update documentation
4. Follow the existing code style
5. Create a clear pull request description

## Support

For questions or support:
- Check the README.md
- Review existing code patterns
- Test with the provided tools
- Contact the maintainers

## License

This project follows the XP Farm plugin collection licensing terms.
