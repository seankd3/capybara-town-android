package com.capybaratown.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class GameModel {
    public static final float WORLD_WIDTH = 900f;
    public static final float WORLD_HEIGHT = 1300f;
    private static final float PLAYER_SPEED = 245f;
    private static final float COLLECT_RADIUS = 54f;
    private static final float HELP_RADIUS = 76f;

    public enum Species {
        CAPYBARA,
        GUINEA_PIG,
        GERBIL
    }

    public static final class Npc {
        public final Species species;
        public final String name;
        public final String personality;
        public final String need;
        public final String requestKind;
        public final float x;
        public final float y;
        private boolean helped;

        private Npc(Species species, String name, String personality, String need, String requestKind, float x, float y) {
            this.species = species;
            this.name = name;
            this.personality = personality;
            this.need = need;
            this.requestKind = requestKind;
            this.x = x;
            this.y = y;
        }

        public boolean isHelped() {
            return helped;
        }
    }

    public static final class Snack {
        public final String kind;
        public final float x;
        public final float y;
        private boolean collected;

        private Snack(String kind, float x, float y) {
            this.kind = kind;
            this.x = x;
            this.y = y;
        }

        public boolean isCollected() {
            return collected;
        }
    }

    private final ArrayList<Npc> npcs = new ArrayList<>();
    private final ArrayList<Snack> snacksOnMap = new ArrayList<>();
    private final LinkedHashMap<String, Integer> snackPouch = new LinkedHashMap<>();
    private float playerX;
    private float playerY;
    private float targetX;
    private float targetY;
    private int snacks;
    private int happiness;
    private boolean paused;
    private boolean won;
    private String toast;
    private float toastTime;

    public GameModel() {
        reset();
    }

    public void reset() {
        npcs.clear();
        snacksOnMap.clear();
        snackPouch.clear();
        registerSnackKind("clover");
        registerSnackKind("berry");
        registerSnackKind("seed");
        registerSnackKind("carrot");
        registerSnackKind("mint");
        npcs.add(new Npc(Species.CAPYBARA, "Moss", "calm spa philosopher / sleepy hydrotherapy king", "needs cool mint for the hydrotherapy pond", "mint", 250f, 330f));
        npcs.add(new Npc(Species.GUINEA_PIG, "Pip", "dramatic snack critic / tiny market goblin", "needs berry enrichment for the forage table", "berry", 660f, 520f));
        npcs.add(new Npc(Species.GERBIL, "Zip", "hyper tunnel courier / conspiracy cartographer", "needs seeds for tunnel enrichment", "seed", 505f, 975f));
        snacksOnMap.add(new Snack("clover", 155f, 620f));
        snacksOnMap.add(new Snack("berry", 350f, 800f));
        snacksOnMap.add(new Snack("seed", 735f, 760f));
        snacksOnMap.add(new Snack("carrot", 205f, 1070f));
        snacksOnMap.add(new Snack("mint", 725f, 1110f));
        playerX = 450f;
        playerY = 700f;
        targetX = playerX;
        targetY = playerY;
        snacks = 0;
        happiness = 10;
        paused = false;
        won = false;
        toast = "Tap paths to caretake. Gather snacks, then comfort sanctuary residents.";
        toastTime = 4f;
    }

    public void tick(float deltaSeconds) {
        if (paused) {
            return;
        }
        float dt = Math.max(0f, Math.min(deltaSeconds, 0.05f));
        movePlayer(dt);
        collectNearbySnacks();
        updateToast(dt);
    }

    public void setMoveTarget(float x, float y) {
        targetX = clamp(x, 20f, WORLD_WIDTH - 20f);
        targetY = clamp(y, 20f, WORLD_HEIGHT - 20f);
    }

    public boolean tryHelpNearby() {
        if (paused || won) {
            return false;
        }
        for (Npc npc : npcs) {
            if (!npc.helped && distance(playerX, playerY, npc.x, npc.y) <= HELP_RADIUS) {
                if (snacks <= 0) {
                    showToast(npc.name + " squeaks politely: bring enrichment snack!");
                    return false;
                }
                if (getSnackCount(npc.requestKind) <= 0) {
                    showToast(npc.name + " is waiting for " + npc.requestKind + ".");
                    return false;
                }
                snackPouch.put(npc.requestKind, getSnackCount(npc.requestKind) - 1);
                snacks -= 1;
                npc.helped = true;
                happiness += 30;
                showToast(helpMessage(npc));
                if (allHelped() || happiness >= 100) {
                    happiness = Math.max(happiness, 100);
                    won = true;
                    showToast("Sanctuary celebration unlocked! Everyone is cozy and cared for.");
                }
                return true;
            }
        }
        showToast("No neighbor is close enough yet.");
        return false;
    }

    public void togglePaused() {
        paused = !paused;
        showToast(paused ? "Paused under the willow." : "Back to sanctuary rounds!");
    }

    public float getPlayerX() {
        return playerX;
    }

    public float getPlayerY() {
        return playerY;
    }

    public int getSnacks() {
        return snacks;
    }

    public int getSnackCount(String kind) {
        Integer count = snackPouch.get(kind);
        return count == null ? 0 : count;
    }

    public Map<String, Integer> getSnackPouch() {
        return Collections.unmodifiableMap(snackPouch);
    }

    public int getHappiness() {
        return happiness;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean hasWon() {
        return won;
    }

    public List<Npc> getNpcs() {
        return Collections.unmodifiableList(npcs);
    }

    public List<Snack> getSnacksOnMap() {
        return Collections.unmodifiableList(snacksOnMap);
    }

    public String getObjectiveText() {
        if (won) {
            return "Complete: every resident is cared for.";
        }
        for (Npc npc : npcs) {
            if (!npc.helped) {
                return "Bring " + npc.requestKind + " to " + npc.name + ": " + npc.need + ".";
            }
        }
        return "Enjoy the sanctuary celebration.";
    }

    public String getCurrentResidentMomentText() {
        if (won) {
            return "Moss, Pip, and Zip declare the sanctuary 100% cozy and 12% snack crumbs.";
        }
        for (Npc npc : npcs) {
            if (!npc.helped) {
                if (getSnackCount(npc.requestKind) > 0) {
                    return readyMoment(npc);
                }
                if (snacks > 0) {
                    return waitingWithWrongSnackMoment(npc);
                }
                return idleMoment(npc);
            }
        }
        return "The residents are comparing tiny thank-you notes.";
    }

    public String getCurrentResidentMomentSpeaker() {
        if (won) {
            return "Sanctuary update";
        }
        for (Npc npc : npcs) {
            if (!npc.helped) {
                return npc.name;
            }
        }
        return "Sanctuary update";
    }

    public String getCurrentResidentMomentPersonality() {
        if (won) {
            return "group celebration";
        }
        for (Npc npc : npcs) {
            if (!npc.helped) {
                return npc.personality;
            }
        }
        return "cozy neighbors";
    }

    public String getToast() {
        return toastTime > 0f ? toast : "";
    }

    int helpedCountForTests() {
        int count = 0;
        for (Npc npc : npcs) {
            if (npc.helped) {
                count += 1;
            }
        }
        return count;
    }

    private void movePlayer(float dt) {
        float dx = targetX - playerX;
        float dy = targetY - playerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance <= 1f) {
            return;
        }
        float step = Math.min(distance, PLAYER_SPEED * dt);
        playerX += dx / distance * step;
        playerY += dy / distance * step;
    }

    private void collectNearbySnacks() {
        for (Snack snack : snacksOnMap) {
            if (!snack.collected && distance(playerX, playerY, snack.x, snack.y) <= COLLECT_RADIUS) {
                snack.collected = true;
                snacks += 1;
                snackPouch.put(snack.kind, getSnackCount(snack.kind) + 1);
                happiness += 3;
                showToast("Picked up " + snack.kind + ". Snack pouch: " + snacks + ".");
            }
        }
    }

    private void registerSnackKind(String kind) {
        snackPouch.put(kind, 0);
    }

    private boolean allHelped() {
        for (Npc npc : npcs) {
            if (!npc.helped) {
                return false;
            }
        }
        return true;
    }

    private String helpMessage(Npc npc) {
        switch (npc.species) {
            case CAPYBARA:
                return npc.name + " murmurs, \"The mint has achieved pond enlightenment.\"";
            case GUINEA_PIG:
                return npc.name + " gives the berries five squeaks and one crumb encore!";
            case GERBIL:
                return npc.name + " updates the tunnel map: seeds were here all along!";
            default:
                return "A neighbor feels helped.";
        }
    }

    private String idleMoment(Npc npc) {
        switch (npc.species) {
            case CAPYBARA:
                return "Moss: \"A good soak is polite soup.\"";
            case GUINEA_PIG:
                return "Pip opens snack court. Berry evidence pending.";
            case GERBIL:
                return "Zip mapped 3 tunnels and 1 tunnel-shaped nap.";
            default:
                return npc.name + " is having a small sanctuary thought.";
        }
    }

    private String waitingWithWrongSnackMoment(Npc npc) {
        switch (npc.species) {
            case CAPYBARA:
                return "Moss is waiting for mint. Pond crown reserved.";
            case GUINEA_PIG:
                return "Pip sniffs pouch. Berry review still pending.";
            case GERBIL:
                return "Zip files pouch under: not seeds, intriguing.";
            default:
                return npc.name + " is waiting for the right enrichment snack.";
        }
    }

    private String readyMoment(Npc npc) {
        switch (npc.species) {
            case CAPYBARA:
                return "Moss senses mint. Royal pond sigh loading.";
            case GUINEA_PIG:
                return "Pip sees berries. Snack court gasps.";
            case GERBIL:
                return "Zip sees seeds. Route official.";
            default:
                return npc.name + " is ready for care.";
        }
    }

    private void showToast(String message) {
        toast = message;
        toastTime = 3.5f;
    }

    private void updateToast(float dt) {
        toastTime = Math.max(0f, toastTime - dt);
    }

    private static float distance(float ax, float ay, float bx, float by) {
        float dx = ax - bx;
        float dy = ay - by;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
