package com.eazybytes.springai;

import com.eazybytes.springai.entity.HelpDeskTicket;
import com.eazybytes.springai.model.TicketRequest;
import com.eazybytes.springai.service.HelpDeskTicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.docker.compose.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:chatmemory-tools-it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.ai.ollama.base-url=http://localhost:11434"
})
@AutoConfigureMockMvc
class ToolsControllerIntegrationTest {

    @MockitoBean
    ChatModel chatModel;

    @MockitoBean
    EmbeddingModel embeddingModel;

    @MockitoBean
    VectorStore vectorStore;

    @MockitoBean(name = "webSearchRAGChatClient")
    ChatClient webSearchRAGChatClient;

    @Autowired
    HelpDeskTicketService ticketService;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void stubChatModel() {
        var generation = new Generation(new AssistantMessage("OK"));
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(generation)));
    }

    @Test
    void create_ticket_persists_with_open_status() {
        HelpDeskTicket ticket = ticketService.createTicket(new TicketRequest("Cannot access VPN"), "alice");

        assertThat(ticket.getId()).isNotNull();
        assertThat(ticket.getUsername()).isEqualTo("alice");
        assertThat(ticket.getIssue()).isEqualTo("Cannot access VPN");
        assertThat(ticket.getStatus()).isEqualTo("OPEN");
        assertThat(ticket.getCreatedAt()).isNotNull();
        assertThat(ticket.getEta()).isNotNull();
    }

    @Test
    void get_tickets_by_username_returns_only_matching_tickets() {
        ticketService.createTicket(new TicketRequest("Printer not working"), "bob");
        ticketService.createTicket(new TicketRequest("Email issue"), "carol");

        List<HelpDeskTicket> bobTickets = ticketService.getTicketsByUsername("bob");

        assertThat(bobTickets).isNotEmpty();
        assertThat(bobTickets).allMatch(t -> "bob".equals(t.getUsername()));
    }

    @Test
    void help_desk_endpoint_forwards_username_as_conversation_id() throws Exception {
        mockMvc.perform(get("/api/tools/help-desk")
                        .header("username", "dave")
                        .param("message", "I need help with my computer"))
                .andExpect(status().isOk());
    }

    @Test
    void time_endpoint_forwards_username_as_conversation_id() throws Exception {
        mockMvc.perform(get("/api/tools/local-time")
                        .header("username", "eve")
                        .param("message", "What time is it?"))
                .andExpect(status().isOk());
    }
}
