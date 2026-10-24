import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

import dev.langchain4j.community.store.embedding.s3.S3VectorsEmbeddingStore;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.Filter;

/**
 * Prerequisites:
 * 1. Configure AWS credentials (via AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY environment variables,
 *    ~/.aws/credentials file, or IAM role).
 * 2. Create an S3 Vector Bucket:
 *    aws s3vectors create-vector-bucket --vector-bucket-name my-vector-bucket
 * 3. Set the S3_VECTORS_BUCKET_NAME environment variable to your bucket name.
 * 4. Optionally set AWS_REGION (defaults to "us-east-1").
 */
public class S3VectorsEmbeddingStoreWithMetadataExample {

    public static void main(String[] args) {

        String bucketName = System.getenv("S3_VECTORS_BUCKET_NAME");
        String region = System.getenv().getOrDefault("AWS_REGION", "us-east-1");

        try (S3VectorsEmbeddingStore embeddingStore = S3VectorsEmbeddingStore.builder()
                .vectorBucketName(bucketName)
                .indexName("s3-vectors-metadata-example-index")
                .region(region)
                .createIndexIfNotExists(true)
                .build()) {

            EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

            TextSegment segment1 = TextSegment.from("I like football.", Metadata.metadata("userId", "1"));
            Embedding embedding1 = embeddingModel.embed(segment1).content();
            embeddingStore.add(embedding1, segment1);

            TextSegment segment2 = TextSegment.from("I like basketball.", Metadata.metadata("userId", "2"));
            Embedding embedding2 = embeddingModel.embed(segment2).content();
            embeddingStore.add(embedding2, segment2);

            Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();

            // search for user 1

            Filter onlyForUser1 = metadataKey("userId").isEqualTo("1");

            EmbeddingSearchRequest embeddingSearchRequest1 = EmbeddingSearchRequest
                .builder()
                .queryEmbedding(queryEmbedding)
                .filter(onlyForUser1)
                .build();

            EmbeddingSearchResult<TextSegment> embeddingSearchResult1 = embeddingStore.search(embeddingSearchRequest1);
            EmbeddingMatch<TextSegment> embeddingMatch1 = embeddingSearchResult1.matches().get(0);

            System.out.println(embeddingMatch1.score());
            System.out.println(embeddingMatch1.embedded().text());

            // search for user 2

            Filter onlyForUser2 = metadataKey("userId").isEqualTo("2");

            EmbeddingSearchRequest embeddingSearchRequest2 = EmbeddingSearchRequest
                .builder()
                .queryEmbedding(queryEmbedding)
                .filter(onlyForUser2)
                .build();

            EmbeddingSearchResult<TextSegment> embeddingSearchResult2 = embeddingStore.search(embeddingSearchRequest2);
            EmbeddingMatch<TextSegment> embeddingMatch2 = embeddingSearchResult2.matches().get(0);

            System.out.println(embeddingMatch2.score());
            System.out.println(embeddingMatch2.embedded().text());
        }
    }
}
