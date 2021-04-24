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

    val canvas = new Canvas

    val mainFrame = new MainFrame {
        val fillToggle = new ToggleButton("Fill Off")
        
        listenTo(fillToggle)


        reactions += {
            case ButtonClicked(component) if component == fillToggle => {
                fillToggle.text = if (fillToggle.selected) {
                    Mode.fill = true; "Fill On"
                } else {
                    Mode.fill = false; "Fill Off"
                }
                canvas.repaint()
            }
        }   


        menuBar = new MenuBar {
            contents ++= Seq(
                new Menu("File") {
                    contents ++= Seq(
                        FileManager.open(),
                        new MenuItem(Action("Save") {
                            FileManager.save()
                        })
                    )
                },
                new Menu("Shapes") {
                    contents ++= Seq(
                        new MenuItem(Action("Oval")         {Mode.operation = new MakeOval}),
                        new MenuItem(Action("Rectangle")    {Mode.operation = new MakeRect}),
                        new MenuItem(Action("Line")         {Mode.operation = new MakeLine}),
                        new MenuItem(Action("Freehand")     {Mode.operation = new MakeFreehand}),
                        new MenuItem(Action("TextBox")      {Mode.operation = new MakeText})
                    )
                },
                new Menu("Color") {
                    contents ++= Seq(
                        new MenuItem(Action("Black")        {Mode.color = black}),
                        new MenuItem(Action("White")        {Mode.color = white}),
                        new MenuItem(Action("Blue")         {Mode.color = blue}),
                        new MenuItem(Action("Yellow")       {Mode.color = yellow}),
                        new MenuItem(Action("Red")          {Mode.color = red}),
                        new MenuItem(Action("Gray")         {Mode.color = gray})
                    )
                },
                fillToggle,
                new Button(Action("Remove")                 {Mode.operation = new Remove}),
                new Button(Action("Undo")                   {canvas.undo()}),
                new Button(Action("Redo")                   {canvas.redo()}),
                new Button(Action("Translate")              {Mode.operation = new Translate; canvas.repaint()}),
                new Button(Action("Rotate") {
                    Mode.operation = new Rotate
                    canvas.repaint()
                }),
                new Button(Action("Scale") {
                    Mode.operation = new Scale
                    canvas.repaint()
                }),
                new Button(Action("Select") {
                    Mode.operation = Select
                    canvas.repaint()
                })
            )
        }

        contents = canvas
        this.centerOnScreen()
    }
    
    def top = mainFrame

}