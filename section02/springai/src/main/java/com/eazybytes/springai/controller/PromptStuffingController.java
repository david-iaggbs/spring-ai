package com.eazybytes.springai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PromptStuffingController {

	static final String STRICT_MODEL = "llama3.2:1b";

	static final double STRICT_TEMPERATURE = 0.1;

	static final double STRICT_TOP_P = 0.9;

	private final ChatClient chatClient;

	@Value("classpath:/promptTemplates/systemPromptTemplate.st")
	Resource systemPromptTemplate;

	public PromptStuffingController(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@GetMapping("/prompt-stuffing")
	public String promptStuffing(@RequestParam("message") String message) {
		return chatClient
				.prompt()
				.options(OllamaOptions.builder()
						.model(STRICT_MODEL)
						.temperature(STRICT_TEMPERATURE)
						.topP(STRICT_TOP_P)
						.build())
				.system(systemPromptTemplate)
				.user(message)
				.call()
				.content();
	}

}
