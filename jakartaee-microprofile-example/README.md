# LangChain4j in Jakarta EE and MicroProfile
This example demonstrates LangChain4J in a Jakarta EE / MicroProfile application on Open Liberty. The application is a chatbot built with LangChain4J and uses Jakarta CDI, Jakarta RESTful Web Services, Jakarta WebSocket, MicroProfile Config, MicroProfile Metrics, and MicroProfile OpenAPI features. The application allows to use models from either Hugging Face, Github, or Ollama.

## Prerequisites:

- [Java 21](https://developer.ibm.com/languages/java/semeru-runtimes/downloads)
- Either one of the following model providers:
  - Hugging Face
    - Sign up and log in to https://huggingface.co.
    - Go to [Access Tokens](https://huggingface.co/settings/tokens).
    - Create a new access token with `read` role.
  - Github
    - Sign up and sign in to https://github.com.
    - Go to your [Settings](https://github.com/settings/profile)/[Developer Settings](https://github.com/settings/developers)/[Persional access tokens](https://github.com/settings/personal-access-tokens).
    - Generate a new token
  - Ollama
    - Download and install [Ollama](https://ollama.com/download)
      - see the [README.md](https://github.com/ollama/ollama/blob/main/README.md#ollama)
    - Pull the following models
      - `ollama pull llama3.2`
      - `ollama pull all-minilm`
      - `ollama pull tinydolphin`

## Environment Set Up

To run this example application, navigate  to the `jakartaee-microprofile-example` directory:

```
cd langchain4j-examples/jakartaee-microprofile-example
```

Set the `JAVA_HOME` environment variable:
```
export JAVA_HOME=<your Java 21 home path>
```

Set the `HUGGING_FACE_API_KEY` environment variable if using Hugging Face.
```
unset GITHUB_API_KEY
unset OLLAMA_BASE_URL
export HUGGING_FACE_API_KEY=<your Hugging Face read token>
```

Set the `GITHUB_API_KEY` environment variable if using Github.
```
unset HUGGING_FACE_API_KEY
unset OLLAMA_BASE_URL
export GITHUB_API_KEY=<your Github API token>
```

Set the `OLLAMA_BASE_URL` environment variable if using Ollama. Use your Ollama URL if not using the default.
```
unset HUGGING_FACE_API_KEY
unset GITHUB_API_KEY
export OLLAMA_BASE_URL=http://localhost:11434
```

## Start the application

Use the Maven wrapper to start the application by using the [Liberty dev mode](https://openliberty.io/docs/latest/development-mode.html):

```
./mvnw liberty:dev
```

## Try out the application

- Navigate to http://localhost:9080
- At the prompt, try the following message examples:
  - ```
    What are large language models?
    ```
  - ```
    Which are the most used models?
    ```
  - ```
    show me the documentation
    ```


### Try out other models

Navigate to the the [OpenAPI UI](http://localhost:9080/openapi/ui) URL for the following 3 REST APIs:

- [HuggingFaceLanguageModel](https://github.com/langchain4j/langchain4j/blob/main/langchain4j-hugging-face/src/main/java/dev/langchain4j/model/huggingface/HuggingFaceLanguageModel.java)
  - Expand the GET `/api/model/language` API.
    1. Click the **Try it out** button.
    2. Type `When was Hugging Face launched?`, or any question, in the question field.
    3. Click the **Execute** button.
  - Alternatively, run the following `curl` command from a command-line session:
    - ```
      curl 'http://localhost:9080/api/model/language?question=When%20was%20Hugging%20Face%20launched%3F'
      ```
- [HuggingFaceChatModel](https://github.com/langchain4j/langchain4j/blob/main/langchain4j-hugging-face/src/main/java/dev/langchain4j/model/huggingface/HuggingFaceChatModel.java)
  - expand the GET `/api/model/chat` API
    1. Click the **Try it out** button.
    2. Type `Which are the most used Large Language Models?`, or any question, in the question field.
    3. Click the **Execute** button.
  - Alternatively, run the following `curl` command from a command-line session:
    - ```
      curl 'http://localhost:9080/api/model/chat?userMessage=Which%20are%20the%20most%20used%20Large%20Language%20Models%3F' | jq
      ```
- [InProcessEmbeddingModel](https://github.com/langchain4j/langchain4j-embeddings)
  - expand the GET `/api/model/similarity` API
    1. Click the **Try it out** button.
    2. Type `I like Jakarta EE and MicroProfile.`, or any text, in the the **text1** field.
    3. Type `I like Python language.`, or any text, in the the **text2** field. 
    3. Click the **Execute** button.
  - Alternatively, run the following `curl` command from a command-line session:
    - ```
      curl 'http://localhost:9080/api/model/similarity?text1=I%20like%20Jakarta%20EE%20and%20MicroProfile.&text2=I%20like%20Python%20language.' | jq
      ```


## Running the tests

Because you started Liberty in dev mode, you can run the provided tests by pressing the `enter/return` key from the command-line session where you started dev mode.

If the tests pass, you see a similar output to the following example:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running it.dev.langchan4j.example.ChatServiceIT
[INFO] ...
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.439 s...
[INFO] ...
[INFO] Running it.dev.langchan4j.example.ModelResourceIT
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.733 s...
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

When you are done checking out the service, exit dev mode by pressing `Ctrl+C` in the command-line session where you ran Liberty, or by typing `q` and then pressing the `enter/return` key.
