package com.vittoriomattei.contextfetcher.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

public class FileContextItem {
    private final VirtualFile virtualFile;
    private final @Nullable LineRange lineRange;

    public FileContextItem(VirtualFile virtualFile, @Nullable LineRange lineRange) {
        this.virtualFile = virtualFile;
        this.lineRange = lineRange;
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public @Nullable LineRange getLineRange() {
        return lineRange;
    }

    public boolean isSnippet() {
        return lineRange != null;
    }

    public String getDisplayName() {
        if (isSnippet()) {
            assert lineRange != null;
            return virtualFile.getName() + " [" + lineRange.startLine() + "–" + lineRange.endLine() + "]";
        }
        return virtualFile.getName();
    }

    public String getFullPath() {
        return virtualFile.getPath();
    }

    public String getPresentableName() {
        return virtualFile.getPresentableName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        FileContextItem that = (FileContextItem) obj;

        if (!virtualFile.equals(that.virtualFile)) return false;
        return lineRange != null ? lineRange.equals(that.lineRange) : that.lineRange == null;
    }

    @Override
    public int hashCode() {
        int result = virtualFile.hashCode();
        result = 31 * result + (lineRange != null ? lineRange.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FileContextItem{" +
                "file=" + virtualFile.getName() +
                ", lineRange=" + lineRange +
                '}';
    }
}