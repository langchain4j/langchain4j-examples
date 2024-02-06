# LangChain4j in Jakarta EE and MicroProfile
This example demonstrates LangChain4J in a Jakarta EE / MicroProfile application on Open Liberty.

## Prerequisites:

- [Java 17](https://developer.ibm.com/languages/java/semeru-runtimes/downloads)
- [Maven](https://maven.apache.org/download.cgi)
- Hugging Face API Key
  - Sign up and login to https://huggingface.co
  - Go to Access Tokens by https://huggingface.co/settings/tokens
  - New a Read token
  

## Environment Set Up

To run this example application, navigate  to the `jakartaee-microprofile-example` directory

```
cd langchain4j-examples/jakartaee-microprofile-example
```

Set environment variables

```
export JAVA_HOME=<your Java 17 home path>
export HUGGING_FACE_API_KEY=<your Hugging Face read token>
```

## Start the application

Use the Maven wrapper to start the application by using the [Liberty dev mode](https://openliberty.io/docs/latest/development-mode.html):

```
./mvnw liberty:dev
```

## Try out the application

- visit http://localhost:9080
- suggested messages to try:
  - ```
    What is large language models?
    ```
  - ```
    tell me more
    ```
  - ```
    which are the top used models?
    ```
  - ```
    any documentation?
    ```


### Try out other models

Visit the http://localhost:9080/openapi/ui URL (OpenAPI UI) for the following 3 REST APIs:

- [HuggingFaceLanguageMode](https://github.com/langchain4j/langchain4j/blob/main/langchain4j-hugging-face/src/main/java/dev/langchain4j/model/huggingface/HuggingFaceLanguageModel.java)
  - expand the GET `/api/model/language` API
    - click the `Try it out` button
    - type `When was langchain4j launched?` or any question on the question field
    - click the `Execute` button
  - or run the following `curl` command on a command-line session:
    - ```
      curl 'http://localhost:9080/api/model/language?question=When%20was%20Open%20Liberty%20launched%3F'
      ```
- [HuggingFaceChatMode](https://github.com/langchain4j/langchain4j/blob/main/langchain4j-hugging-face/src/main/java/dev/langchain4j/model/huggingface/HuggingFaceChatModel.java)
  - expand the GET `/api/model/language` API
    - click the `Try it out` button
    - type `Which are the most popular?` or any question on the question field
    - click the `Execute` button
  - or run the following `curl` command on a command-line session:
    - ```
      curl 'http://localhost:9080/api/model/chat?userMessage=Which%20are%20the%20most%20popular%3F' | jq
      ```
- [HuggingFaceEmbeddingMode](https://github.com/langchain4j/langchain4j/blob/main/langchain4j-hugging-face/src/main/java/dev/langchain4j/model/huggingface/HuggingFaceEmbeddingModel.java)
  - expand the GET `/api/model/similarity` API
    - click the `Try it out` button
    - type `I like Jarkata EE and MicroProfile.` or any text on the text1 field
    - type `I like Python language.` or any text on the text2 field
    - click the `Execute` button
  - or run the following `curl` command on a command-line session
    - ```
      curl 'http://localhost:9080/api/model/similarity?text1=I%20like%20Jarkata%20EE%20and%20MicroProfile.&text2=I%20like%20Python%20language.' | jq
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
