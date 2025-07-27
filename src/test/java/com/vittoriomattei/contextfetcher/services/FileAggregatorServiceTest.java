package com.vittoriomattei.contextfetcher.services;

import com.intellij.openapi.vfs.VirtualFile;
import com.vittoriomattei.contextfetcher.model.FileEntry;
import com.vittoriomattei.contextfetcher.model.LineRange;
import com.vittoriomattei.contextfetcher.test.FileAggregatorTestBase;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class FileAggregatorServiceTest extends FileAggregatorTestBase {

    private FileAggregatorServiceImpl service;
    private VirtualFile testFile1;
    private VirtualFile testFile2;
    private String uniqueDir;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        service = new FileAggregatorServiceImpl();
        uniqueDir = "testRun_" + System.currentTimeMillis();
        testFile1 = createTestFile(uniqueDir + "/test1.java", "public class Test1 {\n    // content\n}");
        testFile2 = createTestFile(uniqueDir + "/test2.java", "public class Test2 {\n    // content\n}");
    }

    @Test
    public void testAddSingleFile() {
        assertTrue(service.addFile(testFile1));
        assertEquals(1, service.getFileCount());
        assertTrue(service.containsFile(testFile1));

        // Adding same file again should return false
        assertFalse(service.addFile(testFile1));
        assertEquals(1, service.getFileCount());
    }

    @Test
    public void testAddMultipleFiles() {
        List<VirtualFile> files = List.of(testFile1, testFile2);
        int added = service.addFiles(files);

        assertEquals(2, added);
        assertEquals(2, service.getFileCount());
        assertTrue(service.containsFile(testFile1));
        assertTrue(service.containsFile(testFile2));
    }

    @Test
    public void testAddSnippet() {
        LineRange range = new LineRange(1, 5);
        assertTrue(service.addSnippet(testFile1, range));

        Optional<FileEntry> entry = service.getFileEntry(testFile1);
        assertTrue(entry.isPresent());
        assertFalse(entry.get().isCompleteFile());
        assertTrue(entry.get().getSnippets().contains(range));
    }

    @Test
    public void testAddSnippetToExistingCompleteFile() {
        // First add as complete file
        service.addFile(testFile1);
        Optional<FileEntry> entry = service.getFileEntry(testFile1);
        assertTrue(entry.isPresent());
        assertTrue(entry.get().isCompleteFile());

        // Then add snippet - should convert to snippet-based
        LineRange range = new LineRange(1, 5);
        service.addSnippet(testFile1, range);

        entry = service.getFileEntry(testFile1);
        assertTrue(entry.isPresent());
        assertFalse(entry.get().isCompleteFile());
        assertTrue(entry.get().getSnippets().contains(range));
    }

    @Test
    public void testRemoveFile() {
        service.addFile(testFile1);
        assertTrue(service.removeFile(testFile1));
        assertEquals(0, service.getFileCount());
        assertFalse(service.containsFile(testFile1));

        // Removing non-existent file should return false
        assertFalse(service.removeFile(testFile2));
    }

    @Test
    public void testRemoveSnippet() {
        LineRange range1 = new LineRange(1, 5);
        LineRange range2 = new LineRange(10, 15);

        service.addSnippet(testFile1, range1);
        service.addSnippet(testFile1, range2);

        assertTrue(service.removeSnippet(testFile1, range1));

        Optional<FileEntry> entry = service.getFileEntry(testFile1);
        assertTrue(entry.isPresent());
        assertFalse(entry.get().getSnippets().contains(range1));
        assertTrue(entry.get().getSnippets().contains(range2));
    }

    @Test
    public void testRemoveLastSnippetRemovesFile() {
        LineRange range = new LineRange(1, 5);
        service.addSnippet(testFile1, range);

        assertTrue(service.removeSnippet(testFile1, range));
        assertFalse(service.containsFile(testFile1));
        assertEquals(0, service.getFileCount());
    }

    @Test
    public void testClear() {
        service.addFile(testFile1);
        service.addSnippet(testFile2, new LineRange(1, 5));

        assertEquals(2, service.getFileCount());

        service.clear();

        assertEquals(0, service.getFileCount());
        assertFalse(service.containsFile(testFile1));
        assertFalse(service.containsFile(testFile2));
    }

    @Test
    public void testChangeNotifications() throws InterruptedException {
        TestChangeListener listener = new TestChangeListener(3);
        service.addChangeListener(listener);

        service.addFile(testFile1);
        service.addSnippet(testFile2, new LineRange(1, 5));
        service.clear();

        assertTrue(listener.waitForEvents(1000));
        assertEquals(3, listener.getEventCount());
    }

    @Test
    public void testNullArgumentsThrowExceptions() {
        assertThrows(IllegalArgumentException.class, () -> service.addFile(null));
        assertThrows(IllegalArgumentException.class, () -> service.addFiles(null));
        assertThrows(IllegalArgumentException.class, () ->
                service.addSnippet(null, new LineRange(1, 5)));
        assertThrows(IllegalArgumentException.class, () ->
                service.addSnippet(testFile1, null));
    }

    @Test
    public void testInvalidLineRanges() {
        // Negative start line
        assertFalse(service.addSnippet(testFile1, new LineRange(-1, 5)));

        // End before start
        assertFalse(service.addSnippet(testFile1, new LineRange(10, 5)));

        assertEquals(0, service.getFileCount());
    }
}