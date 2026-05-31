package com.eazybytes.ollama.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
	ChatClient.Builder chatClientBuilder;

	@Test
	void returns_chat_response() throws Exception {
		when(chatClientBuilder.build().prompt("Hello").call().content())
				.thenReturn("Hi there!");

		mvc.perform(get("/api/chat").param("message", "Hello"))
				.andExpect(status().isOk())
				.andExpect(content().string("Hi there!"));
	}

}
