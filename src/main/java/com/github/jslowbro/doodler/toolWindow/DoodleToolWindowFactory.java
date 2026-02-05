package com.github.jslowbro.doodler.toolWindow;

import com.github.jslowbro.doodler.ui.DoodleCanvas;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.JMenuItem;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

public class DoodleToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel panel = new JPanel(new BorderLayout());
        DoodleCanvas canvas = new DoodleCanvas();

        JPanel toolbar = new JPanel(new BorderLayout());

        JButton undoButton = new JButton(AllIcons.Actions.Undo);
        undoButton.setToolTipText("Undo last action (Cmd+Z)");

        JButton redoButton = new JButton(AllIcons.Actions.Redo);
        redoButton.setToolTipText("Redo last action (Cmd+Shift+Z)");

        Color[] colors = new Color[] {
            new Color(0, 0, 0),
            new Color(96, 96, 96),
            new Color(160, 160, 160),
            new Color(255, 255, 255),
            new Color(255, 0, 0),
            new Color(255, 128, 0),
            new Color(255, 200, 0),
            new Color(0, 200, 0),
            new Color(0, 160, 96),
            new Color(0, 160, 200),
            new Color(0, 96, 200),
            new Color(0, 0, 255),
            new Color(96, 0, 200),
            new Color(160, 0, 160),
            new Color(200, 0, 96),
            new Color(200, 0, 0),
        };

        JButton colorButton = new JButton("Color...");
        colorButton.setToolTipText("Pick pen color");
        Color[] currentColor = new Color[] { colors[0] };
        canvas.setPenColor(currentColor[0]);
        colorButton.setBorder(BorderFactory.createLineBorder(currentColor[0], 2));

        JButton clearButton = new JButton("Clear");
        clearButton.setToolTipText("Clear the canvas");
        clearButton.addActionListener(event -> {
            canvas.clear();
            updateButtons(undoButton, redoButton, canvas);
        });

        undoButton.addActionListener(event -> {
            canvas.undo();
            updateButtons(undoButton, redoButton, canvas);
        });

        redoButton.addActionListener(event -> {
            canvas.redo();
            updateButtons(undoButton, redoButton, canvas);
        });

        JPopupMenu colorMenu = new JPopupMenu();
        JPanel palette = new JPanel(new GridLayout(4, 4, 4, 4));
        palette.setBorder(null);
        for (Color color : colors) {
            JButton swatch = new JButton();
            swatch.setPreferredSize(new Dimension(18, 18));
            swatch.setBackground(color);
            swatch.setOpaque(true);
            swatch.setBorderPainted(false);
            swatch.setFocusPainted(false);
            swatch.setToolTipText("Set pen color");
            swatch.addActionListener(event -> {
                currentColor[0] = color;
                canvas.setPenColor(color);
                colorButton.setBorder(BorderFactory.createLineBorder(color, 2));
                colorMenu.setVisible(false);
            });
            palette.add(swatch);
        }
        colorMenu.add(palette);
        colorButton.addActionListener(event -> colorMenu.show(colorButton, 0, colorButton.getHeight()));

        JPanel leftControls = new JPanel();
        leftControls.add(undoButton);
        leftControls.add(redoButton);
        leftControls.add(colorButton);

        canvas.setTool(DoodleCanvas.Tool.PEN);

        JButton shapesButton = new JButton("Shapes");
        shapesButton.setToolTipText("Pick shape tool");
        JPopupMenu shapesMenu = new JPopupMenu();

        JMenuItem penItem = new JMenuItem("Pen", IconLoader.getIcon("/icons/shape-pen.svg", DoodleToolWindowFactory.class));
        JMenuItem rectItem = new JMenuItem("Rectangle", IconLoader.getIcon("/icons/shape-rectangle.svg", DoodleToolWindowFactory.class));
        JMenuItem circleItem = new JMenuItem("Circle", IconLoader.getIcon("/icons/shape-circle.svg", DoodleToolWindowFactory.class));
        JMenuItem triangleItem = new JMenuItem("Triangle", IconLoader.getIcon("/icons/shape-triangle.svg", DoodleToolWindowFactory.class));
        JMenuItem hexItem = new JMenuItem("Hexagon", IconLoader.getIcon("/icons/shape-hexagon.svg", DoodleToolWindowFactory.class));

        penItem.addActionListener(event -> canvas.setTool(DoodleCanvas.Tool.PEN));
        rectItem.addActionListener(event -> canvas.setTool(DoodleCanvas.Tool.RECTANGLE));
        circleItem.addActionListener(event -> canvas.setTool(DoodleCanvas.Tool.CIRCLE));
        triangleItem.addActionListener(event -> canvas.setTool(DoodleCanvas.Tool.TRIANGLE));
        hexItem.addActionListener(event -> canvas.setTool(DoodleCanvas.Tool.HEXAGON));

        shapesMenu.add(penItem);
        shapesMenu.add(rectItem);
        shapesMenu.add(circleItem);
        shapesMenu.add(triangleItem);
        shapesMenu.add(hexItem);

        shapesButton.addActionListener(event -> shapesMenu.show(shapesButton, 0, shapesButton.getHeight()));

        leftControls.add(shapesButton);
        toolbar.add(leftControls, BorderLayout.WEST);
        toolbar.add(clearButton, BorderLayout.EAST);

        canvas.setHistoryListener(() -> {
            updateButtons(undoButton, redoButton, canvas);
            return kotlin.Unit.INSTANCE;
        });
        updateButtons(undoButton, redoButton, canvas);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(canvas, BorderLayout.CENTER);

        registerUndoRedoShortcuts(panel, canvas, undoButton, redoButton);

        Content content = ContentFactory.getInstance().createContent(panel, null, false);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    private static void updateButtons(JButton undoButton, JButton redoButton, DoodleCanvas canvas) {
        undoButton.setEnabled(canvas.canUndo());
        redoButton.setEnabled(canvas.canRedo());
    }

    private static void registerUndoRedoShortcuts(
        JPanel panel,
        DoodleCanvas canvas,
        JButton undoButton,
        JButton redoButton
    ) {
        String undoKey = "doodler.undo";
        String redoKey = "doodler.redo";

        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK),
            undoKey
        );
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
            redoKey
        );
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK),
            undoKey
        );
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
            redoKey
        );

        panel.getActionMap().put(undoKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                canvas.undo();
                updateButtons(undoButton, redoButton, canvas);
            }
        });

        panel.getActionMap().put(redoKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                canvas.redo();
                updateButtons(undoButton, redoButton, canvas);
            }
        });
    }
}
