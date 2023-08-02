package minigames.scalafxclient

import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.scene.{Scene, Group}
import scalafx.scene.paint.Color
import scalafx.scene.shape.*
import scalafx.scene.layout.*
import scalafx.scene.control.*
import scalafx.geometry.*
import scalafx.collections.*
import scalafx.animation.*
import scalafx.beans.property.*

import java.util.{Timer, TimerTask}
import io.vertx.core.Vertx

/**
 * The app runs the game. This generates a main for us.
 * 
 */
object App extends JFXApp3 { 

    /** host and port numbers for the server */
    lazy val (host:String, port:Int) = 
        this.parameters.unnamed.toList match {
            case hostAndPort :: _ => 
                if hostAndPort.contains(":") then
                    val Array(h, p) = hostAndPort.split(":")
                    (h, p.toInt)
                else 
                    (hostAndPort, 8080)
            case _ => 
                ("localhost", 8080)
        }

    /** A network client started as soon as we call it */
    lazy val mgnClient = MinigameNetworkClient(host, port)

    override def start() = {

        stage = new JFXApp3.PrimaryStage {
            title.value = "Minigame Network"
            width = 24 * 32
            height = 600
            scene = new Scene {
                content = Seq.empty
            }
            onCloseRequest = { (_) => System.exit(0) }
        }

        mgnClient.runMainMenuSequence()
    }

}