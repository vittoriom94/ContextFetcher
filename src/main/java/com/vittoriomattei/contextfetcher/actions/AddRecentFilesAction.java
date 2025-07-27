package com.vittoriomattei.contextfetcher.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.impl.EditorHistoryManager;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBPanel;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;
import com.vittoriomattei.contextfetcher.ui.panel.RecentFilesPopupPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AddRecentFilesAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        List<VirtualFile> recentFiles = Arrays.asList(EditorHistoryManager.getInstance(Objects.requireNonNull(project)).getFiles());
        FileAggregatorService fileService = project.getService(FileAggregatorService.class);
        JPanel popupPanel = new RecentFilesPopupPanel(recentFiles, selectedFile -> {
            this.fileSelectedCallback(selectedFile, fileService);
        });

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(popupPanel, null)
                .setTitle("Add Recent Files to Context")
                .setResizable(true)
                .setMovable(true)
                .createPopup();

        if (e.getInputEvent() != null){
            popup.showUnderneathOf(e.getInputEvent().getComponent());
        } else {
            popup.showCenteredInCurrentWindow(project);
        }
    }

    private void fileSelectedCallback(VirtualFile file, FileAggregatorService fileService) {
        fileService.addFile(file);
    }


}
