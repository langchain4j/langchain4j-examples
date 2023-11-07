import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;

public class HelloWorldExample {

  public static void main(String[] args) {
    OpenAiDalleChatLanguageModel model = new OpenAiDalleChatLanguageModel();

    Response<Image> image = model.generate(
            UserMessage.from("3D cartoon picture of Donald Duck walking on a street in New York")
    );

    System.out.println(image); // TBD
  }
}
