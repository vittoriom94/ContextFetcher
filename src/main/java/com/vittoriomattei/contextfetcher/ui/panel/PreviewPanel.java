package com.vittoriomattei.contextfetcher.ui.panel;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.vittoriomattei.contextfetcher.listeners.ContextUpdateListener;
import com.vittoriomattei.contextfetcher.services.ContextGeneratorService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class PreviewPanel extends JPanel implements ContextUpdateListener, Disposable {

    private final ContextGeneratorService contextService;
    private final CodePanel codePreviewField;
    private final JLabel statusLabel;

    public PreviewPanel(Project project, ContextGeneratorService contextService) {
        super(new BorderLayout());
        this.contextService = contextService;

        this.codePreviewField = new CodePanel(project);
        this.statusLabel = new JBLabel(" ");

        setupComponents();

        JPanel topPanel = new JPanel(new BorderLayout());

        ActionToolbar actionToolbar = getActionToolbar();

        topPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);
        topPanel.add(statusLabel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        add(codePreviewField, BorderLayout.CENTER);
        this.contextService.addContextUpdateListener(this);
    }

    private @NotNull ActionToolbar getActionToolbar() {
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup actionGroup = (ActionGroup) actionManager.getAction("ContextFetcher.GenerateContextToolbar");

        ActionToolbar actionToolbar = actionManager.createActionToolbar(
                "ContextFetcherPreviewToolbar",
                actionGroup,
                true
        );
        actionToolbar.setTargetComponent(this);
        return actionToolbar;
    }

    private void setupComponents() {
        codePreviewField.setBorder(JBUI.Borders.empty(5));

        statusLabel.setBorder(JBUI.Borders.emptyLeft(10));
        statusLabel.setFont(JBUI.Fonts.smallFont().deriveFont(Font.ITALIC));
        statusLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
    }

    @Override
    public void dispose() {
        contextService.removeContextUpdateListener(this);
    }

    @Override
    public void onContextUpdated(String newContent, String status) {
        WriteAction.runAndWait(() -> {
            this.codePreviewField.setMarkdownText(newContent);
            statusLabel.setText(status);
        });

    }
}