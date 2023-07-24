package minigames.server;

/**
 * Client platforms we support. 
 * This is sent down the wire as a string, but we convert it into an enum.
 */
public enum ClientType {
    Swing, Scalajs, Scalafx
}
