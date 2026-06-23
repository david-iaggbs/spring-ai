package com.eazybytes.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires a single {@link ChatClient} that remembers prior turns of a conversation.
 *
 * <p>The memory stack has three layers:
 * <ol>
 *     <li>{@link JdbcChatMemoryRepository} — storage. Auto-configured by the
 *     {@code spring-ai-starter-model-chat-memory-repository-jdbc} starter and
 *     backed by the H2 database in {@code application.properties}; persists one
 *     row per message in {@code SPRING_AI_CHAT_MEMORY}.</li>
 *     <li>{@link MessageWindowChatMemory} — policy. Keeps only the most recent
 *     {@link #MAX_MESSAGES} messages per conversation so the prompt context can't
 *     grow without bound.</li>
 *     <li>{@link MessageChatMemoryAdvisor} — plumbing. On every call it loads the
 *     conversation's prior messages, prepends them to the prompt, and saves the
 *     new user + assistant pair back to the repository.</li>
 * </ol>
 *
 * <p>No persona is set on this client: the lesson is the memory round-trip, so the
 * assistant should answer freely (e.g. "What is my name?") rather than refuse.
 */
@Configuration
public class ChatMemoryChatClientConfig {

	public static final String DEFAULT_MODEL = "llama3.2:1b";

	public static final double DEFAULT_TEMPERATURE = 0.8;

	public static final int MAX_MESSAGES = 10;

	@Bean
	public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
		return MessageWindowChatMemory.builder()
				.chatMemoryRepository(jdbcChatMemoryRepository)
				.maxMessages(MAX_MESSAGES)
				.build();
	}

	@Bean
	public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
		ChatOptions defaultOptions = ChatOptions.builder()
				.model(DEFAULT_MODEL)
				.temperature(DEFAULT_TEMPERATURE)
				.build();

		return chatClientBuilder
				.defaultOptions(defaultOptions)
				.defaultAdvisors(
						new SimpleLoggerAdvisor(),
						MessageChatMemoryAdvisor.builder(chatMemory).build())
				.build();
	}

}
