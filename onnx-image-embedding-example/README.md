# In-process offline image search with CLIP

Demonstrates image search that runs entirely inside the JVM, no API keys and no network access at run time, using
[`OnnxImageEmbeddingModel`](https://github.com/langchain4j/langchain4j/blob/main/langchain4j-embeddings/src/main/java/dev/langchain4j/model/embedding/onnx/OnnxImageEmbeddingModel.java) (langchain4j PR [#4593](https://github.com/langchain4j/langchain4j/pull/4593)).

It loads the **two halves of the same CLIP model**:

- the **vision tower** (`OnnxImageEmbeddingModel`) embeds images,
- the **text tower** (`OnnxEmbeddingModel`) embeds text.

Because both towers project into one shared vector space, embeddings from either end go into the same
`EmbeddingStore`, so you can search images with an image **or** with a plain text description:

- **image-to-image search**: "which stored image looks most like this one?",
- **text-to-image search**: "which stored image matches *a photo of a cat*?".

## Prerequisites

This example needs the version of `langchain4j-embeddings` that ships `OnnxImageEmbeddingModel`, `ImagePreprocessorConfig`,
and the CLIP-capable `OnnxEmbeddingModel` (langchain4j PR #4593). 

## Getting the model files

The CLIP models (`clip-vit-base-patch32`) and its tokenizer are downloaded automatically by the build, with a checksum
and caching, into `target/clip`:

```bash
mvn process-resources
```

They come from [HuggingFace Xenova/clip-vit-base-patch32](https://huggingface.co/Xenova/clip-vit-base-patch32):
`vision_model_quantized.onnx`, `text_model_quantized.onnx`, and `tokenizer.json`.

## Providing images to search over

The example indexes every image in a folder. Either:

- put `.png`/`.jpg` files in `src/main/resources/images`, or
- point at your own folder: `-DimageDir=/path/to/folder/of/images`.

## Run it

```bash
mvn -q exec:java -Dexec.mainClass=dev.langchain4j.example.onnyximageembedding.ImageSearchExample
```

Expected output shows an image store being built, then the top matches for an image query and for the text query
`"a photo of a cat"`, with CLIP-similarity scores.
