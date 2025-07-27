package com.vittoriomattei.contextfetcher.integration;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.vittoriomattei.contextfetcher.model.FileEntry;
import com.vittoriomattei.contextfetcher.model.LineRange;
import com.vittoriomattei.contextfetcher.services.ContextGeneratorService;
import com.vittoriomattei.contextfetcher.services.FileAggregatorServiceImpl;
import com.vittoriomattei.contextfetcher.test.FileAggregatorTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;


public class FullWorkflowIntegrationTest extends FileAggregatorTestBase {

    private FileAggregatorServiceImpl aggregatorService;
    private VirtualFile javaFile;
    private VirtualFile configFile;

    private String uniqueDir;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        uniqueDir = "testRun_" + System.currentTimeMillis();
        aggregatorService = new FileAggregatorServiceImpl();

        javaFile = createTestFile(uniqueDir + "/Service.java",
                """
                        package com.example;
                        
                        public class Service {
                            public void method1() {
                                System.out.println("Method 1");
                            }
                           \s
                            public void method2() {
                                System.out.println("Method 2");
                            }
                        }"""
        );

        configFile = createTestFile(uniqueDir + "/config.properties",
                """
                        app.name=MyApp
                        app.version=1.0.0
                        database.url=jdbc:h2:mem:testdb"""
        );
    }

    @Test
    public void testCompleteWorkflow() {
        // 1. Add files to aggregator
        assertTrue(aggregatorService.addFile(configFile)); // Complete file
        assertTrue(aggregatorService.addSnippet(javaFile, new LineRange(2, 5))); // Just method1

        assertEquals(2, aggregatorService.getFileCount());

        // 2. Generate context
        var filesAndSnippets = aggregatorService.getFileEntries();

        ContextGeneratorService generator = new ContextGeneratorService(
                new ArrayList<>(filesAndSnippets)
        );
        String context = generator.generateContext();

        // 3. Verify generated context
        assertNotNull(context);
        assertTrue(context.contains("# --- Code context ---"));
        assertTrue(context.contains("config.properties"));
        assertTrue(context.contains("Service.java"));
        assertTrue(context.contains("app.name=MyApp")); // Complete config file
        assertTrue(context.contains("public class Service")); // Snippet from java file
        assertTrue(context.contains("method1")); // Should include method1
        assertFalse(context.contains("method2")); // Should not include method2

        // 4. Modify aggregation
        aggregatorService.addSnippet(javaFile, new LineRange(7, 9)); // Add method2

        // 5. Regenerate and verify
        var updatedFiles = aggregatorService.getFileEntries();

        ContextGeneratorService updatedGenerator = new ContextGeneratorService(
                new ArrayList<FileEntry>(updatedFiles)
        );
        String updatedContext = updatedGenerator.generateContext();

        assertTrue(updatedContext.contains("method1"));
        assertTrue(updatedContext.contains("method2")); // Now should include method2
    }

    @Test
    public void testErrorRecovery() {
        // Test with invalid file
        VirtualFile invalidFile = createDisposedFile();

        // Should handle gracefully
        Assert.assertNotNull(invalidFile);
        assertFalse(aggregatorService.addFile(invalidFile));
        assertEquals(0, aggregatorService.getFileCount());

        // Valid operations should still work
        assertTrue(aggregatorService.addFile(javaFile));
        assertEquals(1, aggregatorService.getFileCount());
    }

    private VirtualFile createDisposedFile() {
        try {
            // Simulate file being disposed/deleted
            var file = createTestFile("temp.java", "class Temp{}");
            WriteAction.runAndWait(() -> {file.delete(null);});
            return file;
        } catch (Exception e) {
            return null;
        }
    }
}