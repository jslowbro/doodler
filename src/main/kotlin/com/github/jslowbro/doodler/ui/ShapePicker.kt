package com.github.jslowbro.doodler.ui

import com.intellij.openapi.util.IconLoader
import javax.swing.JButton
import javax.swing.JMenuItem
import javax.swing.JPopupMenu

class ShapePicker(canvas: DoodleCanvas) {

    private val button: JButton
    private val menu: JPopupMenu

    init {
        button = JButton("Shapes")
        menu = JPopupMenu()

        button.toolTipText = "Pick shape tool"

        val rectItem = JMenuItem("Rectangle", IconLoader.getIcon("/icons/shape-rectangle.svg", ShapePicker::class.java))
        val circleItem = JMenuItem("Circle", IconLoader.getIcon("/icons/shape-circle.svg", ShapePicker::class.java))
        val triangleItem = JMenuItem("Triangle", IconLoader.getIcon("/icons/shape-triangle.svg", ShapePicker::class.java))
        val hexItem = JMenuItem("Hexagon", IconLoader.getIcon("/icons/shape-hexagon.svg", ShapePicker::class.java))

        rectItem.addActionListener { canvas.setTool(DoodleCanvas.Tool.RECTANGLE) }
        circleItem.addActionListener { canvas.setTool(DoodleCanvas.Tool.CIRCLE) }
        triangleItem.addActionListener { canvas.setTool(DoodleCanvas.Tool.TRIANGLE) }
        hexItem.addActionListener { canvas.setTool(DoodleCanvas.Tool.HEXAGON) }

        menu.add(rectItem)
        menu.add(circleItem)
        menu.add(triangleItem)
        menu.add(hexItem)

        button.addActionListener { menu.show(button, 0, button.height) }
    }

    fun getButton(): JButton = button
}
