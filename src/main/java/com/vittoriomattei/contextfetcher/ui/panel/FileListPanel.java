package com.vittoriomattei.contextfetcher.ui.panel;

import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.vittoriomattei.contextfetcher.model.FileContextItem;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;
import com.vittoriomattei.contextfetcher.ui.renderer.FileContextItemRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class FileListPanel extends JPanel {

    private DefaultListModel<FileContextItem> listModel;
    private JBList<FileContextItem> fileList;
    private FileAggregatorService fileService;
    private Consumer<FileContextItem> onJumpToSource;
    private Consumer<FileContextItem> onRemoveItem;

    public FileListPanel(
            List<FileContextItem> items,
            FileAggregatorService fileService,
            Consumer<FileContextItem> onJumpToSource,
            Consumer<FileContextItem> onRemoveItem
    ) {
        this.fileService = fileService;
        this.onJumpToSource = onJumpToSource;
        this.onRemoveItem = onRemoveItem;

        this.listModel = new DefaultListModel<>();
        items.forEach(listModel::addElement);

        this.fileList = new JBList<>(listModel);
        fileList.setCellRenderer(new FileContextItemRenderer());

        setLayout(new BorderLayout());
        add(new JBScrollPane(fileList), BorderLayout.CENTER);

        setupClickHandler();
    }


    public void setItems(List<FileContextItem> items) {
        listModel.clear();
        for (FileContextItem item : items) {
            listModel.addElement(item);
        }
    }


    private void setupClickHandler() {
        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = fileList.locationToIndex(e.getPoint());
                if (index < 0) return;

                Rectangle cellBounds = fileList.getCellBounds(index, index);
                if (cellBounds == null || !cellBounds.contains(e.getPoint())) return;

                FileContextItem item = listModel.getElementAt(index);

                // Calculate relative click position
                int relativeX = e.getX() - cellBounds.x;
                int cellWidth = cellBounds.width;

                // Approximate button positions based on the renderer layout
                // The icons are positioned on the right side of the cell
                int iconAreaWidth = 40; // Space for both icons
                int jumpIconStart = cellWidth - iconAreaWidth;
                int removeIconStart = cellWidth - 20;

                if (relativeX >= removeIconStart) {
                    // Clicked on remove icon
                    if (onRemoveItem != null) {
                        onRemoveItem.accept(item);
                    }
                } else if (relativeX >= jumpIconStart) {
                    // Clicked on jump icon
                    if (onJumpToSource != null) {
                        onJumpToSource.accept(item);
                    }
                } else if (e.getClickCount() == 2) {
                    // Double-click on the item itself - jump to source
                    if (onJumpToSource != null) {
                        onJumpToSource.accept(item);
                    }
                }
            }
        });
    }


    public void removeFiles() {
        List<FileContextItem> selectedItems = fileList.getSelectedValuesList();
        for (FileContextItem item : selectedItems) {
            if (item.isSnippet()) {
                assert item.getLineRange() != null;
                fileService.removeSnippet(item.getVirtualFile(), item.getLineRange());
            } else {
                fileService.removeFile(item.getVirtualFile());
            }
        }
    }

    public JBList<FileContextItem> getFileList() {
        return fileList;
    }
}