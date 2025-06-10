package it.dev.langchan4j.example;

public class Util {

	private static String hfApiKey = System.getenv("HUGGING_FACE_API_KEY");
	private static String githubApiKey = System.getenv("GITHUB_API_KEY");
	private static String ollamaBaseUrl = System.getenv("OLLAMA_BASE_URL");
	private static String mistralAiApiKey = System.getenv("MISTRAL_AI_API_KEY");

	public static boolean usingHuggingFace() {
		return hfApiKey != null && hfApiKey.startsWith("hf_");
	}

	public static boolean usingGithub() {
		return githubApiKey != null && githubApiKey.startsWith("ghp_");
	}

	public static boolean usingOllama() {
		return ollamaBaseUrl != null && ollamaBaseUrl.startsWith("http");
	}

	public static boolean usingMistralAi() {
		return mistralAiApiKey != null && mistralAiApiKey.length() > 30;
	}

}
