# Build AI Applications with Jlama and Langchain4J

[Jlama](https://github.com/tjake/Jlama) is a fast modern Java library for running many LLMs.

Jlama is built on Java 21 and utilizes the [Panama Vector API](https://openjdk.org/jeps/448) for fast inference.

### Jlama with Langchain4J

To run the examples you must have java 21 installed then run the following commands:

```shell
# Build and run Rag example
mvn -pl jlama-examples compile exec:exec@rag

# Build and run streaming example
mvn -pl jlama-examples compile exec:exec@streaming

# Build and run basic chat response example
mvn -pl jlama-examples compile exec:exec@chat
```

To use Jlama with Langchain4j you must use Java 21 and include the following JVM arguments:

```
 --add-modules=jdk.incubator.vector
 --enable-native-access=ALL-UNNAMED
 --enable-preview
```