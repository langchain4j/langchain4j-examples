import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.FileSystemDocumentLoader;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DocumentLoaderExamples {

    static class Load_Pdf_From_File_System_Example {

        public static void main(String[] args) {
            Path filePath = toPath("example-files/story-about-happy-carrot.pdf");
            Document document = FileSystemDocumentLoader.loadDocument(filePath);
            System.out.println(document);
        }
    }

    static class Load_Docx_From_File_System_Example {

        public static void main(String[] args) {
            Path filePath = toPath("example-files/story-about-happy-carrot.docx");
            Document document = FileSystemDocumentLoader.loadDocument(filePath);
            System.out.println(document);
        }
    }

    static class Load_The_Whole_Directory_Example {

        public static void main(String[] args) {
            Path directoryPath = toPath("example-files");
            List<Document> documents = FileSystemDocumentLoader.loadDocuments(directoryPath);
            documents.forEach(System.out::println);
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
