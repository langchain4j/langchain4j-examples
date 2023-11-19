import dev.langchain4j.data.image.Image;
import dev.langchain4j.image.openai.dalle.OpenAiDalleResponse;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.image.openai.dalle.OpenAiDalleImageProcessingModel;
import dev.langchain4j.model.output.Response;

public class HelloWorldExample {

  public static void main(String[] args) {
    OpenAiDalleImageProcessingModel model = OpenAiDalleImageProcessingModel
      .builder()
      .apiKey(System.getenv("OPENAI_API_KEY"))
      .logRequests(true)
      .logResponses(true)
            .downloadFolder("/tmp")
      .build();

//    Response<Image> image = model.generate(
//      UserMessage.from(
//        //        "3D cartoon picture of man walking on a street in New York"
//        //        "Charlie." +
//        "Draw happy Charlie based on the following information:" +
//        //        "Charlie's idea had saved the day, but he humbly attributed the success to their teamwork and friendship. " +
//        "They celebrated their victory with a grand party, filled with laughter, dance, and merry games. That night, " +
//        "under the twinkling stars, they made a pact to always stand by each other, come what may. Once upon a " +
//        "time in the town of VeggieVille, there lived a cheerful carrot named Charlie. Charlie was a radiant carrot, " +
//        "always beaming with joy and positivity. His vibrant orange skin and lush green top were a sight to behold, " +
//        "but it was his infectious laughter and warm personality that really set him apart. Charlie had a diverse " +
//        "group of friends, each a vegetable with their own unique characteristics. There was Bella the blushing " +
//        "beetroot, always ready with a riddle or two; Timmy the timid tomato, a gentle soul with a heart of gold; " +
//        "and Percy the prankster potato, whose jokes always brought a smile to everyone's faces. Despite their " +
//        "differences, they shared a close bond, their friendship as robust as their natural goodness."
//      )
//    );
    Response<Image> image = model.generate(
        //        "3D cartoon picture of man walking on a street in New York"
        //        "Charlie." +
//        "Hand drawn picture of Alaric, draw him based on the following information:\n" +
//        //        "Charlie's idea had saved the day, but he humbly attributed the success to their teamwork and friendship. " +
//                "In Eldoria, Sir Alaric, a valiant knight, harbored silent sorrow after a dragon battle. Alone in the moonlit courtyard, he confided in loyal squire Theo. Their shared vulnerability forged a bond, lifting the knight's burden. Together, they faced the kingdom's challenges, finding solace and strength beneath the stars."

            "Donald Duck in New York, cartoon style"
    );

    System.out.println(image.content().getUrl()); // TBD
  }
}
