import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    private FileWriter writer;

    /**
     * Startet den Logger und überschreibt die Datei.
     */
    public void init(String path) throws IOException {
        if (writer == null) {
            writer = new FileWriter(path, false);
        }
    }

    /**
     * Loggt einen Text synchron in die Datei.
     */
    public void log(String text) {
        if (writer != null && text != null) {
            try {
                writer.write(text + "\n");
                writer.flush(); // sofort schreiben
            } catch (IOException e) {
                System.err.println("Logger write failed: " + e);
            }
        }
    }

    /**
     * Schließt den Logger sauber.
     */
    public void stop() {
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                System.err.println("Logger close failed: " + e);
            } finally {
                writer = null;
            }
        }
    }
}
