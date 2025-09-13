package com.vittoriomattei.contextfetcher.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.vittoriomattei.contextfetcher.model.FileContextItem;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;
import com.vittoriomattei.contextfetcher.util.DataKeys;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RemoveSelectedAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        List<FileContextItem> selectedItems = e.getData(DataKeys.SELECTED_FILES_KEY);

        if (project == null || selectedItems == null || selectedItems.isEmpty()) {
            return;
        }

        FileAggregatorService service = project.getService(FileAggregatorService.class);
        if (service != null) {
            service.removeFiles(selectedItems);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        List<FileContextItem> selectedItems = e.getData(DataKeys.SELECTED_FILES_KEY);
        boolean isEnabled = selectedItems != null && !selectedItems.isEmpty();
        e.getPresentation().setEnabled(isEnabled);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return super.getActionUpdateThread();
    }
}