package draw
import scala.swing._
import java.awt.Color._

object Canvas extends Panel {
    override def paintComponent(g: Graphics2D): Unit = {
        preferredSize = new Dimension(600, 600)
        
        g.setColor(YELLOW)
        g.fillRect(10, 10, 20, 20)
        repaint()
        def update() = ???
    }



}