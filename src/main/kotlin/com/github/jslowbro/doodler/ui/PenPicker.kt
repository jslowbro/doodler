package com.github.jslowbro.doodler.ui

import java.awt.Component
import java.awt.Graphics
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JMenuItem
import javax.swing.JPopupMenu

class PenPicker(canvas: DoodleCanvas) {

    private val button: JButton
    private val menu: JPopupMenu

    init {
        button = JButton()
        menu = JPopupMenu()

        button.toolTipText = "Pick pen size"
        button.addActionListener { menu.show(button, 0, button.height) }

        val smallIcon = PenSizeIcon(2f)
        val mediumIcon = PenSizeIcon(4f)
        val largeIcon = PenSizeIcon(7f)

        button.icon = mediumIcon
        canvas.setPenWidth(4f)

        val small = JMenuItem("Small", smallIcon)
        val medium = JMenuItem("Medium", mediumIcon)
        val large = JMenuItem("Large", largeIcon)

        small.addActionListener {
            canvas.setTool(DoodleCanvas.Tool.PEN)
            canvas.setPenWidth(2f)
            button.icon = smallIcon
        }
        medium.addActionListener {
            canvas.setTool(DoodleCanvas.Tool.PEN)
            canvas.setPenWidth(4f)
            button.icon = mediumIcon
        }
        large.addActionListener {
            canvas.setTool(DoodleCanvas.Tool.PEN)
            canvas.setPenWidth(7f)
            button.icon = largeIcon
        }

        menu.add(small)
        menu.add(medium)
        menu.add(large)
    }

    fun getButton(): JButton = button

    private class PenSizeIcon(private val width: Float) : Icon {
        override fun getIconWidth() = 16
        override fun getIconHeight() = 16

        override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
            val g2 = g.create()
            if (g2 is java.awt.Graphics2D) {
                g2.stroke = java.awt.BasicStroke(width, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND)
                g2.color = java.awt.Color(90, 90, 90)
                val cy = y + iconHeight / 2
                g2.drawLine(x + 2, cy, x + iconWidth - 3, cy)
            }
            g2.dispose()
        }
    }
}
