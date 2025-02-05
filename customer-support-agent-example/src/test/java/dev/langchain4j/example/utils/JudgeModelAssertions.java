package dev.langchain4j.example.utils;

import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.output.JsonSchemas;

import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.internal.ValidationUtils.ensureNotEmpty;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static dev.langchain4j.model.chat.request.ResponseFormatType.JSON;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.fail;

public class JudgeModelAssertions {

    private enum ConditionAssessmentResult {

        SATISFIED, NOT_SATISFIED, NOT_SURE
    }

    private record ConditionAssessment(
            int conditionIndex,
            String reasoning,
            ConditionAssessmentResult result) {
    }

    private record ConditionAssessments(List<ConditionAssessment> conditionAssessments) {
    }

    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    private static final ResponseFormat RESPONSE_FORMAT = ResponseFormat.builder()
            .type(JSON)
            .jsonSchema(JsonSchemas.jsonSchemaFrom(ConditionAssessments.class).get())
            .build();

    public static ModelAssertion with(ChatLanguageModel judgeModel) {
        return new ModelAssertion(judgeModel);
    }

    public static class ModelAssertion {

        private final ChatLanguageModel judgeModel;

        ModelAssertion(ChatLanguageModel judgeModel) {
            this.judgeModel = ensureNotNull(judgeModel, "judgeModel");
        }

        public TextAssertion assertThat(String text) {
            return new TextAssertion(judgeModel, text);
        }
    }

    public static class TextAssertion {

        private final ChatLanguageModel judgeModel;
        private final String text;

        TextAssertion(ChatLanguageModel judgeModel, String text) {
            this.judgeModel = ensureNotNull(judgeModel, "judgeModel");
            this.text = ensureNotNull(text, "text");
        }

        public TextAssertion satisfies(String... conditions) {
            return satisfies(asList(conditions));
        }

        public TextAssertion satisfies(List<String> conditions) {

            ensureNotEmpty(conditions, "conditions");

            StringBuilder conditionsFormatted = new StringBuilder();
            int i = 0;
            for (String condition : conditions) {
                conditionsFormatted.append("<condition%s>%s</condition%s>".formatted(i, condition, i++));
                conditionsFormatted.append("\n");
            }

            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(
                            SystemMessage.from("""
                                    Does the following text satisfy the following conditions?
                                    %s
                                    Provide index, reasoning and assessment result for each condition.
                                    """.formatted(conditionsFormatted)
                            ),
                            UserMessage.from("<text>%s</text>".formatted(text))
                    )
                    .parameters(ChatRequestParameters.builder()
                            .responseFormat(RESPONSE_FORMAT)
                            .build())
                    .build();

            ChatResponse chatResponse = judgeModel.chat(chatRequest);

            String json = chatResponse.aiMessage().text();
            try {
                ConditionAssessments conditionAssessments = JSON_MAPPER.readValue(json, ConditionAssessments.class);

                List<String> failures = new ArrayList<>();

                for (ConditionAssessment assessment : conditionAssessments.conditionAssessments) {
                    if (assessment.result != ConditionAssessmentResult.SATISFIED) {
                        failures.add("""
                                Condition %s: %s
                                Reasoning: %s
                                """.formatted(
                                assessment.conditionIndex, conditions.get(assessment.conditionIndex),
                                assessment.reasoning
                        ));
                    }
                }

                if (!failures.isEmpty()) {
                    fail("Some conditions were not satisfied for the text '%s':\n\n%s"
                            .formatted(text, String.join("\n", failures)));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return this;
        }
    }
}