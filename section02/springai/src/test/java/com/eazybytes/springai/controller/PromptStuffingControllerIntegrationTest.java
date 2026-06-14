package com.eazybytes.springai.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"spring.ai.ollama.init.pull-model-strategy=never",
		"spring.ai.ollama.init.chat.include=false"
})
@AutoConfigureMockMvc
class PromptStuffingControllerIntegrationTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
	ChatClient chatClient;

	@Test
	void per_call_ollama_options_override_defaults_with_strict_temperature() throws Exception {
		mvc.perform(get("/api/prompt-stuffing").param("message", "What are your office hours?"))
				.andExpect(status().isOk());

		ArgumentCaptor<ChatOptions> optionsCaptor = ArgumentCaptor.forClass(ChatOptions.class);
		verify(chatClient.prompt()).options(optionsCaptor.capture());

		ChatOptions captured = optionsCaptor.getValue();
		assertThat(captured)
				.as("per-call options should be a provider-specific OllamaOptions instance")
				.isInstanceOf(OllamaOptions.class);
		assertThat(captured.getModel()).isEqualTo(PromptStuffingController.STRICT_MODEL);
		assertThat(captured.getTemperature()).isEqualTo(PromptStuffingController.STRICT_TEMPERATURE);
		assertThat(captured.getTopP()).isEqualTo(PromptStuffingController.STRICT_TOP_P);
	}

	@Test
	void system_resource_is_loaded_from_classpath_and_passed_to_chat_client() throws Exception {
		mvc.perform(get("/api/prompt-stuffing").param("message", "What are your office hours?"))
				.andExpect(status().isOk());

		ArgumentCaptor<Resource> resourceCaptor = ArgumentCaptor.forClass(Resource.class);
		verify(chatClient.prompt().options(org.mockito.ArgumentMatchers.any(ChatOptions.class)))
				.system(resourceCaptor.capture());
		verify(chatClient.prompt().options(org.mockito.ArgumentMatchers.any(ChatOptions.class))
				.system(resourceCaptor.getValue()))
				.user("What are your office hours?");

		Resource captured = resourceCaptor.getValue();
		String stuffed = new String(captured.getInputStream().readAllBytes());
		assertThat(stuffed).contains("Office hours", "Refund policy");
	}

}
