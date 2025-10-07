# LangChain4j Agentic Tutorial

## Table of Contents

- [_1a_Basic_Agent_Example](_1_basic_agent/_1a_Basic_Agent_Example.java) - String output agent
- [_1b_Basic_Agent_Example_Structured](_1_basic_agent/_1b_Basic_Agent_Example_Structured.java) - Structured output agent
- [_2a_Sequential_Agent_Example](_2_sequential_workflow/_2a_Sequential_Agent_Example.java) - Untyped sequential workflow
- [_2b_Sequential_Agent_Example_Typed](_2_sequential_workflow/_2b_Sequential_Agent_Example_Typed.java) - Typed sequential workflow
- [_3a_Loop_Agent_Example](_3_loop_workflow/_3a_Loop_Agent_Example.java) - Basic loop with exit condition
- [_3b_Loop_Agent_Example_States_And_Fail](_3_loop_workflow/_3b_Loop_Agent_Example_States_And_Fail.java) - Advanced loop with state tracking
- [_4_Parallel_Workflow_Example](_4_parallel_workflow/_4_Parallel_Workflow_Example.java) - Concurrent agent execution
- [_5_Conditional_Workflow_Example](_5_conditional_workflow/_5a_Conditional_Workflow_Example.java) - Score-based branching
- [_5_Conditional_Workflow_Example_Async](_5_conditional_workflow/_5b_Conditional_Workflow_Example_Async.java) - Score-based branching
- [_6_Composed_Workflow_Example](_6_composed_workflow/_6_Composed_Workflow_Example.java) - Nested workflow composition
- [_7a_Supervisor_Orchestration](_7_supervisor_orchestration/_7a_Supervisor_Orchestration.java) - Basic supervisor
- [_7b_Supervisor_Orchestration_Advanced](_7_supervisor_orchestration/_7b_Supervisor_Orchestration_Advanced.java) - Advanced supervisor
- [_8_Non_AI_Agents](_8_non_ai_agents/_8_Non_AI_Agents.java) - Deterministic operations
- [_9a_HumanInTheLoop_Simple_Validator](_9_human_in_the_loop/_9a_HumanInTheLoop_Simple_Validator.java) - Human-in-the-loop validation
- [_9b_HumanInTheLoop_Chatbot_With_Memory](_9_human_in_the_loop/_9b_HumanInTheLoop_Chatbot_With_Memory.java) - Interactive chatbot with exit condition

## Quick Navigation

- [Inspecting the AgenticScope](_2_sequential_workflow/_2b_Sequential_Agent_Example_Typed.java)
- [Manipulating the AgenticScope (AgenticAction)](_8_non_ai_agents/_8_Non_AI_Agents.java)
- [Structured input and return types (POJOs)](_1_basic_agent/_1b_Basic_Agent_Example_Structured.java)
- [Untyped vs. typed superagents](_2_sequential_workflow/_2b_Sequential_Agent_Example_Typed.java)
- [Aggregating outputs from multiple agents](_4_parallel_workflow/_4_Parallel_Workflow_Example.java)
- [Agents using tools](_5_conditional_workflow/_5a_Conditional_Workflow_Example.java)
- [Agents using RAG](_5_conditional_workflow/_5a_Conditional_Workflow_Example.java)
- [Latency and faster execution](_7_supervisor_orchestration/_7a_Supervisor_Orchestration.java)
- [Loops and exit conditions](_3_loop_workflow/_3a_Loop_Agent_Example.java)
- [Manipulating logs](util/log/CustomLogging.java)
- [Async / non-blocking agents](_5_conditional_workflow/_5b_Conditional_Workflow_Example_Async.java)
- [Obtaining human validation](_9_human_in_the_loop/_9a_HumanInTheLoop_Simple_Validator.java)
- [Combining AiServices and Agents](_9_human_in_the_loop/_9b_HumanInTheLoop_Chatbot_With_Memory.java)
- [Goal-oriented chatbot to fit in larger workflow](_9_human_in_the_loop/_9b_HumanInTheLoop_Chatbot_With_Memory.java)
- [Memory handling in agentic systems](_9_human_in_the_loop/_9b_HumanInTheLoop_Chatbot_With_Memory.java)

## Examples Coming Soon

- Persisted state
- Declarative API
- Handling interruptions and errors
- Full JSON mode
- Testing invocation order and single agents
- Chatbot subagent with a goal
- Observability
- Guardrails and Permissions
- A2A integration

