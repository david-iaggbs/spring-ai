package com.eazybytes.springai.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatMemoryController.class)
class ChatMemoryControllerTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
	ChatClient chatClient;

	@Test
	void chat_memory_returns_model_reply() throws Exception {
		when(chatClient
				.prompt()
				.user("What is my name?")
				.advisors(any(Consumer.class))
				.call()
				.content())
				.thenReturn("Your name is Madan.");

		mvc.perform(get("/api/chat-memory")
						.header("username", "madan03")
						.param("message", "What is my name?"))
				.andExpect(status().isOk())
				.andExpect(content().string("Your name is Madan."));
	}

	@Test
	void chat_memory_requires_username_header() throws Exception {
		mvc.perform(get("/api/chat-memory").param("message", "Hello"))
				.andExpect(status().isBadRequest());
	}

}
