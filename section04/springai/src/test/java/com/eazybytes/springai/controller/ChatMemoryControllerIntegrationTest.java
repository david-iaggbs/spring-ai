package com.eazybytes.springai.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-context wiring check: the {@link ChatClient} is mocked (no real model call),
 * so this verifies that the controller forwards the {@code username} header as the
 * {@code CONVERSATION_ID} advisor parameter — the contract the memory advisor relies
 * on. The model itself is exercised in {@code ChatMemoryControllerOllamaIT}.
 */
@SpringBootTest(properties = {
		"spring.ai.ollama.init.pull-model-strategy=never",
		"spring.ai.ollama.init.chat.include=false",
		"spring.datasource.url=jdbc:h2:mem:chatmemory-it;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
class ChatMemoryControllerIntegrationTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
	ChatClient chatClient;

	@Test
	@SuppressWarnings("unchecked")
	void forwards_username_header_as_conversation_id() throws Exception {
		mvc.perform(get("/api/chat-memory")
						.header("username", "madan03")
						.param("message", "What is my name?"))
				.andExpect(status().isOk());

		// Capture the per-call advisor customizer the controller passed, then replay it
		// against a spec mock to assert the conversation id it binds.
		ArgumentCaptor<Consumer<ChatClient.AdvisorSpec>> captor = ArgumentCaptor.forClass(Consumer.class);
		verify(chatClient.prompt().user("What is my name?")).advisors(captor.capture());

		ChatClient.AdvisorSpec advisorSpec = mock(ChatClient.AdvisorSpec.class);
		when(advisorSpec.param(any(), any())).thenReturn(advisorSpec);
		captor.getValue().accept(advisorSpec);

		verify(advisorSpec).param(eq(CONVERSATION_ID), eq("madan03"));
	}

}
