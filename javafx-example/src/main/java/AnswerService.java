import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.time.Duration.ofSeconds;

public class AnswerService {

    private static final Logger LOGGER = LogManager.getLogger(AnswerService.class);

    private OpenAiStreamingChatModel model;

    public void init(SearchAction action) {
        action.appendAnswer("Initiating...");
        initChat(action);
    }

    private void initChat(SearchAction action) {
        model = OpenAiStreamingChatModel.builder()
                .apiKey(ApiKeys.OPENAI_API_KEY)
                .timeout(ofSeconds(60))
                .build();
        action.appendAnswer("Done");
        action.setFinished();
    }

    void ask(SearchAction action) {
        LOGGER.info("Asking question '" + action.getQuestion() + "'");

        model.generate(action.getQuestion(), new CustomStreamingResponseHandler(action));
    }
}