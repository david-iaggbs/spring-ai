package com.eazybytes.mcpserverbridge.tool;

import com.eazybytes.mcpserverbridge.entity.HelpDeskTicket;
import com.eazybytes.mcpserverbridge.model.TicketRequest;
import com.eazybytes.mcpserverbridge.service.HelpDeskTicketService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HelpDeskBridgeTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelpDeskBridgeTools.class);

    private final HelpDeskTicketService service;

    @Tool(name = "createTicket", description = "Create a helpdesk support ticket")
    String createTicket(@ToolParam(description = "Ticket request containing issue and username")
    TicketRequest ticketRequest) {
        LOGGER.info("Creating support ticket with request: {}", ticketRequest);
        HelpDeskTicket savedTicket = service.createTicket(ticketRequest);
        LOGGER.info("Ticket created successfully. Ticket ID: {}, Username: {}", savedTicket.getId(), savedTicket.getUsername());
        return "Ticket #" + savedTicket.getId() + " created successfully for user " + savedTicket.getUsername();
    }

    @Tool(name = "getTicketStatus", description = "Fetch ticket status by username")
    List<HelpDeskTicket> getTicketStatus(@ToolParam(description =
            "Username used to retrieve helpdesk tickets") String username) {
        LOGGER.info("Fetching tickets for user: {}", username);
        List<HelpDeskTicket> tickets = service.getTicketsByUsername(username);
        LOGGER.info("Found {} tickets for user: {}", tickets.size(), username);
        return tickets;
    }

}
