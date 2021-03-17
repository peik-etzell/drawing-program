package draw
import scala.swing._
import scala.swing.BorderPanel.Position._
import java.awt.Color._

object GUI extends SimpleSwingApplication {
    def top = new MainFrame {
        title = "Drawing Program"

        
        menuBar = new MenuBar {
            contents += new Menu("File") {
                 contents += new MenuItem(Action("Open") {
                    // TODO
                 })
                 contents += new MenuItem(Action("Save") {
                    // TODO
                 })
            }
            
        }
        val buttonsPanel = new BoxPanel(Orientation.Horizontal) {
            //preferredSize = new Dimension(500, 50)
            background = BLUE
            contents += new Button(Action("QUIT") {
                sys.exit()
            })
        }
        contents = new BorderPanel {
            layout(Canvas) = Center
            layout(buttonsPanel) = North
        }
        size = new Dimension(500, 300)
    }


}