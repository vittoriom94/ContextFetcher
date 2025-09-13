package com.vittoriomattei.contextfetcher.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.vittoriomattei.contextfetcher.services.ContextGeneratorService;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ClearGeneratedTextAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        var service = Objects.requireNonNull(project).getService(ContextGeneratorService.class);
        service.setCurrentContext("");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        var project = e.getProject();
        var service = Objects.requireNonNull(project).getService(ContextGeneratorService.class);

        e.getPresentation().setEnabled(!service.getCurrentContext().isEmpty());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }
}
