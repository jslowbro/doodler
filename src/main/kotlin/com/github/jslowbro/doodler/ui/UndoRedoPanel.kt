package com.github.jslowbro.doodler.ui

import com.intellij.icons.AllIcons
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.AbstractAction
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.KeyStroke

class UndoRedoPanel(canvas: DoodleCanvas, shortcutRoot: JComponent) {

    private val panel: JPanel = JPanel()
    private val undoButton: JButton = JButton(AllIcons.Actions.Undo)
    private val redoButton: JButton = JButton(AllIcons.Actions.Redo)

    init {
        undoButton.toolTipText = "Undo last action (Cmd+Z)"
        redoButton.toolTipText = "Redo last action (Cmd+Shift+Z)"

        undoButton.addActionListener {
            canvas.undo()
            updateButtons(canvas)
        }

        redoButton.addActionListener {
            canvas.redo()
            updateButtons(canvas)
        }

        panel.add(undoButton)
        panel.add(redoButton)

        canvas.setHistoryListener {
            updateButtons(canvas)
        }
        updateButtons(canvas)
        registerShortcuts(shortcutRoot, canvas)
    }

    fun getPanel(): JPanel = panel

    private fun updateButtons(canvas: DoodleCanvas) {
        undoButton.isEnabled = canvas.canUndo()
        redoButton.isEnabled = canvas.canRedo()
    }

    private fun registerShortcuts(root: JComponent, canvas: DoodleCanvas) {
        val undoKey = "doodler.undo"
        val redoKey = "doodler.redo"

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK),
            undoKey
        )
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK),
            redoKey
        )
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK),
            undoKey
        )
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK),
            redoKey
        )

        root.actionMap.put(undoKey, object : AbstractAction() {
            override fun actionPerformed(event: java.awt.event.ActionEvent?) {
                canvas.undo()
                updateButtons(canvas)
            }
        })

        root.actionMap.put(redoKey, object : AbstractAction() {
            override fun actionPerformed(event: java.awt.event.ActionEvent?) {
                canvas.redo()
                updateButtons(canvas)
            }
        })
    }
}
