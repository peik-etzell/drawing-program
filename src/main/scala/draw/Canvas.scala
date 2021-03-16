package draw
import scala.swing._
import java.awt.Color._

object Canvas extends Panel {
    override def paintComponent(g: Graphics2D): Unit = {
        preferredSize = new Dimension(600, 600)
        background = yellow
        def update() = ???
    }



}