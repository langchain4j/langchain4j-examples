package util.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing and formatting LangChain4j HTTP logs into beautiful, readable output.
 * Extracts user messages, assistant responses, and tool calls from verbose HTTP logs.
 */
public class LogParser {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // No state tracking - we only show what's NEW in each request/response
    
    /**
     * Truncates a string to show first and last characters with clear truncation indicators.
     * 
     * @param input the string to truncate
     * @return truncated string with clear truncation indicators on separate lines
     */
    public static String truncateString(String input) {
        int maxChars = CustomLogging.getCharLimit();
        if (input == null || input.length() <= maxChars) {
            return input;
        }
        
        int firstHalf = maxChars / 2;
        int secondHalf = maxChars / 2;
        
        return input.substring(0, firstHalf) + "\n[... truncated ...]\n" + 
               input.substring(input.length() - secondHalf);
    }
    
    /**
     * Logs a user message.
     */
    public static void logUserMessage(String userMessage) {
        System.out.println("USER: " + truncateString(userMessage));
        System.out.println(); // 2 newlines for clear separation
        System.out.println();
    }
    
    /**
     * Logs an assistant response.
     */
    public static void logAssistantResponse(String response) {
        System.out.println("MODEL: " + truncateString(response));
        System.out.println(); // 2 newlines for clear separation
        System.out.println();
    }
    
    /**
     * Logs available tools.
     */
    public static void logAvailableTools(String tools) {
        System.out.println("\tAvailable tools: " + tools);
        System.out.println(); // 2 newlines for clear separation
        System.out.println();
    }
    
    /**
     * Logs a tool call request.
     */
    public static void logToolCallRequest(String toolId, String toolName, String arguments) {
        System.out.println("MODEL REQUESTS TOOL CALL: " + toolName + " (id: " + toolId + ")");
        System.out.println("  Args: " + truncateString(arguments));
        System.out.println(); // 2 newlines for clear separation
        System.out.println();
    }
    
    /**
     * Logs a tool call result.
     */
    public static void logToolCallResult(String toolId, String toolName, String result) {
        System.out.println("TOOL RESULT: " + toolName + " (id: " + toolId + ")");
        System.out.println("  Result: " + truncateString(result));
        System.out.println(); // 2 newlines for clear separation
        System.out.println();
    }
    
    
    
    /**
     * Parses HTTP request logs to extract what's NEW in this request.
     */
    public static void parseHttpRequest(String logMessage) {
        if (!logMessage.contains("HTTP request:") || !logMessage.contains("- body:")) {
            return;
        }
        
        try {
            String jsonBody = extractJsonFromLog(logMessage);
            if (jsonBody == null) return;
            
            JsonNode root = objectMapper.readTree(jsonBody);
            JsonNode messages = root.get("messages");
            JsonNode tools = root.get("tools");
            
            if (messages == null || !messages.isArray()) return;
            
            // Find the LAST message in the conversation (what's new)
            JsonNode lastMessage = messages.get(messages.size() - 1);
            if (lastMessage == null) return;
            
            String role = lastMessage.get("role").asText();
            
            if ("user".equals(role)) {
                // New user question
                String content = lastMessage.get("content").asText();
                if (content != null && !content.isEmpty()) {
                    logUserMessage(content);
                }
                
                // Show available tools AFTER user message when tools are present
                if (tools != null && tools.isArray() && tools.size() > 0) {
                    StringBuilder toolNames = new StringBuilder();
                    for (JsonNode tool : tools) {
                        if (toolNames.length() > 0) toolNames.append(", ");
                        toolNames.append(tool.get("function").get("name").asText());
                    }
                    logAvailableTools(toolNames.toString());
                }
            } else if ("tool".equals(role)) {
                // New tool result
                String toolCallId = lastMessage.get("tool_call_id").asText();
                String content = lastMessage.get("content").asText();
                String toolName = extractToolNameFromHistory(messages, toolCallId);
                logToolCallResult(toolCallId, toolName, content);
            } else if ("assistant".equals(role)) {
                // Check if this is a final response (not a tool call)
                JsonNode toolCalls = lastMessage.get("tool_calls");
                if (toolCalls == null || !toolCalls.isArray() || toolCalls.size() == 0) {
                    String content = lastMessage.get("content").asText();
                    if (content != null && !content.isEmpty()) {
                        logAssistantResponse(content);
                    }
                }
            }
            
        } catch (Exception e) {
            // Ignore parsing errors
        }
    }
    
    /**
     * Extracts tool name from conversation history by matching tool_call_id.
     */
    private static String extractToolNameFromHistory(JsonNode messages, String toolCallId) {
        for (JsonNode message : messages) {
            String role = message.get("role").asText();
            if ("assistant".equals(role)) {
                JsonNode toolCalls = message.get("tool_calls");
                if (toolCalls != null && toolCalls.isArray()) {
                    for (JsonNode toolCall : toolCalls) {
                        String id = toolCall.get("id").asText();
                        if (toolCallId.equals(id)) {
                            return toolCall.get("function").get("name").asText();
                        }
                    }
                }
            }
        }
        return "unknown";
    }
    
    /**
     * Parses HTTP response logs to extract what's NEW in this response.
     */
    public static void parseHttpResponse(String logMessage) {
        if (!logMessage.contains("HTTP response:") || !logMessage.contains("- body:")) {
            return;
        }
        
        try {
            String jsonBody = extractJsonFromLog(logMessage);
            if (jsonBody == null) return;
            
            JsonNode root = objectMapper.readTree(jsonBody);
            JsonNode choices = root.get("choices");
            
            if (choices == null || !choices.isArray() || choices.size() == 0) return;
            
            JsonNode message = choices.get(0).get("message");
            String content = message.get("content").asText();
            
            // Check for tool calls first
            JsonNode toolCalls = message.get("tool_calls");
            if (toolCalls != null && toolCalls.isArray() && toolCalls.size() > 0) {
                // New tool call requests
                for (JsonNode toolCall : toolCalls) {
                    String toolId = toolCall.get("id").asText();
                    String toolName = toolCall.get("function").get("name").asText();
                    String arguments = toolCall.get("function").get("arguments").asText();
                    logToolCallRequest(toolId, toolName, arguments);
                }
            } else if (content != null && !content.isEmpty()) {
                // New assistant response (no tool calls)
                logAssistantResponse(content);
            }
            
        } catch (Exception e) {
            // Ignore parsing errors
        }
    }
    
    /**
     * Extracts JSON content from log messages.
     */
    private static String extractJsonFromLog(String logMessage) {
        // Find the JSON body after "- body:"
        Pattern pattern = Pattern.compile("- body:\\s*(.*?)(?=\\n\\n|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(logMessage);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }
    
}
