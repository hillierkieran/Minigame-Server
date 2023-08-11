package minigames.server.highscore;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

public class HighScoreManagerTests {

    private HighScoreStorage mockStorage;
    private HighScoreManager manager;

    @BeforeEach
    public void setUp() {
        mockStorage = mock(HighScoreStorage.class);
        manager = new HighScoreManager(mockStorage);
    }

    // Note: Methods are not yet implemented in HighScoreManager.
    // Mock expectations will ensure correct methods are being called.
    // TODO: Expand tests when HighScoreManager methods are fleshed out.

    @Disabled("Waiting for method implementation")
    @Test
    @DisplayName("RecordScore should store a new high score if it's better")
    public void testRecordScore() {
        ScoreRecord previousBest = new ScoreRecord("player1", "game1", 100);

        // Simulate some behaviour once methods are implemented
        when(mockStorage.retrievePersonalBest("player1", "game1"))
            .thenReturn(previousBest);

        manager.recordScore("player1", "game1", 150);
        verify(mockStorage).storeScore(any(ScoreRecord.class));
        // This can be more specific once method logic is defined.
    }

    @Disabled("Waiting for method implementation")
    @Test
    @DisplayName("RecordScore should not store score if it's not better")
    public void testRecordScoreNotBetter() {
        ScoreRecord previousBest = new ScoreRecord("player1", "game1", 150);

        when(mockStorage.retrievePersonalBest("player1", "game1"))
            .thenReturn(previousBest);

        manager.recordScore("player1", "game1", 100);

        verify(mockStorage, never()).storeScore(any());
    }

    @Disabled("Waiting for method implementation")
    @Test
    @DisplayName("GetTopScores returns correct scores")
    public void testGetTopScores() {
        ScoreRecord record1 = new ScoreRecord("player1", "game1", 10);
        ScoreRecord record2 = new ScoreRecord("player2", "game1", 20);

        when(mockStorage.retrieveTopScores("game1", 2))
            .thenReturn(Arrays.asList(record1, record2));

        List<ScoreRecord> topScores = manager.getTopScores("game1", 2);

        assertEquals(2, topScores.size());
        assertTrue(topScores.contains(record1));
        assertTrue(topScores.contains(record2));
    }

    @Disabled("Waiting for method implementation")
    @Test
    @DisplayName("GetPersonalBest returns correct score")
    public void testGetPersonalBest() {
        ScoreRecord record = new ScoreRecord("player1", "game1", 100);

        when(mockStorage.retrievePersonalBest("player1", "game1"))
            .thenReturn(record);

        ScoreRecord personalBest = manager.getPersonalBest("player1", "game1");

        assertEquals(record, personalBest);
    }
}