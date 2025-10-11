package ESINF.IO;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Logger determinístico usado para registar erros, avisos e info durante a importação.
 * Produz mensagens formatadas: [FILENAME:LINE] LEVEL | mensagem
 */
public class ImportLogger {

    private final List<String> logs = new ArrayList<>();

    public void log(String file, int line, String level, String message) {
        String entry = String.format("[%s:%d] %s | %s", file, line, level.toUpperCase(), message);
        logs.add(entry);
        System.out.println(entry);
    }

    public void info(String file, int line, String message) {
        log(file, line, "INFO", message);
    }

    public void warning(String file, int line, String message) {
        log(file, line, "WARNING", message);
    }

    public void error(String file, int line, String message) {
        log(file, line, "ERROR", message);
    }

    public void saveToFile(String filename) throws IOException {
        try (FileWriter fw = new FileWriter(filename)) {
            for (String s : logs) {
                fw.write(s + System.lineSeparator());
            }
        }
    }

    public List<String> getLogs() {
        return logs;
    }
}
