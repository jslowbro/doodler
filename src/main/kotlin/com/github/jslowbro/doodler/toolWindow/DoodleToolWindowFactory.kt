package com.github.jslowbro.doodler.toolWindow

import com.github.jslowbro.doodler.ui.ColorPicker
import com.github.jslowbro.doodler.ui.DoodleCanvas
import com.github.jslowbro.doodler.ui.PenPicker
import com.github.jslowbro.doodler.ui.ShapePicker
import com.github.jslowbro.doodler.ui.UndoRedoPanel
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JButton
import javax.swing.JPanel

class DoodleToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel(BorderLayout())
        val canvas = DoodleCanvas()

        val toolbar = JPanel(BorderLayout())

        val colors = arrayOf(
            Color(0, 0, 0),
            Color(96, 96, 96),
            Color(160, 160, 160),
            Color(255, 255, 255),
            Color(255, 0, 0),
            Color(255, 128, 0),
            Color(255, 200, 0),
            Color(0, 200, 0),
            Color(0, 160, 96),
            Color(0, 160, 200),
            Color(0, 96, 200),
            Color(0, 0, 255),
            Color(96, 0, 200),
            Color(160, 0, 160),
            Color(200, 0, 96),
            Color(200, 0, 0),
        )

        val clearButton = JButton(AllIcons.Actions.GC).apply {
            toolTipText = "Clear the canvas"
            addActionListener { canvas.clear() }
        }

        canvas.setTool(DoodleCanvas.Tool.PEN)

        val undoRedoPanel = UndoRedoPanel(canvas, panel)
        val colorPicker = ColorPicker(canvas, colors)
        val penPicker = PenPicker(canvas)
        val shapePicker = ShapePicker(canvas)

        val leftControls = JPanel().apply {
            add(undoRedoPanel.getPanel())
            add(colorPicker.getButton())
            add(penPicker.getButton())
            add(shapePicker.getButton())
        }

        toolbar.add(leftControls, BorderLayout.WEST)
        toolbar.add(clearButton, BorderLayout.EAST)

        panel.add(toolbar, BorderLayout.NORTH)
        panel.add(canvas, BorderLayout.CENTER)

        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true
}
