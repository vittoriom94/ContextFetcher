package com.vittoriomattei.contextfetcher.services;

import com.intellij.openapi.vfs.VirtualFile;
import com.vittoriomattei.contextfetcher.model.FileEntry;
import com.vittoriomattei.contextfetcher.model.LineRange;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface FileAggregatorService {

    /**
     * Adds a complete file to the aggregator
     * @param file the file to add
     * @return true if the file was added, false if it already exists
     */
    boolean addFile(@NotNull VirtualFile file);

    /**
     * Adds multiple complete files
     * @param files the files to add
     * @return number of files actually added
     */
    int addFiles(@NotNull Iterable<VirtualFile> files);

    /**
     * Adds a snippet from a file. If the file is already added as complete,
     * it will be converted to a snippet-based entry.
     * @param file the file containing the snippet
     * @param lineRange the line range of the snippet
     * @return true if the snippet was added
     */
    boolean addSnippet(@NotNull VirtualFile file, @NotNull LineRange lineRange);

    /**
     * Gets all file entries
     */
    @NotNull List<FileEntry> getFileEntries();

    /**
     * Gets a specific file entry
     */
    @NotNull Optional<FileEntry> getFileEntry(@NotNull VirtualFile file);

    /**
     * Removes a file entirely
     */
    boolean removeFile(@NotNull VirtualFile file);

    /**
     * Removes a specific snippet
     */
    boolean removeSnippet(@NotNull VirtualFile file, @NotNull LineRange lineRange);

    /**
     * Checks if a file is tracked
     */
    boolean containsFile(@NotNull VirtualFile file);

    /**
     * Gets count of tracked files
     */
    int getFileCount();

    /**
     * Clears all files and snippets
     */
    void clear();

    // Event handling
    void addChangeListener(@NotNull FilesChangeListener listener);
    void removeChangeListener(@NotNull FilesChangeListener listener);
}