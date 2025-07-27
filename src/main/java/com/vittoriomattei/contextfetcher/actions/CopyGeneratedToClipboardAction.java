package com.vittoriomattei.contextfetcher.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.vittoriomattei.contextfetcher.services.ContextGeneratorService;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.util.Objects;

public class CopyGeneratedToClipboardAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        var service = Objects.requireNonNull(project).getService(ContextGeneratorService.class);

        var context = service.getCurrentContext();
        if (!context.isEmpty()) {
            CopyPasteManager.getInstance().setContents(new StringSelection(context));


            String originalText = service.getStatus();
            service.setStatus("Copied to clipboard!");

            Timer timer = new Timer(2000, ev -> service.setStatus(originalText));
            timer.setRepeats(false);
            timer.start();
            service.setStatus("Copied context to clipboard");
        }

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
