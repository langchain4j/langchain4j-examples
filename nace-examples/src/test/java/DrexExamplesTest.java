import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.exception.AuthenticationException;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.router.RoutingChatModel;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.service.AiServices;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DrexExamplesTest {

    private static final String PATH = "/v1/systemone";
    private static final String TICKET = "I was charged twice. Please refund one charge.";
    // A request for the alias returns the concrete version that served it.
    private static final String TYPED_RESPONSE = """
            {
              "model": "drex-max-v1.1",
              "answers": {
                "refund": {"noul": 0.95},
                "team": {"choice": "billing", "probabilities": {"billing": 0.88, "support": 0.12}},
                "urgency": {
                  "score": 1.44,
                  "legend": {"0": "low", "1": "medium", "2": "high"},
                  "probabilities": {"0": 0.06, "1": 0.44, "2": 0.5}
                }
              },
              "usage": {"input_tokens": 318, "output_tokens": 0}
            }
            """;

    private WireMockServer server;

    @BeforeEach
    void startServer() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void sends_the_latest_alias_and_retains_resolved_model_and_typed_answers(boolean async) throws Exception {
        server.stubFor(post(urlEqualTo(PATH)).willReturn(okJson(TYPED_RESPONSE)));
        var model = model();
        var request = DrexDecisionExamples.triageRequest(TICKET);

        var response = async
                ? model.decideAsync(request).get(5, TimeUnit.SECONDS)
                : model.decide(request);

        assertEquals(ModelProvider.OTHER, model.provider());
        assertEquals("drex-max-v1.1", response.modelName());
        assertEquals(0.95, response.yesNo("refund").probability());
        assertEquals("billing", response.choice("team").value());
        assertEquals(Map.of("billing", 0.88, "support", 0.12), response.choice("team").probabilities());
        assertEquals(1.44, response.scale("urgency").mean());
        assertEquals(318, response.tokenUsage().inputTokenCount());
        server.verify(1, postRequestedFor(urlEqualTo(PATH))
                .withHeader("Authorization", equalTo("Bearer test-nace-key"))
                .withHeader("X-Nace-Client", equalTo("langchain4j-nace-examples/1.0"))
                .withHeader("User-Agent", equalTo("langchain4j-nace-examples/1.0"))
                .withRequestBody(matchingJsonPath("$.model", equalTo("drex-latest")))
                .withRequestBody(matchingJsonPath("$.state", equalTo(TICKET)))
                .withRequestBody(matchingJsonPath("$.questions.refund.type", equalTo("noul")))
                .withRequestBody(matchingJsonPath("$.questions.team.criteria.billing", equalTo("Payments and refunds")))
                .withRequestBody(matchingJsonPath("$.questions.urgency.criteria[2]", equalTo("high"))));
    }

    @Test
    void surfaces_authentication_errors_without_retrying() {
        server.stubFor(post(urlEqualTo(PATH)).willReturn(aResponse()
                .withStatus(401)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":{\"message\":\"Invalid API key\"}}")));

        assertThrows(AuthenticationException.class,
                () -> model().decide(DrexDecisionExamples.triageRequest(TICKET)));

        server.verify(1, postRequestedFor(urlEqualTo(PATH)));
    }

    @Test
    void runs_the_ai_service_with_the_selected_chat_model() {
        stubRoute("simple");
        var simple = new AtomicReference<ChatRequest>();
        var complex = new AtomicReference<ChatRequest>();
        var routed = DrexModelRoutingExample.routedModel(model(), capturingModel(simple), capturingModel(complex));
        var assistant = AiServices.create(DrexModelRoutingExample.Assistant.class, routed);

        var result = assistant.chat("How do I reset my password?");

        assertEquals("Example answer", result.content());
        assertEquals("simple", result.finalResponse().aiMessage().attributes().get(RoutingChatModel.ROUTE_ATTRIBUTE));
        assertEquals(UserMessage.from("How do I reset my password?"), simple.get().messages().get(0));
        assertNull(complex.get());
        server.verify(1, postRequestedFor(urlEqualTo(PATH))
                .withRequestBody(matchingJsonPath("$.questions.route.type", equalTo("choice"))));
    }

    @Test
    void preserves_the_original_request_when_routing_asynchronously() throws Exception {
        stubRoute("simple");
        var selected = new AtomicReference<ChatRequest>();
        var complex = new AtomicReference<ChatRequest>();
        var routed = DrexModelRoutingExample.routedModel(model(), capturingModel(selected), capturingModel(complex));
        var request = ChatRequest.builder()
                .messages(SystemMessage.from("Use the supplied context."), UserMessage.from("Help me sign in."))
                .temperature(0.2)
                .build();

        var response = routed.chatAsync(request).get(5, TimeUnit.SECONDS);

        assertEquals(request.messages(), selected.get().messages());
        assertEquals(request.parameters(), selected.get().parameters());
        assertEquals("simple", response.aiMessage().attributes().get(RoutingChatModel.ROUTE_ATTRIBUTE));
        assertNull(complex.get());
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 529})
    void uses_the_documented_fallback_if_the_decision_request_fails(int status) {
        server.stubFor(post(urlEqualTo(PATH)).willReturn(aResponse().withStatus(status).withBody("{}")));

        assertComplexFallback();
        server.verify(1, postRequestedFor(urlEqualTo(PATH)));
    }

    @Test
    void uses_the_documented_fallback_for_an_unconfigured_choice() {
        stubRoute("unconfigured");

        assertComplexFallback();
        server.verify(1, postRequestedFor(urlEqualTo(PATH)));
    }

    private void assertComplexFallback() {
        var simple = new AtomicReference<ChatRequest>();
        var complex = new AtomicReference<ChatRequest>();
        var routed = DrexModelRoutingExample.routedModel(model(), capturingModel(simple), capturingModel(complex));

        var response = routed.chat(ChatRequest.builder().messages(UserMessage.from("Help")).build());

        assertNull(simple.get());
        assertNotNull(complex.get());
        assertEquals("complex", response.aiMessage().attributes().get(RoutingChatModel.ROUTE_ATTRIBUTE));
    }

    private DecisionModel model() {
        return DrexDecisionExamples.decisionModel(server.baseUrl(), "test-nace-key");
    }

    private void stubRoute(String choice) {
        server.stubFor(post(urlEqualTo(PATH)).willReturn(okJson("""
                {"model":"drex-max-v1.1","answers":{"route":{
                  "choice":"%s","probabilities":{"simple":0.9,"complex":0.1}
                }}}
                """.formatted(choice))));
    }

    private static ChatModel capturingModel(AtomicReference<ChatRequest> captured) {
        return new ChatModel() {
            @Override
            public ChatResponse doChat(ChatRequest request) {
                captured.set(request);
                return ChatResponse.builder().aiMessage(AiMessage.from("Example answer")).build();
            }

            @Override
            public CompletableFuture<ChatResponse> doChatAsync(ChatRequest request) {
                return CompletableFuture.completedFuture(doChat(request));
            }
        };
    }
}
