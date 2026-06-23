package com.eazybytes.springai.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.function.Consumer;

import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.docker.compose.enabled=false",
        "spring.ai.ollama.init.pull-model-strategy=never",
        "spring.ai.ollama.init.chat.include=false",
        "spring.ai.ollama.init.embedding.include=false",
        "spring.datasource.url=jdbc:h2:mem:chatmemory-rag-it;DB_CLOSE_DELAY=-1"
})
class RAGControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean(name = "chatMemoryChatClient", answers = Answers.RETURNS_DEEP_STUBS)
    ChatClient chatClient;

    @MockitoBean(name = "webSearchRAGChatClient", answers = Answers.RETURNS_DEEP_STUBS)
    ChatClient webSearchChatClient;

    @MockitoBean
    VectorStore vectorStore;

    @Test
    void random_chat_forwards_username_as_conversation_id() throws Exception {
        mvc.perform(get("/api/rag/random/chat")
                        .header("username", "madan03")
                        .param("message", "Hello"))
                .andExpect(status().isOk());

        ArgumentCaptor<Consumer<ChatClient.AdvisorSpec>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(chatClient.prompt()).advisors(captor.capture());

        ChatClient.AdvisorSpec advisorSpec = mock(ChatClient.AdvisorSpec.class);
        when(advisorSpec.param(any(), any())).thenReturn(advisorSpec);
        captor.getValue().accept(advisorSpec);
        verify(advisorSpec).param(eq(CONVERSATION_ID), eq("madan03"));
    }

    @Test
    void document_chat_forwards_username_as_conversation_id() throws Exception {
        mvc.perform(get("/api/rag/document/chat")
                        .header("username", "madan03")
                        .param("message", "What is the leave policy?"))
                .andExpect(status().isOk());

        ArgumentCaptor<Consumer<ChatClient.AdvisorSpec>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(chatClient.prompt()).advisors(captor.capture());

        ChatClient.AdvisorSpec advisorSpec = mock(ChatClient.AdvisorSpec.class);
        when(advisorSpec.param(any(), any())).thenReturn(advisorSpec);
        captor.getValue().accept(advisorSpec);
        verify(advisorSpec).param(eq(CONVERSATION_ID), eq("madan03"));
    }

    @Test
    void web_search_chat_forwards_username_as_conversation_id() throws Exception {
        mvc.perform(get("/api/rag/web-search/chat")
                        .header("username", "madan03")
                        .param("message", "What is the latest Java LTS?"))
                .andExpect(status().isOk());

        ArgumentCaptor<Consumer<ChatClient.AdvisorSpec>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(webSearchChatClient.prompt()).advisors(captor.capture());

        ChatClient.AdvisorSpec advisorSpec = mock(ChatClient.AdvisorSpec.class);
        when(advisorSpec.param(any(), any())).thenReturn(advisorSpec);
        captor.getValue().accept(advisorSpec);
        verify(advisorSpec).param(eq(CONVERSATION_ID), eq("madan03"));
    }
}
