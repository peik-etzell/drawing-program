package draw

import java.awt.Point
import java.awt.Color._
import scala.util.Random
import scala.swing._
import java.awt.Dimension
import scala.swing.event._
import scala.collection.mutable.Buffer

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
            Canvas.selected.clear()
        } else if (Canvas.selected.contains(underMouse.head)) {
            Canvas.selected -= underMouse.head
        } else {
            Canvas.selected += underMouse.head
        }
    }
    def drag(point: Point): Unit = {}
    def release(): Unit = {}
    def undo() = {}
    def redo() = {}

    def get(point: Point): Option[Element] = {
        val underCursor = Canvas.elements.reverse.filter(_.hit(point)).headOption
        if (underCursor.isEmpty) {
            Canvas.selected.clear()
        } 
        underCursor
    }
}

class Remove extends Operation {
    var element: Option[Element] = None
    
    def press(point: Point): Unit = {
        element = Select.get(point)
        element.foreach(Canvas.removeElement)
        Canvas.pushHistory(this)
    }
    
    def drag(point: Point): Unit = {}
    
    def release(): Unit = {
        Canvas.operation = new Remove
    }
    def undo(): Unit = {
        element.foreach(Canvas.addElement)
    }
    
    def redo(): Unit = {
        element.foreach(Canvas.removeElement)
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
        Canvas.addElement(element.get)
        Canvas.pushHistory(this)
        Canvas.selected.clear()
    }

    def drag(point: Point): Unit = { 
        element.foreach(_.setPoint(point))        
    }
    def release(): Unit = reset()
    def undo() = Canvas.removeElement(element.get)
    def redo() = Canvas.addElement(element.get)
}

class MakeRect extends Maker {
    def makeShape: Shape = new Rectangle
    def reset(): Unit = {
        Canvas.operation = new MakeRect
    }
}
class MakeOval extends Maker {
    def makeShape: Shape = new Oval
    def reset(): Unit = {
        Canvas.operation = new MakeOval
    }
}
class MakeLine extends Maker {
    def makeShape: Shape = new Line
    def reset(): Unit = {
        Canvas.operation = new MakeLine
    }
}
class MakeFreehand extends Maker {
    def makeShape: Shape = new Freehand
    def reset(): Unit = {
        Canvas.operation = new MakeFreehand
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
            Canvas.addElement(element.get)
            Canvas.pushHistory(this)
            Canvas.selected.clear()
            Canvas.repaint()
            Canvas.operation = new MakeText
        }
        
    }
    
    def drag(point: Point): Unit = {}
    
    def release(): Unit = {}
    
    def undo(): Unit = {Canvas.removeElement(element.get)}
    
    def redo(): Unit = {Canvas.addElement(element.get)}
    
}

/** Subclasses Rotate and Scale, Translate is not linear, 
 * and can be better implemented without storing the transformation discretely in Element.  
  */ 
trait LinearTransformation extends Operation {
    val transformation: Transformation
    var element: Option[Element] = None
    var start: Option[Point] = None
    
    def press(point: Point): Unit = {
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
        Canvas.operation = new Rotate
    }

    def drag(point: Point): Unit = {
        if (this.element.isDefined) {
            if (start.isEmpty) {
                start = Some(point)
                element.foreach(_.transformations += transformation)
                Canvas.pushHistory(this)
            } else {
                transformation.theta = 0.01 * (start.get.x - point.x)
            }
        }
    }
}

class Scale extends LinearTransformation {
    val transformation: Scaling = new Scaling
    
    def release(): Unit = {
        Canvas.operation = new Scale
    }
    def drag(point: Point): Unit = {
        if (this.element.nonEmpty) {
            if (start.isEmpty) {
                start = Some(point)
                element.foreach(_.transformations += transformation)
                Canvas.pushHistory(this)
            } else {
                transformation.sx = 0.005 * (start.get.x - point.x) + 1.0
                transformation.sy = 0.005 * (start.get.y - point.y) + 1.0
            }
        }
    }
}

class Translate extends Operation {
    private var deltax = 0
    private var deltay = 0
    private var elements: Buffer[Element] = Buffer()
    private var start: Option[Point] = None
    
    def press(point: Point): Unit = {
        val underCursor = Select.get(point)
        if (underCursor.isEmpty) return

        if (Canvas.selected.contains(underCursor.get)) {
            elements = Canvas.selected
        } else {
            elements += underCursor.get
            Canvas.selected.clear()
        }
    }

    def release(): Unit = {
        Canvas.operation = new Translate
    }
    def drag(point: Point): Unit = {
        if (this.elements.nonEmpty) {
            if (start.isEmpty) {
                start = Some(point)
                Canvas.pushHistory(this)
            } else {
                val dx = point.x - start.get.x
                val dy = point.y - start.get.y
                start = Some(point)
                deltax += dx
                deltay += dy
                elements.foreach(_.move(dx, dy))
            }
        }
    }
    def undo(): Unit = elements.foreach(_.move(-deltax, -deltay))
    def redo(): Unit = elements.foreach(_.move(deltax, deltay))

}
