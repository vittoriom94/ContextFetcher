package com.vittoriomattei.contextfetcher.ui.panel;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ToolbarPanel extends JPanel {

    private final Project project;
    private final FileAggregatorService fileService;

    private final JButton addRecentFileButton = new JButton("Add Recent File");
    private final JButton clearAllButton = new JButton("Clear All");
    private final JButton generateContextButton = new JButton("Generate Context");

    public interface ContextGenerationListener {
        void onContextGenerated();
    }

    private ContextGenerationListener contextGenerationListener;

    public ToolbarPanel(Project project, FileAggregatorService fileService) {
        super(new BorderLayout());
        this.project = project;
        this.fileService = fileService;

//        setLayout(new GridBagLayout());
//        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//        setupButtons();
//        setupListeners();
//        layoutButtons();


        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup actionGroup = (ActionGroup) actionManager.getAction("ContextFetcher.ToolbarActions");
        ActionToolbar actionToolbar = actionManager.createActionToolbar("ContextFetcherToolbar", actionGroup, true);
        actionToolbar.setTargetComponent(this);
        add(actionToolbar.getComponent(), BorderLayout.WEST);
    }

    public void setContextGenerationListener(ContextGenerationListener listener) {
        this.contextGenerationListener = listener;
    }

    private void setupButtons() {
        Dimension buttonSize = new Dimension(140, 30);
        addRecentFileButton.setPreferredSize(buttonSize);
        clearAllButton.setPreferredSize(buttonSize);
        generateContextButton.setPreferredSize(buttonSize);

        // Make the generate button more prominent
        generateContextButton.setFont(generateContextButton.getFont().deriveFont(Font.BOLD));
    }

    private void setupListeners() {
        addRecentFileButton.addActionListener(this::handleAddRecentFile);
        clearAllButton.addActionListener(this::handleClearAll);
        generateContextButton.addActionListener(this::handleGenerateContext);
    }

    private void layoutButtons() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = JBUI.insets(3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // First row - Add buttons
        gbc.gridy = 0;
        gbc.gridx = 0;
        add(addRecentFileButton, gbc);


        // Second row - Action buttons
        gbc.gridy = 1;
        gbc.gridx = 0;
        add(clearAllButton, gbc);


        gbc.gridx = 1;

        add(generateContextButton, gbc);
    }


    private void handleAddRecentFile(ActionEvent e) {
        List<VirtualFile> recentFiles = Arrays.asList(
                EditorHistoryManager.getInstance(project).getFiles()
        );

        if (recentFiles.isEmpty()) {
            Messages.showInfoMessage(project, "No recent files available.", "ContextFetcher");
            return;
        }

        showFileSelectionPopup(recentFiles);
    }

    private void handleClearAll(ActionEvent e) {
        int result = Messages.showYesNoDialog(
                project,
                "Are you sure you want to clear all files from the context?",
                "Clear All Files",
                Messages.getQuestionIcon()
        );

        if (result == Messages.YES) {
            fileService.clear();
            Messages.showInfoMessage(project, "All files cleared from context.", "ContextFetcher");
        }
    }

    private void handleGenerateContext(ActionEvent e) {
        if (contextGenerationListener != null) {
            contextGenerationListener.onContextGenerated();
        }
    }

    private void showFileSelectionPopup(List<VirtualFile> recentFiles) {
        // State: current filtered list
        DefaultListModel<VirtualFile> listModel = new DefaultListModel<>();
        recentFiles.forEach(listModel::addElement);

        JBList<VirtualFile> fileList = new JBList<>(listModel);
        fileList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value.getPresentableName(), value.getFileType().getIcon(), JLabel.LEFT);
            label.setBorder(JBUI.Borders.empty(2, 4));
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
                label.setOpaque(true);
            }
            return label;
        });

        // TextField with live filtering
        JBTextField searchField = new JBTextField();
        searchField.getEmptyText().setText("Search recent files...");

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = searchField.getText().toLowerCase();
                listModel.clear();
                recentFiles.stream()
                        .filter(file -> file.getPresentableName().toLowerCase().contains(query))
                        .forEach(listModel::addElement);
            }
        });

        // On click, trigger service and keep popup open for multiple selections
        fileList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 1) {
                    VirtualFile selected = fileList.getSelectedValue();
                    if (selected != null) {
                        fileService.addFiles(Collections.singletonList(selected));
                        // Visual feedback - could change the item color or add a checkmark
                        fileList.repaint();
                    }
                }
            }
        });

        // Add instruction label
        JLabel instructionLabel = new JLabel("Click files to add them to context. Multiple selections allowed.");
        instructionLabel.setFont(instructionLabel.getFont().deriveFont(Font.ITALIC, 11f));
        instructionLabel.setBorder(JBUI.Borders.empty(2, 4));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(JBUI.Borders.empty(8));
        panel.add(searchField, BorderLayout.NORTH);
        panel.add(new JBScrollPane(fileList), BorderLayout.CENTER);
        panel.add(instructionLabel, BorderLayout.SOUTH);
        panel.setPreferredSize(new Dimension(350, 450));

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(panel, searchField)
                .setTitle("Add Recent Files to Context")
                .setResizable(true)
                .setMovable(true)
                .setRequestFocus(true)
                .setCancelOnClickOutside(true)
                .createPopup();

        popup.showUnderneathOf(addRecentFileButton);
    }
}