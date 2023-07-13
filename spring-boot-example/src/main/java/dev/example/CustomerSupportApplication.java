package dev.example;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.UrlDocumentLoader;
import dev.langchain4j.data.document.splitter.ParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.retriever.Retriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.InMemoryEmbeddingStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
public class CustomerSupportApplication {

    @Bean
    CustomerSupportAgent customerSupportAgent(ChatLanguageModel chatLanguageModel,
                                              BookingTools bookingTools,
                                              Retriever<TextSegment> retriever) {
        return AiServices.builder(CustomerSupportAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withCapacity(20))
                .tools(bookingTools)
                .retriever(retriever)
                .build();
    }

    @Bean
    Retriever<TextSegment> retriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {

        // You will need to adjust these parameters to find the optimal setting, which will depend on two main factors:
        // - The nature of your data
        // - The embedding model you are using
        int maxResultsRetrieved = 1;
        double minSimilarity = 0.8;

        return EmbeddingStoreRetriever.from(embeddingStore, embeddingModel, maxResultsRetrieved, minSimilarity);
    }

    @Bean
    EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel, ResourceLoader resourceLoader) throws IOException {

        // Normally, you would already have your embedding store filled with your data.
        // However, for the purpose of this demonstration, we will:
        // 1. Load one document ("Miles of Smiles" terms of use)
        // 2. Split it into segments
        // 3. Embed the segments
        // 4. Create an in-memory embedding store
        // 5. Store all the embeddings there

        Document document = loadDocument(resourceLoader);
        List<TextSegment> segments = splitIntoSegments(document);
        List<Embedding> embeddings = embeddingModel.embedAll(segments).get();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);
        return embeddingStore;
    }

    private static Document loadDocument(ResourceLoader resourceLoader) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:miles-of-smiles-terms-of-use.txt");
        return UrlDocumentLoader.load(resource.getURL());
    }

    private static List<TextSegment> splitIntoSegments(Document document) {
        DocumentSplitter splitter = new ParagraphSplitter();
        return splitter.split(document);
    }

    public static void main(String[] args) {
        SpringApplication.run(CustomerSupportApplication.class, args);
    }
}
