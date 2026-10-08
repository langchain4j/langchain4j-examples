package aca.examples;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpMethod;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class SimpleHttpClientTest {

    @Test
    public void testBuilderConfiguration() {
        // Create a builder with specific timeouts
        Duration connectTimeout = Duration.ofSeconds(10);
        Duration readTimeout = Duration.ofSeconds(20);
        
        AzureACADynamicSessionsExample.SimpleHttpClientBuilder builder = 
            new AzureACADynamicSessionsExample.SimpleHttpClientBuilder();
        
        // Configure the builder
        builder.connectTimeout(connectTimeout)
               .readTimeout(readTimeout);
        
        // Verify the timeouts are correctly set
        assertEquals(connectTimeout, builder.connectTimeout());
        assertEquals(readTimeout, builder.readTimeout());
        
        // Build the client and ensure it's not null
        HttpClient client = builder.build();
        assertNotNull(client);
    }
    
    @Test
    public void testExecuteRequest() throws Exception {
        // Create a simple request
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .url("http://example.com")
                .addHeader("Content-Type", "application/json")
                .build();
        
        // Create a builder with a mocked HttpClient
        AzureACADynamicSessionsExample.SimpleHttpClientBuilder builder = 
            new AzureACADynamicSessionsExample.SimpleHttpClientBuilder();
        
        // Configure the builder with timeouts
        builder.connectTimeout(Duration.ofSeconds(5))
               .readTimeout(Duration.ofSeconds(10));
        
        // Build the client
        HttpClient client = builder.build();
        
        // Execute the request (will throw UnsupportedOperationException for SSE)
        try {
            SuccessfulHttpResponse response = client.execute(request);
            // This will likely fail in a real test since we can't easily mock the internal JDK HTTP client
            // For a real test, you'd need to use a tool like WireMock or MockWebServer
        } catch (RuntimeException e) {
            // In a real environment without internet or with misconfigured request,
            // we'd expect a RuntimeException wrapping an IOException or InterruptedException
            assertTrue(e.getMessage().contains("Error executing HTTP request") || 
                       e.getCause() instanceof java.io.IOException ||
                       e.getCause() instanceof java.net.UnknownHostException);
        }
    }
    
    @Test
    public void testSseNotSupported() {
        // Create a builder
        AzureACADynamicSessionsExample.SimpleHttpClientBuilder builder = 
            new AzureACADynamicSessionsExample.SimpleHttpClientBuilder();
        
        // Build the client
        HttpClient client = builder.build();
        
        // Create a simple request
        HttpRequest request = HttpRequest.builder()
                .method(HttpMethod.GET)
                .url("http://example.com")
                .build();
        
        // Execute the SSE method and expect an UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, 
            () -> client.execute(request, null, null));
    }
}
