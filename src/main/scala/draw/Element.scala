package draw
import scala.swing._
import java.awt.Color._

abstract class Element(var start: Point) {

    def rotate() = ???
    def translate(xdir: Int, ydir: Int) = {
        this.start.translate(xdir, ydir)
    }
    def scale(factor: Double) = {
        ???
    }

    def draw(g: Graphics2D)
    def getBounds() = ???
}

abstract class Shape(start: Point, var color: Color = Preferences.color) extends Element(start) {
    var end: Point = start
}

class Oval(start: Point) extends Shape(start) {
    def draw(g: Graphics2D) = {
        
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
        g.fillRect(x, y, dx, dy)
    }
}

class Line(start: Point) extends Shape(start) {
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
