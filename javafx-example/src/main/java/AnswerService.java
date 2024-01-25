import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnswerService {

    private static final Logger LOGGER = LogManager.getLogger(AnswerService.class);

    private Assistant assistant;

    public void init(SearchAction action) {
        action.appendAnswer("Initiating...");
        initChat(action);
    }

    private void initChat(SearchAction action) {
        StreamingChatLanguageModel model = OpenAiStreamingChatModel.withApiKey(ApiKeys.OPENAI_API_KEY);

        assistant = AiServices.builder(Assistant.class)
                .streamingChatLanguageModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
        action.appendAnswer("Done");
        action.setFinished();
    }

    void ask(SearchAction action) {
        LOGGER.info("Asking question '" + action.getQuestion() + "'");

        var responseHandler = new CustomStreamingResponseHandler(action);

        assistant.chat(action.getQuestion())
                .onNext(responseHandler::onNext)
                .onComplete(responseHandler::onComplete)
                .onError(responseHandler::onError)
                .start();
    }
}