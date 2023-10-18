package com.datastax.demo;

import com.dtsx.astra.sdk.utils.TestUtils;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.DocumentType;
import dev.langchain4j.data.document.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.vertexai.VertexAiEmbeddingModel;
import dev.langchain4j.model.vertexai.VextexAiLanguageModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.cassandra.AstraDbEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dtsx.astra.sdk.utils.TestUtils.getAstraToken;
import static com.dtsx.astra.sdk.utils.TestUtils.setupVectorDatabase;
import static dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RagWithAstraAndVertexAITest {

    final String db = "langchain4j";
    final String vectorStore = "happy_carrot_vertex_ai";
    final String embeddingsModel = "textembedding-gecko@001";
    final String completionModel = "text-bison@001";
    final int vectorDimension = 384;


    @Test
    @EnabledIfEnvironmentVariable(named = "ASTRA_DB_APPLICATION_TOKEN", matches = "Astra.*")
    void shouldRagWithOVertexAIAndAstra() {

        // Create a vector database in Astra if needed
        final String databaseId = setupVectorDatabase(db, db);

        // Given
        assertNotNull(databaseId);

        // --- Ingesting documents ---

        // Parsing input file
        Path path = new File(getClass().getResource("/story-about-happy-carrot.txt").getFile()).toPath();
        Document document = FileSystemDocumentLoader.loadDocument(path, DocumentType.TXT);
        DocumentSplitter splitter = DocumentSplitters
                .recursive(100, 10, new OpenAiTokenizer(GPT_3_5_TURBO));

        // Embedding model (OpenAI)
        EmbeddingModel embeddingModel = VertexAiEmbeddingModel.builder()
                .endpoint("us-central1-aiplatform.googleapis.com:443")
                .project("integrations-379317")
                .location("us-central1")
                .publisher("google")
                .modelName(embeddingsModel)
                .maxRetries(3)
                .build();

        // Embed the document and it in the store
        EmbeddingStore<TextSegment> embeddingStore = AstraDbEmbeddingStore.builder()
                .token(getAstraToken())
                .database(databaseId, TestUtils.TEST_REGION)
                .table(db, vectorStore)
                .vectorDimension(vectorDimension) // dimension of the gecko model
                .build();

        // Ingest method 2
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);

        // --------- RAG -------------

        // Specify the question you want to ask the model
        String question = "Who is Charlie?";

        // Embed the question
        Response<Embedding> questionEmbedding = embeddingModel.embed(question);

        // Find relevant embeddings in embedding store by semantic similarity
        // You can play with parameters below to find a sweet spot for your specific use case
        int maxResults = 3;
        double minScore = 0.8;
        List<EmbeddingMatch<TextSegment>> relevantEmbeddings =
                embeddingStore.findRelevant(questionEmbedding.content(), maxResults, minScore);

        // --------- Chat Template -------------

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
        VextexAiLanguageModel chatModel = VextexAiLanguageModel.builder()
                .endpoint("us-central1-aiplatform.googleapis.com:443")
                .project("integrations-379317")
                .location("us-central1")
                .publisher("google")
                .modelName(completionModel)
                .temperature(0.2)
                .maxOutputTokens(50)
                .topK(40)
                .topP(0.95)
                .maxRetries(3)
                .build();

        Response<String> aiMessage = chatModel.generate(prompt);

        // See an answer from the model
        String answer = aiMessage.content();
        System.out.println(answer);
    }
}