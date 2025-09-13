package com.vittoriomattei.contextfetcher.test;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.vittoriomattei.contextfetcher.services.FilesChangeListener;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Base test class with common test utilities
 */
@RunWith(JUnit4.class)
public abstract class FileAggregatorTestBase extends BasePlatformTestCase {

    protected VirtualFile createTestFile(String name, String content) {
        return myFixture.addFileToProject(name, content).getVirtualFile();
    }

    protected static class TestChangeListener implements FilesChangeListener {
        private final List<String> events = new ArrayList<>();
        private final CountDownLatch latch;

        public TestChangeListener(int expectedEvents) {
            this.latch = new CountDownLatch(expectedEvents);
        }

        @Override
        public void onFilesChanged() {
            events.add("changed");
            latch.countDown();
        }

        public boolean waitForEvents(long timeoutMs) throws InterruptedException {
            return latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        }
        public int getEventCount() { return events.size(); }
    }
}