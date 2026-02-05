package com.github.jslowbro.doodler.ui

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.RenderingHints
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.Path2D
import java.util.ArrayDeque
import javax.swing.JComponent

class DoodleCanvas : JComponent() {

    private var currentStroke: Stroke? = null
    private val strokes = mutableListOf<Stroke>()
    private val undoStack = ArrayDeque<List<Stroke>>()
    private val redoStack = ArrayDeque<List<Stroke>>()
    private var historyListener: (() -> Unit)? = null

    private var penColor: Color = Color(0, 0, 0)
    private var penWidth: Float = 3f

    init {
        isOpaque = true
        background = Color(255, 255, 255)

        val mouseHandler = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                currentStroke = Stroke(Path2D.Float(), penColor, penWidth).apply {
                    path.moveTo(e.x.toDouble(), e.y.toDouble())
                }
            }

            override fun mouseDragged(e: MouseEvent) {
                val stroke = currentStroke
                if (stroke != null) {
                    stroke.path.lineTo(e.x.toDouble(), e.y.toDouble())
                    repaint()
                }
            }

            override fun mouseReleased(e: MouseEvent) {
                val stroke = currentStroke
                if (stroke != null) {
                    snapshotForUndo()
                    strokes.add(stroke)
                    currentStroke = null
                    notifyHistoryChanged()
                    repaint()
                }
            }
        }

        addMouseListener(mouseHandler)
        addMouseMotionListener(mouseHandler)

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                repaint()
            }
        })
    }

    fun clear() {
        if (strokes.isEmpty()) {
            return
        }
        snapshotForUndo()
        strokes.clear()
        currentStroke = null
        notifyHistoryChanged()
        repaint()
    }

    fun setPenColor(color: Color) {
        penColor = color
    }

    fun undo() {
        if (undoStack.isEmpty()) {
            return
        }
        redoStack.addLast(copyStrokes())
        val previous = undoStack.removeLast()
        strokes.clear()
        strokes.addAll(previous)
        currentStroke = null
        notifyHistoryChanged()
        repaint()
    }

    fun redo() {
        if (redoStack.isEmpty()) {
            return
        }
        undoStack.addLast(copyStrokes())
        val next = redoStack.removeLast()
        strokes.clear()
        strokes.addAll(next)
        currentStroke = null
        notifyHistoryChanged()
        repaint()
    }

    fun canUndo() = undoStack.isNotEmpty()

    fun canRedo() = redoStack.isNotEmpty()

    fun setHistoryListener(listener: (() -> Unit)?) {
        historyListener = listener
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as? java.awt.Graphics2D ?: return
        g2.color = background
        g2.fillRect(0, 0, width, height)

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        for (stroke in strokes) {
            g2.color = stroke.color
            g2.stroke = BasicStroke(stroke.width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.draw(stroke.path)
        }

        val active = currentStroke
        if (active != null) {
            g2.color = active.color
            g2.stroke = BasicStroke(active.width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.draw(active.path)
        }
    }

    private fun snapshotForUndo() {
        undoStack.addLast(copyStrokes())
        redoStack.clear()
    }

    private fun copyStrokes(): List<Stroke> {
        return strokes.map { it.copy() }
    }

    private fun notifyHistoryChanged() {
        historyListener?.invoke()
    }

    private data class Stroke(
        val path: Path2D,
        val color: Color,
        val width: Float,
    ) {
        fun copy(): Stroke {
            val cloned = path.clone() as Path2D
            return Stroke(cloned, color, width)
        }
    }
}
