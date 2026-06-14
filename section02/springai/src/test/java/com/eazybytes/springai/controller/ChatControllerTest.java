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
	ChatClient.Builder chatClientBuilder;

	@Test
	void returns_chat_response_using_system_and_user_roles() throws Exception {
		when(chatClientBuilder.build()
				.prompt()
				.system(ChatController.IT_HELPDESK_SYSTEM_PROMPT)
				.user("Reset my password")
				.call()
				.content())
				.thenReturn("Sure — open the self-service portal at /reset.");

		mvc.perform(get("/api/chat").param("message", "Reset my password"))
				.andExpect(status().isOk())
				.andExpect(content().string("Sure — open the self-service portal at /reset."));
	}

}
