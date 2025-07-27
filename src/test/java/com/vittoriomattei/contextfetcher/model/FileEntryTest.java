package com.vittoriomattei.contextfetcher.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class FileEntryTest {

    @Mock
    private VirtualFile mockFile;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockFile.getPath()).thenReturn("/test/file.java");
    }

    @Test
    public void testCompleteFileCreation() {
        FileEntry entry = FileEntry.completeFile(mockFile);

        assertTrue(entry.isCompleteFile());
        assertFalse(entry.hasSnippets());
        assertTrue(entry.getSnippets().isEmpty());
        assertEquals(mockFile, entry.getVirtualFile());
    }

    @Test
    public void testSnippetFileCreation() {
        LineRange range1 = new LineRange(1, 10);
        LineRange range2 = new LineRange(20, 30);
        Set<LineRange> snippets = Set.of(range1, range2);

        FileEntry entry = FileEntry.withSnippets(mockFile, snippets);

        assertFalse(entry.isCompleteFile());
        assertTrue(entry.hasSnippets());
        assertEquals(2, entry.getSnippets().size());
        assertTrue(entry.getSnippets().contains(range1));
        assertTrue(entry.getSnippets().contains(range2));
    }

    @Test
    public void testSnippetFileCreationWithEmptySnippets() {
        assertThrows(IllegalArgumentException.class, () ->
                FileEntry.withSnippets(mockFile, Set.of()));
    }

    @Test
    public void testAddSnippetToCompleteFile() {
        FileEntry completeFile = FileEntry.completeFile(mockFile);
        LineRange range = new LineRange(1, 10);

        FileEntry withSnippet = completeFile.addSnippet(range);

        assertFalse(withSnippet.isCompleteFile());
        assertTrue(withSnippet.hasSnippets());
        assertEquals(1, withSnippet.getSnippets().size());
        assertTrue(withSnippet.getSnippets().contains(range));

        // Original should be unchanged
        assertTrue(completeFile.isCompleteFile());
    }

    @Test
    public void testAddSnippetToSnippetFile() {
        LineRange existing = new LineRange(1, 10);
        FileEntry snippetFile = FileEntry.withSnippets(mockFile, Set.of(existing));

        LineRange newRange = new LineRange(20, 30);
        FileEntry updated = snippetFile.addSnippet(newRange);

        assertEquals(2, updated.getSnippets().size());
        assertTrue(updated.getSnippets().contains(existing));
        assertTrue(updated.getSnippets().contains(newRange));
    }

    @Test
    public void testRemoveSnippet() {
        LineRange range1 = new LineRange(1, 10);
        LineRange range2 = new LineRange(20, 30);
        FileEntry entry = FileEntry.withSnippets(mockFile, Set.of(range1, range2));

        FileEntry updated = entry.removeSnippet(range1);

        assertNotNull(updated);
        assertEquals(1, updated.getSnippets().size());
        assertTrue(updated.getSnippets().contains(range2));
        assertFalse(updated.getSnippets().contains(range1));
    }

    @Test
    public void testRemoveLastSnippetReturnsNull() {
        LineRange range = new LineRange(1, 10);
        FileEntry entry = FileEntry.withSnippets(mockFile, Set.of(range));

        FileEntry updated = entry.removeSnippet(range);

        assertNull(updated);
    }

    @Test
    public void testNullFileThrowsException() {
        assertThrows(NullPointerException.class, () ->
                FileEntry.completeFile(null));
    }
}