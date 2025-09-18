package util.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class BeautifulLogAppender extends AppenderBase<ILoggingEvent> {
    
    private static final StringBuilder httpRequestBuffer = new StringBuilder();
    private static final StringBuilder httpResponseBuffer = new StringBuilder();
    private static boolean inHttpRequest = false;
    private static boolean inHttpResponse = false;
    
    @Override
    protected void append(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        String loggerName = event.getLoggerName();
        
        // Process HTTP client logs
        if (loggerName.contains("LoggingHttpClient")) {
            if (message.contains("HTTP request:")) {
                LogParser.parseHttpRequest(message);
            } else if (message.contains("HTTP response:")) {
                LogParser.parseHttpResponse(message);
            }
            return;
        }
        
        // Skip known noisy loggers completely
        if (loggerName.contains("okhttp3") ||
            loggerName.contains("com.fasterxml.jackson") ||
            loggerName.contains("ai.djl") ||
            loggerName.contains("org.apache.tika") ||
            loggerName.contains("ch.qos.logback") ||
            loggerName.contains("ch.qos.logback.classic.LoggerContext") ||
            loggerName.contains("ch.qos.logback.classic.util.ContextInitializer") ||
            loggerName.contains("ch.qos.logback.core.model.processor") ||
            loggerName.contains("ch.qos.logback.classic.joran") ||
            loggerName.contains("ch.qos.logback.classic.util.DefaultJoranConfigurator") ||
            loggerName.contains("ch.qos.logback.classic.util.SerializedModelConfigurator") ||
            loggerName.contains("ch.qos.logback.classic.util.ContextInitializer") ||
            message.contains("logback-classic version") ||
            message.contains("No custom configurators were discovered") ||
            message.contains("Trying to configure with") ||
            message.contains("Constructed configurator") ||
            message.contains("Could NOT find resource") ||
            message.contains("Found resource") ||
            message.contains("Processing appender") ||
            message.contains("About to instantiate appender") ||
            message.contains("Ignoring unknown property") ||
            message.contains("Setting level of") ||
            message.contains("Attaching appender") ||
            message.contains("End of configuration") ||
            message.contains("Registering current configuration") ||
            message.contains("call lasted") ||
            message.contains("ExecutionStatus")) {
            return;
        }
        
        // For all other logs, show with prefix for debugging
        System.out.println("UNFILTERED LOG: [" + event.getLevel() + "] [" + loggerName + "] " + message);
    }
}

