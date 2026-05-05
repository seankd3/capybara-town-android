# Capybara Town Game Design

## Pillars

- Sanctuary first: the player cares for rescued residents through food, enrichment, quiet spaces, and cozy habitats rather than fighting or optimizing under pressure.
- Readable animals: capybaras are large calm ovals, guinea pigs are round snack friends, and gerbils are tiny fast couriers with long tails.
- Small loop, real game: move, collect the right snack, deliver care, raise sanctuary care, finish a short round.

## MVP Loop

1. The player taps the town map to move.
2. Snacks appear around town as clover, berries, seeds, carrots, and mint.
3. Moving close to a snack collects it into a typed snack pouch.
4. Three sanctuary residents request care:
   - Moss the capybara needs mint for the hydrotherapy pond.
   - Pip the guinea pig needs berry forage enrichment.
   - Zip the gerbil needs seeds for tunnel enrichment.
5. The player walks close to an NPC and taps Care to spend that NPC's requested snack.
6. Each completed care task raises sanctuary care.
7. Helping all three residents reaches the sanctuary celebration win condition.

## Controls

- Tap world: move toward that spot.
- Care: deliver a snack-enrichment item to a nearby unhelped resident.
- Pause: freeze or resume the game.
- Reset: restart the round.

## Screen Layout

- Top HUD: care score, requested-snack pouch chips, current objective, short feedback text.
- Main view: warm top-down sanctuary with layered paths, hydrotherapy pond, garden rows, forage table, tunnel entrance, quiet hut, snacks, animals, flowers, bushes, trees, and caretaker.
- Bottom controls: Care, Pause/Resume, Reset.

## Content Scope

The game intentionally uses native Canvas shapes instead of an asset pipeline. This keeps the playable slice small and maintainable while still making each species visually distinct.

## Visual Language

- Capybara: broad rounded body, large snout, tiny ears, relaxed eyes, bath towel and flower detail.
- Guinea pig: squat oval body, cream belly, cheek patches, little snack tray for merchant personality.
- Gerbil: smaller body, long curved tail, courier satchel, curious forward posture.
- Helper: actual small sanctuary caretaker with face, hair, tunic, and snack pouch.
- Sanctuary: warm grass with deterministic flecks, dirt paths, pond highlights, steam curls, garden rows, burrow depth, forage-table awning, trees, bushes, flowers, and soft shadows.
- Celebration: confetti and glow accents appear when sanctuary care reaches the win state.

Next strong additions:

- Add a tiny day timer and daily goals.
- Add animal idle animations and speech bubbles.
- Add a simple save file for unlocked decorations.
