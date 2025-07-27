package com.vittoriomattei.contextfetcher.services;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.vittoriomattei.contextfetcher.listeners.ContextUpdateListener;
import com.vittoriomattei.contextfetcher.model.FileEntry;
import com.vittoriomattei.contextfetcher.model.LineRange;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ContextGeneratorService{

    private List<FileEntry> files;
    private String currentContext = "";
    private String status = "";

    private final CopyOnWriteArrayList<ContextUpdateListener> contextUpdateListeners = new CopyOnWriteArrayList<>();

    public String generateContext() {
        StringBuilder context = new StringBuilder();
        context.append("# --- Code context ---\n\n");

        int filesCount = 0;
        int snippetsCount = 0;

        for (FileEntry file : files) {

            String filePath = file.getVirtualFile().getPresentableUrl();
            context.append("## File: ").append(filePath).append(":\n\n");

            String content = ReadAction.compute(
                    () -> {
                        try {
                            return VfsUtil.loadText(file.getVirtualFile());
                        } catch (IOException e) {
                            System.err.println("Error reading string content with streams: " + e.getMessage());
                            return " ... file content could not be loaded ...\n\n";
                        }
                    }
            );


            Set<LineRange> snippets = file.getSnippets();
            if (snippets.isEmpty()) {
                context.append("```\n").append(content).append("\n```\n\n");
                filesCount++;
            } else {
                for (LineRange snippet : snippets) {
                    context.append("### L").append(snippet.startLine()).append("-").append(snippet.endLine()).append("\n```\n");
                    context.append(getSnippet(content, snippet.startLine(), snippet.endLine()));
                    context.append("\n```\n\n");
                    snippetsCount++;
                }
            }

        }

        context.append("# End of code context\n\n");

        String status = String.format("Context generated: %d file(s)", files.size());
        if (snippetsCount > 0) {
            status += String.format(", %d snippet(s)", snippetsCount);
        }

        notifyContextUpdateListeners(context.toString(), status);
        return context.toString();
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
                    .skip(lineStart) // Skip lines before the start
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
