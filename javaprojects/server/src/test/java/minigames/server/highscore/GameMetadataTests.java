package minigames.server.highscore;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class GameMetadataTests {

    @Test
    @DisplayName("GameMetadata getters and setters work as expected")
    public void testGameMetadataGettersSetters() {
        GameMetadata metadata = new GameMetadata("testGame", true);

        assertEquals("testGame", metadata.getGameName());
        assertTrue(metadata.isLowerBetter());

        metadata.setGameName("anotherGame");
        metadata.setLowerBetter(false);

        assertEquals("anotherGame", metadata.getGameName());
        assertFalse(metadata.isLowerBetter());
    }
}