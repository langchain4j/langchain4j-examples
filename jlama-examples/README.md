# Build AI Applications with Jlama and LangChain4j

[Jlama](https://github.com/tjake/Jlama) is a fast modern Java library for running many LLMs.

Jlama is built on Java 20+ and utilizes the [Panama Vector API](https://openjdk.org/jeps/448) for fast inference.

### Jlama with LangChain4j

To run the examples you must have java 20+ installed and run the following commands:

```shell
cd jlama-examples

# Build and run basic chat response example
./mvnw compile exec:exec@chat

# Build and run streaming example
./mvnw compile exec:exec@stream

# Build and run Rag example
./mvnw compile exec:exec@rag

# Build and run function calling example
./mvnw compile exec:exec@functions
```

To use Jlama with your LangChain4j app you must use Java 20+ and include the following JVM arguments:

```
 --add-modules=jdk.incubator.vector
 --enable-native-access=ALL-UNNAMED
 --enable-preview
```