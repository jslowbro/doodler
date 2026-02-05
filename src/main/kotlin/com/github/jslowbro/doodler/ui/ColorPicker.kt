package com.github.jslowbro.doodler.ui

import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Graphics
import java.awt.GridLayout
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JPopupMenu
import javax.swing.JPanel

class ColorPicker(canvas: DoodleCanvas, colors: Array<Color>) {

    private val button: JButton
    private val menu: JPopupMenu
    private var currentColor: Color = colors[0]

    init {
        button = JButton()
        menu = JPopupMenu()

        button.toolTipText = "Pick pen color"
        button.icon = ColorIcon(currentColor)
        canvas.setPenColor(currentColor)

        val palette = JPanel(GridLayout(4, 4, 4, 4)).apply { border = null }
        for (color in colors) {
            val swatch = JButton()
            swatch.preferredSize = Dimension(18, 18)
            swatch.icon = ColorIcon(color, 14)
            swatch.isBorderPainted = false
            swatch.isFocusPainted = false
            swatch.isContentAreaFilled = false
            swatch.toolTipText = "Set pen color"
            swatch.addActionListener {
                currentColor = color
                canvas.setPenColor(color)
                button.icon = ColorIcon(color)
                menu.isVisible = false
            }
            palette.add(swatch)
        }

        menu.add(palette)
        button.addActionListener { menu.show(button, 0, button.height) }
    }

    fun getButton(): JButton = button

    private class ColorIcon(
        private val color: Color,
        private val size: Int = 14,
    ) : Icon {

        override fun getIconWidth() = size

        override fun getIconHeight() = size

        override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
            g.color = color
            g.fillRect(x, y, size, size)
        }
    }
}
