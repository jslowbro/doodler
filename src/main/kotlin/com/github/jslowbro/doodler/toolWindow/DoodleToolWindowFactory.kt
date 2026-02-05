package com.github.jslowbro.doodler.toolWindow

import com.github.jslowbro.doodler.ui.DoodleCanvas
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel

class DoodleToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel(BorderLayout())
        val canvas = DoodleCanvas()

        val toolbar = JPanel(BorderLayout())
        val clearButton = JButton("Clear").apply {
            toolTipText = "Clear the canvas"
            addActionListener { canvas.clear() }
        }
        toolbar.add(clearButton, BorderLayout.WEST)

        panel.add(toolbar, BorderLayout.NORTH)
        panel.add(canvas, BorderLayout.CENTER)

        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true
}
