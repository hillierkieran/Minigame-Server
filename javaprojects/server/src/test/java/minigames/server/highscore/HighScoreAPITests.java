package minigames.server.highscore;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

public class HighScoreAPITests {

    private HighScoreManager mockManager;
    private GlobalLeaderboard mockLeaderboard;
    private HighScoreAPI api;

    @BeforeEach
    public void setUp() {
        mockManager = mock(HighScoreManager.class);
        mockLeaderboard = mock(GlobalLeaderboard.class);
        api = new HighScoreAPI(mockManager, mockLeaderboard);
    }

    @Test
    @DisplayName("HighScoreAPI records scores using manager")
    public void testRecordScore() {
        api.recordScore("player1", "game1", 100);
        verify(mockManager).recordScore("player1", "game1", 100);
    }

    @Test
    @DisplayName("HighScoreAPI retrieves top scores using manager")
    public void testGetTopScores() {
        api.getTopScores("game1", 10);
        verify(mockManager).getTopScores("game1", 10);
    }

    @Test
    @DisplayName("HighScoreAPI retrieves personal best using manager")
    public void testGetPersonalBest() {
        api.getPersonalBest("player1", "game1");
        verify(mockManager).getPersonalBest("player1", "game1");
    }

    @Test
    @DisplayName("HighScoreAPI retrieves global leaderboard using leaderboard")
    public void testGetGlobalLeaderboard() {
        api.getGlobalLeaderboard();
        verify(mockLeaderboard).computeGlobalScores();
    }
}
