package com.vittoriomattei.contextfetcher.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.vittoriomattei.contextfetcher.services.ContextGeneratorService;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GenerateContextAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        var contextGeneratorService = project.getService(ContextGeneratorService.class);
        var fileAggregatorService = project.getService(FileAggregatorService.class);
        contextGeneratorService.generateContext(fileAggregatorService.getFileEntries());
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        var project = e.getProject();
        var service = Objects.requireNonNull(project).getService(FileAggregatorService.class);
        e.getPresentation().setEnabled(service.getFileCount() + service.getSnippetCount() > 0);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }
}
