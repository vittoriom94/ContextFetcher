package com.vittoriomattei.contextfetcher.model;

import com.intellij.openapi.vfs.VirtualFile;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class FileEntry {
    private final VirtualFile virtualFile;
    private final Set<LineRange> snippets;
    private final boolean isCompleteFile;

    public FileEntry(VirtualFile virtualFile, Set<LineRange> snippets, boolean isCompleteFile) {
        this.virtualFile = Objects.requireNonNull(virtualFile, "VirtualFile cannot be null");
        this.snippets = Collections.unmodifiableSet(new LinkedHashSet<>(snippets));
        this.isCompleteFile = isCompleteFile;
    }

    public static FileEntry completeFile(VirtualFile virtualFile) {
        return new FileEntry(virtualFile, Collections.emptySet(), true);
    }

    public static FileEntry withSnippets(VirtualFile virtualFile, Set<LineRange> snippets) {
        if (snippets.isEmpty()) {
            throw new IllegalArgumentException("Snippets cannot be empty. Use completeFile() instead.");
        }
        return new FileEntry(virtualFile, snippets, false);
    }

    public VirtualFile getVirtualFile() { return virtualFile; }
    public Set<LineRange> getSnippets() { return snippets; }
    public boolean isCompleteFile() { return isCompleteFile; }
    public boolean hasSnippets() { return !snippets.isEmpty(); }

    public FileEntry addSnippet(LineRange lineRange) {
        if (isCompleteFile) {
            // Convert complete file to snippet-based entry
            Set<LineRange> newSnippets = new LinkedHashSet<>();
            newSnippets.add(lineRange);
            return new FileEntry(virtualFile, newSnippets, false);
        }

        Set<LineRange> newSnippets = new LinkedHashSet<>(snippets);
        newSnippets.add(lineRange);
        return new FileEntry(virtualFile, newSnippets, false);
    }

    public FileEntry removeSnippet(LineRange lineRange) {
        if (isCompleteFile || !snippets.contains(lineRange)) {
            return this;
        }

        Set<LineRange> newSnippets = new LinkedHashSet<>(snippets);
        newSnippets.remove(lineRange);

        if (newSnippets.isEmpty()) {
            return null; // Entry should be removed entirely
        }

        return new FileEntry(virtualFile, newSnippets, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileEntry fileEntry = (FileEntry) o;
        return isCompleteFile == fileEntry.isCompleteFile &&
                Objects.equals(virtualFile, fileEntry.virtualFile) &&
                Objects.equals(snippets, fileEntry.snippets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(virtualFile, snippets, isCompleteFile);
    }
}
