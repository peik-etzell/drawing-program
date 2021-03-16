package draw
import scala.swing._

object GUI extends SimpleSwingApplication {
    def top = new MainFrame {
        title = "Drawing Program"

        
        menuBar = new MenuBar {
            contents += new Menu("File") {
                 
            }
            contents += new Button(Action("Quit") {
                sys.exit()
            })
        }
        val gridPanel = new GridPanel(1, 2) {
            contents += Canvas
        }
        contents = gridPanel
        size = new Dimension(500, 300)
    }


}