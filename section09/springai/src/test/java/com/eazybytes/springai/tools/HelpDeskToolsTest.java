package com.eazybytes.springai.tools;

import com.eazybytes.springai.entity.HelpDeskTicket;
import com.eazybytes.springai.model.TicketRequest;
import com.eazybytes.springai.service.HelpDeskTicketService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class HelpDeskToolsTest {

    @Mock
    private HelpDeskTicketService service;

    @InjectMocks
    private HelpDeskTools helpDeskTools;

    @Test
    void createTicketShouldUseUsernameFromToolContext() {
        ToolContext context = new ToolContext(Map.of("username", "alice"));
        TicketRequest request = new TicketRequest("Mouse not detected");

        HelpDeskTicket saved = HelpDeskTicket.builder()
                .id(77L)
                .username("alice")
                .issue("Mouse not detected")
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .eta(LocalDateTime.now().plusDays(7))
                .build();

        when(service.createTicket(eq(request), eq("alice"))).thenReturn(saved);

        String result = helpDeskTools.createTicket(request, context);

        assertThat(result).contains("Ticket #77 created successfully for user alice");
        verify(service).createTicket(request, "alice");
    }

    @Test
    void getTicketStatusShouldDelegateToServiceUsingContextUsername() {
        ToolContext context = new ToolContext(Map.of("username", "bob"));
        List<HelpDeskTicket> tickets = List.of(
                HelpDeskTicket.builder()
                        .id(1L)
                        .username("bob")
                        .issue("Access request")
                        .status("OPEN")
                        .createdAt(LocalDateTime.now())
                        .eta(LocalDateTime.now().plusDays(7))
                        .build()
        );

        when(service.getTicketsByUsername("bob")).thenReturn(tickets);

        List<HelpDeskTicket> result = helpDeskTools.getTicketStatus(context);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("bob");
        verify(service).getTicketsByUsername("bob");
    }
}
