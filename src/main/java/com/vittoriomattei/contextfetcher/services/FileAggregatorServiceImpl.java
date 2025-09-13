package com.vittoriomattei.contextfetcher.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.vittoriomattei.contextfetcher.model.FileContextItem;
import com.vittoriomattei.contextfetcher.model.LineRange;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class FileAggregatorServiceImpl implements FileAggregatorService {

    private static final Logger LOG = Logger.getInstance(FileAggregatorServiceImpl.class);

    private final Set<FileContextItem> items = ConcurrentHashMap.newKeySet();
    private final CopyOnWriteArrayList<FilesChangeListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public boolean addFile(@NotNull VirtualFile file) {
        Objects.requireNonNull(file, "File cannot be null");

        if (!file.isValid()) {
            LOG.warn("Attempted to add invalid file: " + file.getPath());
            return false;
        }

        // Check if file already exists in any form (whole file or snippets)
        boolean fileExists = items.stream()
                .anyMatch(item -> item.getVirtualFile().equals(file));

        if (fileExists) {
            return false; // File already tracked in some form
        }

        FileContextItem wholeFileItem = FileContextItem.wholeFile(file);
        boolean added = items.add(wholeFileItem);

        if (added) {
            notifyListeners();
        }

        return added;
    }

    @Override
    public int addFiles(@NotNull Iterable<VirtualFile> files) {
        Objects.requireNonNull(files, "Files cannot be null");

        int addedCount = 0;
        boolean anyAdded = false;

        for (VirtualFile file : files) {
            if (file != null && file.isValid()) {
                // Check if file already exists
                boolean fileExists = items.stream()
                        .anyMatch(item -> item.getVirtualFile().equals(file));
                
                if (!fileExists) {
                    FileContextItem wholeFileItem = FileContextItem.wholeFile(file);
                    if (items.add(wholeFileItem)) {
                        addedCount++;
                        anyAdded = true;
                    }
                }
            }
        }

        if (anyAdded) {
            notifyListeners();
        }

        return addedCount;
    }

    @Override
    public boolean addSnippet(@NotNull VirtualFile file, @NotNull LineRange lineRange) {
        Objects.requireNonNull(file, "File cannot be null");
        Objects.requireNonNull(lineRange, "LineRange cannot be null");

        if (!file.isValid()) {
            LOG.warn("Attempted to add snippet from invalid file: " + file.getPath());
            return false;
        }

        if (lineRange.startLine() < 0 || lineRange.endLine() < lineRange.startLine()) {
            LOG.warn("Invalid line range: " + lineRange);
            return false;
        }

        // Check if whole file is already tracked
        boolean wholeFileExists = items.stream()
                .anyMatch(item -> item.getVirtualFile().equals(file) && !item.isSnippet());

        if (wholeFileExists) {
            return false; // Cannot add snippet when whole file is already tracked
        }

        // Check if this exact snippet already exists
        FileContextItem newSnippet = FileContextItem.snippet(file, lineRange);
        if (items.contains(newSnippet)) {
            return false; // This exact snippet already exists
        }

        // File doesn't exist, add as snippet
        FileContextItem snippetItem = FileContextItem.snippet(file, lineRange);
        boolean added = items.add(snippetItem);
        
        if (added) {
            notifyListeners();
        }
        
        return added;
    }

    @Override
    public @NotNull List<FileContextItem> getAllItems() {
        return new ArrayList<>(items);
    }

    @Override
    public @NotNull List<FileContextItem> getItemsForFile(@NotNull VirtualFile file) {
        return items.stream()
                .filter(item -> item.getVirtualFile().equals(file))
                .collect(Collectors.toList());
    }

    @Override
    public boolean removeFile(@NotNull VirtualFile file) {
        boolean removed = items.removeIf(item -> item.getVirtualFile().equals(file));
        if (removed) {
            notifyListeners();
        }
        return removed;
    }

    @Override
    public boolean removeSnippet(@NotNull VirtualFile file, @NotNull LineRange lineRange) {
        Objects.requireNonNull(file, "File cannot be null");
        Objects.requireNonNull(lineRange, "LineRange cannot be null");

        FileContextItem snippetToRemove = FileContextItem.snippet(file, lineRange);
        boolean removed = items.remove(snippetToRemove);

        if (removed) {
            notifyListeners();
        }

        return removed;
    }

    @Override
    public boolean containsFile(@NotNull VirtualFile file) {
        return items.stream().anyMatch(item -> item.getVirtualFile().equals(file));
    }

    @Override
    public int getFileCount() {
        return (int) items.stream()
                .map(FileContextItem::getVirtualFile)
                .distinct()
                .count();
    }

    @Override
    public int getSnippetCount() {
        return (int) items.stream()
                .filter(FileContextItem::isSnippet)
                .count();
    }

    @Override
    public void clear() {
        if (!items.isEmpty()) {
            items.clear();
            notifyListeners();
        }
    }

    @Override
    public void addChangeListener(@NotNull FilesChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeListener(@NotNull FilesChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (FilesChangeListener listener : listeners) {
            try {
                listener.onFilesChanged();
            } catch (Exception e) {
                LOG.error("Error notifying file change listener", e);
            }
        }
    }

    public void removeFiles(@NotNull List<FileContextItem> selectedItems) {
        boolean anyRemoved = false;
        for (FileContextItem item : selectedItems) {
            if (items.remove(item)) {
                anyRemoved = true;
            }
        }
        if (anyRemoved) {
            notifyListeners();
        }
    }

    @Override
    public List<FileContextItem> getSortedItems() {
        return items.stream().sorted().collect(Collectors.toList());
    }
}