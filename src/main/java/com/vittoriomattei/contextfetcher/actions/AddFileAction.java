package com.vittoriomattei.contextfetcher.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.ui.Messages;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AddFileAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) {
            Messages.showErrorDialog(project, "No file is currently selected.", "ContextFetcher");
            return;
        }

        FileAggregatorService service = project.getService(FileAggregatorService.class);
        if ( file.isDirectory()) {
            List<VirtualFile> collected =  new ArrayList<>();
            collectFilesRecursively(file, collected);
            service.addFiles(collected);
        } else {
            service.addFile(file);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file != null && file.isDirectory()) {
            e.getPresentation().setIcon(AllIcons.Actions.AddDirectory);
            e.getPresentation().setText("Add Directory to Context");
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
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
}
