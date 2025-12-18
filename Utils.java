import java.io.IOException;
import java.util.Random;

public class Utils {
    
    private static boolean ENABLE_LOGGING = true; 
    public static Random RNG = new Random(1L); 
    private static final Logger LOGGER = new Logger();

    static {
        enableLogging();
    }

    public static void enableLogging() {
        Utils.ENABLE_LOGGING = true;
        try {
            LOGGER.init("./ausgabe.txt");
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void disableLogging() {
        Utils.ENABLE_LOGGING = false;
        LOGGER.stop();
    }

    public static void log(String text) {
        if (Utils.ENABLE_LOGGING) {
            Utils.LOGGER.log(text);
        }
    }

    public static boolean isLoggingEnabled() {
        return ENABLE_LOGGING;
    }

}
