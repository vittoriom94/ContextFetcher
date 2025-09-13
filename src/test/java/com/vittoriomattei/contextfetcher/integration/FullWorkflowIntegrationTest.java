package com.vittoriomattei.contextfetcher.integration;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.vfs.VirtualFile;
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

    @Before
    public void setUp() throws Exception {
        super.setUp();
        String uniqueDir = "testRun_" + System.currentTimeMillis();
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
        // 1. Add snippet first, then more snippets (should work)
        assertTrue(aggregatorService.addSnippet(javaFile, new LineRange(4, 6))); // Just method1
        assertTrue(aggregatorService.addFile(configFile)); // Complete file

        assertEquals(2, aggregatorService.getFileCount());

        // 2. Generate context
        var filesAndSnippets = aggregatorService.getAllItems();

        ContextGeneratorService generator = new ContextGeneratorService();
        String context = generator.generateContext(new ArrayList<>(filesAndSnippets));

        // 3. Verify generated context
        assertNotNull(context);
        assertTrue(context.contains("# --- Code context ---"));
        assertTrue(context.contains("config.properties"));
        assertTrue(context.contains("Service.java"));
        assertTrue(context.contains("app.name=MyApp")); // Complete config file
        assertTrue(context.contains("Method 1")); // Snippet from java file
        assertTrue(context.contains("method1")); // Should include method1
        assertFalse(context.contains("method2")); // Should not include method2

        // 4. Add another snippet from same file - this should work
        assertTrue(aggregatorService.addSnippet(javaFile, new LineRange(8, 10))); // Add method2

        // Should still have 2 distinct files (config file + java file with 2 snippets)
        assertEquals(2, aggregatorService.getFileCount());

        // 5. Regenerate and verify both methods are present
        var updatedFiles = aggregatorService.getAllItems();
        ContextGeneratorService updatedGenerator = new ContextGeneratorService();
        String updatedContext = updatedGenerator.generateContext(new ArrayList<>(updatedFiles));

        assertTrue(updatedContext.contains("method1")); // Should still contain method1
        assertTrue(updatedContext.contains("method2")); // Now should also contain method2
    }

    @Test
    public void testWholeFileVsSnippetRules() {
        // Test 1: Add whole file first, then try to add snippet (should fail)
        assertTrue(aggregatorService.addFile(javaFile));
        assertEquals(1, aggregatorService.getFileCount());

        // This should fail because whole file is already added
        assertFalse(aggregatorService.addSnippet(javaFile, new LineRange(4, 6)));
        assertEquals(1, aggregatorService.getFileCount()); // Count unchanged

        // Clear for next test
        aggregatorService.clear();
        assertEquals(0, aggregatorService.getFileCount());

        // Test 2: Add snippets first, then try to add whole file (should fail)
        assertTrue(aggregatorService.addSnippet(javaFile, new LineRange(4, 6)));
        assertTrue(aggregatorService.addSnippet(javaFile, new LineRange(8, 10))); // Multiple snippets OK
        assertEquals(1, aggregatorService.getFileCount()); // 1 distinct file (java file)

        // This should fail because snippets already exist
        assertFalse(aggregatorService.addFile(javaFile));
        assertEquals(1, aggregatorService.getFileCount()); // Count unchanged

        // Test 3: Adding duplicate snippet should fail
        assertFalse(aggregatorService.addSnippet(javaFile, new LineRange(4, 6))); // Duplicate
        assertEquals(1, aggregatorService.getFileCount()); // Count unchanged
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
            WriteAction.runAndWait(() -> file.delete(null));
            return file;
        } catch (Exception e) {
            return null;
        }
    }
}