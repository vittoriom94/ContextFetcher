package com.vittoriomattei.contextfetcher.ui.panel;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBEmptyBorder;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.function.Consumer;

public class RecentFilesPopupPanel extends JPanel {
    public RecentFilesPopupPanel(List<VirtualFile> recentFiles, Consumer<VirtualFile> onFileSelected){
        super(new BorderLayout(5, 5));
        setBorder(new JBEmptyBorder(8));

        DefaultListModel<VirtualFile> listModel = new DefaultListModel<>();
        recentFiles.forEach(listModel::addElement);

        JBList<VirtualFile> fileList = new JBList<>(listModel);
        setupFileListRenderer(fileList);

        // TextField for filtering
        var searchField = new JBTextField();
        searchField.getEmptyText().setText("Search recent files...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = searchField.getText().toLowerCase();
                listModel.clear();
                recentFiles.stream()
                        .filter(file -> file.getName().toLowerCase().contains(query))
                        .forEach(listModel::addElement);
            }
        });

        // The mouse listener now uses the callback to report selections.
        fileList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                VirtualFile selected = fileList.getSelectedValue();
                if (selected != null) {
                    onFileSelected.accept(selected);
                }
            }
        });

        add(searchField, BorderLayout.NORTH);
        add(new JBScrollPane(fileList), BorderLayout.CENTER);
        setPreferredSize(new Dimension(350, 450));
    }

    private void setupFileListRenderer(JBList<VirtualFile> fileList) {
        fileList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value.getName(), value.getFileType().getIcon(), JLabel.LEFT);
            label.setBorder(JBUI.Borders.empty(2, 4));
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
                label.setOpaque(true);
            }
            return label;
        });
    }
}
