package com.vittoriomattei.contextfetcher.services;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.vittoriomattei.contextfetcher.model.FileContextItem;
import com.vittoriomattei.contextfetcher.model.FileEntry;
import com.vittoriomattei.contextfetcher.model.LineRange;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service(Service.Level.PROJECT)
public final class FileAggregatorServiceImpl implements FileAggregatorService {

    private static final Logger LOG = Logger.getInstance(FileAggregatorServiceImpl.class);

    private final ConcurrentHashMap<VirtualFile, FileEntry> fileEntries = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<FilesChangeListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public boolean addFile(@NotNull VirtualFile file) {
        Objects.requireNonNull(file, "File cannot be null");

        if (!file.isValid()) {
            LOG.warn("Attempted to add invalid file: " + file.getPath());
            return false;
        }

        FileEntry newEntry = FileEntry.completeFile(file);
        FileEntry existing = fileEntries.putIfAbsent(file, newEntry);

        if (existing == null) {
            notifyListeners();
            return true;
        }

        return false;
    }

    @Override
    public int addFiles(@NotNull Iterable<VirtualFile> files) {
        Objects.requireNonNull(files, "Files cannot be null");

        int addedCount = 0;
        boolean anyAdded = false;

        for (VirtualFile file : files) {
            if (file != null && file.isValid()) {
                FileEntry newEntry = FileEntry.completeFile(file);
                if (fileEntries.putIfAbsent(file, newEntry) == null) {
                    addedCount++;
                    anyAdded = true;
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

        FileEntry updatedEntry = fileEntries.compute(file, (k, existing) -> {
            if (existing == null) {
                return FileEntry.withSnippets(file, Set.of(lineRange));
            }

            // Check if snippet already exists
            if (existing.getSnippets().contains(lineRange)) {
                return existing; // No change
            }

            return existing.addSnippet(lineRange);
        });

        notifyListeners();
        return true;
    }

    @Override
    public @NotNull List<FileEntry> getFileEntries() {
        return new ArrayList<>(fileEntries.values());
    }

    @Override
    public @NotNull Optional<FileEntry> getFileEntry(@NotNull VirtualFile file) {
        return Optional.ofNullable(fileEntries.get(file));
    }

    @Override
    public boolean removeFile(@NotNull VirtualFile file) {
        FileEntry removed = fileEntries.remove(file);
        if (removed != null) {
            notifyListeners();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeSnippet(@NotNull VirtualFile file, @NotNull LineRange lineRange) {
        Objects.requireNonNull(file, "File cannot be null");
        Objects.requireNonNull(lineRange, "LineRange cannot be null");

        boolean[] changed = {false};

        fileEntries.computeIfPresent(file, (k, existing) -> {
            FileEntry updated = existing.removeSnippet(lineRange);
            if (updated != existing) {
                changed[0] = true;
            }
            return updated; // null if entry should be removed entirely
        });

        if (changed[0]) {
            notifyListeners();
        }

        return changed[0];
    }

    @Override
    public boolean containsFile(@NotNull VirtualFile file) {
        return fileEntries.containsKey(file);
    }

    @Override
    public int getFileCount() {
        return fileEntries.size();
    }

    @Override
    public int getSnippetCount() {
        return fileEntries.values().stream()
                .mapToInt(entry -> entry.getSnippets().size())
                .sum();
    }

    @Override
    public void clear() {
        if (!fileEntries.isEmpty()) {
            fileEntries.clear();
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
        for (FileContextItem item : selectedItems) {
            if (item.isSnippet()) {
                assert item.getLineRange() != null;
                removeSnippet(item.getVirtualFile(), item.getLineRange());
            } else {
                removeFile(item.getVirtualFile());
            }
        }
    }
}