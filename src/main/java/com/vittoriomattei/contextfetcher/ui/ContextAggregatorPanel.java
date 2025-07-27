package com.vittoriomattei.contextfetcher.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBSplitter;
import com.intellij.util.ui.JBUI;
import com.vittoriomattei.contextfetcher.model.FileContextItem;
import com.vittoriomattei.contextfetcher.model.FileEntry;
import com.vittoriomattei.contextfetcher.services.ContextGeneratorService;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;
import com.vittoriomattei.contextfetcher.services.FilesChangeListener;
import com.vittoriomattei.contextfetcher.model.LineRange;
import com.vittoriomattei.contextfetcher.ui.panel.FileListPanel;
import com.vittoriomattei.contextfetcher.ui.panel.PreviewPanel;
import com.vittoriomattei.contextfetcher.ui.panel.ToolbarPanel;
import com.vittoriomattei.contextfetcher.util.DataKeys;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ContextAggregatorPanel extends JPanel implements Disposable, DataProvider {

    private final Project project;
    private final FileAggregatorService fileService;
    private final ContextGeneratorService contextService;

    // UI Components
    private final ToolbarPanel toolbarPanel;
    private final FileListPanel fileListPanel;
    private final PreviewPanel previewPanel;

    // Data and listeners
    private final FilesChangeListener filesChangeListener = this::refreshFileList;

    public ContextAggregatorPanel(Project project, FileAggregatorService fileService, ContextGeneratorService contextService) {
        super(new BorderLayout());
        this.project = project;
        this.fileService = fileService;
        this.contextService = contextService;

        // Initialize UI components
        this.toolbarPanel = new ToolbarPanel(project, fileService);
        this.fileListPanel = new FileListPanel(
                new ArrayList<>(),
                fileService,
                this::handleJumpToSource,
                this::handleRemoveItem
        );
        this.previewPanel = new PreviewPanel(project, this.contextService);

        setupComponents();
        layoutComponents();
        setupListeners();

        // Initial data load
        refreshFileList();
    }

    private void setupComponents() {

        // Configure file list panel
        fileListPanel.setBorder(IdeBorderFactory.createTitledBorder("Context Files"));

        // Configure preview panel
        previewPanel.setBorder(IdeBorderFactory.createTitledBorder("Generated Context"));
    }

    private void layoutComponents() {
        // Create main content area with split pane
        JBSplitter mainSplitPane = new JBSplitter(true);
        mainSplitPane.setFirstComponent(fileListPanel);
        mainSplitPane.setSecondComponent(previewPanel);
        mainSplitPane.setProportion(0.6f);
        mainSplitPane.setBorder(IdeBorderFactory.createEmptyBorder(JBUI.insets(10)));

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
        });
    }

    @Override
    public void dispose() {
        fileService.removeChangeListener(filesChangeListener);
    }

    @Override
    public @Nullable Object getData(@NotNull @NonNls String dataId) {
        if (DataKeys.SELECTED_FILES_KEY.is(dataId)) {
            return fileListPanel.getSelectedFileList();
        }
        return null;
    }
}