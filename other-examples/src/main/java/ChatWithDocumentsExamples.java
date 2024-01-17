import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;
import static java.util.stream.Collectors.joining;

public class ChatWithDocumentsExamples {

    // Please also check ServiceWithRetrieverExample

    static class IfYouNeedSimplicity {

        public static void main(String[] args) throws Exception {

            EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

            EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(DocumentSplitters.recursive(300, 0))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            Document document = loadDocument(toPath("example-files/story-about-happy-carrot.txt"), new TextDocumentParser());
            ingestor.ingest(document);

            ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder()
                    .chatLanguageModel(OpenAiChatModel.withApiKey(ApiKeys.OPENAI_API_KEY))
                    .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
                    // .chatMemory() // you can override default chat memory
                    // .promptTemplate() // you can override default prompt template
                    .build();

            String answer = chain.execute("Who is Charlie?");
            System.out.println(answer); // Charlie is a cheerful carrot living in VeggieVille...
        }
    }

    static class If_You_Need_More_Control {

        public static void main(String[] args) {

            // Load the document that includes the information you'd like to "chat" about with the model.
            Document document = loadDocument(toPath("example-files/story-about-happy-carrot.txt"), new TextDocumentParser());

            // Split document into segments 100 tokens each
            DocumentSplitter splitter = DocumentSplitters.recursive(
                    100,
                    0,
                    new OpenAiTokenizer(GPT_3_5_TURBO)
            );
            List<TextSegment> segments = splitter.split(document);

            // Embed segments (convert them into vectors that represent the meaning) using embedding model
            EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

            // Store embeddings into embedding store for further search / retrieval
            EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
            embeddingStore.addAll(embeddings, segments);

            // Specify the question you want to ask the model
            String question = "Who is Charlie?";

            // Embed the question
            Embedding questionEmbedding = embeddingModel.embed(question).content();

            // Find relevant embeddings in embedding store by semantic similarity
            // You can play with parameters below to find a sweet spot for your specific use case
            int maxResults = 3;
            double minScore = 0.7;
            List<EmbeddingMatch<TextSegment>> relevantEmbeddings
                    = embeddingStore.findRelevant(questionEmbedding, maxResults, minScore);

            // Create a prompt for the model that includes question and relevant embeddings
            PromptTemplate promptTemplate = PromptTemplate.from(
                    "Answer the following question to the best of your ability:\n"
                            + "\n"
                            + "Question:\n"
                            + "{{question}}\n"
                            + "\n"
                            + "Base your answer on the following information:\n"
                            + "{{information}}");

            String information = relevantEmbeddings.stream()
                    .map(match -> match.embedded().text())
                    .collect(joining("\n\n"));

            Map<String, Object> variables = new HashMap<>();
            variables.put("question", question);
            variables.put("information", information);

            Prompt prompt = promptTemplate.apply(variables);

            // Send the prompt to the OpenAI chat model
            ChatLanguageModel chatModel = OpenAiChatModel.builder()
                    .apiKey(ApiKeys.OPENAI_API_KEY)
                    .timeout(Duration.ofSeconds(60))
                    .build();
            AiMessage aiMessage = chatModel.generate(prompt.toUserMessage()).content();

            // See an answer from the model
            String answer = aiMessage.text();
            System.out.println(answer); // Charlie is a cheerful carrot living in VeggieVille...
        }
    }

    private static Path toPath(String fileName) {
        try {
            URL fileUrl = ChatWithDocumentsExamples.class.getResource(fileName);
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
