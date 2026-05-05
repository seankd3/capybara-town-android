# Capybara Town Game Design

## Pillars

- Cozy first: the player helps neighbors rather than fighting or optimizing under pressure.
- Readable animals: capybaras are large calm ovals, guinea pigs are round snack friends, and gerbils are tiny fast couriers with long tails.
- Small loop, real game: move, collect the right snack, deliver, raise happiness, finish a short round.

## MVP Loop

1. The player taps the town map to move.
2. Snacks appear around town as clover, berries, seeds, carrots, and mint.
3. Moving close to a snack collects it into a typed snack pouch.
4. Three NPCs request help:
   - Moss the capybara needs mint for the bathhouse.
   - Pip the guinea pig needs berry snacks for market day.
   - Zip the gerbil needs seeds for tunnel patrol.
5. The player walks close to an NPC and taps Help to spend that NPC's requested snack.
6. Each completed help task raises happiness.
7. Helping all three neighbors reaches the town party win condition.

## Controls

- Tap world: move toward that spot.
- Help: deliver a snack to a nearby unhelped neighbor.
- Pause: freeze or resume the game.
- Reset: restart the round.

## Screen Layout

- Top HUD: happiness, requested-snack pouch chips, current objective, short feedback text.
- Main view: warm top-down town with layered paths, bathhouse pond, garden rows, snack stall, tunnel entrance, homes, snacks, animals, flowers, bushes, trees, and player.
- Bottom controls: Help, Pause/Resume, Reset.

## Content Scope

The game intentionally uses native Canvas shapes instead of an asset pipeline. This keeps the playable slice small and maintainable while still making each species visually distinct.

## Visual Language

- Capybara: broad rounded body, large snout, tiny ears, relaxed eyes, bath towel and flower detail.
- Guinea pig: squat oval body, cream belly, cheek patches, little snack tray for merchant personality.
- Gerbil: smaller body, long curved tail, courier satchel, curious forward posture.
- Helper: actual small helper with face, hair, tunic, and snack pouch.
- Town: warm grass with deterministic flecks, dirt paths, pond highlights, steam curls, garden rows, burrow depth, stall awning, trees, bushes, flowers, and soft shadows.
- Party: confetti and glow accents appear when the town reaches the win state.

Next strong additions:

- Add a tiny day timer and daily goals.
- Add animal idle animations and speech bubbles.
- Add a simple save file for unlocked decorations.
