package draw
import scala.swing._

abstract class Element {
    private var size: (Int, Int) = ???
    private var position: (Int, Int) = ???
    
    def rotate() = ???
    def translate() = ???
    def scale() = ???
}

abstract class Shape extends Element

class Oval extends Shape
class Rectangle extends Shape
class Line extends Shape

class TextBox extends Element

class Grouping extends Element {
    private val elements = ???
}
