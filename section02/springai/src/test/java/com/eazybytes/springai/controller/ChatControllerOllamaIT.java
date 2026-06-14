package com.eazybytes.springai.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("e2e")
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@TestPropertySource(properties = {
		"spring.ai.ollama.chat.options.model=" + ChatControllerOllamaIT.MODEL,
		"spring.ai.ollama.init.pull-model-strategy=never",
		"logging.level.org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor=DEBUG"
})
class ChatControllerOllamaIT {

	static final String MODEL = "llama3.2:1b";

	@Container
	@ServiceConnection
	static OllamaContainer ollama =
			new OllamaContainer(DockerImageName.parse("ollama/ollama:latest"));

	@BeforeAll
	static void pullModel() throws Exception {
		ollama.execInContainer("ollama", "pull", MODEL);
	}

	@Autowired
	MockMvc mvc;

	@Test
	void real_model_responds_and_both_advisors_log(CapturedOutput output) throws Exception {
		mvc.perform(get("/api/chat")
						.param("message", "How many vacation days do I get?"))
				.andExpect(status().isOk())
				.andExpect(content().string(not(emptyOrNullString())));

		assertThat(output.getOut())
				.as("SimpleLoggerAdvisor should emit a request log line when DEBUG is enabled")
				.contains("SimpleLoggerAdvisor")
				.containsPattern("(?i)request");

		assertThat(output.getOut())
				.as("TokenUsageAuditAdvisor should log Usage from the response metadata")
				.contains("TokenUsageAuditAdvisor")
				.contains("Token usage details");
	}

}
