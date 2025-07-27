package com.vittoriomattei.contextfetcher.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.project.Project;

public class RunAggregationAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        Messages.showInfoMessage(project, "This is the Context Output Action", "Run Aggregation");
    }
}
