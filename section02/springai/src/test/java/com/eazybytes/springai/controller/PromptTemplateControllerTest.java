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

@WebMvcTest(PromptTemplateController.class)
class PromptTemplateControllerTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
	ChatClient chatClient;

	@Test
	void returns_email_draft_using_prompt_template() throws Exception {
		when(chatClient
				.prompt()
				.system(PromptTemplateController.CUSTOMER_SERVICE_SYSTEM_PROMPT)
				.user(any(Consumer.class))
				.call()
				.content())
				.thenReturn("Dear Alice, thanks for reaching out…");

		mvc.perform(get("/api/email")
						.param("customerName", "Alice")
						.param("customerMessage", "My order arrived damaged."))
				.andExpect(status().isOk())
				.andExpect(content().string("Dear Alice, thanks for reaching out…"));
	}

}
