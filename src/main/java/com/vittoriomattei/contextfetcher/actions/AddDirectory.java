package com.vittoriomattei.contextfetcher.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AddDirectory extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile dir = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (project == null || dir == null || !dir.isDirectory()) {
            Messages.showErrorDialog(project, "Please select a directory.", "ContextFetcher");
            return;
        }

        List<VirtualFile> allFiles = new ArrayList<>();
        collectFilesRecursively(dir, allFiles);

        if (allFiles.isEmpty()) {
            Messages.showInfoMessage(project, "No files found in directory.", "ContextFetcher");
            return;
        }

        FileAggregatorService service = project.getService(FileAggregatorService.class);
        service.addFiles(allFiles);

        Messages.showInfoMessage(project,
                "Added " + allFiles.size() + " file(s) from directory: " + dir.getName(),
                "ContextFetcher");
    }

    private void collectFilesRecursively(VirtualFile dir, List<VirtualFile> collected) {
        for (VirtualFile child : dir.getChildren()) {
            if (child.isDirectory()) {
                collectFilesRecursively(child, collected);
            } else {
                collected.add(child);
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        e.getPresentation().setEnabledAndVisible(file != null && file.isDirectory());
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

}
