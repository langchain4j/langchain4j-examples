package util;

import dev.langchain4j.agentic.scope.AgenticScope;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgenticScopePrinter {

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
                        String escaped = valueStr.replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                                .replace("\n", "\\n")
                                .replace("\r", "\\r")
                                .replace("\t", "\\t");
                        sb.append("\"").append(escaped).append("\"");
                    } else {
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

    public static String printConversation(String conversation, int maxChars) {
        if (conversation == null || conversation.isEmpty()) {
            return "(empty conversation)";
        }

        String[] parts = conversation.split("(?m)(?=^User:|^\\w+\\s+agent:)"); // <-- fixed
        StringBuilder sb = new StringBuilder();

        Pattern agentPattern = Pattern.compile("^(\\w+)\\s+agent:(.*)$", Pattern.DOTALL);

        for (String part : parts) {
            if (part.trim().isEmpty()) continue;

            Matcher agentMatcher = agentPattern.matcher(part.trim());
            if (agentMatcher.matches()) {
                String agentType = agentMatcher.group(1);
                String content = agentMatcher.group(2).trim();

                sb.append(agentType).append(" agent:");
                if (!content.isEmpty()) {
                    if (content.length() > maxChars) {
                        sb.append(" ").append(content, 0, maxChars).append(" [truncated...]");
                    } else {
                        sb.append(" ").append(content);
                    }
                }
            } else if (part.startsWith("User:")) {
                String content = part.substring(5).trim();
                sb.append("User:");
                if (!content.isEmpty()) {
                    if (content.length() > maxChars) {
                        sb.append(" ").append(content, 0, maxChars).append(" [truncated...]");
                    } else {
                        sb.append(" ").append(content);
                    }
                }
            } else {
                if (part.length() > maxChars) {
                    sb.append(part, 0, maxChars).append(" [truncated...]");
                } else {
                    sb.append(part);
                }
            }
            sb.append("\n\n");
        }

        return sb.toString().trim();
    }

}