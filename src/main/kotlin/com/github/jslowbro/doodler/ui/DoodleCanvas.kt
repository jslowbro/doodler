package com.github.jslowbro.doodler.ui

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import javax.swing.JComponent
import kotlin.math.max

class DoodleCanvas : JComponent() {

    private var surface: BufferedImage? = null
    private var lastPoint: Point? = null

    var penColor: Color = Color(0, 0, 0)
    var penWidth: Float = 3f

    init {
        isOpaque = true
        background = Color(255, 255, 255)

        val mouseHandler = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                lastPoint = e.point
                ensureSurface()
            }

            override fun mouseDragged(e: MouseEvent) {
                val from = lastPoint ?: e.point
                val to = e.point
                drawLine(from, to)
                lastPoint = to
            }

            override fun mouseReleased(e: MouseEvent) {
                lastPoint = null
            }
        }

        addMouseListener(mouseHandler)
        addMouseMotionListener(mouseHandler)

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                ensureSurface(resizeIfNeeded = true)
                repaint()
            }
        })
    }

    fun clear() {
        ensureSurface()
        val image = surface ?: return
        val g2 = image.createGraphics()
        g2.color = background
        g2.fillRect(0, 0, image.width, image.height)
        g2.dispose()
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        ensureSurface()
        val image = surface ?: return
        g.drawImage(image, 0, 0, null)
    }

    private fun ensureSurface(resizeIfNeeded: Boolean = false) {
        val width = max(1, this.width)
        val height = max(1, this.height)
        val current = surface

        if (current == null) {
            surface = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            clear()
            return
        }

        if (!resizeIfNeeded || (current.width == width && current.height == height)) {
            return
        }

        val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2 = resized.createGraphics()
        g2.color = background
        g2.fillRect(0, 0, width, height)
        g2.drawImage(current, 0, 0, null)
        g2.dispose()
        surface = resized
    }

    private fun drawLine(from: Point, to: Point) {
        ensureSurface()
        val image = surface ?: return
        val g2 = image.createGraphics()
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = penColor
        g2.stroke = BasicStroke(penWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g2.drawLine(from.x, from.y, to.x, to.y)
        g2.dispose()
        repaint()
    }
}
