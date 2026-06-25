package com.eazybytes.springai;

import com.eazybytes.springai.controller.ChatController;
import org.junit.jupiter.api.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
        "spring.ai.openai.api-key=${OPENAI_API_KEY:test-key}",
        "logging.level.org.springframework.ai=DEBUG"
})
class SpringaiApplicationTests {

    @Autowired
    private ChatController chatController;

    @Autowired
    private ChatModel chatModel;

    private ChatClient chatClient;

    @Value("classpath:/promptTemplates/hrPolicy.st")
    Resource hrPolicyTemplate;

    @BeforeEach
    void setup() {
        ChatClient.Builder chatClientBuilder =
                ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor());
        this.chatClient = chatClientBuilder.build();
    }

    @Test
    @DisplayName("Should return relevant response for basic geography question")
    @Timeout(value = 30)
    void evaluateChatControllerResponseRelevancy() {
        // Given
        String question = "What is the capital of India ?";

        // When
        String aiResponse = chatController.chat(question);
        String normalizedResponse = aiResponse.toLowerCase();

        Assertions.assertAll(() -> assertThat(aiResponse).isNotBlank(),
                () -> assertThat(normalizedResponse)
                        .withFailMessage("""
                                ========================================
                                The answer was not considered relevant.
                                Question: "%s"
                                Response: "%s"
                                ========================================
                                """, question, aiResponse)
                        .contains("new delhi"));

    }

    @Test
    @DisplayName("Should return factually correct response for gravity-related question")
    @Timeout(value = 30)
    void evaluateFactAccuracyForGravityQuestion() {
        // Given
        String question = "Who discovered the law of universal gravitation?";

        // When
        String aiResponse = chatController.chat(question);
        String normalizedResponse = aiResponse.toLowerCase();

        Assertions.assertAll(() -> assertThat(aiResponse).isNotBlank(),
                () -> assertThat(normalizedResponse)
                        .withFailMessage("""
                             ========================================
                             The answer was not considered factually correct.
                             Question: "%s"
                             Response: "%s"
                             ========================================
                                """, question, aiResponse)
                        .contains("newton"));

    }

    @Test
    @DisplayName("Should correctly evaluate factual response based on HR policy context (RAG scenario)")
    @Timeout(value = 30)
    public void evaluateHrPolicyAnswerWithRagContext() throws IOException {
        // Given
        String question = "How many paid leaves do employees get annually?";

        // When
        String aiResponse = chatController.promptStuffing(question);

        String retrievedContext = hrPolicyTemplate.getContentAsString(StandardCharsets.UTF_8);
        String normalizedResponse = aiResponse.toLowerCase();

        // Then
        Assertions.assertAll(
                () -> assertThat(aiResponse).isNotBlank(),
                () -> assertThat(normalizedResponse)
                        .withFailMessage("""
                        ========================================
                        The response was not considered factually accurate.
                        Question: %s
                        Response: %s
                        Context: %s
                        ========================================
                        """, question, aiResponse, retrievedContext)
                        .containsAnyOf("18", "eighteen", "paid leave", "paid leaves", "annual leave", "leave entitlements", "benefits", "hr"));
    }

}
