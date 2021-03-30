package draw
import scala.swing._
import java.awt.Color._
import scala.collection.mutable.Buffer
import scala.swing.event.MouseDragged

class Canvas extends Panel {
    private val elements: Buffer[Element] = Buffer()
    private val undos: Buffer[Element] = Buffer()

    def addElement(element: Element) = {
        elements += element
    }
    
    def undo() = {
        if (elements.nonEmpty) {
            val last = elements.last
            undos += last
            elements -= last
        }
    }
    def redo() = {
        if (undos.nonEmpty) {
            val elem = undos.last
            elements += elem
            undos -= elem
        }
    }

    def clear() = {
        elements.clear()
        repaint()
    }
    
    override def paintComponent(g: Graphics2D): Unit = {
        elements.foreach(_.draw(g))
    }
}