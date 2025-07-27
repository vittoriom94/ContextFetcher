package com.vittoriomattei.contextfetcher.ui.panel;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.vittoriomattei.contextfetcher.listeners.ContextUpdateListener;
import com.vittoriomattei.contextfetcher.services.ContextGeneratorService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class PreviewPanel extends JPanel implements ContextUpdateListener, Disposable {

    private final Project project;
    private final ContextGeneratorService contextService;
    private final EditorTextField codePreviewField;
    private final JLabel statusLabel;

    public PreviewPanel(Project project, ContextGeneratorService contextService) {
        super(new BorderLayout());
        this.project = project;
        this.contextService = contextService;

        this.codePreviewField = createCodeEditor();;
        this.statusLabel = new JBLabel(" "); // Space to maintain height


        setupComponents();

        JPanel topPanel = new JPanel(new BorderLayout());

        ActionToolbar actionToolbar = getActionToolbar();

        topPanel.add(actionToolbar.getComponent(), BorderLayout.WEST); // Buttons on the left
        topPanel.add(statusLabel, BorderLayout.CENTER);               // Status label takes remaining space

        add(topPanel, BorderLayout.NORTH);
        add(codePreviewField.getComponent(), BorderLayout.CENTER); // Use getComponent() for an Editor
        this.contextService.addContextUpdateListener(this);
    }

    private @NotNull ActionToolbar getActionToolbar() {
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup actionGroup = (ActionGroup) actionManager.getAction("ContextFetcher.GeneratedContextToolbar");

        ActionToolbar actionToolbar = actionManager.createActionToolbar(
                "ContextFetcherPreviewToolbar",
                actionGroup,
                true // true for horizontal layout
        );
        actionToolbar.setTargetComponent(this); // Important for actions to get context
        return actionToolbar;
    }

    private void setupComponents() {
        codePreviewField.setBorder(JBUI.Borders.empty(5));


        // Style status label
        statusLabel.setBorder(JBUI.Borders.emptyLeft(10)); // Add some space from the buttons
        statusLabel.setFont(JBUI.Fonts.smallFont().deriveFont(Font.ITALIC));
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
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

    @Override
    public void dispose() {
        contextService.removeContextUpdateListener(this);
    }

    @Override
    public void onContextUpdated(String newContent, String status) {
        SwingUtilities.invokeLater(() -> {
            codePreviewField.getDocument().setText(newContent);
            statusLabel.setText(status);
        });
    }
}