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

    val mainFrame = new MainFrame {
        val fillToggle = new ToggleButton("Fill Off")

        listenTo(fillToggle)

        reactions += {
            case ButtonClicked(component) if component == fillToggle => {
                if (Canvas.selected.nonEmpty) {
                    Canvas.selected.foreach(elem => elem.fill = !elem.fill)
                    Canvas.repaint()
                } else {

                
                    fillToggle.text = if (fillToggle.selected) {
                        Canvas.fill = true; "Fill On"
                    } else {
                        Canvas.fill = false; "Fill Off"
                    }
                    Canvas.repaint()
                }
            }
        }   

        menuBar = new MenuBar {
            contents ++= Seq(
                new Menu("File") {
                    contents ++= Seq(
                        FileManager.fileMenu,
                        new MenuItem(Action("Save") {
                            FileManager.save()
                        }),
                        new MenuItem(Action("Close") {
                            Canvas.close()
                        })
                    )
                },
                new Menu("Shapes") {
                    contents ++= Seq(
                        new MenuItem(Action("Oval")         {Canvas.operation = new MakeOval}),
                        new MenuItem(Action("Rectangle")    {Canvas.operation = new MakeRect}),
                        new MenuItem(Action("Line")         {Canvas.operation = new MakeLine}),
                        new MenuItem(Action("Freehand")     {Canvas.operation = new MakeFreehand}),
                        new MenuItem(Action("TextBox")      {Canvas.operation = new MakeText})
                    )
                },
                
                new Menu("Operations") {
                    contents ++= Seq(
                        new MenuItem(Action("Translate")    {Canvas.operation = new Translate; Canvas.repaint()}),
                        new MenuItem(Action("Rotate")       {Canvas.operation = new Rotate; Canvas.repaint()}),
                        new MenuItem(Action("Scale")        {Canvas.operation = new Scale; Canvas.repaint()})
                    )
                },
                new Menu("Attributes") {
                    contents ++= Seq(
                        new BoxPanel(Orientation.Horizontal) {    
                            val label = new Label("Stroke: [15]  ")
                            def update(ds: Int) = {
                                if (Canvas.selected.nonEmpty) {
                                    Canvas.selected.foreach(_.strokeSize += ds)
                                    Canvas.repaint()
                                } else {
                                    Canvas.stroke += ds
                                    label.text = f"Stroke: [${Canvas.stroke}]  "
                                }
                                
                                Canvas.stroke += ds
                            }
                            this.contents += label
                            this.contents += new Button(Action("-") {update(-1)})
                            this.contents += new Button(Action("+") {update(1)})
                        },
                        fillToggle,
                        new Menu("Color") {
                            def update(color: Color) {
                                if (Canvas.selected.nonEmpty) {
                                    Canvas.selected.foreach(_.color = color)
                                    Canvas.repaint()
                                } else {
                                    Canvas.color = color
                                }
                            }
                            contents ++= Seq(
                                new MenuItem(Action("Black")        {update(black)}),
                                new MenuItem(Action("White")        {update(white)}),
                                new MenuItem(Action("Blue")         {update(blue)}),
                                new MenuItem(Action("Yellow")       {update(yellow)}),
                                new MenuItem(Action("Red")          {update(red)}),
                                new MenuItem(Action("Green")        {update(green)})
                            )
                        },

                    )
                },
                new Button(Action("Remove")                 {Canvas.operation = new Remove}),
                new Button(Action("Undo")                   {Canvas.undo()}),
                new Button(Action("Redo")                   {Canvas.redo()}),
                new Button(Action("Select") {
                    Canvas.operation = Select
                    Canvas.repaint()
                })

            )
        }


        contents = Canvas
        this.centerOnScreen()
    }
    
    def top = mainFrame

}