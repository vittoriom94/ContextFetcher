package com.vittoriomattei.contextfetcher.ui;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBSplitter;
import com.intellij.util.ui.JBUI;
import com.vittoriomattei.contextfetcher.services.ContextGeneratorService;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;
import com.vittoriomattei.contextfetcher.ui.panel.FileListPanel;
import com.vittoriomattei.contextfetcher.ui.panel.PreviewPanel;
import com.vittoriomattei.contextfetcher.ui.panel.ToolbarPanel;
import com.vittoriomattei.contextfetcher.util.DataKeys;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ContextFetcherPanel extends JPanel implements DataProvider {

    // UI Components
    private final ToolbarPanel toolbarPanel;
    private final FileListPanel fileListPanel;
    private final PreviewPanel previewPanel;


    public ContextFetcherPanel(Project project, FileAggregatorService fileService, ContextGeneratorService contextService) {
        super(new BorderLayout());

        // Initialize UI components
        this.toolbarPanel = new ToolbarPanel();
        this.fileListPanel = new FileListPanel(
                project,
                fileService
        );
        this.previewPanel = new PreviewPanel(project, contextService);

        setupComponents();
        layoutComponents();

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



    @Override
    public @Nullable Object getData(@NotNull @NonNls String dataId) {
        if (DataKeys.SELECTED_FILES_KEY.is(dataId)) {
            return fileListPanel.getSelectedFileList();
        }
        return null;
    }
}