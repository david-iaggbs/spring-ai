package com.eazybytes.springai.controller;

import com.eazybytes.springai.config.ChatClientConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
	void chat_client_bean_is_built_with_simple_logger_advisor_and_default_prompts() {
		// Forces ChatClientConfig.chatClient(...) to be initialised; the
		// resulting ChatClient is a deep-stub mock built from the test builder.
		ArgumentCaptor<Advisor[]> advisorsCaptor = ArgumentCaptor.forClass(Advisor[].class);
		verify(chatClientBuilder).defaultAdvisors(advisorsCaptor.capture());
		assertThat(advisorsCaptor.getValue())
				.hasAtLeastOneElementOfType(SimpleLoggerAdvisor.class);

		verify(chatClientBuilder.defaultAdvisors(any(Advisor[].class)))
				.defaultSystem(ChatClientConfig.HR_ASSISTANT_SYSTEM_PROMPT);
		verify(chatClientBuilder.defaultAdvisors(any(Advisor[].class))
				.defaultSystem(ChatClientConfig.HR_ASSISTANT_SYSTEM_PROMPT))
				.defaultUser(ChatClientConfig.DEFAULT_USER_MESSAGE);
	}

	@Test
	void full_context_loads_and_routes_chat_request() throws Exception {
		when(chatClientBuilder
				.defaultAdvisors(any(Advisor[].class))
				.defaultSystem(any(String.class))
				.defaultUser(any(String.class))
				.build()
				.prompt()
				.user(any(String.class))
				.call()
				.content())
				.thenReturn("HR reply");

		mvc.perform(get("/api/chat").param("message", "Tell me about the leave policy"))
				.andExpect(status().isOk())
				.andExpect(content().string("HR reply"));
	}

}
