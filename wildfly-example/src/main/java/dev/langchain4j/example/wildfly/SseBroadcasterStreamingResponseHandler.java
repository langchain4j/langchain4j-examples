/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package dev.langchain4j.example.wildfly;

import java.util.concurrent.atomic.AtomicInteger;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

public class SseBroadcasterStreamingResponseHandler implements StreamingChatResponseHandler{

    private final AtomicInteger lastEventId;
    private final SseEventSink sseEventSink;
    private final OutboundSseEvent.Builder eventBuilder;

    public SseBroadcasterStreamingResponseHandler(SseEventSink sseEventSink, Sse sse, int lastEventId) {
        this.lastEventId = new AtomicInteger(lastEventId);
        this.sseEventSink = sseEventSink;
        this.eventBuilder = sse.newEventBuilder();
    }

    @Override
    public void onError(Throwable error) {
        error.printStackTrace();
    }

    @Override
    public void onPartialResponse(String partialResponse) {
        OutboundSseEvent sseEvent = eventBuilder
                .name("token")
                .id(String.valueOf(lastEventId.getAndIncrement()))
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .data(partialResponse.replace("\n", "<br/>"))
                .reconnectDelay(3000)
                .comment("This is a token from the llm")
                .build();
        sseEventSink.send(sseEvent);
    }

    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {
        OutboundSseEvent sseEvent = eventBuilder
                .name("token")
                .id(String.valueOf(lastEventId.getAndIncrement()))
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .data("end-data-token")
                .reconnectDelay(3000)
                .comment("This is a token from the llm")
                .build();
        sseEventSink.send(sseEvent)
                .whenComplete((event, throwable) -> {
                    sseEventSink.close();
                });
    }
}
