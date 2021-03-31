package draw

import java.awt.Point
import java.awt.Color

object Preferences {
    var color: Color = Color.black
    var operation: Operation = makeRect
    var fill = false
}

trait Operation {
    def click(point: Point)
    def move(point: Point)
}

object Select extends Operation {
    def click(point: Point): Unit = ???
    def move(point: Point): Unit = ???
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

object makeRect extends Maker {def makeShape(point: Point): Shape = new Rectangle(point)}
object makeOval extends Maker {def makeShape(point: Point): Shape = new Oval(point)}
object makeLine extends Maker {def makeShape(point: Point): Shape = new Line(point)}



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