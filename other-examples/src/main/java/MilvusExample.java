import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.CollectionDescription;
import dev.langchain4j.store.embedding.MilvusEmbeddingStoreImpl;
import dev.langchain4j.data.segment.TextSegment;


import java.util.Arrays;
import java.util.List;

public class MilvusExample {

    /*
     * Prerequisite:
     * 1 Milvus DB is running locally
     * 2 The collection named 'greetings' exists in the DB. The schema of the collection:
     *      - id field: name 'id', type 'varchar', max_length '36'
     *      - vector field: name 'greeting_vector', type 'float vector', dimensions '2'
     *      - scalar field: name 'text', type 'varchar', max_length '200'
     *      - the vector field has an index
     */

    public static void main(String[] args) {
        CollectionDescription collectionDescription = CollectionDescription.builder()
                .collectionName("greetings")
                .idFieldName("id")
                .vectorFieldName("greeting_vector")
                .scalarFieldName("text")
                .build();

        MilvusEmbeddingStoreImpl embeddingStore = MilvusEmbeddingStoreImpl.builder()
                .databaseName("default")
                .host("localhost")
                .port(19530)
                .keepAliveTimeMs(100000L)
                .keepAliveTimeoutMs(100000L)
                .connectTimeoutMs(100000L)
                .idleTimeoutMs(100000L)
                .collectionDescription(collectionDescription)
                .build();


        // INSERT ENTRIES
        // it's important that the data types and the number of dimensions of the vectors match with the db schema.
        // otherwise Milvus will throw an exception
        Embedding embedding_1 = new Embedding(new float[]{-0.0165459755808115f, 0.00898083765059709f});
        Embedding embedding_2 = new Embedding(new float[]{-0.01295038778334856f, 0.015548201277852f});
        Embedding embedding_3 = new Embedding(new float[]{-0.008846494369208813f, 0.01228294521570f});
        Embedding embedding_4 = new Embedding(new float[]{-0.006242010276764631f, 0.00523260841146f});

        TextSegment textSegment_1 = new TextSegment("good morning", null);
        TextSegment textSegment_2 = new TextSegment("good evening", null);
        TextSegment textSegment_3 = new TextSegment("добрий ранок", null);
        TextSegment textSegment_4 = new TextSegment("kon'nichiwa", null);

        List<Embedding> embeddings = Arrays.asList(embedding_1, embedding_2, embedding_3, embedding_4);
        List<TextSegment> textSegments = Arrays.asList(textSegment_1, textSegment_2, textSegment_3, textSegment_4);
        List<String> ids = embeddingStore.addAll(embeddings, textSegments);

        System.out.printf("ids of the inserted entities:%n");
        ids.forEach(System.out::println);


        // SEARCH
        embeddingStore
                .findRelevant(embedding_1, 2, 0.0)
                .forEach(System.out::println);
    }

}
