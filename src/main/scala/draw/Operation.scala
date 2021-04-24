package draw

import java.awt.Point
import java.awt.Color._
import scala.util.Random
import scala.swing._
import java.awt.Dimension
import scala.swing.event._
import scala.collection.mutable.Buffer

object Mode {
    var operation: Operation = new MakeRect
    var color = black
    var fill = false
    var stroke = 5
    var backgroundColor = white
    var selected: Buffer[Element] = Buffer()
}

trait Operation {
    def press(point: Point)
    def drag(point: Point)
    def release()
    def undo()
    def redo()
}

object Select extends Operation {
    def press(point: Point): Unit = {
        val underMouse = this.get(point)
        if (underMouse.isEmpty) {
            Mode.selected.clear()
        } else if (Mode.selected.contains(underMouse.head)) {
            Mode.selected -= underMouse.head
        } else {
            Mode.selected += underMouse.head
        }
    }
    def drag(point: Point): Unit = {}
    def release(): Unit = {}
    def undo() = {}
    def redo() = {}

    def get(point: Point): Option[Element] = {
        val underCursor = GUI.canvas.elements.reverse.filter(_.hit(point)).headOption
        if (underCursor.isEmpty) {
            Mode.selected.clear()
        } 
        underCursor
    }
}

class Remove extends Operation {
    var element: Option[Element] = None
    
    def press(point: Point): Unit = {
        element = Select.get(point)
        element.foreach(GUI.canvas.removeElement)
    }
    
    def drag(point: Point): Unit = {}
    
    def release(): Unit = {
        Mode.operation = new Remove
    }
    def undo(): Unit = {
        element.foreach(GUI.canvas.addElement)
    }
    
    def redo(): Unit = {
        element.foreach(GUI.canvas.removeElement)
    }
    
}

/**
  * Makes shapes by dragging, 
  */
trait Maker extends Operation {
    var element: Option[Shape] = None

    def makeShape: Shape
    def reset(): Unit 
    def press(point: Point) {
        element = Some(makeShape)
        GUI.canvas.addElement(element.get)
        GUI.canvas.pushHistory(this)
    }

    def drag(point: Point): Unit = { 
        element.foreach(_.setPoint(point))        
    }
    def release(): Unit = reset()
    def undo() = GUI.canvas.removeElement(element.get)
    def redo() = GUI.canvas.addElement(element.get)
}

class MakeRect extends Maker {
    def makeShape: Shape = new Rectangle
    def reset(): Unit = {
        Mode.operation = new MakeRect
    }
}
class MakeOval extends Maker {
    def makeShape: Shape = new Oval
    def reset(): Unit = {
        Mode.operation = new MakeOval
    }
}
class MakeLine extends Maker {
    def makeShape: Shape = new Line
    def reset(): Unit = {
        Mode.operation = new MakeLine
    }
}
class MakeFreehand extends Maker {
    def makeShape: Shape = new Freehand
    def reset(): Unit = {
        Mode.operation = new MakeFreehand
    }
}

class MakeText extends Operation {
    var element: Option[TextBox] = None
    
    def press(point: Point): Unit = {
        var text = ""
           
        new Frame {
            title = "Text"
            preferredSize = new Dimension(200, 70)
            val textField = new TextField    
            contents = new BoxPanel(scala.swing.Orientation.Horizontal) {
                contents += textField
                contents += new Button(Action("Done") {
                    text = textField.text
                    done()
                })
            }
            def done() = {
                textField.text
                make()
                close()
            }
            centerOnScreen()
            visible = true
        }

        def make() = {
            element = Some(new TextBox)
            element.foreach(_.setPoint(point))
            element.foreach(_.text = text)
            GUI.canvas.addElement(element.get)
            GUI.canvas.pushHistory(this)
            GUI.canvas.repaint()
            Mode.operation = new MakeText
        }
        
    }
    
    def drag(point: Point): Unit = {}
    
    def release(): Unit = {}
    
    def undo(): Unit = {GUI.canvas.removeElement(element.get)}
    
    def redo(): Unit = {GUI.canvas.addElement(element.get)}
    
}

// class MakeGroup extends Operation {
//     var elements: Buffer[Element] = Buffer()


//     def press(point: Point): Unit = {
//         Select.get(point).foreach(elements += _)
//     }
    
//     def drag(point: Point): Unit = ???
    
//     def release(): Unit = ???
    
//     def undo(): Unit = ???
    
//     def redo(): Unit = ???
    
// }

/** Subclasses Rotate and Scale, Translate is not linear, 
 * and can be better implemented without storing the transformation discretely in Element.  
  */ 
trait LinearTransformation extends Operation {
    var deltax = 0
    var deltay = 0
    val transformation: Transformation
    var elements: Buffer[Element] = Buffer()
    var start: Option[Point] = None
    
    def press(point: Point): Unit = {
        val underCursor = Select.get(point)
        if (underCursor.isEmpty) return

        if (Mode.selected.contains(underCursor.get)) {
            elements = Mode.selected
        } else {
            elements += underCursor.get
        }
    }

    def release(): Unit
    def drag(point: Point): Unit
    def undo(): Unit = elements.foreach(_.transformations -= transformation)
    def redo(): Unit = elements.foreach(_.transformations += transformation)

}
class Rotate extends LinearTransformation {
    val transformation: Rotation = new Rotation
    
    def centerOfRotation = {
        val x = elements.map(_.center.x).sum / elements.size 
        val y = elements.map(_.center.y).sum / elements.size
        new Point(x, y)
    }

    def release(): Unit = {
        Mode.operation = new Rotate
    }

    def drag(point: Point): Unit = {
        if (this.elements.nonEmpty) {
            if (start.isEmpty) {
                start = Some(point)
                elements.foreach(_.transformations += transformation)
                GUI.canvas.pushHistory(this)
            } else {
                val dx = point.x - start.get.x
                val dy = point.y - start.get.y
                start = Some(point)
                deltax += dx
                deltay += dy
                // transformation.theta = 0.01 * (start.get.x - point.x)
                transformation.theta += 0.01 * dx
            }
        }
    }
}

class Scale extends LinearTransformation {
    val transformation: Scaling = new Scaling
    
    def release(): Unit = {
        Mode.operation = new Scale
    }
    def drag(point: Point): Unit = {
        if (this.elements.nonEmpty) {
            if (start.isEmpty) {
                start = Some(point)
                elements.foreach(_.transformations += transformation)
                GUI.canvas.pushHistory(this)
            } else {
                transformation.sx = 0.005 * (start.get.x - point.x) + 1.0
                transformation.sy = 0.005 * (start.get.y - point.y) + 1.0
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
        this.element = Select.get(point)
    }

    def release(): Unit = {
        Mode.operation = new Translate
    }
    def drag(point: Point): Unit = {
        if (this.element.isDefined) {
            if (start.isEmpty) {
                start = Some(point)
                GUI.canvas.pushHistory(this)
            } else {
                val dx = point.x - start.get.x
                val dy = point.y - start.get.y
                start = Some(point)
                deltax += dx
                deltay += dy
                element.foreach(_.move(dx, dy))
            }
        }
    }
    def undo(): Unit = element.foreach(_.move(-deltax, -deltay))
    def redo(): Unit = element.foreach(_.move(deltax, deltay))

}
