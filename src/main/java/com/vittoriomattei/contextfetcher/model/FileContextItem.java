package com.vittoriomattei.contextfetcher.model;

import com.google.common.collect.Comparators;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Comparator;

public class FileContextItem implements Comparable<FileContextItem> {
    private final VirtualFile virtualFile;
    private final LineRange lineRange;
    private final boolean isSnippet;

    public FileContextItem(VirtualFile virtualFile, LineRange lineRange, boolean isSnippet){
        this.virtualFile = virtualFile;
        this.lineRange = lineRange;
        this.isSnippet = isSnippet;
    }

    // Factory methods for clarity
    public static FileContextItem wholeFile(VirtualFile virtualFile) {
        return new FileContextItem(virtualFile, new LineRange(0, -1), false);
    }

    public static FileContextItem snippet(VirtualFile virtualFile, LineRange lineRange) {
        return new FileContextItem(virtualFile, lineRange, true);
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public LineRange getLineRange() {
        return lineRange;
    }

    public boolean isSnippet() {
        return isSnippet;
    }

    public String getPresentableName() {
        return virtualFile.getPresentableName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        FileContextItem that = (FileContextItem) obj;

        if (isSnippet != that.isSnippet) return false;
        if (!virtualFile.equals(that.virtualFile)) return false;
        return lineRange.equals(that.lineRange);
    }

    @Override
    public int hashCode() {
        int result = virtualFile.hashCode();
        result = 31 * result + lineRange.hashCode();
        result = 31 * result + (isSnippet ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FileContextItem{" +
                "file=" + virtualFile.getName() +
                ", lineRange=" + lineRange +
                ", isSnippet=" + isSnippet +
                '}';
    }

    @Override
    public int compareTo(@NotNull FileContextItem other) {
        if (this == other) {
            return 0;
        }
        int fileComparison = this.virtualFile.getName().compareTo(other.virtualFile.getName());
        if (fileComparison != 0 || (!this.isSnippet && !other.isSnippet)) {
            return fileComparison;
        }
        if (!this.isSnippet && other.isSnippet) {
            return -1;
        } else if (this.isSnippet && !other.isSnippet) {
            return 1;
        }

        // both are snippets, compare the start lines, if they're the same use the end lines
        int startComparison = Integer.compare(this.lineRange.startLine(), other.lineRange.startLine());
        if (startComparison != 0) {
            return startComparison;
        }
        return Integer.compare(this.lineRange.endLine(), other.lineRange.endLine());

    }

}