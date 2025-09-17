package util.log;

public class CustomLogging {
    
    private static LogLevels currentLevel = LogLevels.NONE;
    private static int charLimit = 100;
    
    public static void setLevel(LogLevels level) {
        currentLevel = level;
        configureLogging();
    }
    
    public static void setLevel(LogLevels level, int charLimit) {
        currentLevel = level;
        CustomLogging.charLimit = charLimit;
        configureLogging();
    }
    
    public static LogLevels getLevel() {
        return currentLevel;
    }
    
    public static int getCharLimit() {
        return charLimit;
    }
    
    private static void configureLogging() {
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
    
    public static boolean isPrettyLogging() {
        return currentLevel == LogLevels.PRETTY;
    }
    
    public static boolean isDebugLogging() {
        return currentLevel == LogLevels.DEBUG;
    }
}