package com.eazybytes.springai;

import com.eazybytes.springai.model.TicketRequest;
import com.eazybytes.springai.service.HelpDeskTicketService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
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
        "spring.ai.ollama.chat.options.model=" + ToolsControllerOllamaIT.CHAT_MODEL,
        "spring.ai.ollama.embedding.options.model=" + ToolsControllerOllamaIT.EMBED_MODEL,
        "spring.ai.ollama.init.pull-model-strategy=when_missing",
        "spring.datasource.url=jdbc:h2:mem:chatmemory-tools-e2e;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ToolsControllerOllamaIT {

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
    HelpDeskTicketService ticketService;

    @Autowired
    ChatMemory chatMemory;

    @BeforeAll
    static void pullModels() throws Exception {
        ollama.execInContainer("ollama", "pull", CHAT_MODEL);
        ollama.execInContainer("ollama", "pull", EMBED_MODEL);
    }

    @Test
    void time_tool_returns_ok_for_local_time_query() throws Exception {
        mvc.perform(get("/api/tools/local-time")
                        .header("username", "timeuser")
                        .param("message", "What is the current local time?"))
                .andExpect(status().isOk());
    }

    @Test
    void help_desk_endpoint_returns_ok_when_issue_reported() throws Exception {
        mvc.perform(get("/api/tools/help-desk")
                        .header("username", "ticketuser")
                        .param("message", "Please create a support ticket: I cannot access the VPN"))
                .andExpect(status().isOk());
    }

    @Test
    void help_desk_endpoint_returns_ok_when_querying_ticket_status() throws Exception {
        ticketService.createTicket(new TicketRequest("Keyboard not working"), "statususer");

        mvc.perform(get("/api/tools/help-desk")
                        .header("username", "statususer")
                        .param("message", "What are my open support tickets?"))
                .andExpect(status().isOk());
    }

    @Test
    void conversation_turns_are_stored_in_chat_memory() throws Exception {
        String username = "memoryuser";

        mvc.perform(get("/api/tools/local-time")
                        .header("username", username)
                        .param("message", "My name is Alice and I have a printer issue"))
                .andExpect(status().isOk());

        List<?> messages = chatMemory.get(username);
        assertThat(messages).isNotEmpty();
    }

}
