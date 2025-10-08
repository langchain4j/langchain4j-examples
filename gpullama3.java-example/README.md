# Running GPULlama3 Example Directly with Java

This guide explains how to run the `GPULlama3ChatModelExample` program **without using the Tornado launcher**, directly with `java`, using TornadoVM flags and the Maven-built JAR and dependencies.

---

## **Step 0 — Configure Local LLMs**

To download GPULlama3.java compatible LLMs, follow the instructions in the [GPULlama3.java README](https://github.com/beehive-lab/GPULlama3.java/blob/main/README.md).

For example to get llama3.2-1b:
```bash
wget https://huggingface.co/beehive-lab/Llama-3.2-1B-Instruct-GGUF-FP16/resolve/main/beehive-llama-3.2-1b-instruct-fp16.gguf
```

Export an environment variable:

```bash
export LOCAL_LLMS_PATH=/path/to/downloaded/local/llms
```

This environment variable will be used by the example applications.

## **Step 1 — Get Tornado JVM flags**

Run the following command (You need to have Tornado installed):

```bash
tornado --printJavaFlags
```

Example output:

```bash
/home/mikepapadim/.sdkman/candidates/java/current/bin/java -server \
-XX:-UseCompressedOops -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI \
-XX:-UseCompressedClassPointers --enable-preview \
-Djava.library.path=/home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/lib \
--module-path .:/home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/share/java/tornado \
-Dtornado.load.api.implementation=uk.ac.manchester.tornado.runtime.tasks.TornadoTaskGraph \
-Dtornado.load.runtime.implementation=uk.ac.manchester.tornado.runtime.TornadoCoreRuntime \
-Dtornado.load.tornado.implementation=uk.ac.manchester.tornado.runtime.common.Tornado \
-Dtornado.load.annotation.implementation=uk.ac.manchester.tornado.annotation.ASMClassVisitor \
-Dtornado.load.annotation.parallel=uk.ac.manchester.tornado.api.annotations.Parallel \
--upgrade-module-path /home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/share/java/graalJars \
-XX:+UseParallelGC \
@/home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/etc/exportLists/common-exports \
@/home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/etc/exportLists/opencl-exports \
--add-modules ALL-SYSTEM,tornado.runtime,tornado.annotation,tornado.drivers.common,tornado.drivers.opencl
```

## **Step 2 — Build the Maven classpath**

First, build agentic-tutorial which is a dependency of gpullama3.java-example.

```bash
cd langchain4j-examples/agentic-tutorial
mvn clean install
```

From the project root, run:

```bash
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
```

## **Step 3 — Build the Maven classpath**

```bash
mvn clean package
```

Your main JAR will be located at:
```bash
target/gpullama3.java-example-1.4.0-beta10.jar
```

## **Step 4 — Run the program directly with Java**
You can now run the example with all JVM and Tornado flags:

```bash
JAVA_BIN=/home/mikepapadim/.sdkman/candidates/java/current/bin/java
CP="target/gpullama3.java-example-1.4.0-beta10.jar:$(cat cp.txt)"

$JAVA_BIN \
  -server \
  -XX:-UseCompressedOops \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+EnableJVMCI \
  --enable-preview \
  -Djava.library.path=/home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/lib \
  --module-path .:/home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/share/java/tornado \
  -Dtornado.load.api.implementation=uk.ac.manchester.tornado.runtime.tasks.TornadoTaskGraph \
  -Dtornado.load.runtime.implementation=uk.ac.manchester.tornado.runtime.TornadoCoreRuntime \
  -Dtornado.load.tornado.implementation=uk.ac.manchester.tornado.runtime.common.Tornado \
  -Dtornado.load.annotation.implementation=uk.ac.manchester.tornado.annotation.ASMClassVisitor \
  -Dtornado.load.annotation.parallel=uk.ac.manchester.tornado.api.annotations.Parallel \
  --upgrade-module-path /home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/share/java/graalJars \
  -XX:+UseParallelGC \
  @/home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/etc/exportLists/common-exports \
  @/home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/etc/exportLists/opencl-exports \
  --add-modules ALL-SYSTEM,tornado.runtime,tornado.annotation,tornado.drivers.common,tornado.drivers.opencl \
  -Duse.tornadovm=true \
  -Xms6g -Xmx6g \
  -Dtornado.device.memory=6GB \
  -cp "$CP" \
  GPULlama3ChatModelExample

```

### Optional: Create a shell script
You can save the above command as run-direct.sh and run it with:
```bash
bash run-direct.sh
```


### Optional: Run the program with TornadoVM
```bash
/home/mikepapadim/.sdkman/candidates/java/current/bin/java \
  -server \
  -XX:-UseCompressedOops \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+EnableJVMCI \
  --enable-preview \
  -Djava.library.path=/home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/lib \
  --module-path .:/home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/share/java/tornado \
  -Dtornado.load.api.implementation=uk.ac.manchester.tornado.runtime.tasks.TornadoTaskGraph \
  -Dtornado.load.runtime.implementation=uk.ac.manchester.tornado.runtime.TornadoCoreRuntime \
  -Dtornado.load.tornado.implementation=uk.ac.manchester.tornado.runtime.common.Tornado \
  -Dtornado.load.annotation.implementation=uk.ac.manchester.tornado.annotation.ASMClassVisitor \
  -Dtornado.load.annotation.parallel=uk.ac.manchester.tornado.api.annotations.Parallel \
  --upgrade-module-path /home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/share/java/graalJars \
  -XX:+UseParallelGC \
  @/home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/etc/exportLists/common-exports \
  @/home/mikepapadim/java-ai-demos/GPULlama3.java/external/tornadovm/bin/sdk/etc/exportLists/opencl-exports \
  --add-modules ALL-SYSTEM,tornado.runtime,tornado.annotation,tornado.drivers.common,tornado.drivers.opencl \
  -Xms6g \
  -Xmx6g \
  -Dtornado.device.memory=6GB \
  -cp "target/gpullama3.java-example-1.4.0-beta10.jar:/home/mikepapadim/.m2/repository/dev/langchain4j/langchain4j-core/1.5.0-SNAPSHOT/langchain4j-core-1.5.0-SNAPSHOT.jar:/home/mikepapadim/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.19.2/jackson-annotations-2.19.2.jar:/home/mikepapadim/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.19.2/jackson-core-2.19.2.jar:/home/mikepapadim/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.19.2/jackson-databind-2.19.2.jar:/home/mikepapadim/.m2/repository/org/slf4j/slf4j-api/2.0.17/slf4j-api-2.0.17.jar:/home/mikepapadim/.m2/repository/org/jspecify/jspecify/1.0.0/jspecify-1.0.0.jar:/home/mikepapadim/.m2/repository/dev/langchain4j/langchain4j-gpu-llama3/1.5.0-SNAPSHOT/langchain4j-gpu-llama3-1.5.0-SNAPSHOT.jar:/home/mikepapadim/.m2/repository/org/beehive/gpullama3/gpu-llama3/2.0-SNAPSHOT/gpu-llama3-2.0-SNAPSHOT.jar" \
  GPULlama3StreamingChatModelExample

```

### Run agentic examples:

###### Note: Make sure you have the agentic-tutorial project built first (see step 2).

1) Run GPULlama3_1a_Basic_Agent_Example on GPU:

```bash
tornado -cp target/gpullamas.java-example-1.7.1-beta14.jar:$(cat cp.txt) \
agentic._1_basic_agent.GPULlama3_1a_Basic_Agent_Example GPU
```

2) Run GPULlama3_1b_Basic_Agent_Example_Structured on GPU:

```bash
tornado -cp target/gpullamas.java-example-1.7.1-beta14.jar:$(cat cp.txt) \
agentic._1_basic_agent.GPULlama3_1b_Basic_Agent_Example_Structured GPU
```