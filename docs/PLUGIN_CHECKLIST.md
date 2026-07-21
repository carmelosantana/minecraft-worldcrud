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

### 7a — single-plugin runtime verification (`1.1.2`) — PARTIAL

Evidence below comes from a **single disposable Legendary stack run on 2026-07-20**
(image `05jchambers/legendary-minecraft-geyser-floodgate:latest`) with **all six fixed plugin
JARs mounted together**. The same run backs the gate 7a note in all six repositories.

- [x] Paper, Geyser, Floodgate, and ViaVersion start successfully together. **Verified.** Paper
      reached `Done (18.178s)! For help, type "help"`. The Java port answered a real Minecraft
      protocol handshake — not merely a TCP connect — reporting `Paper 26.1.2 | protocol 775` and
      `PLAYERS: 0 / 20`. `/plugins` reported 9 plugins, all green/enabled: AguaDeFlorida, floodgate,
      Geyser-Spigot, GlutenFreeBread, StarterPack, TheCurse, ViaVersion, WildWeatherUpdate,
      WorldCRUD. Companion versions observed: floodgate v2.2.5-SNAPSHOT (b138-fc99cfc),
      Geyser-Spigot v2.11.0-SNAPSHOT (Geyser 2.11.0-b1200), ViaVersion present; Geyser started on
      UDP port 19200. Each plugin enabled at its new version with **zero exceptions, errors, or
      SEVERE lines attributable to any of the six** — including `Enabling WorldCRUD v1.1.2`.
- [ ] Java and Bedrock smoke tests cover joins plus affected commands, events, permissions,
      persistence, and reloads. **PARTIAL — the Java side was exercised, the Bedrock side was not.
      Left unchecked deliberately.**

      *What was exercised.* The **Floodgate prefix assumption was confirmed empirically, not merely
      from documentation**: reading `/minecraft/plugins/floodgate/config.yml` inside the running
      container on the Floodgate 2.2.5 build showed `username-prefix: "."` and
      `replace-spaces: true`, alongside the shipped comment "Floodgate prepends a prefix to bedrock
      usernames to avoid conflicts". The `.` prefix this fix depends on is now **observed on the
      actual runtime, not assumed** — the single most important upgrade to the evidence.

      The **new failure path was then exercised end-to-end over RCON on the live server** for every
      fixed command across all six plugins — `/aguadeflorida give carm`, `/curse start carm`,
      `/curse book carm`, `/worldcrud listpermissions carm`, `/starterpack give carm`,
      `/gfbread clear carm`, and `/weather trigger rain carm` — and each returned the new
      message with no exception: exactly `No player matches 'carm'; no players are online.` This proves that
      `PlayerLookup.resolve` / `resolveAllowingPartial` / `onlineNames` / `noSuchPlayerMessage`
      actually execute correctly against real Bukkit APIs, that command dispatch reaches them, and
      that the message renders — none of which the unit tests could show.

      *What remains unverified.* **The positive match is still unproven.** No real Bedrock client
      was available, so no player with a `.`-prefixed Java-side username ever joined. What is
      verified is that the resolution path runs without error and that the not-found branch is
      correct; that `/worldcrud listpermissions carm` actually **finds** a Bedrock player named `.acarm` has
      **not** been observed. Only the empty-online-list branch of `noSuchPlayerMessage` was
      exercised; the branch that lists online player names was not. The operator will verify live on
      the dev server with helpers. `resolve` / `resolveAllowingPartial` still have **no unit-test
      coverage** (Bukkit statics, no MockBukkit).
- [ ] Public deployment smoke tests verify `play.xpfarm.org` reaches the intended Java and Bedrock entry points. Belongs to gate 11, not this gate.
- [x] Ollama and Umami unavailable-endpoint tests keep the server and plugins available when applicable. Not applicable — no external integrations.

### 7b — ten-plugin ecosystem matrix — NOT RUN

- [ ] Fresh-volume Legendary stack test covers all ten updater-managed plugins.
- [ ] Per-plugin manifest state recorded.

Out-of-band and not a prerequisite for this patch: no updater manifest entry and no dependency changes.

### 7b — ecosystem matrix (12 plugins) — PASSED 2026-07-21

Trigger: the updater manifest changed — Timber Blast `v1.0.0` was enrolled
(`carmelosantana/minecraft-plugin-updater` commit `6065b03`), taking the roster from 11 to 12.

- [x] Fresh-volume Legendary stack test covers all updater-managed plugins. **12/12 PRESENT.**
      Run via the shared rig (`xpfarm-test-stack matrix up --from-releases`) on a fresh volume,
      roster read from the live `plugins.json` rather than a hardcoded list. The rig cross-checks
      the plugin count the server announces against what it parsed, and asserts each plugin is
      **enabled**, not merely listed.
- [x] Each updater-managed plugin's manifest `enabled` value, default state, and expected
      fresh-volume behavior are recorded separately. All 12 entries have `enabled` absent
      (equivalent to `true`) and no `pin`; every one was therefore expected to install and enable,
      and every one did. No entry was disabled, so there is no intentional-absence row this run.
