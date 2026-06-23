package com.eazybytes.springai.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.qdrant.QdrantContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("e2e")
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.docker.compose.enabled=false",
        "spring.ai.ollama.chat.options.model=" + RAGControllerOllamaIT.CHAT_MODEL,
        "spring.ai.ollama.embedding.options.model=" + RAGControllerOllamaIT.EMBED_MODEL,
        "spring.ai.ollama.init.pull-model-strategy=never",
        "spring.datasource.url=jdbc:h2:mem:chatmemory-rag-e2e;DB_CLOSE_DELAY=-1"
})
class RAGControllerOllamaIT {

    static final String CHAT_MODEL = "llama3.2:1b";
    static final String EMBED_MODEL = "nomic-embed-text";

    @Container
    @ServiceConnection
    static OllamaContainer ollama =
            new OllamaContainer(DockerImageName.parse("ollama/ollama:latest"));

    @Container
    @ServiceConnection
    static QdrantContainer qdrant =
            new QdrantContainer(DockerImageName.parse("qdrant/qdrant:latest"));

    @MockitoBean(name = "webSearchRAGChatClient")
    ChatClient webSearchChatClient;

    @Autowired
    MockMvc mvc;

    @Autowired
    ChatMemory chatMemory;

    @Autowired
    VectorStore vectorStore;

    @BeforeAll
    static void pullModels() throws Exception {
        ollama.execInContainer("ollama", "pull", CHAT_MODEL);
        ollama.execInContainer("ollama", "pull", EMBED_MODEL);
    }

    @Test
    void random_chat_returns_ok() throws Exception {
        mvc.perform(get("/api/rag/random/chat")
                        .header("username", "e2e-user")
                        .param("message", "Say hello in one word"))
                .andExpect(status().isOk());
    }

    @Test
    void document_chat_returns_ok() throws Exception {
        mvc.perform(get("/api/rag/document/chat")
                        .header("username", "e2e-user")
                        .param("message", "What is in the HR policy?"))
                .andExpect(status().isOk());
    }

    @Test
    void conversation_history_is_persisted_in_chat_memory() throws Exception {
        String username = "e2e-memory-user";

        mvc.perform(get("/api/rag/random/chat")
                        .header("username", username)
                        .param("message", "My name is TestBot."))
                .andExpect(status().isOk());

        List<?> messages = chatMemory.get(username);
        assertThat(messages).isNotEmpty();
    }

    @Test
    void hr_policy_documents_are_stored_in_vector_store() {
        SearchRequest request = SearchRequest.builder()
                .query("leave policy")
                .topK(3)
                .build();
        List<?> results = vectorStore.similaritySearch(request);
        assertThat(results).isNotEmpty();
    }

    @Test
    @Disabled("llama3.2:1b is too small to reliably recall names across turns")
    void model_recalls_name_in_second_turn() throws Exception {
        String username = "e2e-recall-user";

        mvc.perform(get("/api/rag/random/chat")
                        .header("username", username)
                        .param("message", "My name is TestBot."))
                .andExpect(status().isOk());

        String response = mvc.perform(get("/api/rag/random/chat")
                        .header("username", username)
                        .param("message", "What is my name?"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).containsIgnoringCase("TestBot");
    }
}
