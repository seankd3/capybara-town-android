package com.capybaratown.game;

public final class GameModelTest {
    public static void main(String[] args) {
        collectsSnackAndHelpsEverySpecies();
        resetRestoresStartingState();
        System.out.println("GameModelTest passed");
    }

    private static void collectsSnackAndHelpsEverySpecies() {
        GameModel game = new GameModel();
        for (GameModel.Snack snack : game.getSnacksOnMap()) {
            moveTo(game, snack.x, snack.y);
        }
        assertEquals(5, game.getSnacks(), "all snacks collected");

        for (GameModel.Npc npc : game.getNpcs()) {
            moveTo(game, npc.x, npc.y);
            assertTrue(game.tryHelpNearby(), "helped " + npc.name);
        }

        assertTrue(game.hasWon(), "win condition reached");
        assertEquals(3, game.helpedCountForTests(), "all species helped");
        assertTrue(game.getHappiness() >= 100, "happiness reached party threshold");
    }

    private static void resetRestoresStartingState() {
        GameModel game = new GameModel();
        moveTo(game, game.getSnacksOnMap().get(0).x, game.getSnacksOnMap().get(0).y);
        game.reset();
        assertEquals(0, game.getSnacks(), "snacks reset");
        assertEquals(10, game.getHappiness(), "happiness reset");
        assertTrue(!game.hasWon(), "win flag reset");
    }

    private static void moveTo(GameModel game, float x, float y) {
        game.setMoveTarget(x, y);
        for (int i = 0; i < 240; i++) {
            game.tick(1f / 30f);
        }
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + " but was " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }
}
