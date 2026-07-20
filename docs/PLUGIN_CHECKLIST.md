# New or Edited Plugin Checklist

Leave an unchecked box with a short explanation when a gate is not complete; do not silently remove inapplicable checks.

- Plugin name: `WorldCRUD`
- Slug: `worldcrud`
- Repository: `worldcrud` (local; remote not verified during this change)
- Owner: `Carmelo Santana`
- Target version: `1.1.2` (patch, bug-fix only)
- Paper version: `26.1.2 build 74`
- Java version: `25`
- Updater destination: `worldcrud.jar` (not verified against the manifest during this change)
- External services: `none`
- Status: `active`
- Autonomy: `autonomous`

Maven `artifactId`: `worldcrud`. `plugin.yml` name: `WorldCRUD`. Releasable JAR: `worldcrud-<version>.jar`.
Current released version at time of this change: `v1.1.1` — a shipped, released plugin. This is a
bug-fix patch on existing work, **not** a new plugin. Gates already satisfied by the released
plugin are marked as such rather than re-run.

This file did not exist before `1.1.2`; it is created here recording real current state.

## 1. Scope

- [x] Status is explicitly recorded as active, experimental, or excluded. `active`.
- [x] Purpose, commands, events, permissions, configuration, persistence, and acceptance checks are defined. Unchanged from the released plugin; see `README.md`. This patch adds no command, permission, config key, or persisted field.
- [x] Known limitations and any intentionally withheld gates are recorded. See below.

### What changes in 1.1.2

Six command call sites resolved a user-typed player name with `Bukkit.getPlayer(String)`.
Floodgate joins a Bedrock account under a prefixed Java-side username — `.acarm` for a player who
calls themselves `carm`, using Floodgate's default `username-prefix: "."`. `Bukkit.getPlayer`
matches a prefix of the *name*, so `getPlayer("carm")` cannot match `.acarm`: that name starts with
a dot, not a `c`. An operator naming the unprefixed form got "Player not found" for a player
standing in front of them.

All six now resolve through `PlayerLookup.resolveAllowingPartial`, which tries the typed name, then
the Floodgate-prefixed form, then a case-insensitive sweep (which also covers a server that
reconfigured the prefix), and only then Bukkit's partial matching — preserving the previous
partial-name behaviour rather than regressing it to exact-only.

Affected commands: `/worldcrud tp`, `difficulty`, `setpermission`, `removepermission`,
`listpermissions`, `playerdata player`.

Failed lookups now list who *is* online. Geyser bakes the command tree into one login packet and
never sends command-suggestion packets, so Bedrock players have no tab completion and no other
channel through which the correct prefixed name can reach them.

### Known limitations

- **No real Bedrock client was used.** The fix is verified by unit tests over the candidate-name
  logic and by a green build. Whether a live Floodgate join actually produces the `.`-prefixed name
  assumed here is **not** verified in this change.
- `PlayerLookup.resolve` and `resolveAllowingPartial` are **not** unit-tested: both call `Bukkit`
  statics, which need a running server, and this project has no MockBukkit dependency. Only the
  pure functions `targetNameCandidates` and `noSuchPlayerMessage` are covered.
- The Floodgate prefix is hardcoded to `.` rather than made configurable, deliberately — a plugin
  config key would be a second, unvalidatable source of truth for a value owned by Floodgate's
  config. A reconfigured prefix still resolves via the case-insensitive sweep.
- `PlayerLookup` is duplicated per plugin rather than shared, per the ecosystem design note.

## 2. Repository

- [x] Working tree was clean on `main` before this change; work is on branch `fix/floodgate-name-resolution`.
- [x] Existing user-owned worktree changes were identified and preserved. None existed.
- [ ] Remote, branch protection, and origin configuration verified. **Not performed** — out of scope for a local bug-fix branch; nothing was pushed, tagged, or merged.

## 3. Metadata

- [x] AGPL-3.0-or-later `LICENSE` and Maven license metadata are present and consistent. Both present from the released plugin.
- [x] `https://xpfarm.org` metadata is present in `pom.xml`.
- [x] Repository slug, artifact, releasable JAR, and `plugin.yml` names are consistent.
- [x] No secrets introduced by this change.

Gates 2 and 3 were satisfied by the existing released plugin; scaffold is not re-run.

**Note:** no source file in this repository carries a license header. `PlayerLookup.java` and
`PlayerLookupTest.java` were given one in the style used by the sibling `electric-furnace` repo, so
the two new files differ from their neighbours here. Backfilling headers across the existing
sources is a separate change and was not done.

## 4. Compatibility

- [x] Java 25 / Paper 26.1.2 build 74 compile succeeds. `mvn clean verify` green.
- [x] Hard dependencies, soft dependencies, and load ordering reviewed. Unchanged; none added. `PlayerLookup` uses only Bukkit API and does **not** link against the Floodgate API, so no `softdepend` is required.
- [x] Geyser/Floodgate/ViaVersion review covers Bedrock-safe input and identity behavior. This change *is* that review's outcome for player-name input: it is the Bedrock identity fix.

## 5. External services

- [x] External integrations are disabled by default or require explicit configuration. Not applicable — none.
- [x] Ollama/Umami-style endpoints optional and failure-tolerant. Not applicable.
- [x] Endpoint failure cannot fail startup. Not applicable.

