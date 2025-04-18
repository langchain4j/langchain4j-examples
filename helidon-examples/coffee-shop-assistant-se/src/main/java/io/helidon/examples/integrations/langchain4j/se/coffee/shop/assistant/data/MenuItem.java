package io.helidon.examples.integrations.langchain4j.se.coffee.shop.assistant.data;

import java.util.List;

/**
 * Represents a menu item in the coffee shop.
 *
 * A menu item includes details such as its name, description, category, price,
 * tags, and optional add-ons.
 */
public class MenuItem {
    private String name;
    private String description;
    private String category;
    private double price;
    private List<String> tags;
    private List<String> addOns;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getAddOns() {
        return addOns;
    }

    public void setAddOns(List<String> addOns) {
        this.addOns = addOns;
    }
}
