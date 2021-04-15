package draw

import scala.swing._
import scala.swing.BorderPanel.Position._
import java.awt.Color._
import scala.collection.mutable.Buffer
import javax.swing.UIManager    
import scala.swing.event.ButtonClicked

object GUI extends SimpleSwingApplication {

    try {
        val systemLook = UIManager.getSystemLookAndFeelClassName()
        UIManager.setLookAndFeel(systemLook)
    } catch {
        case e: Exception => {
            println("Error using your system UI style, using 'Nimbus'\n" + e)
            val defaultLook = new javax.swing.plaf.nimbus.NimbusLookAndFeel
            UIManager.setLookAndFeel(defaultLook)
        }
    }

    val canvas = new Canvas // TODO: Some handling for background canvases after opening new ones on top

    val mainFrame = new MainFrame {
        title = "Drawing Program"

        val toggle = new ToggleButton("Fill Off")

        listenTo(toggle)

        reactions += {
            case ButtonClicked(component) if component == toggle => {
                toggle.text = if (toggle.selected) {
                    Mode.fill = true; "Fill On"
                 } else {
                    Mode.fill = false; "Fill Off"
                }
            }
        }   

        menuBar = new MenuBar {
            contents += new Menu("File") {
                 contents += new MenuItem(Action("Open") {
                    FileManager.loadDrawing()
                 })
                 contents += new MenuItem(Action("Save") {
                    FileManager.saveDrawing() // Not functional
                 })
            }
            contents += new Menu("Shapes") {
                contents ++= Seq(
                    new MenuItem(Action("Oval") {Mode.operation = new MakeOval}),
                    new MenuItem(Action("Rectangle") {Mode.operation = new MakeRect}),
                    new MenuItem(Action("Line") {Mode.operation = new MakeLine}),
                    new MenuItem(Action("Freehand") {Mode.operation = new MakeFreehand}),
                    new MenuItem(Action("TextBox") {Mode.operation = new MakeText})
                )
            }
            contents += new Menu("Color") {
                contents ++= Seq(
                    new MenuItem(Action("Black") {Mode.color = black}),
                    new MenuItem(Action("White") {Mode.color = white}),
                    new MenuItem(Action("Blue") {Mode.color = blue}),
                    new MenuItem(Action("Yellow") {Mode.color = yellow}),
                    new MenuItem(Action("Red") {Mode.color = red}),
                    new MenuItem(Action("Gray") {Mode.color = gray})
                )
            }

            contents += toggle

            contents += new Button(Action("Clear") {
                canvas.clear()
            })
            contents += new Button(Action("Undo") {
                canvas.undo()
            })
            contents += new Button(Action("Redo") {
                canvas.redo()
            })
            contents += new Button(Action("Translate") {
                Mode.operation = new Translate
                canvas.repaint()
            })
            contents += new Button(Action("Rotate") {
                Mode.operation = new Rotate
                canvas.repaint()
            })
            contents += new Button(Action("Scale") {
                Mode.operation = new Scale
                canvas.repaint()
            })
            contents += new Button(Action("Select") {
                Mode.operation = Select
                canvas.repaint()
            })
        }

        // val buttonsPanel = new BoxPanel(Orientation.Horizontal) {
        //     //preferredSize = new Dimension(500, 50)
        //     background = BLUE
        //     contents += new Button(Action("Quit") {
        //         sys.exit()
        //     })
        //     contents += new Button(Action("Clear") {
                
        //     })
        // }
        contents = new BorderPanel {
            layout(canvas) = Center
            
        }
        this.centerOnScreen()
    }
    

    def top = mainFrame

}