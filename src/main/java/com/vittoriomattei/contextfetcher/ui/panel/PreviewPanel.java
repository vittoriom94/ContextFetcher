package com.vittoriomattei.contextfetcher.ui.panel;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.util.ui.JBUI;
import com.vittoriomattei.contextfetcher.model.FileEntry;
import com.vittoriomattei.contextfetcher.services.ContextGeneratorService;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;
import com.vittoriomattei.contextfetcher.model.LineRange;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.List;

public class PreviewPanel extends JPanel {

    private final Project project;
    private final FileAggregatorService fileService;
    private final EditorTextField codePreviewField;
    private final JButton copyButton;
    private final JButton clearButton;
    private final JLabel statusLabel;

    public PreviewPanel(Project project, FileAggregatorService fileService) {
        super(new BorderLayout());
        this.project = project;
        this.fileService = fileService;

        this.codePreviewField = createCodeEditor();
        this.copyButton = new JButton("Copy to Clipboard");
        this.clearButton = new JButton("Clear text");
        this.statusLabel = new JLabel(" "); // Space to maintain height

        setupComponents();
        layoutComponents();
        setupListeners();
    }

    private void setupComponents() {
        codePreviewField.setBorder(JBUI.Borders.empty(5));

        // Style buttons
        copyButton.setEnabled(false); // Initially disabled until content is generated
        clearButton.setFont(clearButton.getFont().deriveFont(Font.BOLD));

        // Style status label
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.ITALIC, 11f));
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
    }

    private void layoutComponents() {
        // Create top button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        buttonPanel.add(clearButton);
        buttonPanel.add(copyButton);
        buttonPanel.add(Box.createHorizontalStrut(10)); // Spacer
        buttonPanel.add(statusLabel);

        // Add components to main panel
        add(buttonPanel, BorderLayout.NORTH);
        add(codePreviewField, BorderLayout.CENTER);
    }

    private void setupListeners() {
        clearButton.addActionListener(e -> clearPreview());
        copyButton.addActionListener(e -> copyToClipboard());
    }

    public void generateContext() {
        try {
            @NotNull List<FileEntry> files = fileService.getFileEntries();

            if (files.isEmpty()) {
                codePreviewField.setText("No files or snippets selected for context generation.");
                statusLabel.setText("No content to generate");
                copyButton.setEnabled(false);
                return;
            }

            statusLabel.setText("Generating context...");

            // Generate context on background thread to avoid UI blocking
            SwingUtilities.invokeLater(() -> {
                try {
                    ContextGeneratorService contextGeneratorService = new ContextGeneratorService(files);
                    String context = contextGeneratorService.generateContext();

                    codePreviewField.setText(context);
                    copyButton.setEnabled(true);

                    // Update status with file count
                    int fileCount = files.size();

                    int snippetCount = 0;
                    for (FileEntry file : files) {
                        snippetCount += file.getSnippets().size();
                    }

                    String status = String.format("Context generated: %d file(s)", fileCount);
                    if (snippetCount > 0) {
                        status += String.format(", %d snippet(s)", snippetCount);
                    }
                    statusLabel.setText(status);

                } catch (Exception ex) {
                    codePreviewField.setText("Error generating context: " + ex.getMessage());
                    statusLabel.setText("Generation failed");
                    copyButton.setEnabled(false);
                }
            });

        } catch (Exception e) {
            codePreviewField.setText("Error generating context: " + e.getMessage());
            statusLabel.setText("Generation failed");
            copyButton.setEnabled(false);
        }
    }

    private void copyToClipboard() {
        String text = codePreviewField.getText();
        if (hasContent()) {
            CopyPasteManager.getInstance().setContents(new StringSelection(text));

            // Provide visual feedback
            String originalText = statusLabel.getText();
            statusLabel.setText("Copied to clipboard!");

            // Reset status after 2 seconds
            Timer timer = new Timer(2000, e -> statusLabel.setText(originalText));
            timer.setRepeats(false);
            timer.start();
        }
    }

    public void clearPreview() {
        codePreviewField.setText("");
        copyButton.setEnabled(false);
        statusLabel.setText("Preview cleared");
    }

    public boolean hasContent() {
        String text = codePreviewField.getText();
        return !text.trim().isEmpty();
    }

    private EditorTextField createCodeEditor() {
        Document document = EditorFactory.getInstance().createDocument("");
        EditorTextField editor = new EditorTextField(document, project, null, false, false);
        editor.setOneLineMode(false);
        editor.setEnabled(false); // read-only

        // Delay caret visibility update
        SwingUtilities.invokeLater(() -> {
            EditorEx ex = (EditorEx) editor.getEditor();
            if (ex != null) {
                ex.setCaretVisible(false);
            }
        });

        return editor;
    }
}