package draw

import java.awt.Dimension
import scala.swing._
import java.awt.Point
import java.awt.Color._


object Tester extends App {
    println("Tester started")
    GUI.startup(Array())

    val width = 1920
    val height = 1080

    GUI.mainFrame.size = new Dimension(1920, 1080)

    // Line 1
    var x = 50
    var y = 50
    def oper = Mode.operation
    Mode.operation = new MakeRect
    Thread.sleep(500)

    def drag(from: Point, to: Point) = {
        oper.press(from)
        oper.drag(from)
        oper.drag(to)
        oper.release()
    }
    def drag(through: Seq[Point]) = {
        oper.press(through.head)
        for (p <- through) {
            oper.drag(p)
        }
        oper.release()
    }

    for {
        n <- 50 to 1000 by 80
    } {
        drag(new Point(n, 50), new Point(n + 50, 100))
        Thread.sleep(150)
    }

    Mode.operation = new Rotate
    for {
        i <- 1 to 100
        n <- 75 to 1025 by 80
    } {
        drag(new Point(n, 75), new Point(n + 10, 75))
        Thread.sleep(10)
    }

    Mode.operation = new Scale


}
