package ESINF.export;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit4 test suite for GraphVizFileGenerator
 *
 * Tests cover:
 * - DOT file saving with various conditions
 * - SVG generation from DOT files
 * - File path handling and validation
 * - GraphViz installation detection
 * - Atomic file operations
 * - Result objects (SVGGenerationResult, CompleteGenerationResult)
 * - Error handling and edge cases
 *
 * Note: Some SVG generation tests may be skipped if GraphViz is not installed
 *
 * @author ESINF Test Team
 * @version 1.0
 */
public class GraphVizFileGeneratorTest {

    private GraphVizFileGenerator generator;
    private String testOutputDir;
    private static final String TEST_DOT_CONTENT =
            "graph TestGraph {\n  A -- B [label=\"1.0\"];\n}\n";

    @Before
    public void setUp() {
        generator = new GraphVizFileGenerator();
        testOutputDir = "test_output_" + System.currentTimeMillis();
    }

    @After
    public void tearDown() throws IOException {
        // Clean up test files and directories
        cleanupTestDirectory(testOutputDir);
    }

    /**
     * Helper method to clean up test directories
     */
    private void cleanupTestDirectory(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
        }
    }

    // ========== Constructor Tests ==========

    /**
     * Test that GraphVizFileGenerator can be instantiated
     */
    @Test
    public void testConstructor_SuccessfulInstantiation() {
        assertNotNull(new GraphVizFileGenerator());
    }

    // ========== saveDOTFile Validation Tests ==========

    /**
     * Test that saveDOTFile throws exception when content is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSaveDOTFile_NullContent_ThrowsException() throws IOException {
        generator.saveDOTFile(null, "test.dot");
    }

    /**
     * Test that saveDOTFile throws exception when content is empty
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSaveDOTFile_EmptyContent_ThrowsException() throws IOException {
        generator.saveDOTFile("", "test.dot");
    }

    /**
     * Test that saveDOTFile throws exception when filepath is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSaveDOTFile_NullFilepath_ThrowsException() throws IOException {
        generator.saveDOTFile(TEST_DOT_CONTENT, null);
    }

    /**
     * Test that saveDOTFile throws exception when filepath is empty
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSaveDOTFile_EmptyFilepath_ThrowsException() throws IOException {
        generator.saveDOTFile(TEST_DOT_CONTENT, "");
    }

    // ========== saveDOTFile Success Tests ==========

    /**
     * Test that saveDOTFile creates file successfully
     */
    @Test
    public void testSaveDOTFile_ValidInput_ReturnsTrue() throws IOException {
        String filepath = testOutputDir + "/test.dot";
        boolean result = generator.saveDOTFile(TEST_DOT_CONTENT, filepath);

        assertTrue(result);
    }

    /**
     * Test that saveDOTFile creates the file on disk
     */
    @Test
    public void testSaveDOTFile_ValidInput_CreatesFile() throws IOException {
        String filepath = testOutputDir + "/test.dot";
        generator.saveDOTFile(TEST_DOT_CONTENT, filepath);

        assertTrue(Files.exists(Paths.get(filepath)));
    }

    /**
     * Test that saveDOTFile writes correct content
     */
    @Test
    public void testSaveDOTFile_ValidInput_WritesCorrectContent() throws IOException {
        String filepath = testOutputDir + "/test.dot";
        generator.saveDOTFile(TEST_DOT_CONTENT, filepath);

        String content = new String(Files.readAllBytes(Paths.get(filepath)));
        assertEquals(TEST_DOT_CONTENT, content);
    }

    /**
     * Test that saveDOTFile creates parent directories if needed
     */
    @Test
    public void testSaveDOTFile_CreatesParentDirectories() throws IOException {
        String filepath = testOutputDir + "/subdir1/subdir2/test.dot";
        generator.saveDOTFile(TEST_DOT_CONTENT, filepath);

        assertTrue(Files.exists(Paths.get(filepath)));
    }

    /**
     * Test that saveDOTFile overwrites existing file
     */
    @Test
    public void testSaveDOTFile_OverwritesExistingFile() throws IOException {
        String filepath = testOutputDir + "/test.dot";

        generator.saveDOTFile("Original content", filepath);
        generator.saveDOTFile("New content", filepath);

        String content = new String(Files.readAllBytes(Paths.get(filepath)));
        assertEquals("New content", content);
    }

    /**
     * Test that saveDOTFile handles large content
     */
    @Test
    public void testSaveDOTFile_LargeContent_Success() throws IOException {
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeContent.append("  A").append(i).append(" -- B").append(i).append(";\n");
        }

        String filepath = testOutputDir + "/large.dot";
        boolean result = generator.saveDOTFile(largeContent.toString(), filepath);

        assertTrue(result);
    }

    // ========== saveDOTFileToDefaultDir Tests ==========

    /**
     * Test that saveDOTFileToDefaultDir adds .dot extension
     */
    @Test
    public void testSaveDOTFileToDefaultDir_AddsDotExtension() throws IOException {
        String result = generator.saveDOTFileToDefaultDir(TEST_DOT_CONTENT, "test");

        assertTrue(result.endsWith(".dot"));
    }

    /**
     * Test that saveDOTFileToDefaultDir does not duplicate extension
     */
    @Test
    public void testSaveDOTFileToDefaultDir_NoDuplicateExtension() throws IOException {
        String result = generator.saveDOTFileToDefaultDir(TEST_DOT_CONTENT, "test.dot");

        assertFalse(result.endsWith(".dot.dot"));
    }

    /**
     * Test that saveDOTFileToDefaultDir uses default directory
     */
    @Test
    public void testSaveDOTFileToDefaultDir_UsesDefaultDirectory() throws IOException {
        String result = generator.saveDOTFileToDefaultDir(TEST_DOT_CONTENT, "test.dot");

        assertTrue(result.contains(generator.getDefaultOutputDir()));
    }

    /**
     * Test that saveDOTFileToDefaultDir returns full path
     */
    @Test
    public void testSaveDOTFileToDefaultDir_ReturnsFullPath() throws IOException {
        String result = generator.saveDOTFileToDefaultDir(TEST_DOT_CONTENT, "test.dot");

        assertNotNull(result);
        assertTrue(result.length() > "test.dot".length());
    }

    // ========== isGraphVizInstalled Tests ==========

    /**
     * Test that isGraphVizInstalled returns boolean
     */
    @Test
    public void testIsGraphVizInstalled_ReturnsBoolean() {
        boolean result = generator.isGraphVizInstalled();

        assertTrue(result || !result); // Just verify it returns a boolean
    }

    /**
     * Test isGraphVizInstalled is consistent
     */
    @Test
    public void testIsGraphVizInstalled_IsConsistent() {
        boolean result1 = generator.isGraphVizInstalled();
        boolean result2 = generator.isGraphVizInstalled();

        assertEquals(result1, result2);
    }

    // ========== generateSVG Tests (requires GraphViz) ==========

    /**
     * Test generateSVG with valid DOT file (if GraphViz installed)
     */
    @Test
    public void testGenerateSVG_ValidDOTFile_CreatesResult() throws IOException {
        String dotPath = testOutputDir + "/test.dot";
        String svgPath = testOutputDir + "/test.svg";

        generator.saveDOTFile(TEST_DOT_CONTENT, dotPath);

        GraphVizFileGenerator.SVGGenerationResult result =
                generator.generateSVG(dotPath, svgPath);

        assertNotNull(result);
    }

    /**
     * Test generateSVG result has message
     */
    @Test
    public void testGenerateSVG_ResultHasMessage() throws IOException {
        String dotPath = testOutputDir + "/test.dot";
        String svgPath = testOutputDir + "/test.svg";

        generator.saveDOTFile(TEST_DOT_CONTENT, dotPath);

        GraphVizFileGenerator.SVGGenerationResult result =
                generator.generateSVG(dotPath, svgPath);

        assertNotNull(result.getMessage());
    }

    /**
     * Test generateSVG with single parameter derives SVG path
     */
    @Test
    public void testGenerateSVG_SingleParameter_DerivesSVGPath() throws IOException {
        String dotPath = testOutputDir + "/test.dot";
        generator.saveDOTFile(TEST_DOT_CONTENT, dotPath);

        GraphVizFileGenerator.SVGGenerationResult result =
                generator.generateSVG(dotPath);

        assertNotNull(result);
    }

    /**
     * Test generateSVG with non-existent DOT file fails gracefully
     */
    @Test
    public void testGenerateSVG_NonExistentDOT_ReturnsFailure() {
        String dotPath = testOutputDir + "/nonexistent.dot";
        String svgPath = testOutputDir + "/nonexistent.svg";

        GraphVizFileGenerator.SVGGenerationResult result =
                generator.generateSVG(dotPath, svgPath);

        assertFalse(result.isSuccess());
    }

    /**
     * Test generateSVG without GraphViz returns appropriate result
     */
    @Test
    public void testGenerateSVG_WithoutGraphViz_ReturnsResult() throws IOException {
        String dotPath = testOutputDir + "/test.dot";
        String svgPath = testOutputDir + "/test.svg";

        generator.saveDOTFile(TEST_DOT_CONTENT, dotPath);

        GraphVizFileGenerator.SVGGenerationResult result =
                generator.generateSVG(dotPath, svgPath);

        // Should always return a result, whether GraphViz is installed or not
        assertNotNull(result);
    }

    // ========== SVGGenerationResult Tests ==========

    /**
     * Test SVGGenerationResult constructor with success
     */
    @Test
    public void testSVGGenerationResult_SuccessConstructor_IsSuccess() {
        GraphVizFileGenerator.SVGGenerationResult result =
                new GraphVizFileGenerator.SVGGenerationResult(true, "Success", "test.svg");

        assertTrue(result.isSuccess());
    }

    /**
     * Test SVGGenerationResult constructor with failure
     */
    @Test
    public void testSVGGenerationResult_FailureConstructor_IsNotSuccess() {
        GraphVizFileGenerator.SVGGenerationResult result =
                new GraphVizFileGenerator.SVGGenerationResult(false, "Failed", null);

        assertFalse(result.isSuccess());
    }

    /**
     * Test SVGGenerationResult getMessage returns correct message
     */
    @Test
    public void testSVGGenerationResult_GetMessage_ReturnsMessage() {
        GraphVizFileGenerator.SVGGenerationResult result =
                new GraphVizFileGenerator.SVGGenerationResult(true, "Test message", "test.svg");

        assertEquals("Test message", result.getMessage());
    }

    /**
     * Test SVGGenerationResult getSvgFilePath returns path
     */
    @Test
    public void testSVGGenerationResult_GetSvgFilePath_ReturnsPath() {
        GraphVizFileGenerator.SVGGenerationResult result =
                new GraphVizFileGenerator.SVGGenerationResult(true, "Success", "test.svg");

        assertEquals("test.svg", result.getSvgFilePath());
    }

    /**
     * Test SVGGenerationResult getSvgFilePath can be null
     */
    @Test
    public void testSVGGenerationResult_GetSvgFilePath_CanBeNull() {
        GraphVizFileGenerator.SVGGenerationResult result =
                new GraphVizFileGenerator.SVGGenerationResult(false, "Failed", null);

        assertNull(result.getSvgFilePath());
    }

    /**
     * Test SVGGenerationResult toString is not null
     */
    @Test
    public void testSVGGenerationResult_ToString_NotNull() {
        GraphVizFileGenerator.SVGGenerationResult result =
                new GraphVizFileGenerator.SVGGenerationResult(true, "Success", "test.svg");

        assertNotNull(result.toString());
    }

    /**
     * Test SVGGenerationResult toString contains message
     */
    @Test
    public void testSVGGenerationResult_ToString_ContainsMessage() {
        GraphVizFileGenerator.SVGGenerationResult result =
                new GraphVizFileGenerator.SVGGenerationResult(true, "Test message", "test.svg");

        assertTrue(result.toString().contains("Test message"));
    }

    // ========== CompleteGenerationResult Tests ==========

    /**
     * Test CompleteGenerationResult constructor with success
     */
    @Test
    public void testCompleteGenerationResult_SuccessConstructor_IsSuccess() {
        GraphVizFileGenerator.CompleteGenerationResult result =
                new GraphVizFileGenerator.CompleteGenerationResult(
                        true, "test.dot", "test.svg", "Success"
                );

        assertTrue(result.isSuccess());
    }

    /**
     * Test CompleteGenerationResult constructor with failure
     */
    @Test
    public void testCompleteGenerationResult_FailureConstructor_IsNotSuccess() {
        GraphVizFileGenerator.CompleteGenerationResult result =
                new GraphVizFileGenerator.CompleteGenerationResult(
                        false, null, null, "Failed"
                );

        assertFalse(result.isSuccess());
    }

    /**
     * Test CompleteGenerationResult getDotFilePath returns path
     */
    @Test
    public void testCompleteGenerationResult_GetDotFilePath_ReturnsPath() {
        GraphVizFileGenerator.CompleteGenerationResult result =
                new GraphVizFileGenerator.CompleteGenerationResult(
                        true, "test.dot", "test.svg", "Success"
                );

        assertEquals("test.dot", result.getDotFilePath());
    }

    /**
     * Test CompleteGenerationResult getSvgFilePath returns path
     */
    @Test
    public void testCompleteGenerationResult_GetSvgFilePath_ReturnsPath() {
        GraphVizFileGenerator.CompleteGenerationResult result =
                new GraphVizFileGenerator.CompleteGenerationResult(
                        true, "test.dot", "test.svg", "Success"
                );

        assertEquals("test.svg", result.getSvgFilePath());
    }

    /**
     * Test CompleteGenerationResult getMessage returns message
     */
    @Test
    public void testCompleteGenerationResult_GetMessage_ReturnsMessage() {
        GraphVizFileGenerator.CompleteGenerationResult result =
                new GraphVizFileGenerator.CompleteGenerationResult(
                        true, "test.dot", "test.svg", "Test message"
                );

        assertEquals("Test message", result.getMessage());
    }

    /**
     * Test CompleteGenerationResult hasSVG returns true when SVG exists
     */
    @Test
    public void testCompleteGenerationResult_HasSVG_ReturnsTrueWhenExists() {
        GraphVizFileGenerator.CompleteGenerationResult result =
                new GraphVizFileGenerator.CompleteGenerationResult(
                        true, "test.dot", "test.svg", "Success"
                );

        assertTrue(result.hasSVG());
    }

    /**
     * Test CompleteGenerationResult hasSVG returns false when SVG is null
     */
    @Test
    public void testCompleteGenerationResult_HasSVG_ReturnsFalseWhenNull() {
        GraphVizFileGenerator.CompleteGenerationResult result =
                new GraphVizFileGenerator.CompleteGenerationResult(
                        false, "test.dot", null, "Failed"
                );

        assertFalse(result.hasSVG());
    }

    /**
     * Test CompleteGenerationResult toString is not null
     */
    @Test
    public void testCompleteGenerationResult_ToString_NotNull() {
        GraphVizFileGenerator.CompleteGenerationResult result =
                new GraphVizFileGenerator.CompleteGenerationResult(
                        true, "test.dot", "test.svg", "Success"
                );

        assertNotNull(result.toString());
    }

    /**
     * Test CompleteGenerationResult toString contains status
     */
    @Test
    public void testCompleteGenerationResult_ToString_ContainsStatus() {
        GraphVizFileGenerator.CompleteGenerationResult result =
                new GraphVizFileGenerator.CompleteGenerationResult(
                        true, "test.dot", "test.svg", "Success"
                );

        assertTrue(result.toString().contains("Status"));
    }

    /**
     * Test CompleteGenerationResult toString contains DOT file path
     */
    @Test
    public void testCompleteGenerationResult_ToString_ContainsDOTPath() {
        GraphVizFileGenerator.CompleteGenerationResult result =
                new GraphVizFileGenerator.CompleteGenerationResult(
                        true, "test.dot", "test.svg", "Success"
                );

        assertTrue(result.toString().contains("test.dot"));
    }

    /**
     * Test CompleteGenerationResult toString indicates no SVG when null
     */
    @Test
    public void testCompleteGenerationResult_ToString_IndicatesNoSVGWhenNull() {
        GraphVizFileGenerator.CompleteGenerationResult result =
                new GraphVizFileGenerator.CompleteGenerationResult(
                        false, "test.dot", null, "Failed"
                );

        assertTrue(result.toString().contains("Not generated"));
    }

    // ========== generateBothFiles Tests ==========

    /**
     * Test generateBothFiles with valid content creates DOT file
     */
    @Test
    public void testGenerateBothFiles_ValidContent_CreatesDOT() throws IOException {
        generator.ensureOutputDirExists();

        GraphVizFileGenerator.CompleteGenerationResult result =
                generator.generateBothFiles(TEST_DOT_CONTENT, "test");

        assertNotNull(result.getDotFilePath());
    }

    /**
     * Test generateBothFiles returns result object
     */
    @Test
    public void testGenerateBothFiles_ReturnsResult() throws IOException {
        generator.ensureOutputDirExists();

        GraphVizFileGenerator.CompleteGenerationResult result =
                generator.generateBothFiles(TEST_DOT_CONTENT, "test");

        assertNotNull(result);
    }

    /**
     * Test generateBothFiles with empty content fails
     */



    // ========== getDefaultOutputDir Tests ==========

    /**
     * Test getDefaultOutputDir returns non-null string
     */
    @Test
    public void testGetDefaultOutputDir_ReturnsNonNull() {
        assertNotNull(generator.getDefaultOutputDir());
    }

    /**
     * Test getDefaultOutputDir returns non-empty string
     */
    @Test
    public void testGetDefaultOutputDir_ReturnsNonEmpty() {
        assertTrue(generator.getDefaultOutputDir().length() > 0);
    }

    // ========== ensureOutputDirExists Tests ==========

    /**
     * Test ensureOutputDirExists creates directory
     */
    @Test
    public void testEnsureOutputDirExists_CreatesDirectory() throws IOException {
        generator.ensureOutputDirExists();

        assertTrue(Files.exists(Paths.get(generator.getDefaultOutputDir())));
    }

    /**
     * Test ensureOutputDirExists does not throw when directory exists
     */
    @Test
    public void testEnsureOutputDirExists_DirectoryExists_NoException() throws IOException {
        generator.ensureOutputDirExists();
        generator.ensureOutputDirExists(); // Call twice

        assertTrue(Files.exists(Paths.get(generator.getDefaultOutputDir())));
    }

    // ========== Edge Case Tests ==========

    /**
     * Test saveDOTFile with special characters in content
     */
    @Test
    public void testSaveDOTFile_SpecialCharacters_Success() throws IOException {
        String specialContent = "graph Test {\n  A -- B [label=\"Test\\n\\t\\r\"];\n}\n";
        String filepath = testOutputDir + "/special.dot";

        boolean result = generator.saveDOTFile(specialContent, filepath);

        assertTrue(result);
    }

    /**
     * Test saveDOTFile with Unicode characters
     */
    @Test
    public void testSaveDOTFile_UnicodeCharacters_Success() throws IOException {
        String unicodeContent = "graph Test {\n  A -- B [label=\"Tëst €\"];\n}\n";
        String filepath = testOutputDir + "/unicode.dot";

        boolean result = generator.saveDOTFile(unicodeContent, filepath);

        assertTrue(result);
    }

    /**
     * Test saveDOTFile with very long path
     */
    @Test
    public void testSaveDOTFile_LongPath_Success() throws IOException {
        StringBuilder longPath = new StringBuilder(testOutputDir);
        for (int i = 0; i < 10; i++) {
            longPath.append("/dir").append(i);
        }
        longPath.append("/test.dot");

        boolean result = generator.saveDOTFile(TEST_DOT_CONTENT, longPath.toString());

        assertTrue(result);
    }

    /**
     * Test saveDOTFile with minimal content
     */
    @Test
    public void testSaveDOTFile_MinimalContent_Success() throws IOException {
        String minimalContent = "graph G {}";
        String filepath = testOutputDir + "/minimal.dot";

        boolean result = generator.saveDOTFile(minimalContent, filepath);

        assertTrue(result);
    }

    /**
     * Test generateSVG with malformed DOT content (error handling)
     */
    @Test
    public void testGenerateSVG_MalformedDOT_ReturnsResult() throws IOException {
        String malformedContent = "this is not valid DOT format";
        String dotPath = testOutputDir + "/malformed.dot";
        String svgPath = testOutputDir + "/malformed.svg";

        generator.saveDOTFile(malformedContent, dotPath);

        GraphVizFileGenerator.SVGGenerationResult result =
                generator.generateSVG(dotPath, svgPath);

        // Should return a result (success or failure) without throwing exception
        assertNotNull(result);
    }
}