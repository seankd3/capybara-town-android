# Capybara Town Game Design

## Pillars

- Cozy first: the player helps neighbors rather than fighting or optimizing under pressure.
- Readable animals: capybaras are large calm ovals, guinea pigs are round snack friends, and gerbils are tiny fast couriers with long tails.
- Small loop, real game: move, collect, deliver, raise happiness, finish a short round.

## MVP Loop

1. The player taps the town map to move.
2. Snacks appear around town as clover, berries, seeds, carrots, and mint.
3. Moving close to a snack collects it into the snack pouch.
4. Three NPCs request help:
   - Moss the capybara needs herbs for the bathhouse.
   - Pip the guinea pig needs berry snacks for market day.
   - Zip the gerbil needs trail mix for tunnel patrol.
5. The player walks close to an NPC and taps Help to spend one snack.
6. Each completed help task raises happiness.
7. Helping all three neighbors reaches the town party win condition.

## Controls

- Tap world: move toward that spot.
- Help: deliver a snack to a nearby unhelped neighbor.
- Pause: freeze or resume the game.
- Reset: restart the round.

## Screen Layout

- Top HUD: happiness, snack count, current objective, short feedback text.
- Main view: warm top-down town with paths, bathhouse pond, garden rows, tunnel entrance, homes, snacks, animals, and player.
- Bottom controls: Help, Pause/Resume, Reset.

## Content Scope

The MVP intentionally uses native Canvas shapes instead of an asset pipeline. This keeps the first playable small and maintainable while still making each species visually distinct.

Next strong additions:

- Add a tiny day timer and daily goals.
- Add animal idle animations and speech bubbles.
- Add more resource types with species-specific preferences.
- Add a simple save file for unlocked decorations.
