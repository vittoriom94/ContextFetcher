package com.vittoriomattei.contextfetcher.ui.renderer;

import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.vittoriomattei.contextfetcher.model.FileContextItem;

import javax.swing.*;
import java.awt.*;

public class FileContextItemRenderer implements ListCellRenderer<FileContextItem> {

    @Override
    public Component getListCellRendererComponent(
            JList<? extends FileContextItem> list,
            FileContextItem value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
        // Root panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(4, 8));
        panel.setOpaque(true);

        // Colors
        if (isSelected) {
            panel.setBackground(list.getSelectionBackground());
        } else {
            panel.setBackground(list.getBackground());
        }

        // Left side: File icon and name
        JPanel leftPanel = new JPanel(new BorderLayout(5, 0));
        leftPanel.setOpaque(false);

        // File type icon
        Icon fileIcon = FileTypeManager.getInstance().getFileTypeByFile(value.getVirtualFile()).getIcon();
        JLabel iconLabel = new JLabel(fileIcon);
        leftPanel.add(iconLabel, BorderLayout.WEST);

        // File name and line range
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        // Primary text (file name)
        JLabel nameLabel = new JLabel(value.getPresentableName());
        if (isSelected) {
            nameLabel.setForeground(list.getSelectionForeground());
        } else {
            nameLabel.setForeground(list.getForeground());
        }
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN));
        textPanel.add(nameLabel);

        // Secondary text (line range if snippet)
        if (value.isSnippet()) {
            String rangeText = "Lines " + (value.getLineRange().startLine() + 1) + "–" + (value.getLineRange().endLine() + 1);
            JLabel rangeLabel = new JLabel(rangeText);
            rangeLabel.setFont(rangeLabel.getFont().deriveFont(Font.ITALIC, 11f));
            rangeLabel.setForeground(JBColor.GRAY);
            textPanel.add(rangeLabel);
        }

        leftPanel.add(textPanel, BorderLayout.CENTER);
        panel.add(leftPanel, BorderLayout.CENTER);

        return panel;
    }
}