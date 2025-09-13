package com.vittoriomattei.contextfetcher.services;

import com.intellij.openapi.vfs.VirtualFile;
import com.vittoriomattei.contextfetcher.model.FileContextItem;
import com.vittoriomattei.contextfetcher.model.LineRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
     * Gets all file context items
     */
    @NotNull List<FileContextItem> getAllItems();

    /**
     * Gets all file context items for a specific file
     */
    @NotNull List<FileContextItem> getItemsForFile(@NotNull VirtualFile file);

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

    int getSnippetCount();

    /**
     * Clears all files and snippets
     */
    void clear();

    void addChangeListener(@NotNull FilesChangeListener listener);
    void removeChangeListener(@NotNull FilesChangeListener listener);
    void removeFiles(@NotNull List<FileContextItem> selectedItems);

    List<FileContextItem> getSortedItems();
}