package dev.example;

import dev.langchain4j.service.SystemMessage;

interface CustomerSupportAgent {

    @SystemMessage({
            "You are a customer support agent of a car rental company named 'Miles of Smiles'.",
            "Before providing information about booking or cancelling booking, you must always check:",
            "booking number, customer name and surname."
    })
    String chat(String message);
}