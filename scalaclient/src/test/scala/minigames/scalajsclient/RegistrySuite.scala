package minigames.scalajsclient

import scala.scalajs.js

class RegistrySuite extends munit.FunSuite {

    test("the test framework runs") {
        assertEquals(true, true)
    }

    test("ClientRegistry registers and finds GameClients") {
        // We don't have Mockito, but we can create empty implementations of interfaces 
        val myGc1 = new GameClient {
            def closeGame() = ??? // ??? is Scala shorthand for "throw NotImplementedError"
            def execute(metadata: GameMetadata, command: js.Dynamic): Unit = ???
            def load(metadata: GameMetadata, playerName: String): Unit = ???
        }

        ClientRegistry.registerClient("client", myGc1)

        assertEquals(ClientRegistry.getClient("client"), Some(myGc1))
        assertEquals(ClientRegistry.getClient("not registered"), None)
    }

}