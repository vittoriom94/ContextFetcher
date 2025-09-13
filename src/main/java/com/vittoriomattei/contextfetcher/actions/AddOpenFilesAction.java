package com.vittoriomattei.contextfetcher.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.project.Project;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;

import java.util.Arrays;
import java.util.List;

public class AddOpenFilesAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        VirtualFile[] files = FileEditorManager.getInstance(project).getOpenFiles();

        if (files.length == 0) {
            Messages.showErrorDialog(project, "No files are open.", "ContextFetcher");
            return;
        }

        FileAggregatorService service = project.getService(FileAggregatorService.class);
        List<VirtualFile> fileList = Arrays.asList(files);
        service.addFiles(fileList);
    }
}
