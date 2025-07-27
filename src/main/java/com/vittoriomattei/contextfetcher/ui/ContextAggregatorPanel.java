package com.vittoriomattei.contextfetcher.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.vittoriomattei.contextfetcher.model.FileContextItem;
import com.vittoriomattei.contextfetcher.model.FileEntry;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;
import com.vittoriomattei.contextfetcher.services.FilesChangeListener;
import com.vittoriomattei.contextfetcher.model.LineRange;
import com.vittoriomattei.contextfetcher.ui.panel.FileListPanel;
import com.vittoriomattei.contextfetcher.ui.panel.PreviewPanel;
import com.vittoriomattei.contextfetcher.ui.panel.ToolbarPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ContextAggregatorPanel extends JPanel implements Disposable {

    private final Project project;
    private final FileAggregatorService fileService;

    // UI Components
    private final ToolbarPanel toolbarPanel;
    private final FileListPanel fileListPanel;
    private final PreviewPanel previewPanel;

    // Data and listeners
    private final FilesChangeListener filesChangeListener = this::refreshFileList;

    public ContextAggregatorPanel(Project project, FileAggregatorService fileService) {
        super(new BorderLayout());
        this.project = project;
        this.fileService = fileService;

        // Initialize UI components
        this.toolbarPanel = new ToolbarPanel(project, fileService);
        this.fileListPanel = new FileListPanel(
                new ArrayList<>(),
                fileService,
                this::handleJumpToSource,
                this::handleRemoveItem
        );
        this.previewPanel = new PreviewPanel(project, fileService);

        setupComponents();
        layoutComponents();
        setupListeners();

        // Initial data load
        refreshFileList();
    }

    private void setupComponents() {
        // Set up toolbar panel callback
        toolbarPanel.setContextGenerationListener(previewPanel::generateContext);
        toolbarPanel.setRemoveFilesListener(fileListPanel::removeFiles);

        // Configure file list panel
        fileListPanel.setBorder(BorderFactory.createTitledBorder("Context Files"));

        // Configure preview panel
        previewPanel.setBorder(BorderFactory.createTitledBorder("Generated Context"));
    }

    private void layoutComponents() {
        // Create main content area with split pane
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setTopComponent(fileListPanel);
        mainSplitPane.setBottomComponent(previewPanel);
        mainSplitPane.setDividerLocation(300); // Initial size
        mainSplitPane.setResizeWeight(0.6); // Give more space to file list initially

        // Layout main panel
        add(toolbarPanel, BorderLayout.NORTH);
        add(mainSplitPane, BorderLayout.CENTER);
    }

    private void setupListeners() {
        // Register change listener for file service
        fileService.addChangeListener(filesChangeListener);
    }

    private void handleJumpToSource(FileContextItem item) {
        VirtualFile file = item.getVirtualFile();
        int line = 0; // Default to line 0 (the first line)

        if (item.getLineRange() != null) {
            // OpenFileDescriptor is 0-based, so subtract 1
            line = item.getLineRange().startLine() - 1;
        }

        // Navigate to the file and line, and activate the editor
        new OpenFileDescriptor(project, file, line, 0).navigate(true);
    }

    private void handleRemoveItem(FileContextItem item) {
        if (item.getLineRange() == null) {
            // Remove entire file
            fileService.removeFile(item.getVirtualFile());
        } else {
            // Remove specific snippet
            LineRange range = item.getLineRange();
            fileService.removeSnippet(
                    item.getVirtualFile(),
                    range
            );
        }
        // The file change listener will automatically call refreshFileList()
    }

    private void refreshFileList() {
        SwingUtilities.invokeLater(() -> {
            List<FileContextItem> items = new ArrayList<>();

            for (FileEntry item : fileService.getFileEntries()) {
                Set<LineRange> snippets = item.getSnippets();
                if (snippets.isEmpty()) {
                    items.add(new FileContextItem(item.getVirtualFile(), null));
                } else {
                    for (LineRange snippet : snippets) {
                        items.add(new FileContextItem(item.getVirtualFile(), snippet));
                    }
                }
            }

            fileListPanel.setItems(items);

            // Clear preview if no items
            if (items.isEmpty()) {
                previewPanel.clearPreview();
            }
        });
    }

    @Override
    public void dispose() {
        fileService.removeChangeListener(filesChangeListener);
    }
}