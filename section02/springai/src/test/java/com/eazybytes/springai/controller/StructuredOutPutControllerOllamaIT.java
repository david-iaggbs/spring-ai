package com.eazybytes.springai.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2E coverage for structured-output deserialization end-to-end. A successful
 * {@code 200 OK} is sufficient evidence the wiring works: if Spring AI's
 * converter could not deserialize the model's reply into the requested Java
 * type, the controller would have propagated an exception and the response
 * would be {@code 500}. We intentionally do not assert on response content —
 * a 1 B-parameter model is not reliable enough to ground field-level
 * assertions; the lesson here is the conversion plumbing, not LLM accuracy.
 */
@Tag("e2e")
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
@TestPropertySource(properties = {
		"spring.ai.ollama.chat.options.model=" + StructuredOutPutControllerOllamaIT.MODEL,
		"spring.ai.ollama.init.pull-model-strategy=never"
})
class StructuredOutPutControllerOllamaIT {

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
	void chat_bean_deserializes_real_model_response_into_country_cities() throws Exception {
		mvc.perform(get("/api/chat-bean")
						.param("message", "Tell me the country France and three of its largest cities."))
				.andExpect(status().isOk());
	}

	@Test
	void chat_list_deserializes_real_model_response_into_string_list() throws Exception {
		mvc.perform(get("/api/chat-list")
						.param("message", "List three primary colours."))
				.andExpect(status().isOk());
	}

	@Test
	void chat_map_deserializes_real_model_response_into_generic_map() throws Exception {
		mvc.perform(get("/api/chat-map")
						.param("message", "Give me an object with the keys 'country' and 'capital' for France."))
				.andExpect(status().isOk());
	}

	/**
	 * Disabled against {@code llama3.2:1b}: a 1 B-parameter model does not
	 * reliably emit a top-level JSON array of objects, so this endpoint
	 * routinely returns a single object that {@code ParameterizedTypeReference<List<...>>}
	 * cannot deserialize. The {@code chat-bean-list} wiring is fully covered by
	 * {@code StructuredOutPutControllerTest} (unit) and
	 * {@code StructuredOutPutControllerIntegrationTest} (integration) — re-enable
	 * this e2e by pointing {@code spring.ai.ollama.chat.options.model} at a
	 * larger model (e.g. {@code llama3.1:8b}).
	 */
	@Test
	@Disabled("llama3.2:1b too small to consistently return a top-level JSON array of objects")
	void chat_bean_list_deserializes_real_model_response_into_list_of_country_cities() throws Exception {
		mvc.perform(get("/api/chat-bean-list")
						.param("message",
								"Return a JSON ARRAY (not an object) with TWO entries. " +
								"Each entry must have the fields 'country' and 'cities'. " +
								"Use France with two cities, and Spain with two cities."))
				.andExpect(status().isOk());
	}

}
