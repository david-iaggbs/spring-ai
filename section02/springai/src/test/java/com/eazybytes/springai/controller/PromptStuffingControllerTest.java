package com.eazybytes.springai.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromptStuffingController.class)
class PromptStuffingControllerTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
	ChatClient chatClient;

	@Test
	void returns_answer_grounded_in_stuffed_system_prompt() throws Exception {
		when(chatClient
				.prompt()
				.options(any(ChatOptions.class))
				.system(any(Resource.class))
				.user("What is your refund policy?")
				.call()
				.content())
				.thenReturn("Full refund within 14 days of purchase.");

		mvc.perform(get("/api/prompt-stuffing").param("message", "What is your refund policy?"))
				.andExpect(status().isOk())
				.andExpect(content().string("Full refund within 14 days of purchase."));
	}

}
