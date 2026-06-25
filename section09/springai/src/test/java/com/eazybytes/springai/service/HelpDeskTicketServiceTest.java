package com.eazybytes.springai.service;

import com.eazybytes.springai.entity.HelpDeskTicket;
import com.eazybytes.springai.model.TicketRequest;
import com.eazybytes.springai.repository.HelpDeskTicketRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class HelpDeskTicketServiceTest {

    @Mock
    private HelpDeskTicketRepository helpDeskTicketRepository;

    @InjectMocks
    private HelpDeskTicketService helpDeskTicketService;

    @Test
    void createTicketShouldPersistOpenTicketWithUserAndIssue() {
        TicketRequest request = new TicketRequest("Laptop is overheating");

        when(helpDeskTicketRepository.save(any(HelpDeskTicket.class)))
                .thenAnswer(invocation -> {
                    HelpDeskTicket ticket = invocation.getArgument(0);
                    ticket.setId(101L);
                    return ticket;
                });

        HelpDeskTicket result = helpDeskTicketService.createTicket(request, "alice");

        ArgumentCaptor<HelpDeskTicket> captor = ArgumentCaptor.forClass(HelpDeskTicket.class);
        verify(helpDeskTicketRepository).save(captor.capture());

        HelpDeskTicket saved = captor.getValue();
        assertThat(saved.getIssue()).isEqualTo("Laptop is overheating");
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getStatus()).isEqualTo("OPEN");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getEta()).isNotNull();
        assertThat(saved.getEta()).isAfter(saved.getCreatedAt());
        assertThat(saved.getEta()).isBeforeOrEqualTo(LocalDateTime.now().plusDays(8));

        assertThat(result.getId()).isEqualTo(101L);
    }

    @Test
    void getTicketsByUsernameShouldDelegateToRepository() {
        HelpDeskTicket ticket = HelpDeskTicket.builder()
                .id(11L)
                .username("bob")
                .issue("VPN not working")
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .eta(LocalDateTime.now().plusDays(7))
                .build();

        when(helpDeskTicketRepository.findByUsername("bob")).thenReturn(List.of(ticket));

        List<HelpDeskTicket> result = helpDeskTicketService.getTicketsByUsername("bob");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(11L);
        verify(helpDeskTicketRepository).findByUsername("bob");
    }
}