## 6. Tests and build

- [x] Unit tests cover separable logic and failure paths where applicable. 8 tests added in `PlayerLookupTest` covering candidate ordering, the already-prefixed case, trimming, null/blank, and both failure-message forms. Written failing first: with the helper absent the build failed to compile with `cannot find symbol: variable PlayerLookup` at 11 sites.
- [x] `mvn --batch-mode --no-transfer-progress clean verify` succeeds. `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`. Both `PlayerLookupTest` (8) and the pre-existing `WorldTypesTest` (2) confirmed executing in `target/surefire-reports/`.
- [x] The releasable JAR and embedded `plugin.yml` were inspected; `original-*` JARs are excluded. Verified by unzipping the built JAR. Embedded `plugin.yml` reads `version: '1.1.2'`, `api-version: '1.21'`, `main: org.xpfarm.worldcrud.WorldCRUDPlugin`. Bytecode major version of the first `.class` entry is **69 (Java 25)**, matching the ecosystem standard.

      **Exclusion is at the CI release-asset step, not at build time.** `target/` contains both
      `worldcrud-1.1.2.jar` and `original-worldcrud-1.1.2.jar` — the `original-*` JAR *is* still
      produced locally. It is excluded from released assets by `.github/workflows/build.yml`, which
      filters `! -name 'original-*'` on both the SHA256SUMS step and the `gh release upload` step
      (and excludes `!target/original-*.jar` from the uploaded build artifact). So no `original-*`
      JAR can reach a release, but one does exist on disk after a local build.

      `maven-shade-plugin` is a **no-op** here: every dependency is `provided`/`test` scope, so it
      shades nothing and exists only to rename the untouched jar, which is what creates the
      `original-*` file. `agua-de-florida` resolved this by removing shading entirely; doing the
      same here is out of scope for this change.

**Surefire:** no `maven-surefire-plugin` declaration was needed. Maven 3.9.16's default surefire
(3.5.4) auto-detects `JUnitPlatformProvider` and runs Jupiter tests here; this was verified by name
in the build output and in `target/surefire-reports/`, not assumed. Adding the `3.5.2` pin used by
`electric-furnace` would have been a downgrade, so the pom's build section is unchanged.

## 7. Matrix

### 7a — single-plugin runtime verification — NOT RUN

- [ ] Paper, Geyser, Floodgate, and ViaVersion start successfully together. **Not performed** for `1.1.2`; no stack was booted during this change.
- [ ] Java and Bedrock smoke tests cover joins plus affected commands. **Not performed — and this is the gap that matters.** The entire point of this fix is behaviour under a real Floodgate-prefixed Bedrock join, and no Bedrock client was available. The correctness argument rests on unit tests over the candidate-name logic plus the documented Floodgate default prefix.
- [ ] Public deployment smoke tests verify `play.xpfarm.org` entry points. Belongs to gate 11.
- [x] Ollama and Umami unavailable-endpoint tests. Not applicable — no external integrations.

### 7b — ten-plugin ecosystem matrix — NOT RUN

- [ ] Fresh-volume Legendary stack test covers all ten updater-managed plugins.
- [ ] Per-plugin manifest state recorded.

Out-of-band and not a prerequisite for this patch: no updater manifest entry and no dependency changes.

## 8. CI/CD

- [x] Standard plugin Actions workflow is installed. Present in `.github/` from prior releases.
- [ ] Successful main Actions run is recorded before tagging. **Not performed** — nothing pushed or tagged.
- [ ] Workflow permissions reviewed against the documented contract. Not re-reviewed in this change.

## 9. Release — NOT DONE

- [x] Semantic version matches the POM. `pom.xml` bumped `1.1.1` → `1.1.2`; `plugin.yml` uses the filtered `${project.version}`.
- [ ] Successful tag Actions run and GitHub release recorded. **Not performed** — no tag, no push, no release, by instruction.
- [ ] Release contains exactly one updater-matching JAR plus `SHA256SUMS.txt`.
- [ ] Downloaded release assets pass `sha256sum --check`.

## 10. Updater

- [ ] Updater manifest covers repository, destination, asset regex, legacy globs, enabled state, pin. **Not verified** in this change; no manifest edit was made or needed for a patch release.
- [ ] Fresh install, upgrade, no-op, archival, and failure behaviors pass. Not run.
- [ ] Updater dry-run uses a disposable directory. Not run.
- [ ] Failure retains the installed JAR and fail-open behavior. Not run.

## 11. Deployment

- [ ] Dokploy redeployment notes identify the full recreation used to rerun the one-shot updater. Not performed.
- [ ] Updater completion, Minecraft startup, destination JAR, and logs inspected. Not performed.
- [ ] No production plugin hot reload was used. No deployment occurred.

**Rollback:** this patch changes only name resolution at six command call sites and adds one new
class. Reverting the commit restores `1.1.1` behaviour exactly; there is no migration, no persisted
state change, and no config change.

## 12. Handoff

- [ ] Current-state documentation refreshed with release, CI, updater, and deployment state. Not performed — the work is an unpushed local branch.
- [x] Known limitations and skipped checks are recorded. See §1 and the unchecked boxes above.
- [x] Evidence distinguishes what was verified from what was not. The build and unit-test results are real and reproducible; every runtime, release, updater, and deployment gate is explicitly unchecked.
