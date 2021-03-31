package draw

import scala.swing._
import scala.swing.BorderPanel.Position._
import java.awt.Color._
import scala.swing.event.{MouseClicked, MouseMoved, MouseDragged}
import scala.collection.mutable.Buffer
import javax.swing.UIManager
import scala.swing.event.ButtonClicked

object GUI extends SimpleSwingApplication {
    
    val defaultLook = new javax.swing.plaf.nimbus.NimbusLookAndFeel
    UIManager.setLookAndFeel(defaultLook)

    // try {
    //     val systemLook = UIManager.getSystemLookAndFeelClassName()
    //     UIManager.setLookAndFeel(systemLook)
    // } catch {
    //     case e: Exception => println("Error using your system UI style, using 'Nimbus'\n" + e)
    // }

    var operation: Operation = makeRect
    val canvas = new Canvas // TODO: Some handling for background canvases after opening new ones on top
    var color = black
    
    val mainFrame = new MainFrame {
        title = "Drawing Program"
        
        contents = canvas

        size = new Dimension(800, 600)
        val toggle = new ToggleButton("Fill Off")


        listenTo(canvas.mouse.moves)
        listenTo(canvas.mouse.clicks)
        listenTo(toggle)

        reactions += {
            case MouseMoved(_, point, _) => {
                Preferences.operation.move(point)
            }
            case MouseClicked(_, point, _, _, _) => {
                Preferences.operation.click(point)
            }
            case ButtonClicked(component) if component == toggle => {
                toggle.text = if (toggle.selected) {
                    Preferences.fill = true; "Fill On"
                 } else {
                    Preferences.fill = false; "Fill Off"
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
                    Preferences.operation = makeOval
                })
                contents += new MenuItem(Action("Rectangle") {
                    Preferences.operation = makeRect
                })
                contents += new MenuItem(Action("Line") {
                    Preferences.operation = makeLine
                })
                contents += new MenuItem(Action("Freehand") {
                    ???
                })
            }
            contents += new Menu("Color") {
                contents += new MenuItem(Action("Black") {
                    Preferences.color = black
                    
                })
                contents += new MenuItem(Action("White") {
                    Preferences.color = white
                })
                contents += new MenuItem(Action("Blue") {
                    Preferences.color = blue
                })
                contents += new MenuItem(Action("Yellow") {
                    Preferences.color = yellow
                })
                contents += new MenuItem(Action("Red") {
                    Preferences.color = red
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