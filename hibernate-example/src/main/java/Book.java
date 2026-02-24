import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.Array;

import dev.langchain4j.store.embedding.hibernate.MetadataAttribute;
import dev.langchain4j.store.embedding.hibernate.UnmappedMetadata;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "Book")
@Table(name = "books")
public class Book {
	@Id
	private String isbn;
	private String title;
	private String fileName;
	@dev.langchain4j.store.embedding.hibernate.Embedding
	@Array(length = 384)
	private float[] embedding;
	@UnmappedMetadata
	private Map<String, Object> metadata;
	@MetadataAttribute
	private String language;
	private List<String> keywords;

	public Book() {
	}

	public Book(String isbn, String title, String fileName, String language, String... keywords) {
		this.isbn = isbn;
		this.title = title;
		this.fileName = fileName;
		this.language = language;
		this.keywords = Arrays.asList( keywords);
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public float[] getEmbedding() {
		return embedding;
	}

	public void setEmbedding(float[] embedding) {
		this.embedding = embedding;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
}