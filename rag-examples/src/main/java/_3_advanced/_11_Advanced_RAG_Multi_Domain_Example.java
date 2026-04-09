package _3_advanced;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiScoringModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

public class _11_Advanced_RAG_Multi_Domain_Example {

    /**
     * This example demonstrates how to perform Multi-Domain RAG.
     * It addresses the common enterprise use-case where a single vector database 
     * holds documents from different domains (e.g., activity, nutrition, user profile),
     * and a single query needs to retrieve a specific number of documents from each domain,
     * merging and re-ranking them into a final context.
     */

    interface FitnessCoach {
        String answer(String query);
    }

    public static void main(String[] args) {

        // 1. Initialize models and a single Embedding Store
        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .build();
        
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        ScoringModel scoringModel = OpenAiScoringModel.withApiKey(System.getenv("OPENAI_API_KEY"));

        // (In a real app, you would ingest documents here with Metadata.from("domain", "activity"), etc.)

        // 2. Create distinct retrievers for each domain with specific filters and limits
        ContentRetriever activityRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .filter(metadataKey("domain").isEqualTo("activity"))
                .maxResults(5) // Pull up to 5 workout records
                .build();

        ContentRetriever nutritionRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .filter(metadataKey("domain").isEqualTo("nutrition"))
                .maxResults(3) // Pull up to 3 meal records
                .build();

        ContentRetriever userProfileRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .filter(metadataKey("domain").isEqualTo("user"))
                .maxResults(1) // Pull the single most relevant user profile context
                .build();

        // 3. Configure a Query Router to fan out the prompt to all domain retrievers simultaneously
        DefaultQueryRouter queryRouter = new DefaultQueryRouter(
                activityRetriever, 
                nutritionRetriever, 
                userProfileRetriever
        );

        // 4. Build the Augmentor with the router and a re-ranker to sort the merged results
        RetrievalAugmentor augmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(queryRouter)
                .contentAggregator(new dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator(scoringModel))
                .build();

        // 5. Create the AI Service
        FitnessCoach coach = AiServices.builder(FitnessCoach.class)
                .chatLanguageModel(chatModel)
                .retrievalAugmentor(augmentor)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        // The query will now automatically fan out to the 3 domains, respect the individual maxResults, 
        // merge the 9 total documents, re-rank them, and send them to the LLM.
        String response = coach.answer("How am I doing on my fitness goals this week?");
        System.out.println(response);
    }
}