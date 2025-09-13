package com.vittoriomattei.contextfetcher.ui.panel;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.vittoriomattei.contextfetcher.model.FileContextItem;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;
import com.vittoriomattei.contextfetcher.services.FilesChangeListener;
import com.vittoriomattei.contextfetcher.ui.renderer.FileContextItemRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class FileListPanel extends JPanel implements FilesChangeListener, Disposable {
    private final JBList<FileContextItem> fileListComponent;
    private final DefaultListModel<FileContextItem> fileListModel;
    private final FileAggregatorService fileService;

    public FileListPanel(Project project, FileAggregatorService fileService) {
        super(new BorderLayout());
        this.fileService = fileService;

        this.fileListModel = new DefaultListModel<>();
        var fileList = fileService.getAllItems();
        fileService.addChangeListener(this);
        fileListModel.addAll(fileList);
        this.fileListComponent = new JBList<>(fileListModel);
        this.fileListComponent.setCellRenderer(new FileContextItemRenderer());
        MouseListener doubleClickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int clickedComponent = fileListComponent.locationToIndex(e.getPoint());
                    if (clickedComponent == -1) {
                        return;
                    }
                    var clickedFile = fileListModel.getElementAt(clickedComponent);
                    List<FileContextItem> fileToRemove = new ArrayList<>();
                    fileToRemove.add(clickedFile);
                    fileService.removeFiles(fileToRemove);
                }
                if (e.getClickCount() < 2) {
                    return;
                }
                int clickedComponent = fileListComponent.locationToIndex(e.getPoint());
                if (clickedComponent == -1) {
                    return;
                }
                var clickedFile = fileListModel.getElementAt(clickedComponent);
                var line = clickedFile.isSnippet() ? clickedFile.getLineRange().startLine() : -1;
                OpenFileDescriptor ofd = new OpenFileDescriptor(project, clickedFile.getVirtualFile(), line, -1);
                ofd.navigate(true);
            }
        };
        this.fileListComponent.addMouseListener(
                doubleClickListener

        );

        add(this.fileListComponent);
    }

    @Override
    public void onFilesChanged() {
        this.fileListModel.clear();
        this.fileListModel.addAll(this.fileService.getSortedItems());
    }

    public List<FileContextItem> getSelectedFileList() {
        return this.fileListComponent.getSelectedValuesList();
    }

    @Override
    public void dispose() {
        this.fileService.removeChangeListener(this);
    }
}