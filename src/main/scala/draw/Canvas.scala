package draw
import scala.swing._
import java.awt.Color._
import scala.collection.mutable.Buffer
import scala.collection.mutable.Stack
import scala.swing.event.MouseDragged
import scala.swing.event.MouseReleased
import scala.swing.event.MousePressed
import scala.swing.event.Key

object Canvas extends Panel {
    var elements: Buffer[Element] = Buffer()
    private val recent: Stack[Buffer[Element]] = Stack()
    private val history: Stack[Operation] = Stack()
    private val undone: Stack[Operation] = Stack()

    var operation: Operation = new MakeRect
    var color = black
    var fill = false
    var stroke = 15
    val selected: Buffer[Element] = Buffer()

    preferredSize = new Dimension(1280, 960)

    listenTo(mouse.clicks)
    listenTo(mouse.moves)


    reactions += {
        case MousePressed(_, point, 1152, _, _) => { // Pressed with control
            Select.press(point)
        }
        case MousePressed(_, point, _, _, _) => {
            operation.press(point)
            this.repaint()
        }
        case MouseDragged(_, point, _) => {
            operation.drag(point)
            this.repaint()
        }
        case MouseReleased(_, _, _, _, _) => {
            operation.release()
            this.repaint()
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

    def open(elems: Buffer[Element]) = {
        recent.push(elements)
        elements = elems
        repaint()
    }

    def close() = {
        if (!recent.isEmpty) {
            elements = recent.pop()
        } else {
            elements.clear()
        }
        repaint()
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

    override def paintComponent(g: Graphics2D): Unit = {
        g.setFont(new Font("Monospaced", 0, 25))
        g.setBackground(white)
        g.clearRect(0, 0, 1920, 1080)
        
        elements.foreach(_.draw(g))
        Debug.draw(g)
    }
}
