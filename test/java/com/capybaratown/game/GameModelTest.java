package com.capybaratown.game;

public final class GameModelTest {
    public static void main(String[] args) {
        collectsSnackAndHelpsEverySpecies();
        speciesRequestsRequireMatchingSnack();
        residentMomentsReflectPersonalityAndProgress();
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

    private static void speciesRequestsRequireMatchingSnack() {
        GameModel game = new GameModel();
        GameModel.Npc moss = game.getNpcs().get(0);
        moveTo(game, 155f, 620f); // clover, not Moss's requested mint
        moveTo(game, moss.x, moss.y);

        assertEquals(1, game.getSnacks(), "wrong snack stays in pouch");
        assertEquals(1, game.getSnackCount("clover"), "clover count tracked");
        assertTrue(!game.tryHelpNearby(), "wrong snack cannot satisfy species request");
        assertTrue(!moss.isHelped(), "Moss still waits for mint");

        moveTo(game, 725f, 1110f);
        moveTo(game, moss.x, moss.y);
        assertTrue(game.tryHelpNearby(), "matching snack helps Moss");
        assertEquals(0, game.getSnackCount("mint"), "requested mint spent");
        assertEquals(1, game.getSnackCount("clover"), "unrequested snack remains");
    }

    private static void residentMomentsReflectPersonalityAndProgress() {
        GameModel game = new GameModel();
        GameModel.Npc moss = game.getNpcs().get(0);
        GameModel.Npc pip = game.getNpcs().get(1);

        assertEquals("Moss", game.getCurrentResidentMomentSpeaker(), "starting moment speaker");
        assertContains(game.getCurrentResidentMomentPersonality(), "spa philosopher", "Moss personality");
        assertContains(game.getCurrentResidentMomentText(), "good soak", "Moss idle moment");

        moveTo(game, 155f, 620f);
        assertContains(game.getCurrentResidentMomentText(), "waiting for mint", "wrong snack moment");

        moveTo(game, 725f, 1110f);
        assertContains(game.getCurrentResidentMomentText(), "senses mint", "ready snack moment");
        moveTo(game, moss.x, moss.y);
        assertTrue(game.tryHelpNearby(), "Moss helped with mint");

        assertEquals("Pip", game.getCurrentResidentMomentSpeaker(), "moment advances to Pip");
        assertContains(game.getCurrentResidentMomentPersonality(), "snack critic", "Pip personality");
        assertContains(game.getCurrentResidentMomentText(), "berries", "Pip moment mentions berries");

        for (GameModel.Snack snack : game.getSnacksOnMap()) {
            moveTo(game, snack.x, snack.y);
        }
        for (GameModel.Npc npc : game.getNpcs()) {
            moveTo(game, npc.x, npc.y);
            game.tryHelpNearby();
        }
        assertEquals("Sanctuary update", game.getCurrentResidentMomentSpeaker(), "win moment speaker");
        assertContains(game.getCurrentResidentMomentText(), "100% cozy", "win moment");
        assertContains(game.getCurrentResidentMomentPersonality(), "group celebration", "win personality");
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

    private static void assertEquals(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected \"" + expected + "\" but was \"" + actual + "\"");
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }

    private static void assertContains(String actual, String expectedPart, String label) {
        if (!actual.contains(expectedPart)) {
            throw new AssertionError(label + ": expected \"" + actual + "\" to contain \"" + expectedPart + "\"");
        }
    }
}
