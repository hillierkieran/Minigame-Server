package minigames.commands

class CommandPackageSuite extends munit.FunSuite() {

    // An example test
    // This just checks the serialisation of a commandpackage is consistent -- we're not testing it is correct
    // (for which we should check the fields)
    test("An empty command package can be written to string and parsed back") {
        val cp = CommandPackage(gameServer="my server", gameId="my id", player="Bob", commands = Nil)
        assertEquals(CommandPackage.fromJson(cp.toJson), cp)
    }




}