package minigames.scalafxclient

/**
 * Holds information on which games are available and can be served up to clients.
 */
def getGameClient(name:String):Option[GameClient] = name match {
    case "MuddleText" => Some(minigames.muddletext.MuddleText())
    case _ => None

}