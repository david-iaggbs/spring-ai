package com.eazybytes.springai;

import com.eazybytes.springai.model.TicketRequest;
import com.eazybytes.springai.service.HelpDeskTicketService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.qdrant.QdrantContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.docker.compose.enabled=false",
                "spring.datasource.url=jdbc:h2:mem:chatmemory-tools-e2e;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
@Testcontainers
@Tag("e2e")
class ToolsControllerOllamaIT {

    @Container
    static OllamaContainer ollama = new OllamaContainer("ollama/ollama:latest");

    @Container
    static QdrantContainer qdrant = new QdrantContainer("qdrant/qdrant:latest");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.ai.ollama.base-url", ollama::getEndpoint);
        registry.add("spring.ai.vectorstore.qdrant.host", qdrant::getHost);
        registry.add("spring.ai.vectorstore.qdrant.port",
                () -> String.valueOf(qdrant.getMappedPort(6334)));
    }

    @BeforeAll
    static void pullModel() throws Exception {
        ollama.execInContainer("ollama", "pull", "llama3.2:1b");
    }

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    HelpDeskTicketService ticketService;

    @Test
    void time_tool_returns_time_for_local_time_query() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("username", "timeuser");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/tools/local-time?message=What is the current local time?",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();
    }

    @Test
    void help_desk_creates_ticket_when_issue_reported() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("username", "ticketuser");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/tools/help-desk?message=Please create a support ticket: I cannot access the VPN",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();
        assertThat(response.getBody()).containsIgnoringCase("ticket");
    }

    @Test
    void help_desk_retrieves_ticket_status() {
        ticketService.createTicket(new TicketRequest("Keyboard not working"), "statususer");

        HttpHeaders headers = new HttpHeaders();
        headers.set("username", "statususer");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/tools/help-desk?message=What are my open support tickets?",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();
    }

    @Test
    void conversation_history_is_persisted_in_chat_memory() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("username", "memoryuser");

        restTemplate.exchange(
                "/api/tools/local-time?message=My name is Alice and I have a printer issue",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        ResponseEntity<String> secondResponse = restTemplate.exchange(
                "/api/tools/local-time?message=What issue did I mention earlier?",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondResponse.getBody()).isNotBlank();
        assertThat(secondResponse.getBody()).containsIgnoringCase("printer");
    }
}
