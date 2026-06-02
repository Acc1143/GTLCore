# AGENTS.md

This file is for AI agents working in this repository.

## Project Context

- This repository is a fork of GTL / GTLCore: https://github.com/EasterFG/GTLCore
- Target environment is Minecraft 1.20.1 Forge.
- The project is a Java 17 Gradle mod project.
- Important dependency jars are stored in `libs/`, including GTCEu, Applied Energistics 2, and ExtendedAE.
- The mod uses Sponge Mixin to patch behavior in GTL, GTCEu, AE2, ExtendedAE, and other dependencies at runtime.

## Hard Rules

- Do not run build, compile, test, lint, formatting, or runtime validation commands unless the user explicitly asks.
- This includes `gradle`, `gradlew`, `mvn`, `mvnw`, `npm`, `pnpm`, `yarn`, and similar tools.
- Static inspection commands are fine: `rg`, `Get-Content`, `git diff`, `git status`, `jar tf`, `javap`, and `tar -xOf`.
- Do not revert user changes. The working tree may already be dirty.
- Use `apply_patch` for source edits.

## Important Files

- Mixin config: `src/main/resources/gtlcore.mixin.json`
- Main config holder: `src/main/java/org/gtlcore/gtlcore/config/ConfigHolder.java`
- AE2 GUI style hook: `src/main/java/org/gtlcore/gtlcore/mixin/ae2/gui/StyleManagerMixin.java`
- ExtendedAE pattern provider mixins:
  - `src/main/java/org/gtlcore/gtlcore/mixin/extendedae/PartExPatternProviderMixin.java`
  - `src/main/java/org/gtlcore/gtlcore/mixin/extendedae/TileExPatternProviderMixin.java`
- GTCEu ME pattern buffer mixins:
  - `src/main/java/org/gtlcore/gtlcore/mixin/gtm/MEPatternBufferPartMachineMixin.java`
  - `src/main/java/org/gtlcore/gtlcore/mixin/gtm/MEPatternBufferTerminalInventoryMixin.java`

## Current Custom Changes

### GTCEu ME Pattern Buffer

Target object: `gtceu:me_pattern_buffer`.

The intended custom capacity is `12 x 9 = 108` pattern slots.

Implementation notes:

- `MEPatternBufferPartMachineMixin` patches `MEPatternBufferPartMachine`.
- Constructor pattern slot count is changed from `27` to `108`.
- `createUIWidget()` row count is changed from `3` to `12`.
- `MEPatternBufferTerminalInventoryMixin` patches the anonymous terminal inventory inner class so its `size()` also uses `108` instead of `27`.

### ExtendedAE Extended Pattern Provider

The ExtendedAE pattern provider capacity is still config driven.

Config:

- Field: `ConfigHolder.INSTANCE.exPatternProvider`
- Default: `36`
- Range: `9..90`

Implementation notes:

- `PartExPatternProviderMixin` and `TileExPatternProviderMixin` replace ExtendedAE's hardcoded `36` with `ConfigHolder.INSTANCE.exPatternProvider`.
- Do not hardcode this value to `108`.
- The GUI uses pre-generated static styles and textures for 1 to 10 rows.
- More than 10 rows is intentionally unsupported.

GUI row formula:

- Columns: `9`
- `rows = ceil(exPatternProvider / 9)`
- Supported rows: `1..10`

`StyleManagerMixin` redirects ExtendedAE's `ex_pattern_provider.json` to `gtl_ex_pattern_provider_<rows>.json` when the configured row count is not the original 4 rows.

Important details:

- Original 4-row layout still uses ExtendedAE's original `ex_pattern_provider.json`.
- Rows `1`, `2`, `3`, and `5..10` use GTL-provided JSON files in `src/main/resources/assets/ae2/screens/`.
- Matching PNG files live in `src/main/resources/assets/ae2/textures/guis/`.
- The generated textures are built from ExtendedAE's original `ex_pattern_provider.png` by keeping the top area, repeating/removing pattern rows, and moving the bottom area.
- AE2 player inventory slots are bottom-relative through `common/player_inventory.json`, so increasing the screen/background height moves them down automatically.

## Mixin Notes

- Mixin package root is `org.gtlcore.gtlcore.mixin`.
- Add new mixins or accessors to `src/main/resources/gtlcore.mixin.json`.
- For private AE2 style fields, use accessor mixins instead of reflection.
- Check dependency bytecode with `javap` when source is not available.
- Check dependency resources with `jar tf` and `tar -xOf` when JSON or assets are inside jars.

## Dependency Resource Notes

ExtendedAE's original screen style is in:

- `libs/ExtendedAE-1.20-1.1.12-forge.jar`
- Resource path: `assets/ae2/screens/ex_pattern_provider.json`

AE2 common player inventory style is in:

- `libs/appliedenergistics2-forge-15.2.1.jar`
- Resource path: `assets/ae2/screens/common/player_inventory.json`

## Common Pitfalls

- Do not assume a GUI JSON can be dynamic. AE2 style JSON is static once loaded; this project currently uses pre-generated row-specific JSON/PNG files instead.
- Do not move AE2 player inventory manually unless needed. It is bottom-relative and follows generated background height.
- Do not leave slot count and GUI slot count mismatched.
- Do not assume old generated files or tracked resource files are safe to delete.
- Keep unrelated dirty files untouched.
