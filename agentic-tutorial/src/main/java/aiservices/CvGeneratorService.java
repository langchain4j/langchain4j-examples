package aiservices;
// TODO remove this, just for testing differences between agents and aiServices


import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import model.Cv;

public interface CvGeneratorService {
    @UserMessage("""
            generate a recipe this user will like: {{userInfo}}
            """)
    Cv generateCv(@V("userInfo") String userInfo);

}
