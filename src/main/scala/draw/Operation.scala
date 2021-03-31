package draw

import java.awt.Point
import java.awt.Color

object Mode {
    var color: Color = Color.black
    var operation: Operation = MakeRect
    var fill = false
    var backgroundColor = Color.white
    var selected: Option[Element] = None
}

trait Operation {
    def click(point: Point)
    def move(point: Point)
}

object Select extends Operation {
    def click(point: Point): Unit = {
        Mode.selected = GUI.canvas.elements.reverse.filter(_.hit(point)).headOption
    }
    def move(point: Point): Unit = {}
}

abstract class Maker extends Operation {
    private var clicked = false
    private var shape: Shape = null
    
    def makeShape(point: Point): Shape

    def click(point: Point) {
        if (!clicked) {
            shape = makeShape(point)
            GUI.canvas.addElement(shape)
            clicked = true 
        } else {
            clicked = false
        }
    }

    def move(point: Point) {
        if (clicked) {
            shape.end = point
            GUI.canvas.repaint()
        }
    }
}

object MakeRect extends Maker {
    def makeShape(point: Point): Shape = new Rectangle(point)
}
object MakeOval extends Maker {
    def makeShape(point: Point): Shape = new Oval(point)
}
object MakeLine extends Maker {
    def makeShape(point: Point): Shape = new Line(point)
}



object Rotate extends Operation {
    def click(point: Point): Unit = ???
    def move(point: Point): Unit = ???
}

object Translate extends Operation {
    def click(point: Point): Unit = ???
    def move(point: Point): Unit = ???
}

object Scale extends Operation {
    def click(point: Point): Unit = ???
    def move(point: Point): Unit = ???
}