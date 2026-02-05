package com.github.jslowbro.doodler.toolWindow;

import com.github.jslowbro.doodler.ui.DoodleCanvas;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

public class DoodleToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel panel = new JPanel(new BorderLayout());
        DoodleCanvas canvas = new DoodleCanvas();

        JPanel toolbar = new JPanel(new BorderLayout());
        JButton clearButton = new JButton("Clear");
        clearButton.setToolTipText("Clear the canvas");
        clearButton.addActionListener(event -> canvas.clear());
        toolbar.add(clearButton, BorderLayout.WEST);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(canvas, BorderLayout.CENTER);

        Content content = ContentFactory.getInstance().createContent(panel, null, false);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }
}
