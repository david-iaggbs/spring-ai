package com.eazybytes.springai.controller;

import com.eazybytes.springai.model.CountryCities;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"spring.ai.ollama.init.pull-model-strategy=never",
		"spring.ai.ollama.init.chat.include=false"
})
@AutoConfigureMockMvc
class StructuredOutPutControllerIntegrationTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
	ChatClient chatClient;

	@Test
	void chat_bean_calls_entity_with_country_cities_class() throws Exception {
		when(chatClient
				.prompt()
				.system(StructuredOutPutController.NEUTRAL_SYSTEM_PROMPT)
				.user("France please")
				.call()
				.entity(CountryCities.class))
				.thenReturn(new CountryCities("France", List.of("Paris")));

		mvc.perform(get("/api/chat-bean").param("message", "France please"))
				.andExpect(status().isOk());

		verify(chatClient.prompt().system(StructuredOutPutController.NEUTRAL_SYSTEM_PROMPT)
				.user("France please").call())
				.entity(CountryCities.class);
	}

	@Test
	void chat_list_uses_list_output_converter() throws Exception {
		when(chatClient
				.prompt()
				.system(StructuredOutPutController.NEUTRAL_SYSTEM_PROMPT)
				.user("rainbow")
				.call()
				.entity(StructuredOutPutController.LIST_CONVERTER))
				.thenReturn(List.of("a", "b"));

		mvc.perform(get("/api/chat-list").param("message", "rainbow"))
				.andExpect(status().isOk());

		verify(chatClient.prompt().system(StructuredOutPutController.NEUTRAL_SYSTEM_PROMPT)
				.user("rainbow").call())
				.entity(StructuredOutPutController.LIST_CONVERTER);
	}

	@Test
	void chat_map_uses_map_output_converter() throws Exception {
		when(chatClient
				.prompt()
				.system(StructuredOutPutController.NEUTRAL_SYSTEM_PROMPT)
				.user("describe")
				.call()
				.entity(StructuredOutPutController.MAP_CONVERTER))
				.thenReturn(Map.of("k", "v"));

		mvc.perform(get("/api/chat-map").param("message", "describe"))
				.andExpect(status().isOk());

		verify(chatClient.prompt().system(StructuredOutPutController.NEUTRAL_SYSTEM_PROMPT)
				.user("describe").call())
				.entity(StructuredOutPutController.MAP_CONVERTER);
	}

	@Test
	void chat_bean_list_uses_parameterized_type_reference() throws Exception {
		when(chatClient
				.prompt()
				.system(StructuredOutPutController.NEUTRAL_SYSTEM_PROMPT)
				.user("two countries")
				.call()
				.entity(StructuredOutPutController.COUNTRY_CITIES_LIST_TYPE))
				.thenReturn(List.of(new CountryCities("France", List.of("Paris"))));

		mvc.perform(get("/api/chat-bean-list").param("message", "two countries"))
				.andExpect(status().isOk());

		verify(chatClient.prompt().system(StructuredOutPutController.NEUTRAL_SYSTEM_PROMPT)
				.user("two countries").call())
				.entity(StructuredOutPutController.COUNTRY_CITIES_LIST_TYPE);
	}

}
