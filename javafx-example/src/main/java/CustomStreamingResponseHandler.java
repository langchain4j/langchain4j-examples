import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CustomStreamingResponseHandler implements StreamingResponseHandler<AiMessage> {

    private static final Logger LOGGER = LogManager.getLogger(CustomStreamingResponseHandler.class);

    private final SearchAction action;

    public CustomStreamingResponseHandler(SearchAction action) {
        this.action = action;
    }

    @Override
    public void onNext(String token) {
        Platform.runLater(() -> action.appendAnswer(token));
    }

    @Override
    public void onComplete(Response<AiMessage> response) {
        Platform.runLater(() -> {
            LOGGER.info("Answer is complete for '" + action.getQuestion() + "', size: "
                    + action.getAnswer().length());
            action.setFinished();
        });
    }

    @Override
    public void onError(Throwable error) {
        Platform.runLater(() -> {
            LOGGER.error("Error while receiving answer: " + error.getMessage());
            action.appendAnswer("\nSomething went wrong: " + error.getMessage());
            action.setFinished();
        });
    }
}
