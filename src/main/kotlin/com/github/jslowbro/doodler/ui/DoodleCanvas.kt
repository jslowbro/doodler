package com.github.jslowbro.doodler.ui

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.RenderingHints
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D
import java.util.ArrayDeque
import javax.swing.JComponent
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class DoodleCanvas : JComponent() {

    private var currentStroke: Stroke? = null
    private var currentShape: ShapeItem? = null
    private var shapeStart: java.awt.Point? = null
    private val strokes = mutableListOf<Stroke>()
    private val shapes = mutableListOf<ShapeItem>()
    private val undoStack = ArrayDeque<CanvasState>()
    private val redoStack = ArrayDeque<CanvasState>()
    private var historyListener: (() -> Unit)? = null

    private var tool: Tool = Tool.PEN
    private var penColor: Color = Color(0, 0, 0)
    private var penWidth: Float = 3f

    init {
        isOpaque = true
        background = Color(255, 255, 255)

        val mouseHandler = object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                when (tool) {
                    Tool.PEN -> {
                        currentStroke = Stroke(Path2D.Float(), penColor, penWidth).apply {
                            path.moveTo(e.x.toDouble(), e.y.toDouble())
                        }
                    }
                    else -> {
                        shapeStart = e.point
                        currentShape = buildShape(tool, e.point, e.point)
                    }
                }
            }

            override fun mouseDragged(e: MouseEvent) {
                when (tool) {
                    Tool.PEN -> {
                        val stroke = currentStroke
                        if (stroke != null) {
                            stroke.path.lineTo(e.x.toDouble(), e.y.toDouble())
                            repaint()
                        }
                    }
                    else -> {
                        val start = shapeStart
                        if (start != null) {
                            currentShape = buildShape(tool, start, e.point)
                            repaint()
                        }
                    }
                }
            }

            override fun mouseReleased(e: MouseEvent) {
                when (tool) {
                    Tool.PEN -> {
                        val stroke = currentStroke
                        if (stroke != null) {
                            snapshotForUndo()
                            strokes.add(stroke)
                            currentStroke = null
                            notifyHistoryChanged()
                            repaint()
                        }
                    }
                    else -> {
                        val shape = currentShape
                        if (shape != null) {
                            snapshotForUndo()
                            shapes.add(shape)
                            currentShape = null
                            shapeStart = null
                            notifyHistoryChanged()
                            repaint()
                        }
                    }
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
        if (strokes.isEmpty() && shapes.isEmpty()) {
            return
        }
        snapshotForUndo()
        strokes.clear()
        shapes.clear()
        currentStroke = null
        currentShape = null
        notifyHistoryChanged()
        repaint()
    }

    fun setTool(tool: Tool) {
        this.tool = tool
    }

    fun setPenColor(color: Color) {
        penColor = color
    }

    fun undo() {
        if (undoStack.isEmpty()) {
            return
        }
        redoStack.addLast(copyState())
        val previous = undoStack.removeLast()
        strokes.clear()
        strokes.addAll(previous.strokes)
        shapes.clear()
        shapes.addAll(previous.shapes)
        currentStroke = null
        currentShape = null
        notifyHistoryChanged()
        repaint()
    }

    fun redo() {
        if (redoStack.isEmpty()) {
            return
        }
        undoStack.addLast(copyState())
        val next = redoStack.removeLast()
        strokes.clear()
        strokes.addAll(next.strokes)
        shapes.clear()
        shapes.addAll(next.shapes)
        currentStroke = null
        currentShape = null
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

        for (shape in shapes) {
            g2.color = shape.color
            g2.stroke = BasicStroke(shape.width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.draw(shape.toShape())
        }

        val active = currentStroke
        if (active != null) {
            g2.color = active.color
            g2.stroke = BasicStroke(active.width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.draw(active.path)
        }

        val draft = currentShape
        if (draft != null) {
            g2.color = draft.color
            g2.stroke = BasicStroke(draft.width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.draw(draft.toShape())
        }
    }

    private fun snapshotForUndo() {
        undoStack.addLast(copyState())
        redoStack.clear()
    }

    private fun copyState(): CanvasState {
        return CanvasState(
            strokes = strokes.map { it.copy() },
            shapes = shapes.map { it.copy() },
        )
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

    private data class ShapeItem(
        val type: Tool,
        val bounds: Rectangle2D,
        val color: Color,
        val width: Float,
    ) {
        fun copy(): ShapeItem {
            return ShapeItem(type, Rectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height), color, width)
        }

        fun toShape(): java.awt.Shape {
            return when (type) {
                Tool.RECTANGLE -> Rectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height)
                Tool.CIRCLE -> {
                    val size = min(bounds.width, bounds.height)
                    Ellipse2D.Double(bounds.x, bounds.y, size, size)
                }
                Tool.TRIANGLE -> {
                    val path = Path2D.Double()
                    val x = bounds.x
                    val y = bounds.y
                    val w = bounds.width
                    val h = bounds.height
                    path.moveTo(x + w / 2.0, y)
                    path.lineTo(x + w, y + h)
                    path.lineTo(x, y + h)
                    path.closePath()
                    path
                }
                Tool.HEXAGON -> {
                    val path = Path2D.Double()
                    val cx = bounds.x + bounds.width / 2.0
                    val cy = bounds.y + bounds.height / 2.0
                    val r = min(bounds.width, bounds.height) / 2.0
                    for (i in 0 until 6) {
                        val angle = Math.toRadians(60.0 * i - 30.0)
                        val px = cx + r * cos(angle)
                        val py = cy + r * sin(angle)
                        if (i == 0) {
                            path.moveTo(px, py)
                        } else {
                            path.lineTo(px, py)
                        }
                    }
                    path.closePath()
                    path
                }
                else -> Rectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height)
            }
        }
    }

    private fun buildShape(tool: Tool, start: java.awt.Point, end: java.awt.Point): ShapeItem {
        val x = min(start.x, end.x).toDouble()
        val y = min(start.y, end.y).toDouble()
        val w = abs(start.x - end.x).toDouble()
        val h = abs(start.y - end.y).toDouble()
        val bounds = Rectangle2D.Double(x, y, maxOf(1.0, w), maxOf(1.0, h))
        return ShapeItem(tool, bounds, penColor, penWidth)
    }

    private data class CanvasState(
        val strokes: List<Stroke>,
        val shapes: List<ShapeItem>,
    )

    enum class Tool {
        PEN,
        RECTANGLE,
        CIRCLE,
        TRIANGLE,
        HEXAGON,
    }
}
