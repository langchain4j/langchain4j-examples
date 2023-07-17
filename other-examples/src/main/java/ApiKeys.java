public class ApiKeys {

    // You can use "demo" api key for demonstration purposes.
    // You can get your own OpenAI API key here: https://platform.openai.com/account/api-keys
    public static final String OPENAI_API_KEY = "demo";

    // You can get your own HuggingFace API key here: https://huggingface.co/settings/tokens
    public static final String HF_API_KEY = System.getenv("HF_API_KEY");

    // You can get your own Judge0 RapidAPI key here: https://rapidapi.com/judge0-official/api/judge0-ce
    public static final String RAPID_API_KEY = System.getenv("RAPID_API_KEY");
}
