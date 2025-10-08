import dev.langchain4j.agent.tool.*;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.jlama.JlamaChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;

import java.util.*;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static java.util.stream.Collectors.toList;


public class JlamaAiFunctionCallingExamples {

    static class Payment_Data_From_AiServices {

        static ChatModel mistralAiModel = JlamaChatModel.builder()
                .modelName("tjake/Mistral-7B-Instruct-v0.3-JQ4")
                .temperature(0.0f) //Force same output every run
                .build();

        interface Assistant {
            @SystemMessage({
                    "You are a payment transaction support agent.",
                    "You MUST use the payment transaction tool to search the payment transaction data.",
                    "If there is a date, convert it in a human readable format."
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
                    .chatModel(mistralAiModel)
                    .tools(paymentTool)
                    .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                    .build();

            // STEP 3: User gets the final response from the agent
            String answer = agent.chat(userMessage);
            System.out.println(answer); //According to the payment transaction tool, the payment status of transaction T1005 is Pending and the payment date is 2021-10-08.
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
