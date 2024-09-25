import dev.langchain4j.model.github.GitHubModelsChatModel;

import static dev.langchain4j.model.github.GitHubModelsChatModelName.GPT_4_O_MINI;

public class GitHubModelsChatModelExamples {

    static class Simple_Prompt {

        public static void main(String[] args) {

            GitHubModelsChatModel model = GitHubModelsChatModel.builder()
                    .gitHubToken(System.getenv("GITHUB_TOKEN"))
                    .modelName(GPT_4_O_MINI)
                    .logRequestsAndResponses(true)
                    .build();

            String response = model.generate("Provide 3 short bullet points explaining why Java is awesome");

            System.out.println(response);
        }
    }
}
