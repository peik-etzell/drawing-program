package draw
import scala.swing._
import java.awt.Color._
import scala.collection.mutable.Buffer
import scala.swing.event.MouseMoved
import scala.swing.event.MouseClicked

class Canvas extends Panel {
    private val elements: Buffer[Element] = Buffer()
    private val undos: Buffer[Element] = Buffer()

    listenTo(mouse.clicks)
    listenTo(mouse.moves)

    reactions += {
        case MouseMoved(_, point, _) => {
            Preferences.operation.move(point)
        }
        case MouseClicked(_, point, _, _, _) => {
            Preferences.operation.click(point)
        }
    }

    def addElement(element: Element) = {
        elements += element
    }
    
    def undo() = {
        if (elements.nonEmpty) {
            val last = elements.last
            undos += last
            elements -= last
            repaint()
        }
    }
    def redo() = {
        if (undos.nonEmpty) {
            val elem = undos.last
            elements += elem
            undos -= elem
            repaint()
        }
    }

    def clear() = {
        elements.clear()
        repaint()
    }
    
    override def paintComponent(g: Graphics2D): Unit = {
        g.clearRect(0, 0, 1000, 1000)
        elements.foreach(_.draw(g))
    }
}
