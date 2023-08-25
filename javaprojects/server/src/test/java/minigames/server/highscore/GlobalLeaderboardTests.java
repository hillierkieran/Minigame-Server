package minigames.server.highscore;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

public class GlobalLeaderboardTests {

    private HighScoreStorage mockStorage;
    private GlobalLeaderboard leaderboard;

    @BeforeEach
    public void setUp() {
        mockStorage = mock(HighScoreStorage.class);
        leaderboard = new GlobalLeaderboard(mockStorage);
    }

    @Test
    @DisplayName("ComputeGlobalScores returns correct global scores when lower scores are better")
    public void testComputeGlobalScores() {
        ScoreRecord record1 = new ScoreRecord("player1", "game1", 10);
        ScoreRecord record2 = new ScoreRecord("player2", "game1", 20);

        when(mockStorage.retrieveAllScores())
            .thenReturn(Arrays.asList(record1, record2));
        when(mockStorage.getGameMetadata("game1"))
            .thenReturn(new GameMetadata("game1", true));

        Map<String, Integer> globalScores = leaderboard.computeGlobalScores();

        assertEquals(2, globalScores.size());
        assertTrue(globalScores.containsKey("player1"));
        assertTrue(globalScores.containsKey("player2"));
        assertEquals(1, globalScores.get("player1"));
        assertEquals(2, globalScores.get("player2"));
    }

    @Test
    @DisplayName("ComputeGlobalScores returns correct global scores when higher scores are better")
    public void testComputeGlobalScoresHigherIsBetter() {
        ScoreRecord record1 = new ScoreRecord("player1", "game1", 10);
        ScoreRecord record2 = new ScoreRecord("player2", "game1", 20);
    
        when(mockStorage.retrieveAllScores())
            .thenReturn(Arrays.asList(record1, record2));
        when(mockStorage.getGameMetadata("game1"))
            .thenReturn(new GameMetadata("game1", false));
    
        Map<String, Integer> globalScores = leaderboard.computeGlobalScores();
    
        assertTrue(globalScores.containsKey("player1"));
        assertTrue(globalScores.containsKey("player2"));
        assertEquals(2, globalScores.get("player1"));
        assertEquals(1, globalScores.get("player2"));
    }
}