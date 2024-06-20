# Build Native Java Images with GraalVM with Langchain4J and Gemini Function Calling

Demonstrate `Function Calling` code using Gemini with Langchain4j

__Use Case__: Suppose we want the AI model to respond with information that it does not have.
For example the status of your recent payment transactions.
Users can ask questions about current status for certain payment transactions and use function calling to answer them.

__Environment__:
Please set the following environment variables before running this example:
```shell
export VERTEX_AI_GEMINI_PROJECT_ID=<your project id>
export VERTEX_AI_GEMINI_LOCATION=<region, ex us-central1>
export VERTEX_AI_GEMINI_MODEL=<the model in use, ex.gemini-1.5-flash-001>
```

For example, let's consider a sample dataset and a function that retrieves the payment status given a transaction:

```java
    record Transaction(String id) {
    }
    
    record Status(String name) {
    }
    
    private static final Map<Transaction, Status> DATASET = Map.of(
            new Transaction("001"), new Status("pending"),
            new Transaction("002"), new Status("approved"),
            new Transaction("003"), new Status("rejected"));
    
    @Tool("Get the status of a payment transaction")
    public Status paymentStatus(@P("The id of the payment transaction") String transaction) {
        System.out.println();
        return DATASET.get(new Transaction(transaction));
    }
```

Function is registered as `@Tool`, which are Java methods the language model can use to call. 
Langchain4j greatly simplifies code you need to write to support function invocation.
It brokers the function invocation conversation for you.

Lets add the boot starters for 4 AI Models that support function calling:

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-spring-boot-starter</artifactId>
    <version>${langchain4j.version}</version>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-vertex-ai-gemini</artifactId>
    <version>${langchain4j.version}</version>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
    <version>${langchain4j.version}</version>
</dependency>
```

and configure them in `application.properties`:

```
# Google VertexAI Gemini
langchain4j.gemini.project-id=${VERTEX_AI_GEMINI_PROJECT_ID}
langchain4j.gemini.location=${VERTEX_AI_GEMINI_LOCATION}
langchain4j.gemini.chat.options.model=${VERTEX_AI_GEMINI_MODEL}
```

Now you can test them with the same prompt:

```java
@Bean
ApplicationRunner applicationRunner() {
  return args -> {
    String userMessage = """
        Please use multi-turn conversation to answer the following questions:
        What is the status of my payment transactions 002, 001, 003?
        Please indicate the status for each transaction and return the results in JSON format.
        """;

    // test with VertexAI Gemini using REST API
    functionCallGeminiWithREST(userMessage);

    // test with VertexAI Gemini using gRPC
    functionCallGeminiWithGRPC(userMessage);
  };
}
```

The output would look something like:

```text
    What is the status of my payment transactions 002, 001, 003?
    Please indicate the status for each transaction and return the results in JSON format.
    ```json
    {
        "002": "approved",
        "001": "pending",
        "003": "rejected"
    }
    ```
```

## Related [Langchain4J](https://docs.langchain4j.dev/) documentation:
* [Langchain4J Google VertexAI Gemini](https://docs.langchain4j.dev/integrations/language-models/google-gemini) and [Function Calling](https://docs.langchain4j.dev/tutorials/tools)

## JIT Java Build
You can build this as a regular Java archive.

```shell
./mvnw clean package -DskipTests
```

Run the Java archive:

```shell
java -jar target/native-java-gemini-function-calling-example-1.0.0.jar

# or

./mvnw spring-boot:run
```
## Native Java (GraalVM) Build
You can build this as a native executable.

First make sure that you are using GraalVM 21 JDK. For example, install the GraalVM 21 SDK with [SDKMan](https://sdkman.io/install) or from the [GraalVM site](https://www.graalvm.org/downloads/)

```shell
java -version

# output
java version "21.0.3" 2024-04-16 LTS
Java(TM) SE Runtime Environment Oracle GraalVM 21.0.3+7.1 (build 21.0.3+7-LTS-jvmci-23.1-b37)
Java HotSpot(TM) 64-Bit Server VM Oracle GraalVM 21.0.3+7.1 (build 21.0.3+7-LTS-jvmci-23.1-b37, mixed mode, sharing)
```

Then build:

```
./mvnw clean package -Pnative native:compile -DskipTests
```

Run the native executable:

```
./target/langchain4j-function-calling 
```


__Important note__: Please register runtime hints for the Native Java image
* Register the Assistant class for reflection and dynamic proxy generation
* Register the FunctionCallingService class for reflection
* Register the paymentStatus() method for invocation - full reflection support is required, including the ability to invoke
```java
public static class FunctionCallingRuntimeHints implements RuntimeHintsRegistrar {
  @Override
  public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
    try {
      // Register all the classes and methods that are used through reflection
      // or dynamic proxy generation in LangChain4j, especially those
      // related to function calling.
      // Register method for reflection
      var mcs = MemberCategory.values();
      hints.reflection().registerType(Langchain4JFunctionCallingApplication.Assistant.class, mcs);
      hints.proxies().registerJdkProxy(Langchain4JFunctionCallingApplication.Assistant.class);
      hints.reflection().registerType(FunctionCallingService.class, mcs);

      hints.reflection().registerMethod(
          FunctionCallingService.class.getMethod("paymentStatus", String.class),
          ExecutableMode.INVOKE
      );

      // ... register other necessary classes and methods ...
    } catch (NoSuchMethodException e) {
      // Handle the exception appropriately (e.g., log it)
        System.out.println("Error: " + e.getMessage());
    }
  }
```