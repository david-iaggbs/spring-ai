package com.eazybytes.springai.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("e2e")
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
@TestPropertySource(properties = {
		"spring.ai.ollama.chat.options.model=" + StreamControllerOllamaIT.MODEL,
		"spring.ai.ollama.init.pull-model-strategy=never"
})
class StreamControllerOllamaIT {

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
	void real_model_streams_chunks_until_completion() throws Exception {
		MvcResult result = mvc.perform(get("/api/stream")
						.param("message", "Briefly: what's the parental leave policy?"))
				.andExpect(request().asyncStarted())
				.andReturn();

		mvc.perform(asyncDispatch(result))
				.andExpect(status().isOk())
				.andExpect(content().string(not(emptyOrNullString())));
	}

}
