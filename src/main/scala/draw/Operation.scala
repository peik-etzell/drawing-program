package draw

import java.awt.Point
import java.awt.Color._
import scala.util.Random

object Mode {
    var operation: Operation = new MakeRect
    var color = black
    var fill = false
    var stroke = 5
    var backgroundColor = white
    var selected: Option[Element] = None
}

trait Operation {
    def press(point: Point)
    def move(point: Point)
    def drag(point: Point)
    def release()
    def undo()
    def redo()
}

object Select extends Operation {
    def press(point: Point): Unit = {
        Mode.selected = GUI.canvas.elements.reverse.filter(_.hit(point)).headOption
    }
    def move(point: Point) = {}
    def drag(point: Point): Unit = {}
    def release(): Unit = {}
    def undo() = {}
    def redo() = {}
}

trait Maker extends Operation {
    var element: Option[Shape] = None

    def makeShape(point: Point): Shape
    def reset(): Unit 
    def press(point: Point) {}
    def move(point: Point) {}

    def drag(point: Point): Unit = {
        if (element.isEmpty) {
            element = Some(makeShape(point))
            GUI.canvas.addElement(element.get)
            GUI.canvas.pushHistory(this)
        } else {
            element.foreach(_.setPoint(point))
            GUI.canvas.repaint()
        }
    }
    def release(): Unit = reset()
    def undo() = GUI.canvas.removeElement(element.get)
    def redo() = GUI.canvas.addElement(element.get)
}

class MakeRect extends Maker {
    def makeShape(point: Point): Shape = new Rectangle(point)
    def reset(): Unit = {
        Mode.operation = new MakeRect
    }
}
class MakeOval extends Maker {
    def makeShape(point: Point): Shape = new Oval(point)
    def reset(): Unit = {
        Mode.operation = new MakeOval
    }
}
class MakeLine extends Maker {
    def makeShape(point: Point): Shape = new Line(point)
    def reset(): Unit = {
        Mode.operation = new MakeLine
    }
}
class MakeFreehand extends Maker {
    def makeShape(point: Point): Shape = new Freehand(point)
    def reset(): Unit = {
        Mode.operation = new MakeLine
    }
}

trait Transform extends Operation {
    val transformation: Transformation
    var element: Option[Element] = None
    var start: Option[Point] = None
    
    def press(point: Point): Unit = {
        Select.press(point)
    }

    def release(): Unit
    def drag(point: Point): Unit
    def move(point: Point): Unit = {}
    def undo(): Unit = element.foreach(_.transformations -= transformation)
    def redo(): Unit = element.foreach(_.transformations += transformation)

}
class Rotate extends Transform {
    val transformation: Rotation = new Rotation
    
    def release(): Unit = {
        Mode.operation = new Rotate
    }
    def drag(point: Point): Unit = {
        if (Mode.selected.isDefined) {
            if (start.isEmpty) {
                element = Mode.selected
                start = Some(point)
                element.foreach(_.transformations += transformation)
                GUI.canvas.pushHistory(this)
            } else {
                transformation.theta = 0.01 * (start.get.x - point.x)
                GUI.canvas.repaint()
            }
        }
    }
}

class Translate extends Operation {
    var deltax = 0
    var deltay = 0
    var element: Option[Element] = None
    var start: Option[Point] = None
    
    def press(point: Point): Unit = {
        Select.press(point)
    }

    def release(): Unit = {
        Mode.operation = new Translate
    }
    def drag(point: Point): Unit = {
        if (Mode.selected.isDefined) {
            if (start.isEmpty) {
                element = Mode.selected
                start = Some(point)
                
                GUI.canvas.pushHistory(this)
            } else {
                val dx = point.x - start.get.x
                val dy = point.y - start.get.y
                start = Some(point)
                deltax += dx
                deltay += dy
                element.foreach(_.move(dx, dy))
                GUI.canvas.repaint()
            }
        }
    }
    def move(point: Point): Unit = {}
    def undo(): Unit = element.foreach(_.move(-deltax, -deltay))
    def redo(): Unit = element.foreach(_.move(deltax, deltay))

}

class Scale extends Transform {
    val transformation: Scaling = new Scaling
    
    def release(): Unit = {
        Mode.operation = new Scale
    }
    def drag(point: Point): Unit = {
        if (Mode.selected.isDefined) {
            if (start.isEmpty) {
                element = Mode.selected
                start = Some(point)
                element.foreach(_.transformations += transformation)
                GUI.canvas.pushHistory(this)
            } else {
                transformation.sx = 0.005 * (start.get.x - point.x) + 1.0
                transformation.sy = 0.005 * (start.get.y - point.y) + 1.0
                GUI.canvas.repaint()
            }
        }
    }
}