package draw
import scala.swing._
import java.awt.Color._
import scala.collection.mutable.Buffer
import scala.swing.event.MouseMoved
import scala.swing.event.MouseClicked
import scala.collection.mutable.Stack
import scala.swing.event.MouseDragged
import scala.swing.event.MouseReleased
import scala.swing.event.MousePressed

class Canvas extends Panel {
    val elements: Buffer[Element] = Buffer()
    private val history: Stack[Operation] = Stack()
    private val undone: Stack[Operation] = Stack()

    listenTo(mouse.clicks)
    listenTo(mouse.moves)


    reactions += {
        case MouseMoved(_, point, _) => {
            Mode.operation.move(point)
        }
        case MousePressed(_, point, _, _, _) => {
            Mode.operation.press(point)
        }
        case MouseDragged(_, point, _) => {
            Mode.operation.drag(point)
        }
        case MouseReleased(_, _, _, _, _) => {
            Mode.operation.release()
        }
    }

    def addElement(element: Element) = {
        elements += element
    }

    def removeElement(element: Element) = {
        elements -= element
    }

    def pushHistory(operation: Operation) = {
        history.push(operation)
        undone.clear()
    }
    
    def undo() = {
        try {
            val operation = history.pop()
            operation.undo()
            undone.push(operation)
            repaint()
        } catch {
            case e: NoSuchElementException => println("Nothing to undo.")
        }
    }
    def redo() = {
        try {
            val operation = undone.pop()
            operation.redo()
            history.push(operation)
            repaint()
        } catch {
            case e: NoSuchElementException => println("Nothing to redo.")
        }
    }

    def clear() = {
        elements.clear()
        repaint()
    }
    
    override def paintComponent(g: Graphics2D): Unit = {
        g.setBackground(Mode.backgroundColor)
        g.clearRect(0, 0, 1920, 1080)
        
        elements.foreach(_.draw(g))
        g.setColor(black)
        Debug.draw(g)
    }
}
