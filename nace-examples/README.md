# Nace Drex examples

Two runnable examples using the existing `TypeSafeDecisionModel` and Nace's
System One-compatible API. No additional provider or Nace SDK is needed.

Requires Java 17+ and the released LangChain4j dependencies selected by this module's POM.
Get a Nace API key from [Nace Console](https://console.nace.ai).
Run the commands below from the repository root, after setting the required environment variables.

## 1. Typed decisions

Set `NACE_API_KEY`, then run:

```shell
./mvnw -f nace-examples/pom.xml compile exec:java -Dexec.mainClass=DrexDecisionExamples
```

The example sends a support ticket to `drex-max-v1.1` and asks three questions in one request:

- Is the customer asking for a refund? (yes/no probability)
- Which team should handle it? (choice and probabilities)
- How urgent is it? (ordered scale and probabilities)

To try another ticket:

```shell
./mvnw -f nace-examples/pom.xml compile exec:java \
  -Dexec.mainClass=DrexDecisionExamples \
  -Dexec.args="I cannot log in after resetting my password."
```

This example prints actual answers and token usage. The yes/no probability means
P(yes), not confidence in either outcome. Choose application thresholds using evaluated data.

## 2. Select a chat model for an AI Service

In addition to `NACE_API_KEY`, set `OPENAI_API_KEY`, `SMALL_CHAT_MODEL` and
`LARGE_CHAT_MODEL` to two chat model IDs available to that OpenAI account.
Both models must support the requests you send. You can replace these configured
`ChatModel` instances with models from other providers, each with its own credentials.

```shell
./mvnw -f nace-examples/pom.xml compile exec:java \
  -Dexec.mainClass=DrexModelRoutingExample \
  -Dexec.args="How should I debug intermittent timeouts across three services?"
```

`DecisionModelChatModelRouter` asks Drex to choose the `simple` or `complex` route,
then `RoutingChatModel` passes the original generation request to that chat model.
The AI Service prints the chosen route and generated answer.

This is **choice-based routing through `/v1/systemone`**, not the specialized
[Drex Router `/v1/route` API](https://console.nace.ai/docs/drex/guides/router).
Generation usage printed by the AI Service excludes the routing request. Account for
both services when comparing cost and latency; no savings are assumed by the example.

## Behavior and limitations

- The base URL is `https://console.nace.ai`, without `/v1`; the existing client appends
  `v1/systemone`. The small subclass overrides only provider identity to `OTHER` so
  metrics do not attribute Nace calls to TypeSafe. Headers identify the example to Nace.
- The native client supports text decisions and `decideAsync`. It rejects image inputs,
  even though some Drex models support images through other clients. The routing
  example is text-only and does not inspect image pixels.
- A failed routing decision falls back to the configured `complex` route, which can
  incur generation cost even if Nace authentication or availability fails. This is
  selection fallback, not retrying a failed generation on another model. Both clients
  use bounded timeouts and disable automatic retries in these examples.
- Route descriptions do not enforce model capabilities. Select candidates compatible
  with your context size, tools, output format and input modalities before routing.
- These examples preserve the standard answers, probability distributions and token
  usage exposed by the native client; Nace-specific request IDs/timings are not exposed.

## Offline verification

```shell
./mvnw -f nace-examples/pom.xml test
```

The tests run these examples' request construction and routing factory against a local
HTTP stub and fake chat models. No Nace/OpenAI credentials or live calls are required.
They cover typed answers, async transport, authentication errors, AI Services model
selection, original request preservation and the configured fallback.

The main classes make real API calls when run with your credentials. Live service
availability and benchmark gains are separate from the offline tests.
