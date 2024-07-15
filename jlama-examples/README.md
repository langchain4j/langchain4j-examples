# Build AI Applications with Jlama and LangChain4j

[Jlama](https://github.com/tjake/Jlama) is a fast modern Java library for running many LLMs.

Jlama is built on Java 21 and utilizes the [Panama Vector API](https://openjdk.org/jeps/448) for fast inference.

### Jlama with LangChain4j

To run the examples you must have java 21 installed then run the following commands:

```shell
cd jlama-examples

# Build and run basic chat response example
./mvnw compile exec:exec@chat

# Build and run streaming example
./mvnw compile exec:exec@stream

# Build and run Rag example
./mvnw compile exec:exec@rag
```

To use Jlama with LangChain4j you must use Java 21 and include the following JVM arguments:

```
 --add-modules=jdk.incubator.vector
 --enable-native-access=ALL-UNNAMED
 --enable-preview
```