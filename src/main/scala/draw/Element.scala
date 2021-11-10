package draw
import scala.swing._
import java.awt.Color._
import scala.collection.mutable.Buffer
import java.awt.BasicStroke

trait Transformation
case class Rotation(var theta: Double = 0.0) extends Transformation
case class Scaling(var sx: Double = 1.0, var sy: Double = 1.0) extends Transformation

trait Element {
    val transformations: Buffer[Transformation] = Buffer()
    var color: Color = Canvas.color 
    var strokeSize: Int = Canvas.stroke
    var fill: Boolean = Canvas.fill

    def points: Seq[Point]
    def setPoint(point: Point)
    def center: Point 
    def move(dx: Int, dy: Int) = this.points.foreach(_.translate(dx, dy))

    def render(g: Graphics2D)

    def draw(g: Graphics2D) = {
        g.setColor(color)
        g.setStroke(new BasicStroke(strokeSize, 2, 0))
       
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

trait Shape extends Element {
    var points: Buffer[Point] = Buffer(new Point(0, 0), new Point(0, 0))
    private var started = false
    def setPoint(point: Point) = {
        if (started) {
            points(1) = point
        } else {
            started = true
            points(0) = point
            points(1) = point
        }
    }
}

class Oval extends Shape {
    def center = points(0)
    def r: Int = points(0).distance(points(1)).floor.toInt
    def inside(point: Point): Boolean = center.distance(point) <= r
    def render(g: Graphics2D) = {
        if (fill) {
            g.fillOval(points(0).x - r, points(0).y - r, 2 * r, 2 * r)
        } else {
            g.drawOval(points(0).x - r, points(0).y - r, 2 * r, 2 * r)
        }
    }
    override def toString(): String = f"Oval[${center.x}, ${center.y}]"
}

class Rectangle extends Shape {
    private def width = (points(0).x - points(1).x).abs
    private def height = (points(0).y - points(1).y).abs
    
    private def topx = points(0).x min points(1).x
    private def topy = points(0).y min points(1).y

    def center = {
        new Point((points(0).x + points(1).x) / 2, (points(1).y + points(0).y) / 2)
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

    override def toString(): String = f"Rectangle[${center.x}, ${center.y}]"
}

class Line extends Shape {
    def center = new Point((points(0).x + points(1).x) / 2, (points(0).y + points(1).y) / 2)
    def render(g: Graphics2D) = g.drawLine(points(0).x, points(0).y, points(1).x, points(1).y)
    def inside(point: Point): Boolean = {
        val radius = 10
        point.distance(points(0)) + point.distance(points(1)) <= points(0).distance(points(1)) + radius
    }

    override def toString(): String = f"Line[from=(${points(0).x}, ${points(0).y}) to=(${points(1).x}, ${points(1).y})]"
}

class Freehand extends Shape {
    points = Buffer()
    def center = {
        val x = points.map(_.x).sum / (points.size max 1)
        val y = points.map(_.y).sum / (points.size max 1)
        new Point(x, y)
    }
    override def setPoint(point: Point) = {
        points += point
    }
    def inside(point: Point): Boolean = {
        points.exists(p => p.distance(point) <= 15)
    }
    def render(g: Graphics2D) = {
        val pointPairs = {
            points.dropRight(1) zip points.drop(1)
        }
        for (pair <- pointPairs) {
            val start = pair._1
            val end = pair._2
            g.drawLine(start.x, start.y, end.x, end.y)
        }
    }

    override def toString(): String = "Freehand"
}

class TextBox extends Element {
    var center: Point = new Point(0, 0)
    var text: String = ""
    def points: Seq[Point] = Seq(center)

    private def width = text.length * 15 // Magic numbers to get it about right
    private def height = 10 
    
    
    def setPoint(point: Point): Unit = {
        center.setLocation(point)
    }
    
    
    def render(g: Graphics2D) = {
        g.drawString(text, center.x - width / 2, center.y + height / 2)
    }
    def inside(point: Point): Boolean = {
        point.x >= center.x - width / 2 && 
        point.y >= center.y - height / 2 && 
        point.x <= center.x + width / 2 && 
        point.y <= center.y + height / 2
    }

    override def toString(): String = f"TextBox['${text}']"
}

object Debug extends Element {
    def center: Point = new Point(0, 0)
    def points: Seq[Point] = ???
    def setPoint(point: Point): Unit = ???
    def inside(point: Point): Boolean = false
    
    def printout = f"""
        color:      ${Canvas.color}
        operation:  ${Canvas.operation}
        fill:       ${Canvas.fill}
        selected:   [${Canvas.selected.mkString(", ")}]
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