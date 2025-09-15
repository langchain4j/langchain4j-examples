package util;

import dev.langchain4j.agentic.scope.AgenticScope;

import java.util.Map;

/**
 * Utility class for pretty printing AgenticScope objects in a readable format.
 * Provides methods to display AgenticScope content with truncation and formatting.
 */
public class AgenticScopePrinter {
    
    /**
     * Returns the AgenticScope in a pretty format with default truncation (100 characters).
     * 
     * @param agenticScope the AgenticScope to format
     * @return formatted string representation
     */
    public static String printPretty(AgenticScope agenticScope) {
        return printPretty(agenticScope, 100);
    }
    
    /**
     * Returns the AgenticScope in a pretty JSON format with custom truncation.
     * 
     * @param agenticScope the AgenticScope to format
     * @param maxChars the maximum number of characters to display per field before truncation
     * @return formatted JSON string representation
     */
    public static String printPretty(AgenticScope agenticScope, int maxChars) {
        if (agenticScope == null) {
            return "null";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"memoryId\": \"").append(agenticScope.memoryId()).append("\",\n");
        sb.append("  \"state\": {\n");
        
        Map<String, Object> state = agenticScope.state();
        if (state == null || state.isEmpty()) {
            sb.append("    // empty\n");
        } else {
            int count = 0;
            for (Map.Entry<String, Object> entry : state.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (count > 0) {
                    sb.append(",\n");
                }
                
                sb.append("    \"").append(key).append("\": ");
                
                if (value == null) {
                    sb.append("null");
                } else {
                    String valueStr = value.toString();
                    if (valueStr.length() <= maxChars) {
                        // Escape quotes and newlines for JSON
                        String escaped = valueStr.replace("\\", "\\\\")
                                               .replace("\"", "\\\"")
                                               .replace("\n", "\\n")
                                               .replace("\r", "\\r")
                                               .replace("\t", "\\t");
                        sb.append("\"").append(escaped).append("\"");
                    } else {
                        // Truncate and show indication
                        String truncated = valueStr.substring(0, maxChars);
                        String escaped = truncated.replace("\\", "\\\\")
                                                 .replace("\"", "\\\"")
                                                 .replace("\n", "\\n")
                                                 .replace("\r", "\\r")
                                                 .replace("\t", "\\t");
                        sb.append("\"").append(escaped).append(" [truncated...]\"");
                    }
                }
                count++;
            }
            sb.append("\n");
        }
        sb.append("  }\n");
        sb.append("}");
        
        return sb.toString();
    }
    
    /**
     * Prints only the state of the AgenticScope in a compact format.
     * 
     * @param agenticScope the AgenticScope to print
     * @param maxChars the maximum number of characters to display per field before truncation
     */
    public static void printStateOnly(AgenticScope agenticScope, int maxChars) {
        if (agenticScope == null) {
            System.out.println("AgenticScope state: null");
            return;
        }
        
        System.out.println("=== STATE ===");
        Map<String, Object> state = agenticScope.state();
        if (state == null || state.isEmpty()) {
            System.out.println("(empty)");
        } else {
            for (Map.Entry<String, Object> entry : state.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (value == null) {
                    System.out.println(key + ": (null)");
                } else {
                    String valueStr = value.toString();
                    if (valueStr.length() <= maxChars) {
                        System.out.println(key + ": " + valueStr);
                    } else {
                        System.out.println(key + ": " + valueStr.substring(0, maxChars) + " [truncated...]");
                    }
                }
            }
        }
        System.out.println("============");
    }
}
