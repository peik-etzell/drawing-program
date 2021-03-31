package draw

import scala.swing._
import scala.swing.BorderPanel.Position._
import java.awt.Color._
import scala.collection.mutable.Buffer
import javax.swing.UIManager
import scala.swing.event.ButtonClicked

object GUI extends SimpleSwingApplication {
    
    val defaultLook = new javax.swing.plaf.nimbus.NimbusLookAndFeel
    UIManager.setLookAndFeel(defaultLook)

    try {
        val systemLook = UIManager.getSystemLookAndFeelClassName()
        UIManager.setLookAndFeel(systemLook)
    } catch {
        case e: Exception => println("Error using your system UI style, using 'Nimbus'\n" + e)
    }

    val canvas = new Canvas // TODO: Some handling for background canvases after opening new ones on top
    
    val mainFrame = new MainFrame {
        title = "Drawing Program"
        
        contents = canvas

        size = new Dimension(800, 600)
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
                contents += new MenuItem(Action("Oval") {
                    Mode.operation = MakeOval
                })
                contents += new MenuItem(Action("Rectangle") {
                    Mode.operation = MakeRect
                })
                contents += new MenuItem(Action("Line") {
                    Mode.operation = MakeLine
                })
                contents += new MenuItem(Action("Freehand") {
                    ???
                })
            }
            contents += new Menu("Color") {
                contents += new MenuItem(Action("Black") {
                    Mode.color = black
                })
                contents += new MenuItem(Action("White") {
                    Mode.color = white
                })
                contents += new MenuItem(Action("Blue") {
                    Mode.color = blue
                })
                contents += new MenuItem(Action("Yellow") {
                    Mode.color = yellow
                })
                contents += new MenuItem(Action("Red") {
                    Mode.color = red
                })
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
        
        // contents = new BorderPanel {
        //     layout(canvas) = Center
        //     // layout(buttonsPanel) = North
        // }

        
    }

    def top = mainFrame
}