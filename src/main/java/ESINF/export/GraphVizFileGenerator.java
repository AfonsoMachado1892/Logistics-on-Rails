package ESINF.export;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

/**
 * File generator for GraphViz DOT and SVG files (robust)
 */
public class GraphVizFileGenerator {

    private static final String DEFAULT_OUTPUT_DIR = "output/usei12";
    private static final String GRAPHVIZ_NEATO = "neato";
    private static final long TIMEOUT_MINUTES = 5; // Timeout para neato

    /**
     * Saves DOT content to a file (atomic-ish: write to temp then move)
     *
     * @param dotContent the DOT format string
     * @param filepath path to save the file
     * @return true if successful, false otherwise
     * @throws IOException if file operation fails
     */
    public boolean saveDOTFile(String dotContent, String filepath) throws IOException {
        if (dotContent == null || dotContent.isEmpty()) {
            throw new IllegalArgumentException("DOT content cannot be null or empty");
        }

        if (filepath == null || filepath.isEmpty()) {
            throw new IllegalArgumentException("Filepath cannot be null or empty");
        }

        Path path = Paths.get(filepath);
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // Write to temporary file first to avoid partial writes
        Path temp = Files.createTempFile(parentDir != null ? parentDir : Paths.get("."), "dot_tmp_", ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            writer.write(dotContent);
        }

        // Move atomically if possible, otherwise replace
        try {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        }

        return Files.exists(path);
    }

    /**
     * Saves DOT file to default output directory
     *
     * @param dotContent the DOT format string
     * @param filename filename (without path)
     * @return the full path where file was saved
     * @throws IOException if file operation fails
     */
    public String saveDOTFileToDefaultDir(String dotContent, String filename) throws IOException {
        if (!filename.toLowerCase().endsWith(".dot")) {
            filename += ".dot";
        }
        String fullPath = DEFAULT_OUTPUT_DIR + "/" + filename;
        saveDOTFile(dotContent, fullPath);
        return fullPath;
    }

    /**
     * Generates SVG file from DOT file using GraphViz "neato" (synchronous version)
     *
     * @param dotFilePath path to the DOT file
     * @param svgFilePath path where SVG should be saved
     * @return SVGGenerationResult with status and messages
     */
    public SVGGenerationResult generateSVG(String dotFilePath, String svgFilePath) {
        if (!isGraphVizInstalled()) {
            return new SVGGenerationResult(false, "GraphViz 'neato' not found in PATH", null);
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    GRAPHVIZ_NEATO,
                    "-n",  // Adicionado: Modo no-layout para posições fixas
                    "-Tsvg",
                    "-Goverlap=false",
                    dotFilePath,
                    "-o",
                    svgFilePath
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Consumir output em thread separado para evitar deadlock
            Thread outputConsumer = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Pode logar se quiser: System.out.println(line);
                    }
                } catch (IOException ignored) {
                }
            });
            outputConsumer.start();

            // Espera com timeout
            boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
            outputConsumer.join();  // Espera consumer terminar

            if (finished) {
                int exitCode = process.exitValue();
                if (exitCode == 0) {
                    Path svgPath = Paths.get(svgFilePath);
                    if (Files.exists(svgPath)) {
                        return new SVGGenerationResult(true, "SVG generated successfully at: " + svgFilePath, svgFilePath);
                    } else {
                        return new SVGGenerationResult(false, "neato completed but SVG file was not created.", null);
                    }
                } else {
                    return new SVGGenerationResult(false, "neato failed with exit code " + exitCode, null);
                }
            } else {
                process.destroyForcibly();
                return new SVGGenerationResult(false, "neato timed out after " + TIMEOUT_MINUTES + " minutes.", null);
            }
        } catch (IOException | InterruptedException e) {
            return new SVGGenerationResult(false, "Error executing neato: " + e.getMessage(), null);
        }
    }

    public SVGGenerationResult generateSVG(String dotFilePath) {
        String svgFilePath = dotFilePath.replaceAll("\\.dot$", ".svg");
        return generateSVG(dotFilePath, svgFilePath);
    }

    /**
     * Generates SVG asynchronously in a background thread
     *
     * @param dotFilePath path to the DOT file
     * @param svgFilePath path where SVG should be saved
     * @param callback callback to notify when done
     */
    public void generateSVGAsync(String dotFilePath, String svgFilePath, SVGCallback callback) {
        new Thread(() -> {
            SVGGenerationResult result = generateSVG(dotFilePath, svgFilePath);
            if (callback != null) {
                callback.onComplete(result);
            }
        }).start();
    }

    public void generateSVGAsync(String dotFilePath, SVGCallback callback) {
        String svgFilePath = dotFilePath.replaceAll("\\.dot$", ".svg");
        generateSVGAsync(dotFilePath, svgFilePath, callback);
    }

    /**
     * Checks if GraphViz is installed on the system (tries to run 'neato -V')
     *
     * @return true if GraphViz/neato is available, false otherwise
     */
    public boolean isGraphVizInstalled() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(GRAPHVIZ_NEATO, "-V");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                // Apenas verifica se roda
            }
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public String getDefaultOutputDir() {
        return DEFAULT_OUTPUT_DIR;
    }

    public void ensureOutputDirExists() throws IOException {
        Path path = Paths.get(DEFAULT_OUTPUT_DIR);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    public static class SVGGenerationResult {
        private final boolean success;
        private final String message;
        private final String svgFilePath;

        public SVGGenerationResult(boolean success, String message, String svgFilePath) {
            this.success = success;
            this.message = message;
            this.svgFilePath = svgFilePath;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getSvgFilePath() {
            return svgFilePath;
        }

        @Override
        public String toString() {
            if (success) {
                return "✓ " + message;
            } else {
                return "✗ " + message;
            }
        }
    }

    /**
     * Convenience method that saves DOT and generates SVG
     */
    public CompleteGenerationResult generateBothFiles(String dotContent, String baseName) {
        try {
            ensureOutputDirExists();
            String dotPath = saveDOTFileToDefaultDir(dotContent, baseName + ".dot");
            SVGGenerationResult svgResult = generateSVG(dotPath);

            return new CompleteGenerationResult(
                    svgResult.isSuccess(),
                    dotPath,
                    svgResult.getSvgFilePath(),
                    svgResult.getMessage()
            );
        } catch (IOException e) {
            return new CompleteGenerationResult(false, null, null, "Failed to generate files: " + e.getMessage());
        }
    }

    public static class CompleteGenerationResult {
        private final boolean success;
        private final String dotFilePath;
        private final String svgFilePath;
        private final String message;

        public CompleteGenerationResult(boolean success, String dotFilePath,
                                        String svgFilePath, String message) {
            this.success = success;
            this.dotFilePath = dotFilePath;
            this.svgFilePath = svgFilePath;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getDotFilePath() {
            return dotFilePath;
        }

        public String getSvgFilePath() {
            return svgFilePath;
        }

        public String getMessage() {
            return message;
        }

        public boolean hasSVG() {
            return svgFilePath != null;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== File Generation Result ===\n");
            sb.append("Status: ").append(success ? "Success" : "Failed").append("\n");

            if (dotFilePath != null) {
                sb.append("DOT file: ").append(dotFilePath).append("\n");
            }

            if (svgFilePath != null) {
                sb.append("SVG file: ").append(svgFilePath).append("\n");
            } else {
                sb.append("SVG file: Not generated\n");
            }

            sb.append("Message: ").append(message);

            return sb.toString();
        }
    }

    // Interface para callback assíncrono
    public interface SVGCallback {
        void onComplete(SVGGenerationResult result);
    }
}