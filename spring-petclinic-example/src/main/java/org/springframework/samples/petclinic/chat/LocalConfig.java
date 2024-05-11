package org.springframework.samples.petclinic.chat;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

import static dev.langchain4j.data.document.loader.UrlDocumentLoader.load;
import static dev.langchain4j.model.azure.AzureOpenAiModelName.GPT_3_5_TURBO;
import static org.springframework.samples.petclinic.chat.LocalProperties.PREFIX;

@Configuration
@EnableConfigurationProperties(LocalProperties.class)
public class LocalConfig {

	@Bean
	@ConditionalOnProperty(name = PREFIX + ".memory.use-local", havingValue = "true")
	ChatMemoryProvider chatMemoryProvider(LocalProperties properties) {
		LocalProperties.LocalMemoryProperties LocalMemoryProperties = properties.getMemory();
		ChatMemoryStore store = new InMemoryChatMemoryStore();
		return memoryId -> MessageWindowChatMemory.builder()
			.id(memoryId)
			.maxMessages(LocalMemoryProperties.getMemorySize())
			.chatMemoryStore(store)
			.build();
	}

	@Bean
	@ConditionalOnProperty(name = PREFIX + ".content-retriever.use-local", havingValue = "true")
	ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel,
			LocalProperties properties) {

		LocalProperties.ContentRetrieverProperties contentRetrieverProperties = properties.getContentRetriever();
		int maxResults = contentRetrieverProperties.getMaxResults() == null ? 1
				: Integer.parseInt(contentRetrieverProperties.getMaxResults());
		double minScore = contentRetrieverProperties.getMinScore() == null ? 0.6
				: Double.parseDouble(contentRetrieverProperties.getMinScore());

		return EmbeddingStoreContentRetriever.builder()
			.embeddingStore(embeddingStore)
			.embeddingModel(embeddingModel)
			.maxResults(maxResults)
			.minScore(minScore)
			.build();
	}

	@Bean
	@ConditionalOnProperty(name = PREFIX + ".content-retriever.use-local", havingValue = "true")
	EmbeddingModel embeddingModel() {
		return new AllMiniLmL6V2EmbeddingModel();
	}

	@Bean
	@ConditionalOnProperty(name = PREFIX + ".content-retriever.use-local", havingValue = "true")
	EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel, ResourceLoader resourceLoader,
			LocalProperties properties) throws IOException {
		LocalProperties.ContentRetrieverProperties contentRetrieverProperties = properties.getContentRetriever();
		EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

		Resource resource = resourceLoader.getResource(contentRetrieverProperties.getContentPath());
		Document document = load(resource.getURL(), new TextDocumentParser());

		DocumentSplitter documentSplitter = DocumentSplitters.recursive(100, 0, new OpenAiTokenizer(GPT_3_5_TURBO));
		EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
			.documentSplitter(documentSplitter)
			.embeddingModel(embeddingModel)
			.embeddingStore(embeddingStore)
			.build();
		ingestor.ingest(document);

		return embeddingStore;
	}

}
