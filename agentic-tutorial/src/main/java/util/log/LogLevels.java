package util.log;

/**
 * Enumeration of available logging levels for the agentic tutorial.
 * Provides clean, simple control over logging behavior.
 */
public enum LogLevels {
    /**
     * No logging output - silent execution
     */
    NONE,
    
    /**
     * Beautiful, clean logs showing agent conversations and tool calls
     * Perfect for learning and understanding agent behavior
     */
    PRETTY,
    
    /**
     * Full HTTP request/response dumps
     * Useful for debugging low-level issues
     */
    DEBUG,
    
    /**
     * Standard INFO level logging
     * Shows basic information without HTTP details
     */
    INFO
}
