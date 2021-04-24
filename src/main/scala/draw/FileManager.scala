package draw

import scala.collection.mutable.Buffer
import java.io._
import java.awt.Color
import scala.swing._
import java.awt.Dimension

object FileManager {
    val saveDirectory = "./saves/"

    def save() = {
        var fileName = ""
        
        new Frame {
            title = "Filename"
            preferredSize = new Dimension(200, 70)
            val textField = new TextField 
            contents = new BoxPanel(scala.swing.Orientation.Horizontal) {
                contents += textField
                contents += new Button(Action("Save") {
                    if (!textField.text.isBlank()) {
                        fileName = textField.text
                        if (!fileName.endsWith(".txt")) {
                            fileName += ".txt"
                        }
                        write()
                        close()
                    }
                })
            }
            centerOnScreen()
            visible = true
        }

        def write() = {
            try {
                val file = new File(saveDirectory + fileName)
                val lineWriter = new BufferedWriter(new FileWriter(file))

                try {
                    for (e <- GUI.canvas.elements) {                
                        lineWriter.write(e match {
                            case _: Rectangle   => "REC;"
                            case _: Oval        => "OVL;"
                            case _: Line        => "LNE;"
                            case _: Freehand    => "FRH;"
                            case tbx: TextBox     => f"TBX;${tbx.text};" // Quite an ugly solution; adds nonstandard textelement
                            case _ => throw new Exception("Unknown element")
                        })
                        for (p <- e.points) {
                            lineWriter.write(f"${p.x}-${p.y};")
                        }
                        val c = e.color
                        val (r, g, b) = (c.getRed(), c.getGreen(), c.getBlue())
                        lineWriter.write(f"CLR;$r:$g:$b;")
                        lineWriter.write(f"STK;${e.strokeSize};")
                        lineWriter.write(f"FIL;${e.fill};")
                        for (t <- e.transformations) {
                            lineWriter.write(t match {
                                case Rotation(theta) => f"ROT:$theta;"
                                case Scaling(sx, sy) => f"SCA:$sx:$sy;"
                            })
                        }
                        lineWriter.newLine()
                    }
                } finally {
                    lineWriter.close()
                }
            } catch {
                case e: IOException => {
                    println("There was an error writing the file")
                }
            }
        }
    }

    def open(): Menu = {
        val elements = Buffer[Element]()

        def read(file: File): Unit = {
            try {
                val fileReader = new FileReader(file)
                val lineReader = new BufferedReader(fileReader)

                try {
                    var inputLine = lineReader.readLine()
                    while (inputLine != null) {
                        val line = inputLine.split(';').toBuffer
                        // Buffer(elemTag, points, ...)
                        val element: Element = line.head match {
                            case "REC" => new Rectangle
                            case "OVL" => new Oval
                            case "LNE" => new Line
                            case "FRH" => new Freehand
                            case "TBX" => new TextBox
                            case _     => throw new Exception("Corrupted file")
                        }
                        line.trimStart(1) // Remove elemTag
                        element match {
                            case tbx: TextBox => tbx.text = line.head; line.trimStart(1)
                            case _ => 
                        }
                        val pointEnd = line.indexOf("CLR")
                        val pointData = line.take(pointEnd)
                        for (pp <- pointData) {
                            val temp = pp.split(':')
                            element.setPoint(new Point(temp(0).toInt, temp(1).toInt))
                        }
                        line.trimStart(pointEnd) // Remove pointData
                        // Buffer("CLR", r-g-b, "STK", Int, "FIL", Bool)
                        val (color, stroke, fill) = (line(1), line(3), line(5))
                        val splitColor = color.split(':').map(_.toInt)
                        val (r, g, b) = (splitColor(0), splitColor(1), splitColor(2))
                        element.color = new Color(r, g, b)
                        element.strokeSize = stroke.toInt
                        element.fill = fill.toBoolean

                        line.trimStart(6)

                        for (t <- line) {
                            val tfmn = t.split(':')
                            element.transformations += {
                                tfmn.head match {
                                    case "ROT" => {
                                        Rotation(tfmn(1).toDouble)
                                    }
                                    case "SCA" => {
                                        Scaling(tfmn(1).toDouble, tfmn(2).toDouble)
                                    }
                                }
                            }
                        }
                        elements += element
                        inputLine = lineReader.readLine()
                    }
                } finally {
                    fileReader.close()
                    lineReader.close()
                }
            } catch {
                case e: FileNotFoundException => {
                    println("No such file")
                }
                case e: IOException => {
                    println("There was an error")
                }
            }
        }
        new Menu("Open") {
            val currentDirectory = new File(saveDirectory)
            val listing = currentDirectory.listFiles()
            val items = {
                for {
                    file <- listing
                } yield {
                    val fileName = file.toString().drop(saveDirectory.length())
                    new MenuItem(Action(fileName) {
                        read(file)
                        GUI.canvas.open(elements)
                    }) 
                }
            }
            contents ++= items
        }
    }
}