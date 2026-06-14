package com.eazybytes.springai.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"spring.ai.ollama.init.pull-model-strategy=never",
		"spring.ai.ollama.init.chat.include=false"
})
@AutoConfigureMockMvc
class PromptTemplateControllerIntegrationTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
	ChatClient chatClient;

	@Test
	@SuppressWarnings({"unchecked", "rawtypes"})
	void template_resource_and_params_are_propagated_to_user_prompt_spec() throws Exception {
		mvc.perform(get("/api/email")
						.param("customerName", "Alice")
						.param("customerMessage", "My order arrived damaged."))
				.andExpect(status().isOk());

		ArgumentCaptor<Consumer<ChatClient.PromptUserSpec>> captor =
				ArgumentCaptor.forClass(Consumer.class);
		verify(chatClient.prompt().system(anyString())).user(captor.capture());

		ChatClient.PromptUserSpec userSpec =
				Mockito.mock(ChatClient.PromptUserSpec.class, Mockito.RETURNS_SELF);
		captor.getValue().accept(userSpec);

		verify(userSpec).text(Mockito.any(Resource.class));
		verify(userSpec).param("customerName", "Alice");
		verify(userSpec).param("customerMessage", "My order arrived damaged.");
	}

}
