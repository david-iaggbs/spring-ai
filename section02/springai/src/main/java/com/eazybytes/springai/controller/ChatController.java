package com.eazybytes.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

	static final String IT_HELPDESK_SYSTEM_PROMPT = """
			You are an internal IT helpdesk assistant. Your role is to assist
			employees with IT-related issues such as resetting passwords,
			unlocking accounts, and answering questions related to IT policies.
			If a user requests help with anything outside of these
			responsibilities, respond politely and inform them that you are
			only able to assist with IT support tasks within your defined scope.
			""";

	private final ChatClient chatClient;

	public ChatController(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	@GetMapping("/chat")
	public String chat(@RequestParam("message") String message) {
		return chatClient
				.prompt()
				.system(IT_HELPDESK_SYSTEM_PROMPT)
				.user(message)
				.call()
				.content();
	}

}
