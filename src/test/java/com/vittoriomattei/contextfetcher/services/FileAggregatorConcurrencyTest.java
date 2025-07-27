package com.vittoriomattei.contextfetcher.services;

import com.intellij.openapi.vfs.VirtualFile;
import com.vittoriomattei.contextfetcher.model.LineRange;
import com.vittoriomattei.contextfetcher.test.FileAggregatorTestBase;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FileAggregatorConcurrencyTest extends FileAggregatorTestBase {

    private FileAggregatorServiceImpl service;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        service = new FileAggregatorServiceImpl();
    }

    @Test
    public void testConcurrentFileAddition() throws Exception {
        int threadCount = 10;
        int filesPerThread = 100;
        // Pre-create files on the main test thread using myFixture
        List<VirtualFile> allFiles = new ArrayList<>(threadCount * filesPerThread);
        for (int t = 0; t < threadCount; t++) {
            for (int i = 0; i < filesPerThread; i++) {
                var file = myFixture
                        .addFileToProject("thread" + t + "_file" + i + ".java", "class Test{}")
                        .getVirtualFile();
                allFiles.add(file);
            }
        }

        // Now test concurrent addition of these files
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<Integer>> futures = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            Future<Integer> future = executor.submit(() -> {
                try {
                    int added = 0;
                    for (int i = 0; i < filesPerThread; i++) {
                        int index = threadId * filesPerThread + i;
                        var file = allFiles.get(index);
                        if (service.addFile(file)) {
                            added++;
                        }
                    }
                    return added;
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        assertTrue("Latch value: " + latch.getCount(), latch.await(10, TimeUnit.SECONDS));

        int totalAdded = 0;
        for (Future<Integer> future : futures) {
            totalAdded += future.get();
        }

        assertEquals(threadCount * filesPerThread, totalAdded);
        assertEquals(threadCount * filesPerThread, service.getFileCount());

        executor.shutdown();
    }

    @Test
    public void testConcurrentSnippetManipulation() throws Exception {
        var testFile = createTestFile("concurrent.java", "class Test{\n".repeat(1000) + "}");

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Each thread adds and removes snippets
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 50; i++) {
                        LineRange range = new LineRange(threadId * 100 + i, threadId * 100 + i + 10);
                        service.addSnippet(testFile, range);
                        if (i % 2 == 0) {
                            service.removeSnippet(testFile, range);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        // Should still have the file with some snippets
        assertTrue(service.containsFile(testFile));

        executor.shutdown();
    }

    @Test
    public void testListenerNotificationOrder() throws InterruptedException {
        TestChangeListener listener = new TestChangeListener(100);
        service.addChangeListener(listener);

        // Rapid changes
        for (int i = 0; i < 100; i++) {
            var file = createTestFile("rapid" + i + ".java", "class Test{}");
            service.addFile(file);
        }

        assertTrue(listener.waitForEvents(5000));
        assertEquals(100, listener.getEventCount());
    }
}