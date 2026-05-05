package com.capybaratown.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        public final String need;
        public final float x;
        public final float y;
        private boolean helped;

        private Npc(Species species, String name, String need, float x, float y) {
            this.species = species;
            this.name = name;
            this.need = need;
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
        npcs.add(new Npc(Species.CAPYBARA, "Moss", "needs herbs for the bathhouse", 250f, 330f));
        npcs.add(new Npc(Species.GUINEA_PIG, "Pip", "needs berry snacks for market day", 660f, 520f));
        npcs.add(new Npc(Species.GERBIL, "Zip", "needs trail mix for tunnel patrol", 505f, 975f));
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
        toast = "Tap paths to scamper. Gather snacks, then help neighbors.";
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
                    showToast(npc.name + " squeaks politely: bring one snack!");
                    return false;
                }
                snacks -= 1;
                npc.helped = true;
                happiness += 30;
                showToast(helpMessage(npc));
                if (allHelped() || happiness >= 100) {
                    happiness = Math.max(happiness, 100);
                    won = true;
                    showToast("Town party unlocked! Everyone is cozy and fed.");
                }
                return true;
            }
        }
        showToast("No neighbor is close enough yet.");
        return false;
    }

    public void togglePaused() {
        paused = !paused;
        showToast(paused ? "Paused under the willow." : "Back to town chores!");
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
            return "Complete: the whole town is happy.";
        }
        for (Npc npc : npcs) {
            if (!npc.helped) {
                return "Help " + npc.name + ": " + npc.need + ".";
            }
        }
        return "Enjoy the town party.";
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
                happiness += 3;
                showToast("Picked up " + snack.kind + ". Snack pouch: " + snacks + ".");
            }
        }
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
                return npc.name + " warms the bath stones. Happiness rises!";
            case GUINEA_PIG:
                return npc.name + " opens the squeaky snack stall!";
            case GERBIL:
                return npc.name + " reopens the speedy tunnel route!";
            default:
                return "A neighbor feels helped.";
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
