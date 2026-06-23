package com.eazybytes.springai.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RAGController.class)
class RAGControllerTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean(name = "chatMemoryChatClient", answers = Answers.RETURNS_DEEP_STUBS)
	ChatClient chatClient;

	@MockitoBean(name = "webSearchRAGChatClient", answers = Answers.RETURNS_DEEP_STUBS)
	ChatClient webSearchChatClient;

	@MockitoBean
	VectorStore vectorStore;

	@Test
	void random_chat_returns_model_reply() throws Exception {
		when(chatClient.prompt()
				.advisors(any(Consumer.class))
				.user(anyString())
				.call()
				.content())
				.thenReturn("Hello from random chat.");

		mvc.perform(get("/api/rag/random/chat")
						.header("username", "madan03")
						.param("message", "Hello"))
				.andExpect(status().isOk())
				.andExpect(content().string("Hello from random chat."));
	}

	@Test
	void document_chat_returns_model_reply() throws Exception {
		when(chatClient.prompt()
				.advisors(any(Consumer.class))
				.user(anyString())
				.call()
				.content())
				.thenReturn("Leave policy allows 20 days per year.");

		mvc.perform(get("/api/rag/document/chat")
						.header("username", "madan03")
						.param("message", "What is the leave policy?"))
				.andExpect(status().isOk())
				.andExpect(content().string("Leave policy allows 20 days per year."));
	}

	@Test
	void web_search_chat_returns_model_reply() throws Exception {
		when(webSearchChatClient.prompt()
				.advisors(any(Consumer.class))
				.user(anyString())
				.call()
				.content())
				.thenReturn("Java 21 is the current LTS.");

		mvc.perform(get("/api/rag/web-search/chat")
						.header("username", "madan03")
						.param("message", "What is the latest Java LTS?"))
				.andExpect(status().isOk())
				.andExpect(content().string("Java 21 is the current LTS."));
	}

	@Test
	void all_endpoints_require_username_header() throws Exception {
		mvc.perform(get("/api/rag/random/chat").param("message", "Hello"))
				.andExpect(status().isBadRequest());
		mvc.perform(get("/api/rag/document/chat").param("message", "Hello"))
				.andExpect(status().isBadRequest());
		mvc.perform(get("/api/rag/web-search/chat").param("message", "Hello"))
				.andExpect(status().isBadRequest());
	}
}
