# Spring PetClinic Sample Application With OpenAI and Langchain4j

## Understanding the Spring Petclinic application with a few diagrams

[See the presentation here](https://speakerdeck.com/michaelisvy/spring-petclinic-sample-application)

## Run Petclinic locally

Spring Petclinic is a [Spring Boot](https://spring.io/guides/gs/spring-boot) application built
using [Maven](https://spring.io/guides/gs/maven/). You can build a jar
file and run it from the command line (it should work just as well with Java 17 or newer), Before you build the application, you need to setup some openai properties in `src/main/resources/application.properties`:
```bash
git clone https://github.com/langchain4j/langchain4j-examples
cd langchain4j-examples/spring-petclinic-example
mv src/main/resources/application.properties.example src/main/resources/application.properties
```

edit the `src/main/resources/application.properties` file and add the following properties:
```properties
langchain4j.azure.open-ai.chat-model.endpoint=https://*****.openai.azure.com/
langchain4j.azure.open-ai.chat-model.deployment-name=gpt-4
langchain4j.azure.open-ai.chat-model.api-key=**
```

You can build the application by running the following command:
```bash
./mvnw package
java -jar target/*.jar
```

You can then access the Petclinic at <http://localhost:8080/>
<img width="1042" alt="petclinic-screenshot" src="https://github.com/showpune/spring-petclinic-langchain4j/assets/1787505/52878caa-8bdd-48c4-a2e7-193f68054c3e">

And the OpenAI chatbot at <http://localhost:8080/chat.html>.
<img width="1042" alt="petclinic-screenshot" src="https://github.com/showpune/spring-petclinic-langchain4j/assets/1787505/11caef70-6411-4e72-9ae9-4902fb8ac96b">

## Workthrough of the chat agent
You can talk with the agent, it can help to recommend the vet according to the symptoms of the pet. The agent can also help to book an appointment with the vet.
![image](https://github.com/showpune/spring-petclinic-langchain4j/assets/1787505/e158ca83-0ada-4f8c-8843-6055b9cb017f)
Go to the owner page, you can see you are registered as an owner with the help of the agent
![image](https://github.com/showpune/spring-petclinic-langchain4j/assets/1787505/e7da4ede-5405-437d-a35f-fcd60af45ba7)

### Prompt
The prompt is defined in [agent](https://github.com/showpune/spring-petclinic-langchain4j/blob/master/src/main/java/org/springframework/samples/petclinic/chat/Agent.java)

### Memory
Memory Store: The demo still use the local memory defined in [memory store](https://github.com/showpune/spring-petclinic-langchain4j/blob/c95a598f4fdaf68a3f331b32ca42ef5ef95e5c17/src/main/java/org/springframework/samples/petclinic/chat/LocalConfig.java#L39), it means it can not share memory between instances, you can enhance it use memory on Redis

Memory ID: It use the username as memory id [Memory ID](https://github.com/showpune/spring-petclinic-langchain4j/blob/c95a598f4fdaf68a3f331b32ca42ef5ef95e5c17/src/main/java/org/springframework/samples/petclinic/chat/Agent.java#L13)

### Interact with natives functions
The Demo provided two local tools to interactive with native functions
1) [Vets and their specialist](https://github.com/showpune/spring-petclinic-langchain4j/blob/c95a598f4fdaf68a3f331b32ca42ef5ef95e5c17/src/main/java/org/springframework/samples/petclinic/chat/VetTools.java#L41): The agent will know the system can return list of Vets, include their specialist, it can be used to recommend a vet
2) [Owner and Pets](https://github.com/showpune/spring-petclinic-langchain4j/blob/master/src/main/java/org/springframework/samples/petclinic/chat/OwnerTools.java): he agent will know the system register new owner and their pets

### Content Retriever
It still use the local file as [content retriever](https://github.com/showpune/spring-petclinic-langchain4j/blob/c95a598f4fdaf68a3f331b32ca42ef5ef95e5c17/src/main/java/org/springframework/samples/petclinic/chat/LocalConfig.java#L51), it provided the guideline how the agent should work, which is in [Term of Use](https://github.com/showpune/spring-petclinic-langchain4j/blob/master/src/main/resources/petclinic-terms-of-use.txt)

### Talk with Other language
You can also talk with the agent with your own language, like Chinese
![image](https://github.com/showpune/spring-petclinic-langchain4j/assets/1787505/cd2a7a8c-dac5-440f-b7a9-f03239b8735a)

The problem is that your [Term of Use](https://github.com/showpune/spring-petclinic-langchain4j/blob/master/src/main/resources/petclinic-terms-of-use.txt) is in English, the traditional way is that provide a localized term of use for each language, but you can use openAI to make it easier

We can define a [Retrieval Augmentor](https://github.com/showpune/spring-petclinic-langchain4j/blob/c95a598f4fdaf68a3f331b32ca42ef5ef95e5c17/src/main/java/org/springframework/samples/petclinic/chat/AgentConfig.java#L47C21-L47C39), and translate your ask into English before you retrieve the content




## License

The Spring PetClinic sample application is released under version 2.0 of
the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
