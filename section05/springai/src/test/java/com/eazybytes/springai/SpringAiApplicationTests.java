package com.eazybytes.springai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
		"spring.docker.compose.enabled=false",
		"spring.ai.ollama.init.pull-model-strategy=never",
		"spring.ai.ollama.init.chat.include=false",
		"spring.ai.ollama.init.embedding.include=false",
		"spring.datasource.url=jdbc:h2:mem:chatmemory-ctx;DB_CLOSE_DELAY=-1"
})
class SpringAiApplicationTests {

	@MockitoBean
	VectorStore vectorStore;

	@MockitoBean(name = "webSearchRAGChatClient")
	ChatClient webSearchRAGChatClient;

	@Test
	void contextLoads() {
	}

}
