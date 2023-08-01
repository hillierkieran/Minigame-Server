package minigames.scalafxclient

/**
  * Something that can be ticked.
  */
trait Tickable {
    def tick(now:Long, delta:Long):Unit
}