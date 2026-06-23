package com.eazybytes.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * Exposes a single chat endpoint that remembers the conversation per user.
 *
 * <p>The {@code username} header is used as the {@code CONVERSATION_ID}: each user
 * gets a fully isolated message history, and one stateless {@link ChatClient} can
 * serve many concurrent conversations simply by passing a different id per call.
 */
@RestController
@RequestMapping("/api")
public class ChatMemoryController {

	private final ChatClient chatClient;

	public ChatMemoryController(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@GetMapping("/chat-memory")
	public ResponseEntity<String> chatMemory(@RequestHeader("username") String username,
			@RequestParam("message") String message) {
		String answer = chatClient
				.prompt()
				.user(message)
				.advisors(advisorSpec -> advisorSpec.param(CONVERSATION_ID, username))
				.call()
				.content();
		return ResponseEntity.ok(answer);
	}

}
