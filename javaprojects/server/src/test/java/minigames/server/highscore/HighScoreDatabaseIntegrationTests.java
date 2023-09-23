package minigames.server.highscore;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class HighScoreDatabaseIntegrationTests {

    private static final String TEST_ENV = "testEnv";
    private static final String TEST_GAME_NAME = "TestGame";
    private static final String TEST_PLAYER_ID = "TestPlayer";
    private static final int    TEST_SCORE = 100;

    private HighScoreAPI api;


    @BeforeAll
    public static void initialise() {
        System.setProperty(TEST_ENV, "true");
    }

    @BeforeEach
    public void setup() {
        api = new HighScoreAPI();
        api.deleteGame(TEST_GAME_NAME); // drop table
        api.registerGame(TEST_GAME_NAME, false); // create table
    }

    @AfterEach
    public void teardown() {
        if (api == null) api = new HighScoreAPI();
        api.deleteGame(TEST_GAME_NAME); // drop table
    }

    @AfterAll
    public static void cleanup() {
        System.clearProperty(TEST_ENV);
    }


    // Begin tests

    @Test
    public void testRegisterGame() {
        api.deleteGame(TEST_GAME_NAME);
        assertFalse(api.isGameRegistered(TEST_GAME_NAME));
        api.registerGame(TEST_GAME_NAME, false);
        assertTrue(api.isGameRegistered(TEST_GAME_NAME));
    }


    @Test
    public void testRecordScore() {
        assertNull(api.getPersonalBest(TEST_PLAYER_ID, TEST_GAME_NAME));
        api.recordScore(TEST_PLAYER_ID, TEST_GAME_NAME, TEST_SCORE);
        assertEquals(TEST_SCORE,
            api.getPersonalBest(TEST_PLAYER_ID, TEST_GAME_NAME).getScore());
    }
    @Test
    public void testRecordScore_WhenNewScoreIsNotBetter() {
        int higherScore = TEST_SCORE + 10;
        api.recordScore(TEST_PLAYER_ID, TEST_GAME_NAME, higherScore);
        api.recordScore(TEST_PLAYER_ID, TEST_GAME_NAME, TEST_SCORE);
        assertEquals(higherScore,
            api.getPersonalBest(TEST_PLAYER_ID, TEST_GAME_NAME).getScore());
    }


    @Test
    public void testGetPersonalBest() {
        assertNull(api.getPersonalBest(TEST_PLAYER_ID, TEST_GAME_NAME));
        api.recordScore(TEST_PLAYER_ID, TEST_GAME_NAME, TEST_SCORE);
        assertEquals(TEST_SCORE, api.getPersonalBest(TEST_PLAYER_ID, TEST_GAME_NAME).getScore());
    }


    @Test
    public void testGetHighScores() {
        assertTrue(api.getHighScores(TEST_GAME_NAME).isEmpty());
        api.recordScore(TEST_PLAYER_ID, TEST_GAME_NAME, TEST_SCORE);
        assertFalse(api.getHighScores(TEST_GAME_NAME).isEmpty());
    }


    @Test
    public void testGetHighScoresToString() {
        assertTrue(api.getHighScores(TEST_GAME_NAME).isEmpty());
        api.recordScore(TEST_PLAYER_ID, TEST_GAME_NAME, TEST_SCORE);
        assertTrue(api.getHighScoresToString(TEST_GAME_NAME)
            .contains(TEST_PLAYER_ID + " " + TEST_SCORE));
    }


    @Test
    public void testDeleteScore() {
        api.recordScore(TEST_PLAYER_ID, TEST_GAME_NAME, TEST_SCORE);
        assertNotNull(api.getPersonalBest(TEST_PLAYER_ID, TEST_GAME_NAME));
        api.deleteScore(TEST_PLAYER_ID, TEST_GAME_NAME);
        assertNull(api.getPersonalBest(TEST_PLAYER_ID, TEST_GAME_NAME));
    }


    @Test
    public void testDeleteGame() {
        api.recordScore(TEST_PLAYER_ID, TEST_GAME_NAME, TEST_SCORE);
        assertTrue(api.isGameRegistered(TEST_GAME_NAME));
        api.deleteGame(TEST_GAME_NAME);
        assertFalse(api.isGameRegistered(TEST_GAME_NAME));
    }
}