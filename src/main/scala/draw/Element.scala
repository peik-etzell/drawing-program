package draw
import scala.swing._
import java.awt.Color._
import scala.collection.mutable.Buffer
import java.awt.BasicStroke

trait Transformation
case class Rotation(var theta: Double = 0.0) extends Transformation
case class Scaling(var sx: Double = 1.0, var sy: Double = 1.0) extends Transformation

trait Element {
    var color: Color = Mode.color
    var strokeSize = Mode.stroke
    val transformations: Buffer[Transformation] = Buffer()

    def points: Seq[Point]
    def center: Point 
    def move(dx: Int, dy: Int) = this.points.foreach(_.translate(dx, dy))

    def render(g: Graphics2D)

    def draw(g: Graphics2D) = {
        g.setColor(color)
        g.setStroke(new BasicStroke(strokeSize))
       
        g.translate(center.x, center.y)
        for (t <- transformations.reverse) {
            t match {
                case Rotation(theta) => {
                    g.rotate(theta)
                }
                case Scaling(sx, sy) => {
                    g.scale(sx, sy)
                }
            }
        }
        g.translate(-center.x, -center.y)

        this.render(g)
        
        g.translate(center.x, center.y)
        for (t <- transformations) {
            t match {
                case Rotation(theta) => {
                    g.rotate(-theta)
                }
                case Scaling(sx, sy) => {
                    g.scale(1/sx, 1/sy)
                }
            }
        }
        g.translate(-center.x, -center.y)
    }

    def hit(point: Point): Boolean = {
        import math._
        
        var x: Double = point.x - center.x
        var y: Double = point.y - center.y
        
        for (t <- transformations.reverse) {
            t match {
                case Rotation(theta) => {
                    val tempx = x * cos(theta) + y * sin(theta)
                    val tempy = y * cos(theta) + x * sin(-theta)
                    x = tempx
                    
                    y = tempy
                }
                case Scaling(sx, sy) => {
                    x /= sx
                    y /= sy
                }
            }
        }
        val xInt = x.round.toInt + center.x
        val yInt = y.round.toInt + center.y
        inside(new Point(xInt, yInt))
    }
    def inside(point: Point): Boolean
}

abstract class Shape(var start: Point) extends Element {
    var end: Point = start
    var fill = Mode.fill
    def setPoint(point: Point) = {
        end = point
    }

    def points: Seq[Point] = Seq(start, end)
}

class Oval(start: Point) extends Shape(start) {
    def center = start
    
    def r: Int = start.distance(end).floor.toInt
    def inside(point: Point): Boolean = center.distance(point) <= r
    def render(g: Graphics2D) = {
        if (fill) {
            g.fillOval(start.x - r, start.y - r, 2 * r, 2 * r)
        } else {
            g.drawOval(start.x - r, start.y - r, 2 * r, 2 * r)
        }
    }
}

class Rectangle(start: Point) extends Shape(start) {
    import math._

    private def width = (start.x - end.x).abs
    private def height = (start.y - end.y).abs
    
    private def topx = min(start.x, end.x)
    private def topy = min(start.y, end.y)

    def center = {
        new Point((start.x + end.x) / 2, (start.y + end.y) / 2)
    }
    
    def inside(point: Point): Boolean = {
        point.x >= topx && 
        point.y >= topy && 
        point.x <= topx + width && 
        point.y <= topy + height
    }
    
    def render(g: Graphics2D) = {
        if (fill) {
            g.fillRect(topx, topy, width, height)
        } else {
            g.drawRect(topx, topy, width, height)
        }   
    }
}

class Line(start: Point) extends Shape(start) {
    def center = new Point((start.x + end.x) / 2, (start.y + end.y) / 2)
    def render(g: Graphics2D) = g.drawLine(start.x, start.y, end.x, end.y)
    def inside(point: Point): Boolean = {
        val radius = 10
        point.distance(start) + point.distance(end) <= start.distance(end) + radius
    }
}

class TextBox(start: Point, var text: String) extends Element {
    var size: Int = Mode.fontSize
    
    def center: Point = start
    var points: Seq[Point] = Seq(start)
    def render(g: Graphics2D) = {
        g.setFont(g.getFont().deriveFont(size.toFloat))
        g.drawString(text, start.x, start.y)
    }
    def inside(point: Point): Boolean = ???
}

class Grouping extends Element { 
    private val elements = Buffer[Element]()
    var points: Seq[Point] = Seq()

    def center: Point = {
        // val mid = elements.map(elem => (elem.center.x, elem.center.y)).reduce((sum, p) => (sum._1 + p._1, sum._2 + p._2))
        val x = elements.map(_.center.x).sum / elements.size
        val y = elements.map(_.center.y).sum / elements.size
        // new Point(mid._1 / elements.size, mid._2 / elements.size)
        new Point(x, y)
    }
    
    def addElement(element: Element) = {
        elements += element
        points = points ++ element.points
    }
    def inside(point: Point): Boolean = ???

    def render(g: Graphics2D) = {
        for (element <- elements) {
            element.draw(g)
        }
    }
}

class Freehand(start: Point) extends Shape(start) {
    private val line: Buffer[Point] = Buffer(start)
    def center = {
        val x = points.map(_.x).sum / points.size
        val y = points.map(_.y).sum / points.size
        new Point(x, y)
    }
    
    override def setPoint(point: Point) = {
        line += point
    }

    def inside(point: Point): Boolean = {
        line.exists(p => p.distance(point) <= 10)
    }

    override def points: Seq[Point] = line.toSeq

    this.transformations += new Rotation(10)

    def render(g: Graphics2D) = {
        val pointPairs = {
            points.dropRight(1) zip points.tail
        }
        for (pair <- pointPairs) {
            val start = pair._1
            val end = pair._2
            g.drawLine(start.x, start.y, end.x, end.y)
        }
    }
}

object Debug extends Element {
    def center: Point = new Point(0, 0)
    def points: Seq[Point] = ???
    def inside(point: Point): Boolean = false
    
    def printout = f"""
        color: ${Mode.color}
        operation: ${Mode.operation}
        fill: ${Mode.fill}
        selected: ${Mode.selected}
        """  
    
    def render(g: Graphics2D) = {
        g.setFont(g.getFont().deriveFont(15.toFloat))
        
        var height = 0
        val lineSpacing = 15

        for (line <- printout.linesIterator) {
            height += lineSpacing
            g.drawString(line, 10, height)
        }
    }
}