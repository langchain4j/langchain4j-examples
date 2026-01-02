package dev.langchain4j.example.mcp.stdio;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class Calculator {

    @Tool
    public long add(@P("a") long a, @P("b") long b) {
        return a + b;
    }
}
