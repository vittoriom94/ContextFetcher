package com.vittoriomattei.contextfetcher.services;

import com.intellij.openapi.vfs.VirtualFile;
import com.vittoriomattei.contextfetcher.model.FileEntry;
import com.vittoriomattei.contextfetcher.model.LineRange;
import com.vittoriomattei.contextfetcher.test.FileAggregatorTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class ContextGeneratorServiceTest extends FileAggregatorTestBase {

    private VirtualFile javaFile;
    private VirtualFile textFile;
    private String uniqueDir;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        uniqueDir = "testRun_" + System.currentTimeMillis();

        javaFile = createTestFile(uniqueDir + "/Example.java", """
                package com.example;
                
                public class Example {
                    private String name;
                   \s
                    public Example(String name) {
                        this.name = name;
                    }
                   \s
                    public String getName() {
                        return name;
                    }
                }""");

        textFile = createTestFile(uniqueDir + "/README.md", """
                # Project Title
                
                This is a sample project.
                
                ## Installation
                
                Run the following commands:
                ```
                npm install
                ```""");
    }

    @Test
    public void testGenerateContextWithCompleteFiles() {
        List<FileEntry> files = new ArrayList<>();
        files.add(new FileEntry(javaFile, Set.of(), true));
        files.add(new FileEntry(textFile, Set.of(), true));


        ContextGeneratorService generator = new ContextGeneratorService(files);
        String context = generator.generateContext();

        Assert.assertNotNull(context);
        Assert.assertTrue(context.contains("# --- Code context ---"));
        Assert.assertTrue(context.contains("## File: " + javaFile.getPresentableUrl()));
        Assert.assertTrue(context.contains("## File: " + textFile.getPresentableUrl()));
        Assert.assertTrue(context.contains("public class Example"));
        Assert.assertTrue(context.contains("# Project Title"));
        Assert.assertTrue(context.contains("# End of code context"));
    }

    @Test
    public void testGenerateContextWithSnippets() {

        List<FileEntry> files = new ArrayList<>();
        FileEntry fileEntry = new FileEntry(javaFile, Set.of(new LineRange(2, 4), new LineRange(9, 11)), false);
        files.add(fileEntry);

        ContextGeneratorService generator = new ContextGeneratorService(files);
        String context = generator.generateContext();

        Assert.assertNotNull(context);
        Assert.assertTrue(context.contains("### L2-4"));
        Assert.assertTrue(context.contains("### L9-11"));
        Assert.assertTrue(context.contains("public class Example"));
        Assert.assertTrue(context.contains("public String getName()"));

        // Should not contain constructor (lines 5-7)
        Assert.assertFalse(context.contains("public Example(String name)"));
    }

    @Test
    public void testGenerateContextWithMixedFiles() {


        List<FileEntry> files = new ArrayList<>();
        files.add(new FileEntry(javaFile, Set.of(new LineRange(0, 2)), false));
        files.add(new FileEntry(textFile, Set.of(), true));

        ContextGeneratorService generator = new ContextGeneratorService(files);
        String context = generator.generateContext();

        Assert.assertTrue(context.contains("### L0-2")); // Snippet format
        Assert.assertTrue(context.contains("# Project Title")); // Complete file content
    }

    @Test
    public void testGenerateContextWithEmptyFiles() {
        List<FileEntry> files = new ArrayList<>();

        ContextGeneratorService generator = new ContextGeneratorService(files);
        String context = generator.generateContext();

        Assert.assertNotNull(context);
        Assert.assertTrue(context.contains("# --- Code context ---"));
        Assert.assertTrue(context.contains("# End of code context"));
    }

    @Test
    public void testSnippetExtraction() {
        List<FileEntry> files = new ArrayList<>();
        files.add(new FileEntry(javaFile, Set.of(new LineRange(5, 7)), false));

        ContextGeneratorService generator = new ContextGeneratorService(files);
        String context = generator.generateContext();

        Assert.assertTrue(context.contains("public Example(String name)"));
        Assert.assertTrue(context.contains("this.name = name;"));

        // Should not contain other parts
        Assert.assertFalse(context.contains("public String getName()"));
    }

    @Test
    public void testInvalidLineRangeHandling() {
        List<FileEntry> files = new ArrayList<>();
        files.add(new FileEntry(javaFile, Set.of(new LineRange(-1, 2),    // Invalid start
                new LineRange(100, 200), // Beyond file length
                new LineRange(1, 3)      // Valid range

        ), false));

        ContextGeneratorService generator = new ContextGeneratorService(files);
        String context = generator.generateContext();

        // Should handle gracefully without throwing exceptions
        Assert.assertNotNull(context);
        Assert.assertTrue(context.contains("# --- Code context ---"));
    }
}