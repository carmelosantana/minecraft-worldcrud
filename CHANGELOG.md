# Changelog

All notable changes to WorldCRUD are documented here.

## 1.1.2 - 2026-07-20

### Fixed

- Commands that take a player name now find Bedrock players. Floodgate joins a Bedrock
  account under a prefixed Java-side username (`.acarm` for a player who calls themselves
  `carm`), and `Bukkit.getPlayer` matches a prefix of the *name*, so `carm` never matched
  a name beginning with a dot. `/worldcrud tp`, `difficulty`, `setpermission`,
  `removepermission`, `listpermissions`, and `playerdata player` now try the
  Floodgate-prefixed form as well, then fall back to a case-insensitive sweep. Existing
  partial-name matching is preserved.
- A failed player lookup now lists who is online instead of dead-ending. Geyser sends no
  command-suggestion packets, so a Bedrock player has no tab completion and no other way
  to discover the prefixed username.

## 1.1.1 - 2026-07-19

### Fixed

- SHA256SUMS.txt now records bare JAR filenames instead of the build-time
  `target/` path, so `sha256sum --check` works against downloaded release assets.

## 1.1.0 - 2026-07-13

### Changed

- Updated the build baseline to Paper 26.1.2 and Java 25.
- Updated Maven compiler and shading plugins for Java 25 bytecode.
- Added GitHub Actions for tests, release JARs, SHA-256 checksums, and tagged releases.
- Verified plugin startup and command registration on the current server stack.

### Tested

- Paper 26.1.2 build 74
- Geyser 2.11.0
- Floodgate 2.2.5 build 138
- ViaVersion 5.11.0
