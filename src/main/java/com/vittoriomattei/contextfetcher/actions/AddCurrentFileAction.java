package com.vittoriomattei.contextfetcher.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.ui.Messages;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class AddCurrentFileAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null || file.isDirectory()) {
            Messages.showErrorDialog(project, "No file is currently selected.", "ContextFetcher");
            return;
        }

        FileAggregatorService service = project.getService(FileAggregatorService.class);
        service.addFile(file);

        Messages.showInfoMessage(project, "Added current file: " + file.getName(), "ContextFetcher");
    }

    @Override
    public void update(AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        e.getPresentation().setEnabledAndVisible(file != null && !file.isDirectory());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

}
