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
    var fontSize = 25
    var backgroundColor = white
    var selected: Option[Element] = None
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
        Mode.selected = this.get(point)
    }
    def drag(point: Point): Unit = {}
    def release(): Unit = {}
    def undo() = {}
    def redo() = {}

    def get(point: Point) = {
        GUI.canvas.elements.reverse.filter(_.hit(point)).headOption
    }
}

/**
  * Makes shapes by dragging, 
  */
trait Maker extends Operation {
    var element: Option[Shape] = None

    def makeShape(point: Point): Shape
    def reset(): Unit 
    def press(point: Point) {}

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
            element = Some(new TextBox(point, text))
            GUI.canvas.addElement(element.get)
            GUI.canvas.pushHistory(this)
            GUI.canvas.repaint()
            Mode.operation = new MakeText
        }
        
    }
    
    def drag(point: Point): Unit = {}
    
    def release(): Unit = {}
    
    def undo(): Unit = {}
    
    def redo(): Unit = {}
    
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
    val transformation: Transformation
    var element: Option[Element] = None
    var start: Option[Point] = None
    
    def press(point: Point): Unit = {
        val underCursor = Select.get(point)
        // if (underCursor = Mode.selected) {
        //     element = 
        // }

        element = Select.get(point)
    }

    def release(): Unit
    def drag(point: Point): Unit
    def undo(): Unit = element.foreach(_.transformations -= transformation)
    def redo(): Unit = element.foreach(_.transformations += transformation)

}
class Rotate extends LinearTransformation {
    val transformation: Rotation = new Rotation
    
    def release(): Unit = {
        Mode.operation = new Rotate
    }
    def drag(point: Point): Unit = {
        if (this.element.isDefined) {
            if (start.isEmpty) {
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

class Scale extends LinearTransformation {
    val transformation: Scaling = new Scaling
    
    def release(): Unit = {
        Mode.operation = new Scale
    }
    def drag(point: Point): Unit = {
        if (this.element.isDefined) {
            if (start.isEmpty) {
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
                GUI.canvas.repaint()
            }
        }
    }
    def undo(): Unit = element.foreach(_.move(-deltax, -deltay))
    def redo(): Unit = element.foreach(_.move(deltax, deltay))

}
