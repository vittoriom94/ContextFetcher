package com.vittoriomattei.contextfetcher.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ClearAllAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        var service = Objects.requireNonNull(project).getService(FileAggregatorService.class);
        service.clear();
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
