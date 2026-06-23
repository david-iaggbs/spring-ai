package com.eazybytes.springai;

import com.eazybytes.springai.controller.HelpDeskController;
import com.eazybytes.springai.controller.TimeController;
import com.eazybytes.springai.tools.HelpDeskTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({HelpDeskController.class, TimeController.class})
class ToolsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean(name = "helpDeskChatClient")
    ChatClient helpDeskChatClient;

    @MockitoBean(name = "timeChatClient")
    ChatClient timeChatClient;

    @MockitoBean
    HelpDeskTools helpDeskTools;

    @BeforeEach
    void stubHelpDeskChatClient() {
        ChatClient.ChatClientRequestSpec spec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);

        when(helpDeskChatClient.prompt()).thenReturn(spec);
        when(spec.advisors(any(Consumer.class))).thenReturn(spec);
        when(spec.user(anyString())).thenReturn(spec);
        when(spec.tools(any())).thenReturn(spec);
        when(spec.toolContext(any(Map.class))).thenReturn(spec);
        when(spec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("I can help you with that. Ticket #42 created successfully.");
    }

    @BeforeEach
    void stubTimeChatClient() {
        ChatClient.ChatClientRequestSpec spec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);

        when(timeChatClient.prompt()).thenReturn(spec);
        when(spec.advisors(any(Consumer.class))).thenReturn(spec);
        when(spec.user(anyString())).thenReturn(spec);
        when(spec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn("The current local time is 14:30.");
    }

    @Test
    void help_desk_chat_returns_response() throws Exception {
        mockMvc.perform(get("/api/tools/help-desk")
                        .header("username", "alice")
                        .param("message", "I need help with my laptop"))
                .andExpect(status().isOk())
                .andExpect(content().string("I can help you with that. Ticket #42 created successfully."));
    }

    @Test
    void help_desk_endpoint_requires_username_header() throws Exception {
        mockMvc.perform(get("/api/tools/help-desk")
                        .param("message", "I need help"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void time_endpoint_returns_response() throws Exception {
        mockMvc.perform(get("/api/tools/local-time")
                        .header("username", "bob")
                        .param("message", "What time is it?"))
                .andExpect(status().isOk())
                .andExpect(content().string("The current local time is 14:30."));
    }

    @Test
    void time_endpoint_requires_username_header() throws Exception {
        mockMvc.perform(get("/api/tools/local-time")
                        .param("message", "What time is it?"))
                .andExpect(status().isBadRequest());
    }
}
