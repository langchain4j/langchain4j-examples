package org.springframework.samples.petclinic.chat;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = OpenAIProperties.PREFIX)
public class OpenAIProperties {

	static final String PREFIX = "langchain4j.azure.open-ai";

	@NestedConfigurationProperty
	ChatModelProperties chatModel;

}

@Getter
@Setter
class ChatModelProperties {

	String endpoint;

	String apiKey;

	String organizationId;

	String deploymentName;

	Double temperature;

	Double topP;

	List<String> stop;

	Integer maxTokens;

	Double presencePenalty;

	Double frequencyPenalty;

	Map<String, Integer> logitBias;

	String responseFormat;

	Integer seed;

	String user;

	Duration timeout;

	Integer maxRetries;

	Boolean logRequestsAndResponses;

}