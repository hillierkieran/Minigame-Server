package minigames.server;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;

/**
 * Tests of the GameRegistry. Also ensures there's at least one example test containing mockito.
 */
public class GameRegistryTests {

    @Test
    @DisplayName("GameRegistry should show games for the requested platform and not others")
    public void filtersForClient() {
        GameRegistry gr = new GameRegistry();

        GameServer gs1 = mock(GameServer.class);
        when(gs1.getSupportedClients()).thenReturn(new ClientType[] { ClientType.Swing });
        gr.registerGameServer("Swing only", gs1);
        
        GameServer gs2 = mock(GameServer.class);
        when(gs2.getSupportedClients()).thenReturn(new ClientType[] { ClientType.Scalajs });
        gr.registerGameServer("Scalajs only", gs2);

        List<GameServer> available = gr.getGamesForPlatform(ClientType.Swing);
        assertTrue(available.contains(gs1));
        assertFalse(available.contains(gs2));
    }
    
}
