import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.jlama.JlamaChatModel;
import dev.langchain4j.model.jlama.JlamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
import static java.util.stream.Collectors.joining;

public class JlamaBasicRagEmbedExamples {

    static class Chat_Story_From_My_Document {

        public static void main(String[] args) {

            // In this very simple example, we are getting data that we want to use for RAG.
            // We will use a history about origin of the Llama by National Geographic https://www.nationalgeographic.es/animales/llama.
            Document document = loadDocument(toPath("example-files/story-about-origin-of-the-llama.txt"), new TextDocumentParser());

            // In a RAG system, it is crucial to split the document into smaller chunks so that it's more effective
            // to identify and retrieve the most relevant information in the retrieval process later
            DocumentSplitter splitter = DocumentSplitters.recursive(200, 0);
            List<TextSegment> segments = splitter.split(document);

            // Now, for each text segment, we need to create text embeddings, which are numeric representations of the text in the vector space.
            EmbeddingModel embeddingModel = JlamaEmbeddingModel.builder().modelName("intfloat/e5-small-v2").build();
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

            // Once we get the text embeddings, we will store them in a vector database for efficient processing and retrieval.
            // For simplicity, this example uses an in-memory store, but you can choose any external compatible store for production environments.
            EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
            embeddingStore.addAll(embeddings, segments);

            // Whenever users ask a question, we also need to create embeddings for this question using the same embedding models as before.
            String question = "Who create the llamas?";
            Embedding questionEmbedding = embeddingModel.embed(question).content();

            // We can perform a search on the vector database and retrieve the most relevant text chunks based on the user question.
            int maxResults = 3;
            double minScore = 0.7;
            List<EmbeddingMatch<TextSegment>> relevantEmbeddings
                    = embeddingStore.findRelevant(questionEmbedding, maxResults, minScore);

            // Now we can offer the relevant information as the context information within the prompt.
            // Here is a prompt template where we can include both the retrieved text and user question in the prompt.
            PromptTemplate promptTemplate = PromptTemplate.from(
                    "Context information is below.:\n"
                            + "------------------\n"
                            + "{{information}}\n"
                            + "------------------\n"
                            + "Given the context information and not prior knowledge, answer the query.\n"
                            + "Query: {{question}}\n"
                            + "Answer:");
            String information = relevantEmbeddings.stream()
                    .map(match -> match.embedded().text())
                    .collect(joining("\n\n"));

            Map<String, Object> promptInputs = new HashMap<>();
            promptInputs.put("question", question);
            promptInputs.put("information", information);

            Prompt prompt = promptTemplate.apply(promptInputs);

            // Now we can use the Jlama chat model to generate the answer to the user question based on the context information.
            ChatLanguageModel chatModel = JlamaChatModel.builder()
                    .modelName("tjake/TinyLlama-1.1B-Chat-v1.0-Jlama-Q4")
                    .temperature(0.2f) // expect a more focused and deterministic answer
                    .build();

            AiMessage aiMessage = chatModel.generate(prompt.toUserMessage()).content();
            String answer = aiMessage.text();
            System.out.println(answer); // According to Inca legend, the llamas were created by the mythical founders of the Inca Empire....
        }
    }

    static Path toPath(String fileName) {
        try {
            URL fileUrl = JlamaBasicRagEmbedExamples.class.getResource(fileName);
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
