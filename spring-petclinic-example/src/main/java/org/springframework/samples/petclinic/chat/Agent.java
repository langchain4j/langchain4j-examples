package org.springframework.samples.petclinic.chat;

import dev.langchain4j.service.*;

public interface Agent {

	String SYSTEM_PROMPT = "You are a customer support agent of a pet clinic. You will answer question from a petclinic customer."
			+ "you will always answer the customer question according to Pet clinic Terms of Use";

	String USER_PROMPT = "The Customer Name is {{username}}";

	@SystemMessage({ SYSTEM_PROMPT, USER_PROMPT })
	String chat(@UserMessage String message, @MemoryId @UserName @V("username") String username);

}
