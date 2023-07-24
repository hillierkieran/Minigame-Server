package scalafxclient

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

/**
 * The app runs the game. This generates a main for us
 */
object App extends JFXApp3 { 

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
    }

}