- [x] Paper, Geyser, Floodgate, and ViaVersion start successfully together.
      Paper reached `Done (15.543s)! For help, type "help"`; the Java port answered a real
      protocol handshake reporting `Paper 26.1.2 | protocol 775`, `PLAYERS: 0 / 20`. Companions:
      Geyser-Spigot 2.11.0-SNAPSHOT, floodgate 2.2.5-SNAPSHOT, ViaVersion 5.11.0.
- [ ] Java and Bedrock smoke tests cover joins. **Not performed — no client attaches to this
      stack by design.** Per `PLUGIN_LIFECYCLE.md` §7 this is not a blocker; client behavior is a
      tracked gate-12 play-test obligation, not a matrix result.
- [x] `play.xpfarm.org` reaches the intended Java and Bedrock entry points.
      Read-only production check, separate from the disposable stack: DNS `168.231.74.113`;
      Java `25565` answered a real handshake (`Paper 26.1.2 | protocol 775`, 1 player online);
      Bedrock UDP `19132` reachable.
- [x] Ollama and Umami unavailable-endpoint tests keep the server and plugins available.
      Neither service exists in this stack, so this is the negative path by construction. Both
      self-disabled cleanly: `Ollama integration is disabled; no API client or listeners were
      started.` and `Umami analytics is disabled; no tracking listeners or network clients were
      started.` Server stayed healthy (`list` responded) with all 12 enabled.

This plugin's row: the updater reported `WorldCRUD: installed v1.1.2` from the published release
asset and Paper enabled it alongside the other 11. `--from-releases` was used deliberately — it
installs the real published assets through the real updater, so this is what production installs.

Co-resident: AguaDeFlorida 2.0.0, CopperKingdom 0.2.1, TheCurse 0.2.2, DeathDepot 1.1.1, ElectricFurnace 0.2.1, GlutenFreeBread 1.1.3, Ollama 0.2.1, StarterPack 1.1.2, TimberBlast 1.0.0, Umami 1.1.1, WildWeatherUpdate 1.0.2.

Zero exceptions, SEVERE lines, or enable failures attributable to any plugin. No secrets in any
log line. Stack torn down with `matrix down`; lease released, no orphaned containers.

## 8. CI/CD

- [x] Standard plugin Actions workflow is installed. Present in `.github/` from prior releases.
- [x] Successful main Actions run is recorded before tagging. `fix/floodgate-name-resolution` was merged fast-forward to `main` and pushed on 2026-07-20. The `main`-branch Actions run for commit `a14832c` completed with conclusion `success` **before** tag `v1.1.2` was created. No tag was pushed against a red or in-flight run.
- [ ] Workflow permissions reviewed against the documented contract. Not re-reviewed in this change.

## 9. Release — `v1.1.2` COMPLETE

- [x] Semantic version matches the POM, plugin metadata, and `v<version>` tag. Verified: `pom.xml` `<version>` `1.1.2` equals tag `v1.1.2` equals the `plugin.yml` version read out of the built JAR.
- [x] Successful tag Actions run and GitHub release recorded. Annotated tag `v1.1.2` created on verified commit `a14832c` and pushed; the tag Actions run completed with conclusion `success`. GitHub release published 2026-07-20 14:47:52 UTC with `draft=false`, `prerelease=false`, and it is now the repository's Latest release.
- [x] Release contains exactly one updater-matching JAR plus `SHA256SUMS.txt`. Verified by downloading the published release assets: exactly one JAR matching the updater asset pattern, plus `SHA256SUMS.txt`, and no `original-*` JAR.
- [x] Downloaded release assets pass `sha256sum --check`. Reported `OK` for the JAR.

## 10. Updater

- [ ] Updater manifest covers repository, destination, asset regex, legacy globs, enabled state, pin. **Not verified** in this change; no manifest edit was made or needed for a patch release.
- [ ] Fresh install, upgrade, no-op, archival, and failure behaviors pass. Not run.
- [ ] Updater dry-run uses a disposable directory. Not run.
- [ ] Failure retains the installed JAR and fail-open behavior. Not run.

Updater enrollment work was **not performed in this pass** (`v1.1.2` release only).

## 11. Deployment

- [ ] Dokploy redeployment notes identify the full recreation used to rerun the one-shot updater. Not performed.
- [ ] Updater completion, Minecraft startup, destination JAR, and logs inspected. Not performed.
- [ ] No production plugin hot reload was used. No deployment occurred.

**Not performed.** The operator will deploy and verify live on `play.xpfarm.org` via the dev server with helpers.

**Rollback:** this patch changes only name resolution at six command call sites and adds one new
class. Reverting the commit restores `1.1.1` behaviour exactly; there is no migration, no persisted
state change, and no config change.

## 12. Handoff

- [ ] Current-state documentation refreshed with release, CI, updater, and deployment state. Not performed — the work is an unpushed local branch.
- [x] Known limitations and skipped checks are recorded. See §1 and the unchecked boxes above.
- [x] Evidence distinguishes what was verified from what was not. The build and unit-test results are real and reproducible; every runtime, release, updater, and deployment gate is explicitly unchecked.
