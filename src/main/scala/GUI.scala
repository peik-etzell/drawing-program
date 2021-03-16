import scala.swing._

object GUI extends SimpleSwingApplication {
    def top = new MainFrame {
        title = "Drawing Program"

        size = new Dimension(500, 300)
    }


}