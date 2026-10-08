package aca.examples;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.ServerSentEventParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.time.Duration;

public class SimpleHttpClientBuilder implements HttpClientBuilder {
    private Duration connectTimeout;
    private Duration readTimeout;

    @Override
    public Duration connectTimeout() {
        return this.connectTimeout;
    }

    @Override
    public HttpClientBuilder connectTimeout(Duration timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    @Override
    public Duration readTimeout() {
        return this.readTimeout;
    }

    @Override
    public HttpClientBuilder readTimeout(Duration timeout) {
        this.readTimeout = timeout;
        return this;
    }

    @Override
    public HttpClient build() {
        return new SimpleHttpClient(this);
    }

    private static class SimpleHttpClient implements HttpClient {
        private final java.net.http.HttpClient httpClient;
        private final Duration readTimeout;

        public SimpleHttpClient(SimpleHttpClientBuilder builder) {
            java.net.http.HttpClient.Builder clientBuilder = java.net.http.HttpClient.newBuilder();
            if (builder.connectTimeout() != null) {
                clientBuilder.connectTimeout(builder.connectTimeout());
            }
            this.httpClient = clientBuilder.build();
            this.readTimeout = builder.readTimeout();
        }

        @Override
        public SuccessfulHttpResponse execute(HttpRequest request) {
            try {
                java.net.http.HttpRequest.Builder reqBuilder = java.net.http.HttpRequest.newBuilder()
                        .uri(URI.create(request.url()));

                request.headers().forEach((name, values) -> {
                    if (values != null) {
                        for (String value : values) {
                            reqBuilder.header(name, value);
                        }
                    }
                });

                if (request.body() != null) {
                    reqBuilder.method(
                            request.method().name(),
                            java.net.http.HttpRequest.BodyPublishers.ofString(request.body())
                    );
                } else {
                    reqBuilder.method(
                            request.method().name(),
                            java.net.http.HttpRequest.BodyPublishers.noBody()
                    );
                }

                if (readTimeout != null) {
                    reqBuilder.timeout(readTimeout);
                }

                HttpResponse<String> response = httpClient.send(
                        reqBuilder.build(),
                        HttpResponse.BodyHandlers.ofString()
                );

                return SuccessfulHttpResponse.builder()
                        .statusCode(response.statusCode())
                        .headers(response.headers().map())
                        .body(response.body())
                        .build();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Error executing HTTP request", e);
            }
        }

        @Override
        public void execute(HttpRequest request, ServerSentEventParser parser, ServerSentEventListener listener) {
            throw new UnsupportedOperationException("SSE not supported in this simple implementation");
        }
    }
}
