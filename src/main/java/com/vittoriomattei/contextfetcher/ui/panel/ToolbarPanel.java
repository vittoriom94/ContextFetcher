package com.vittoriomattei.contextfetcher.ui.panel;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;

import javax.swing.*;
import java.awt.*;

public class ToolbarPanel extends JPanel {

    public ToolbarPanel() {
        super(new BorderLayout());

        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup actionGroup = (ActionGroup) actionManager.getAction("ContextFetcher.MainToolbarGroup");
        ActionToolbar actionToolbar = actionManager.createActionToolbar("ContextFetcherToolbar", actionGroup, true);
        actionToolbar.setTargetComponent(this);
        add(actionToolbar.getComponent(), BorderLayout.WEST);
    }





}