package com.eazybytes.springai.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"spring.ai.ollama.init.pull-model-strategy=never",
		"spring.ai.ollama.init.chat.include=false"
})
@AutoConfigureMockMvc
class ChatControllerIntegrationTest {

	@Autowired
	MockMvc mvc;

	@Autowired
	ChatClient.Builder chatClientBuilder;

	@TestConfiguration
	static class MockChatClientBuilderConfig {

		@Bean
		@Primary
		ChatClient.Builder testChatClientBuilder() {
			return Mockito.mock(ChatClient.Builder.class, Answers.RETURNS_DEEP_STUBS);
		}

	}

	@Test
	void full_context_loads_and_routes_chat_request() throws Exception {
		when(chatClientBuilder.build()
				.prompt()
				.system(any(String.class))
				.user(any(String.class))
				.call()
				.content())
				.thenReturn("Helpdesk reply");

		mvc.perform(get("/api/chat").param("message", "How do I unlock my account?"))
				.andExpect(status().isOk())
				.andExpect(content().string("Helpdesk reply"));
	}

}
