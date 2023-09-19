package minigames.server.highscore;

import java.util.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import minigames.server.database.DatabaseTable;

/**
 * Unit tests for Highscore API.
 * 
 * @author Kieran Hillier (Group: Merge Mavericks)
 */
public class HighScoreUnitTests {

    private static final String  TEST_GAME_NAME = "testGame";
    private static final Boolean TEST_IS_LOWER_BETTER = false;

    private static final String  TEST_PLAYER_ID = "testPlayer";
    private static final int     TEST_SCORE = 100;

    @Mock
    private GameTable mockGameTable;
    @Mock
    private ScoreTable mockScoreTable;
    @Mock
    private GameRecord mockGameObject;

    private HighScoreStorage storage;
    private HighScoreManager manager;
    private GlobalLeaderboard leaderboard;
    private HighScoreAPI api;
    private GameRecord realGameObject;
    private List<ScoreRecord> mockScoreList;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockGameTable.retrieveOne(any())).thenReturn(mockGameObject);
        when(mockGameObject.isLowerBetter()).thenReturn(TEST_IS_LOWER_BETTER);

        storage = new DerbyHighScoreStorage(mockGameTable, mockScoreTable);
        manager = new HighScoreManager(storage);
        leaderboard = new GlobalLeaderboard(storage);
        api = new HighScoreAPI(manager, leaderboard);
        realGameObject = new GameRecord(TEST_GAME_NAME, TEST_IS_LOWER_BETTER);
    }


    // Begin tests

    @Test
    public void testRegisterGame_WhenNewGame() {
        when(mockGameTable.retrieveOne(any())).thenReturn(null);
        api.registerGame(TEST_GAME_NAME, TEST_IS_LOWER_BETTER);
        verify(mockGameTable         ).create(any(GameRecord.class));
        verify(mockGameTable, never()).update(any());
    }
    @Test
    public void testRegisterGame_WhenNewValue() {
        when(mockGameObject.isLowerBetter()).thenReturn(!TEST_IS_LOWER_BETTER);
        api.registerGame(TEST_GAME_NAME, TEST_IS_LOWER_BETTER);
        verify(mockGameTable, never()).create(any());
        verify(mockGameTable         ).update(any(GameRecord.class));
    }


    @Test
    public void testRecordScore_WhenNewScoreIsBetter() {
        ScoreRecord previousBest = new ScoreRecord(TEST_PLAYER_ID, TEST_GAME_NAME, 90);
        when(mockGameTable.retrieveOne(any())).thenReturn(realGameObject);
        when(mockScoreTable.retrieveOne(any(ScoreRecord.class))).thenReturn(previousBest);
        api.recordScore(TEST_PLAYER_ID, TEST_GAME_NAME, TEST_SCORE); // new score is 100, which IS better
        verify(mockScoreTable).update(any(ScoreRecord.class));
    }
    @Test
    public void testRecordScore_WhenNewScoreIsNotBetter() {
        ScoreRecord previousBest = new ScoreRecord(TEST_PLAYER_ID, TEST_GAME_NAME, 110);
        when(mockGameTable.retrieveOne(any())).thenReturn(realGameObject);
        when(mockScoreTable.retrieveOne(any(ScoreRecord.class))).thenReturn(previousBest);
        api.recordScore(TEST_PLAYER_ID, TEST_GAME_NAME, TEST_SCORE); // new score is 100, which is NOT better
        verify(mockScoreTable, never()).update(any());
    }
    @Test
    public void testRecordScore_WhenDatabaseError() {
        when(mockGameTable.retrieveOne(any()))
            .thenThrow(new RuntimeException("Database error"));
        assertThrows(RuntimeException.class, () -> {
            api.recordScore(TEST_PLAYER_ID, TEST_GAME_NAME, TEST_SCORE);
        });
    }


    @Test
    public void testGetPersonalBest_WhenScoreExists() {
        ScoreRecord expectedScore = new ScoreRecord(TEST_PLAYER_ID, TEST_GAME_NAME, TEST_SCORE);
        when(mockScoreTable.retrieveOne(any())).thenReturn(expectedScore);
        ScoreRecord result = api.getPersonalBest(TEST_PLAYER_ID, TEST_GAME_NAME);
        assertNotNull(result);
        assertEquals(expectedScore, result);
    }
    @Test
    public void testGetPersonalBest_WhenScoreDoesNotExists() {
        when(mockScoreTable.retrieveOne(any())).thenReturn(null);
        assertNull(api.getPersonalBest(TEST_PLAYER_ID, TEST_GAME_NAME));
    }


    @Test
    public void testGetHighScores_WhenScoresExist() {
        List<ScoreRecord> expectedScores = Arrays.asList(
            new ScoreRecord(TEST_PLAYER_ID + "1", TEST_GAME_NAME, TEST_SCORE),
            new ScoreRecord(TEST_PLAYER_ID + "2", TEST_GAME_NAME, TEST_SCORE + 10)
        );
        when(mockGameTable.retrieveOne(any())).thenReturn(realGameObject);
        when(mockScoreTable.retrieveMany(any())).thenReturn(expectedScores);
        List<ScoreRecord> results = api.getHighScores(TEST_GAME_NAME);
        assertFalse(results.isEmpty());
        assertEquals(2, results.size());
        assertTrue(results.containsAll(expectedScores));
    }
    @Test
    public void testGetHighScores_WhenEmpty() {
        when(mockGameTable.retrieveOne(any())).thenReturn(realGameObject);
        when(mockScoreTable.retrieveMany(any())).thenReturn(Collections.emptyList());
        assertTrue(api.getHighScores(TEST_GAME_NAME).isEmpty());
    }


    @Test
    public void testGetGlobalLeaderboard_WhenScoresExist() {
        List<ScoreRecord> scores = Arrays.asList(
            new ScoreRecord(TEST_PLAYER_ID + "1", TEST_GAME_NAME + "1", TEST_SCORE),
            new ScoreRecord(TEST_PLAYER_ID + "2", TEST_GAME_NAME + "1", TEST_SCORE + 10),
            new ScoreRecord(TEST_PLAYER_ID + "1", TEST_GAME_NAME + "2", TEST_SCORE + 20),
            new ScoreRecord(TEST_PLAYER_ID + "2", TEST_GAME_NAME + "2", TEST_SCORE + 30)
        );
        when(mockScoreTable.retrieveAll()).thenReturn(scores);
        Map<String, Integer> leaderboard = api.getGlobalLeaderboard();
        assertFalse(leaderboard.isEmpty());
        assertEquals(2, leaderboard.size());
    }
    @Test
    public void testGetGlobalLeaderboard_WhenEmpty() {
        when(mockScoreTable.retrieveAll()).thenReturn(Collections.emptyList());
        assertTrue(api.getGlobalLeaderboard().isEmpty());
    }
}