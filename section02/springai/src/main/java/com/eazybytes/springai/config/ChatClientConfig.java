package com.eazybytes.springai.config;

import com.eazybytes.springai.advisors.TokenUsageAuditAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	public static final String DEFAULT_MODEL = "llama3.2:1b";

	public static final double DEFAULT_TEMPERATURE = 0.8;

	public static final String HR_ASSISTANT_SYSTEM_PROMPT = """
			You are an internal HR assistant. Your role is to help
			employees with questions related to HR policies, such as
			leave policies, working hours, benefits, and code of conduct.
			If a user asks for help with anything outside of these topics,
			kindly inform them that you can only assist with queries related to
			HR policies.
			""";

	public static final String DEFAULT_USER_MESSAGE = "How can you help me?";

	@Bean
	public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
		ChatOptions defaultOptions = ChatOptions.builder()
				.model(DEFAULT_MODEL)
				.temperature(DEFAULT_TEMPERATURE)
				.build();

		return chatClientBuilder
				.defaultOptions(defaultOptions)
				.defaultAdvisors(new SimpleLoggerAdvisor(), new TokenUsageAuditAdvisor())
				.defaultSystem(HR_ASSISTANT_SYSTEM_PROMPT)
				.defaultUser(DEFAULT_USER_MESSAGE)
				.build();
	}

}
