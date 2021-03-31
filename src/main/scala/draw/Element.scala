package draw
import scala.swing._
import java.awt.Color._

abstract class Element(var start: Point) {
    def center: Point = ???
    def rotate() = ???
    def translate(xdir: Int, ydir: Int) = {
        this.start.translate(xdir, ydir)
    }
    def scale(factor: Double) = {
        ???
    }

    def draw(g: Graphics2D)
    def hit(point: Point): Boolean = ???
}

abstract class Shape(start: Point) extends Element(start) {
    var color: Color = Mode.color
    var end: Point = start
    var fill = Mode.fill
}

class Oval(start: Point) extends Shape(start) {
    override def center = start
    def radius: Int = start.distance(end).floor.toInt

    def draw(g: Graphics2D) = {
        g.setColor(color)
        if (fill) {
            g.fillOval(start.x - radius / 2, start.y - radius / 2, radius, radius)
        } else {
            g.drawOval(start.x - radius / 2, start.y - radius / 2, radius, radius)
        }
    }
}


class Rectangle(start: Point) extends Shape(start) {
    private def dx = (start.x - end.x).abs
    private def dy = (start.y - end.y).abs
    
    def draw(g: Graphics2D): Unit = {
        import math._
        val x = min(start.x, end.x)
        val y = min(start.y, end.y)
        
        g.setColor(color)
        if (fill) {
            g.fillRect(x, y, dx, dy)
        } else {
            g.drawRect(x, y, dx, dy)
        }   
    }
}

class Line(start: Point) extends Shape(start) {
    override def center = {
        new Point((start.x + end.x) / 2, (start.y + end.y) / 2)
    }
    
    def draw(g: Graphics2D) = {
        g.setColor(color)
        g.drawLine(start.x, start.y, end.x, end.y)
    }
}

class TextBox(start: Point) extends Element(start) {
    def draw(g: Graphics2D) = ???
}

class Grouping(start: Point) extends Element(start) {
    private val elements = ???

    def draw(g: Graphics2D) = {
        
    }
}

class Freehand(start: Point)
