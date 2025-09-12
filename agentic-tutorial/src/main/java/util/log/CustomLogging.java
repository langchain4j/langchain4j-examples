package util.log;

/**
 * Centralized logging configuration for the agentic tutorial.
 * Provides a simple, clean API for controlling log output.
 */
public class CustomLogging {
    
    private static LogLevels currentLevel = LogLevels.NONE;
    private static int charLimit = 100;
    
    /**
     * Sets the logging level for the application.
     * This should be called at the top of each tutorial class.
     * 
     * @param level the desired logging level
     */
    public static void setLevel(LogLevels level) {
        currentLevel = level;
        configureLogging();
    }
    
    /**
     * Sets the logging level and character limit for the application.
     * This should be called at the top of each tutorial class.
     * 
     * @param level the desired logging level
     * @param charLimit the maximum number of characters to show in truncated logs
     */
    public static void setLevel(LogLevels level, int charLimit) {
        currentLevel = level;
        CustomLogging.charLimit = charLimit;
        configureLogging();
    }
    
    /**
     * Gets the current logging level.
     * 
     * @return the current logging level
     */
    public static LogLevels getLevel() {
        return currentLevel;
    }
    
    /**
     * Gets the current character limit for truncated logs.
     * 
     * @return the current character limit
     */
    public static int getCharLimit() {
        return charLimit;
    }
    
    /**
     * Configures the logging system based on the current level.
     * This is called automatically when setLevel() is called.
     */
    private static void configureLogging() {
        // Turn off Logback's own logging BEFORE it initializes
        System.setProperty("logback.statusListenerClass", "ch.qos.logback.core.status.NopStatusListener");
        
        switch (currentLevel) {
            case NONE:
                System.setProperty("logback.configurationFile", "log/logback-none.xml");
                break;
            case PRETTY:
                System.setProperty("logback.configurationFile", "log/logback-beautiful.xml");
                System.out.println("Pretty logging enabled - showing clean agent conversations");
                break;
            case DEBUG:
                System.setProperty("logback.configurationFile", "log/logback-full.xml");
                System.out.println("Debug logging enabled - showing complete HTTP logs");
                break;
            case INFO:
                System.setProperty("logback.configurationFile", "log/logback-info.xml");
                System.out.println("Info logging enabled - showing basic information");
                break;
        }
    }
    
    /**
     * Convenience method to check if pretty logging is enabled.
     * 
     * @return true if pretty logging is enabled
     */
    public static boolean isPrettyLogging() {
        return currentLevel == LogLevels.PRETTY;
    }
    
    /**
     * Convenience method to check if debug logging is enabled.
     * 
     * @return true if debug logging is enabled
     */
    public static boolean isDebugLogging() {
        return currentLevel == LogLevels.DEBUG;
    }
}
