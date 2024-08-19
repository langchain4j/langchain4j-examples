import dev.langchain4j.agent.tool.*;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModelName;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;

import java.util.*;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static java.util.stream.Collectors.toList;


public class MistralAiFunctionCallingExamples {

    static class Payment_Data_From_AiServices {

        static ChatLanguageModel mistralAiModel = MistralAiChatModel.builder()
                .apiKey(System.getenv("MISTRAL_AI_API_KEY")) // Please use your own Mistral AI API key
                .modelName(MistralAiChatModelName.MISTRAL_LARGE_LATEST.toString())
                .build();

        interface Assistant {
            @SystemMessage({
                    "You are a payment transaction support agent.",
                    "You MUST use the payment transaction tool to search the payment transaction data.",
                    "If there a date convert it in a human readable format."
            })
            String chat(String userMessage);
        }

        public static void main(String[] args) {
            // STEP 1: User specify tools and query
            // User define all the necessary tools to be used in the chat
            // This example uses the Payment_Transaction_Tool who define two functions as our two tools
            Payment_Transaction_Tool paymentTool = Payment_Transaction_Tool.build();
            // User define the query to be used in the chat
            String userMessage = "What is the status and the payment date of transaction T1005?";

            // STEP 2: User asks the agent and AiServices call to the functions
            Assistant agent = AiServices.builder(Assistant.class)
                    .chatLanguageModel(mistralAiModel)
                    .tools(paymentTool)
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .build();

            // STEP 3: User gets the final response from the agent
            String answer = agent.chat(userMessage);
            System.out.println(answer); //According to the payment transaction tool, the payment status of transaction T1005 is Pending and the payment date is 2021-10-08.
        }

    }

    static class Payment_Data_From_Manual_Configuration{

            static ChatLanguageModel mistralAiModel = MistralAiChatModel.builder()
                    .apiKey(System.getenv("MISTRAL_AI_API_KEY")) // Please use your own Mistral AI API key
                    .modelName(MistralAiChatModelName.MISTRAL_LARGE_LATEST.toString())
                    .logRequests(true)
                    .logResponses(true)
                    .build();

            public static void main(String[] args) throws Exception {
                // This sample retrieve payment status as shown in this Mistral AI tutorial: https://docs.mistral.ai/guides/function-calling/

                // STEP 1: User specify tools and query
                // Tools
                Payment_Transaction_Tool paymentTool = Payment_Transaction_Tool.build();
                List<ToolSpecification>  tools = ToolSpecifications.toolSpecificationsFrom(paymentTool);
                // User query
                List<ChatMessage> chatMessages = new ArrayList<>();
                UserMessage userMessage = userMessage("What is the status of transaction T1005?");
                chatMessages.add(userMessage);

                // STEP 2: Model generate function arguments
                // Tool_choice: With multiple tools it's set to "auto" by default.
                AiMessage aiMessage = mistralAiModel.generate(chatMessages,tools).content();
                aiMessage.toolExecutionRequests().forEach(toolSpec -> { // return all tools to call to answer the user query
                    System.out.println("Function name: " + toolSpec.name());
                    System.out.println("Function args:" + toolSpec.arguments());
                });
                chatMessages.add(aiMessage);

                // STEP 3: User execute function to obtain tool results
                // Tool execution results is become into user tool messages (ToolExecutionResultMessage).
                List<ToolExecutionResultMessage> toolExecutionResultMessages = toolExecutor(paymentTool, aiMessage.toolExecutionRequests());
                chatMessages.addAll(toolExecutionResultMessages);

                // STEP 4: Model generate final response
                AiMessage finalResponse = mistralAiModel.generate(chatMessages).content();
                System.out.println(finalResponse.text()); //According to the payment data, the payment status of transaction T1005 is Pending.
            }

            private static List<ToolExecutionResultMessage> toolExecutor(
                    Object objectWithTools,
                    List<ToolExecutionRequest> toolExecutionRequests) throws Exception {
                String memoryId = UUID.randomUUID().toString();
                return toolExecutionRequests.stream()
                        .map(request -> {
                            try {
                                ToolExecutor toolExecutor = new DefaultToolExecutor(objectWithTools,
                                                objectWithTools.getClass().getDeclaredMethod(request.name(),
                                                String.class));
                                return ToolExecutionResultMessage.from(request, toolExecutor.execute(request, memoryId));
                            } catch (NoSuchMethodException e) {
                                System.err.println("No such tool found: " + request.name());
                            }
                            return null;
                        })
                        .collect(toList());
            }
    }

    static class Payment_Transaction_Tool {
        static Payment_Transaction_Tool build(){
            return new Payment_Transaction_Tool();
        }

        // Tool to be executed by mistral model to get payment status
        @Tool("Get payment status of a transaction") // function description
        static String retrievePaymentStatus(@P("Transaction id to search payment data") String transactionId) {
           return getPaymentDataField(transactionId, "payment_status");
        }

        // Tool to be executed by mistral model to get payment date
        @Tool("Get payment date of a transaction") // function description
        static String retrievePaymentDate(@P("Transaction id to search payment data") String transactionId) {
            return getPaymentDataField(transactionId, "payment_date");
        }

        private static Map<String, List<String>> getPaymentData() {
            Map<String, List<String>> data = new HashMap<>();
            data.put("transaction_id", Arrays.asList("T1001", "T1002", "T1003", "T1004", "T1005"));
            data.put("customer_id", Arrays.asList("C001", "C002", "C003", "C002", "C001"));
            data.put("payment_amount", Arrays.asList("125.50", "89.99", "120.00", "54.30", "210.20"));
            data.put("payment_date", Arrays.asList("2021-10-05", "2021-10-06", "2021-10-07", "2021-10-05", "2021-10-08"));
            data.put("payment_status", Arrays.asList("Paid", "Unpaid", "Paid", "Paid", "Pending"));
            return data;
        }

        private static String getPaymentDataField(String transactionId, String data) {
            List<String> transactionIds = getPaymentData().get("transaction_id");
            List<String> paymentData = getPaymentData().get(data);

            int index = transactionIds.indexOf(transactionId);
            if (index != -1) {
                return paymentData.get(index);
            } else {
                return "Transaction ID not found";
            }
        }
    }
}
