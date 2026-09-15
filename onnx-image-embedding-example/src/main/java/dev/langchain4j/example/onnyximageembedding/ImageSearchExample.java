package dev.langchain4j.example.onnyximageembedding;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.ImagePreprocessorConfig;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxImageEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import dev.langchain4j.model.embedding.request.EmbeddingRequest;
import dev.langchain4j.model.embedding.response.EmbeddingResponse;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.CosineSimilarity;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Shows off in-process, fully-offline image search with LangChain4j.
 *
 * <p>It uses the two halves of the same <a href="https://huggingface.co/Xenova/clip-vit-base-patch32">CLIP</a>
 * model to embed both images (via {@link OnnxImageEmbeddingModel}) and text (via {@link OnnxEmbeddingModel});
 * because both towers project into one shared vector space, the embeddings produced by either end can be stored in
 * the same {@link EmbeddingStore} and searched interchangeably. This lets you do:
 *
 * <ul>
 *   <li><b>image-to-image search</b>: embed a query image and ask "which stored image looks most like it?",</li>
 *   <li><b>text-to-image search</b>: embed a caption and ask "which stored image matches this description?".</li>
 * </ul>
 *
 * <p>Run {@code mvn -q process-resources} once to download the CLIP model files (and the tokenizer) into
 * {@code target/clip}, then run this class.
 *
 * <p>Set {@code -DimageDir=/path/to/folder/of/images} to index your own folder; by default it looks for a
 * {@code images} folder on the classpath and falls back to the current directory.
 */
public class ImageSearchExample {

    private static final Path CLIP_DIR = Paths.get("target", "clip");

    private static final Path VISION_MODEL = CLIP_DIR.resolve("vision_model.onnx");
    private static final Path TEXT_MODEL = CLIP_DIR.resolve("text_model.onnx");
    private static final Path TOKENIZER = CLIP_DIR.resolve("tokenizer.json");

    public static void main(String[] args) throws Exception {
        ensureModelFilesPresent();

        // The vision model holds a native ONNX session and is AutoCloseable; the text model is not.
        try (OnnxImageEmbeddingModel imageModel = buildImageModel()) {
            OnnxEmbeddingModel textModel = buildTextModel();

            EmbeddingStore<TextSegment> imageStore = indexImages(imageModel);

            imageToImageSearch(imageModel, imageStore);
            textToImageSearch(textModel, imageStore);

            // Bonus: embeddings from the two towers land in the same vector space, so you can even
            // compare an image against a caption directly without a store.
            System.out.println("dimension of image embeddings : " + imageModel.dimension());
            System.out.println("dimension of text embeddings  : " + textModel.dimension());
        }
    }

    private static OnnxImageEmbeddingModel buildImageModel() {
        return OnnxImageEmbeddingModel.builder()
                .pathToModel(VISION_MODEL)
                // CLIP was trained with a 224x224 center crop and this exact mean/std normalisation.
                .preprocessorConfig(ImagePreprocessorConfig.CLIP)
                .build();
    }

    private static OnnxEmbeddingModel buildTextModel() {
        // CLIP's text tower pools internally, so it needs only CLS. The tokenizer comes from the same model repo.
        return new OnnxEmbeddingModel(TEXT_MODEL, TOKENIZER, PoolingMode.CLS);
    }

    /**
     * Embeds every image in the {@code imageDir} folder (or, as a fallback, every image the classloader finds under
     * {@code /images}) and stores it. The image URI is recorded as the {@link TextSegment} so that search results
     * tell you which file matched.
     */
    private static EmbeddingStore<TextSegment> indexImages(OnnxImageEmbeddingModel imageModel) throws Exception {
        List<URI> imageUris = collectImages();
        if (imageUris.isEmpty()) {
            throw new IllegalStateException("No images found. Put some images in resources/images or set "
                    + "-DimageDir=/path/to/folder/of/images and re-run.");
        }

        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        List<String> ids = new ArrayList<>();
        List<Embedding> embeddings = new ArrayList<>();
        List<TextSegment> segments = new ArrayList<>();

        for (URI imageUri : imageUris) {
            Embedding embedding = embedImage(imageModel, imageUri);
            ids.add(imageUri.toString());
            segments.add(TextSegment.from(imageUri.toString()));
            embeddings.add(embedding);
            System.out.println("Indexed " + imageUri);
        }

        store.addAll(ids, embeddings, segments);
        System.out.println("Indexed " + embeddings.size() + " images.");
        return store;
    }

    private static Embedding embedImage(OnnxImageEmbeddingModel imageModel, URI imageUri) {
        EmbeddingResponse response = imageModel.embed(EmbeddingRequest.builder()
                .input(ImageContent.from(imageUri))
                .build());
        return response.embeddings().get(0);
    }

    private static void imageToImageSearch(OnnxImageEmbeddingModel imageModel, EmbeddingStore<TextSegment> store)
            throws Exception {
        List<URI> imageUris = collectImages();
        URI queryUri = imageUris.get(0);

        System.out.println("\n=== Image-to-image search ===");
        System.out.println("Query image: " + queryUri);

        Embedding queryEmbedding = embedImage(imageModel, queryUri);
        searchAndPrint(store, queryEmbedding, 3);
    }

    private static void textToImageSearch(OnnxEmbeddingModel textModel, EmbeddingStore<TextSegment> store) {
        System.out.println("\n=== Text-to-image search ===");
        System.out.println("Query text: \"a photo of a cat\"");

        Embedding queryEmbedding = textModel.embed("a photo of a cat").content();
        searchAndPrint(store, queryEmbedding, 3);
    }

    private static void searchAndPrint(EmbeddingStore<TextSegment> store, Embedding queryEmbedding, int maxResults) {
        List<EmbeddingMatch<TextSegment>> matches = store.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .build()).matches();

        int rank = 1;
        for (EmbeddingMatch<TextSegment> match : matches) {
            double similarity = CosineSimilarity.between(queryEmbedding, match.embedding());
            System.out.println(rank++ + ") " + match.embedded().text() + "  (similarity " + similarity + ")");
        }
    }

    private static List<URI> collectImages() throws Exception {
        Path imageDir = Paths.get(System.getProperty("imageDir", findDefaultImageDir()));
        if (Files.isDirectory(imageDir)) {
            try (Stream<Path> stream = Files.list(imageDir)) {
                return stream.filter(Files::isRegularFile)
                        .filter(ImageSearchExample::isImage)
                        .map(Path::toUri)
                        .toList();
            }
        }
        return List.of();
    }

    private static String findDefaultImageDir() {
        java.net.URL resources = ImageSearchExample.class.getResource("/images");
        if (resources != null && "file".equals(resources.getProtocol())) {
            try {
                return Paths.get(resources.toURI()).toString();
            } catch (java.net.URISyntaxException ignored) {
                // fall through to the working directory
            }
        }

        return ".";
    }

    private static boolean isImage(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
                || name.endsWith(".gif") || name.endsWith(".bmp");
    }

    private static void ensureModelFilesPresent() {
        if (!Files.exists(VISION_MODEL) || !Files.exists(TEXT_MODEL) || !Files.exists(TOKENIZER)) {
            throw new IllegalStateException(
                    "CLIP model files are missing under " + CLIP_DIR + ". "
                            + "Run `mvn -q process-resources` first to download them.");
        }
    }
}
