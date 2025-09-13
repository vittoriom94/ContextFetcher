package com.vittoriomattei.contextfetcher.services;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.vittoriomattei.contextfetcher.listeners.ContextUpdateListener;
import com.vittoriomattei.contextfetcher.model.FileContextItem;
import com.vittoriomattei.contextfetcher.model.LineRange;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ContextGeneratorService{

    private String currentContext = "";
    private String status = "";

    private final CopyOnWriteArrayList<ContextUpdateListener> contextUpdateListeners = new CopyOnWriteArrayList<>();

    public String generateContext(List<FileContextItem> fileItems) {
        StringBuilder context = new StringBuilder();
        context.append("# --- Code context ---\n\n");

        int filesCount = 0;
        int snippetsCount = 0;

        // Group items by file to avoid loading file content multiple times
        Map<VirtualFile, List<FileContextItem>> groupedItems = fileItems.stream()
                .collect(Collectors.groupingBy(FileContextItem::getVirtualFile));

        for (VirtualFile virtualFile : groupedItems.keySet()) {
            String filePath = virtualFile.getPresentableUrl();
            context.append("## File: ").append(filePath).append(":\n\n");

            String content = ReadAction.compute(
                    () -> {
                        try {
                            return StringUtil.convertLineSeparators(VfsUtil.loadText(virtualFile));
                        } catch (IOException e) {
                            System.err.println("Error reading string content with streams: " + e.getMessage());
                            return " ... file content could not be loaded ...\n\n";
                        }
                    }
            );

            var fileType = FileTypeManager.getInstance().getFileTypeByExtension(FileUtilRt.getExtension(virtualFile.getName()));
            String extension = fileType != UnknownFileType.INSTANCE ? fileType.getName().toLowerCase() : "";
            
            List<FileContextItem> itemsForFile = groupedItems.get(virtualFile);
            
            for (FileContextItem item : itemsForFile) {
                if (item.isSnippet()) {
                    LineRange lineRange = item.getLineRange();
                    context.append("### L").append(lineRange.startLine() + 1).append("-").append(lineRange.endLine() + 1).append("\n```").append(extension).append("\n");
                    context.append(getSnippet(content, lineRange.startLine() + 1, lineRange.endLine() + 1));
                    context.append("\n```\n\n");
                    snippetsCount++;
                } else {
                    // Whole file
                    context.append("```").append(extension).append("\n").append(content).append("\n```\n\n");
                    filesCount++;
                }
            }
        }

        context.append("# End of code context\n\n");

        String status = String.format("Context generated: %d file(s)", filesCount);
        if (snippetsCount > 0) {
            status += String.format(", %d snippet(s)", snippetsCount);
        }

        currentContext = context.toString();
        this.status = status;
        notifyContextUpdateListeners(context.toString(), status);
        return currentContext;
    }

    private String getSnippet(String fileContent, int lineStart, int lineEnd) {
        if (fileContent == null || fileContent.isEmpty()) {
            return "";
        }

        // Ensure lineStart and lineEnd are valid and in order
        if (lineStart < 0) {
            lineStart = 0;
        }
        if (lineEnd < lineStart) {
            lineEnd = lineStart;
        }

        try (BufferedReader reader = new BufferedReader(new StringReader(fileContent))) {
            return reader.lines() // Get a Stream of lines
                    .skip(lineStart - 1) // Skip lines before the start (convert from 1-based to 0-based)
                    .limit(lineEnd - lineStart + 1) // Take the desired number of lines
                    .collect(Collectors.joining("\n")); // Join them with newlines
        } catch (IOException e) {
            System.err.println("Error reading string content with streams: " + e.getMessage());
            return " ... snippet could not be loaded ...\n\n";
        }
    }

    public String getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(@NotNull String context) {
        this.currentContext = context;
        notifyContextUpdateListeners(this.currentContext, "Cleared");
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(@NotNull String status) {
        this.status = status;
    }

    public void notifyContextUpdateListeners(String newContext, String newStatus) {
        for (var listener: contextUpdateListeners) {
            listener.onContextUpdated(newContext, newStatus);
        }
    }

    public void addContextUpdateListener(@NotNull  ContextUpdateListener listener) {
        if (!contextUpdateListeners.contains(listener)) {
            contextUpdateListeners.add(listener);
        }
    }

    public void removeContextUpdateListener(@NotNull ContextUpdateListener listener) {
        contextUpdateListeners.remove(listener);
    }

}
