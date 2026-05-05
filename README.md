# Capybara Town Android

Capybara Town is a first playable MVP of a cozy 2D Android game about a tiny town of capybaras, guinea pigs, and gerbils.

## What Is Playable

- Tap anywhere in town to move the little helper.
- Pick up clover, berries, seeds, carrots, and mint scattered along paths and gardens.
- Match species-specific requests: Moss wants mint for the bathhouse, Pip wants berries for the snack stall, and Zip wants seeds for the tunnel route.
- Walk near Moss the capybara, Pip the guinea pig, and Zip the gerbil, then tap **Help** to deliver the requested snack.
- Happiness rises as snacks are collected and neighbors are helped.
- Reach 100 happiness by helping all three neighbors to unlock the town party.
- Use **Pause** and **Reset** from the bottom controls.

## Build And Run

This repo is an Android Studio-ready Kotlin project using a custom Android `View` and Canvas drawing. The current shell does not include Gradle or an Android SDK, so no Gradle wrapper was generated locally.

Recommended:

1. Open the repo in Android Studio.
2. Let Android Studio sync Gradle.
3. Run the `app` configuration on an emulator or device.

Command line when Android tooling is installed:

```sh
gradle assembleDebug
gradle test
```

## Local Logic Test

A deterministic non-Android smoke test can run with only a JDK:

```sh
./tools/run-logic-tests.sh
```

The test verifies snack collection, species-specific snack requests, helping all three species, the win condition, and reset behavior.

## Project Notes

- Game design doc: [docs/game-design.md](docs/game-design.md)
- Mock visual artifact: [docs/mock-visual.svg](docs/mock-visual.svg)
- Codex build report: [CODEX_REPORT.md](CODEX_REPORT.md)

## Current Visual Direction

The game uses Canvas-only generated art: rounded animal sprites with shadows, a textured green town, layered dirt paths, a bath pond, snack stall, burrow, garden rows, trees, bushes, flowers, and a warm native Android HUD. No external art assets are required.
