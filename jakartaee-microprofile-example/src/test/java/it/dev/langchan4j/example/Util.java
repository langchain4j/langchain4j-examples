package it.dev.langchan4j.example;

public class Util {

	private static String hfApiKey = System.getenv("HUGGING_FACE_API_KEY");
	private static String githubApiKey = System.getenv("GITHUB_API_KEY");

	public static boolean usingHuggingFace() {
		return hfApiKey != null && hfApiKey.startsWith("hf_");
	}

	public static boolean usingGithub() {
		return githubApiKey != null && githubApiKey.startsWith("ghp_");
	}

}
