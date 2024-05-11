package org.springframework.samples.petclinic.chat;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties(prefix = LocalProperties.PREFIX)
public class LocalProperties {

	static final String PREFIX = "langchain4j.local.spring";

	@NestedConfigurationProperty
	ContentRetrieverProperties contentRetriever;

	@NestedConfigurationProperty
	LocalMemoryProperties memory;

	@Getter
	@Setter
	public static class ContentRetrieverProperties {

		String maxResults;

		String minScore;

		String contentPath;

	}

	@Getter
	@Setter
	public static class LocalMemoryProperties {

		boolean useLocal;

		int memorySize;

	}

}
