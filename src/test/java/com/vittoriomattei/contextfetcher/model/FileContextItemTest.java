package com.vittoriomattei.contextfetcher.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class FileContextItemTest {

    @Mock
    private VirtualFile mockFile1;
    
    @Mock
    private VirtualFile mockFile2;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockFile1.getName()).thenReturn("TestFile1.java");
        when(mockFile1.getPresentableName()).thenReturn("TestFile1.java");
        when(mockFile2.getName()).thenReturn("TestFile2.java");
        when(mockFile2.getPresentableName()).thenReturn("TestFile2.java");
    }

    @Test
    public void testWholeFileCreation() {
        FileContextItem item = FileContextItem.wholeFile(mockFile1);

        assertFalse(item.isSnippet());
        assertEquals(mockFile1, item.getVirtualFile());
        assertEquals(new LineRange(0, -1), item.getLineRange());
        assertEquals("TestFile1.java", item.getPresentableName());
    }

    @Test
    public void testSnippetCreation() {
        LineRange range = new LineRange(5, 10);
        FileContextItem item = FileContextItem.snippet(mockFile1, range);

        assertTrue(item.isSnippet());
        assertEquals(mockFile1, item.getVirtualFile());
        assertEquals(range, item.getLineRange());
        assertEquals("TestFile1.java", item.getPresentableName());
    }

    @Test
    public void testEqualsAndHashCode() {
        LineRange range1 = new LineRange(1, 5);
        LineRange range2 = new LineRange(1, 5);
        LineRange range3 = new LineRange(2, 6);

        FileContextItem item1 = FileContextItem.snippet(mockFile1, range1);
        FileContextItem item2 = FileContextItem.snippet(mockFile1, range2);
        FileContextItem item3 = FileContextItem.snippet(mockFile1, range3);
        FileContextItem item4 = FileContextItem.wholeFile(mockFile1);

        // Equal snippets
        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());

        // Different ranges
        assertNotEquals(item1, item3);
        assertNotEquals(item1.hashCode(), item3.hashCode());

        // Snippet vs whole file (same file, different types)
        assertNotEquals(item1, item4);
    }

    @Test
    public void testCompareTo() {
        // Same file, different types
        FileContextItem wholeFile = FileContextItem.wholeFile(mockFile1);
        FileContextItem snippet = FileContextItem.snippet(mockFile1, new LineRange(1, 5));

        // Whole files come before snippets for same file
        assertTrue(wholeFile.compareTo(snippet) < 0);
        assertTrue(snippet.compareTo(wholeFile) > 0);

        // Different files - alphabetical by name
        FileContextItem file2Item = FileContextItem.wholeFile(mockFile2);
        assertTrue(wholeFile.compareTo(file2Item) < 0); // TestFile1 < TestFile2

        // Same file, both snippets - compare by line ranges
        FileContextItem snippet1 = FileContextItem.snippet(mockFile1, new LineRange(1, 5));
        FileContextItem snippet2 = FileContextItem.snippet(mockFile1, new LineRange(10, 15));
        assertTrue(snippet1.compareTo(snippet2) < 0); // Earlier lines come first
    }

    @Test
    public void testToString() {
        FileContextItem wholeFile = FileContextItem.wholeFile(mockFile1);
        FileContextItem snippet = FileContextItem.snippet(mockFile1, new LineRange(1, 5));

        String wholeFileStr = wholeFile.toString();
        String snippetStr = snippet.toString();

        assertTrue(wholeFileStr.contains("TestFile1.java"));
        assertTrue(wholeFileStr.contains("isSnippet=false"));

        assertTrue(snippetStr.contains("TestFile1.java"));
        assertTrue(snippetStr.contains("isSnippet=true"));
        assertTrue(snippetStr.contains("lineRange=1 - 5"));
    }
}