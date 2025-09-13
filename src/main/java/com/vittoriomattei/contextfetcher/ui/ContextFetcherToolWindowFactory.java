package com.vittoriomattei.contextfetcher.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.vittoriomattei.contextfetcher.services.ContextGeneratorService;
import com.vittoriomattei.contextfetcher.services.FileAggregatorService;

public class ContextFetcherToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        FileAggregatorService fileService = project.getService(FileAggregatorService.class);
        ContextGeneratorService contextService = project.getService(ContextGeneratorService.class);
        ContextFetcherPanel panel = new ContextFetcherPanel(project, fileService, contextService);

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}


