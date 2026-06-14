package com.eazybytes.springai.controller;

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
	ChatClient chatClient;

	@Test
	void returns_chat_response_relying_on_default_system_prompt() throws Exception {
		when(chatClient
				.prompt()
				.user("How many leave days do I have?")
				.call()
				.content())
				.thenReturn("You have 20 paid leave days remaining.");

		mvc.perform(get("/api/chat").param("message", "How many leave days do I have?"))
				.andExpect(status().isOk())
				.andExpect(content().string("You have 20 paid leave days remaining."));
	}

}
