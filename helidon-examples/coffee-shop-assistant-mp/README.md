# **Coffee Shop Assistant (Helidon MP Version)**

This is a **demo application** showcasing the **Helidon integration with LangChain4J**. It demonstrates how to build an **AI-powered coffee shop assistant** using **Helidon Inject**, OpenAI models, and embedding storage.

NOTE: LangChain4J integration is a preview feature. The APIs shown here are subject to change. These APIs will be finalized in a future release of Helidon.

## **Features**

- Integration with **OpenAI chat models**.
- Utilization of **embedding models**, **embedding store**, **ingestor**, and **content retriever**.
- **Helidon Inject** for dependency injection.
- **Embedding store initialization** from a JSON file.
- Support for **callback functions** to enhance interactions.

## **Build the Application**

To build the application, run:

```shell
mvn clean package
```

## **Run the Application**

Execute the following command to start the application:

```shell
java -jar target/helidon-examples-langchain4j-coffee-shop-assistant-mp.jar
```

Once running, you can interact with the assistant via your browser.

Example:

```
http://localhost:8080/chat?question="What can you offer today?"
```

## Sample Questions and Expected Responses

Here are some example queries you can try:

### Menu and Recommendations

- **"What hot drinks do you have?"**
   - *Expected Response:* A list of **hot drinks** such as **Latte, Cappuccino, Espresso, and Hot Chocolate**.

- **"I'm looking for something sweet. What do you recommend?"**
   - *Expected Response:* Suggestions like **Caramel Frappuccino, Blueberry Muffin, Chocolate Chip Cookie, and Hot Chocolate**.

- **"What drinks can I get with caramel?"**
   - *Expected Response:* Options like **Caramel Frappuccino** and **Latte with caramel syrup add-on**.

### Dietary Preferences

- **"Do you have any vegan options?"**
   - *Expected Response:* Items like **Avocado Toast, Iced Matcha Latte (with non-dairy milk), and Blueberry Muffin (if applicable)**.

### Orders and Availability

- **"Do you have any breakfast items?"**
   - *Expected Response:* Options such as **Avocado Toast, Blueberry Muffin, and Bagel with Cream Cheese**.

- **"Can I order a latte and a cookie?"**
   - *Expected Response:*  
     *"Your order for a coffee and a chocolate chip cookie has been saved. The total is $5.00. Would you like anything else?"*
