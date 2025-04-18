package io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant.data;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import io.helidon.service.registry.Service;

import dev.langchain4j.agent.tool.Tool;

/**
 * A service for managing and processing customer orders.
 *
 * This service contains callback functions designed to be invoked by a Language Learning Model (LLM).
 * It provides functionality to save orders, log order details, and generate a unique order ID for tracking.
 */
@Service.Singleton
public class OrderService {
    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());

    /**
     * Saves a new order and returns its unique order ID.
     *
     * This method logs the order details and generates a unique identifier for tracking. It is designed
     * to be called by an LLM.
     *
     * @param orderItems a map where each key is a menu item name and the corresponding value is its price
     * @return a newly generated unique order ID
     */
    @Tool("Saves the order represented by a map there key is a menu item name and value is its price. Returns new order id.")
    public String saveOrder(Map<String, BigDecimal> orderItems) {
        StringBuilder msg = new StringBuilder("\n** New Order **\n");

        var orderId = UUID.randomUUID();
        msg.append("Order id: ").append(orderId).append("\n");
        for (var entry : orderItems.entrySet()) {
            msg.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        LOGGER.info(msg.toString());
        return orderId.toString();
    }
}
