package com.vittoriomattei.contextfetcher.services;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.vittoriomattei.contextfetcher.model.FileEntry;
import com.vittoriomattei.contextfetcher.model.LineRange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ContextGeneratorService{

    private final List<FileEntry> files;

    public ContextGeneratorService(List<FileEntry> files) {
        this.files = files;
    }

    public String generateContext() {
        StringBuilder context = new StringBuilder();
        context.append("# --- Code context ---\n\n");

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
            } else {
                for (LineRange snippet : snippets) {
                    context.append("### L").append(snippet.startLine()).append("-").append(snippet.endLine()).append("\n```\n");
                    context.append(getSnippet(content, snippet.startLine(), snippet.endLine()));
                    context.append("\n```\n\n");
                }
            }

        }

        context.append("# End of code context\n\n");
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
}
