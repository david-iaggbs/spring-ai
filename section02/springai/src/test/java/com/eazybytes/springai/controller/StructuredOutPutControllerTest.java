package com.eazybytes.springai.controller;

import com.eazybytes.springai.model.CountryCities;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StructuredOutPutController.class)
class StructuredOutPutControllerTest {

	@Autowired
	MockMvc mvc;

	@MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
	ChatClient chatClient;

	@Test
	void chat_bean_returns_typed_country_cities() throws Exception {
		when(chatClient
				.prompt()
				.system(StructuredOutPutController.NEUTRAL_SYSTEM_PROMPT)
				.user("France please")
				.call()
				.entity(CountryCities.class))
				.thenReturn(new CountryCities("France", List.of("Paris", "Lyon", "Marseille")));

		mvc.perform(get("/api/chat-bean").param("message", "France please"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.country").value("France"))
				.andExpect(jsonPath("$.cities[0]").value("Paris"))
				.andExpect(jsonPath("$.cities[2]").value("Marseille"));
	}

	@Test
	void chat_list_returns_plain_string_list_from_list_converter() throws Exception {
		when(chatClient
				.prompt()
				.system(StructuredOutPutController.NEUTRAL_SYSTEM_PROMPT)
				.user("colours of the rainbow")
				.call()
				.entity(StructuredOutPutController.LIST_CONVERTER))
				.thenReturn(List.of("red", "orange", "yellow"));

		mvc.perform(get("/api/chat-list").param("message", "colours of the rainbow"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0]").value("red"))
				.andExpect(jsonPath("$[2]").value("yellow"));
	}

	@Test
	void chat_map_returns_generic_map_from_map_converter() throws Exception {
		when(chatClient
				.prompt()
				.system(StructuredOutPutController.NEUTRAL_SYSTEM_PROMPT)
				.user("describe France")
				.call()
				.entity(StructuredOutPutController.MAP_CONVERTER))
				.thenReturn(Map.of("country", "France", "capital", "Paris"));

		mvc.perform(get("/api/chat-map").param("message", "describe France"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.country").value("France"))
				.andExpect(jsonPath("$.capital").value("Paris"));
	}

	@Test
	void chat_bean_list_returns_parameterized_list_of_pojos() throws Exception {
		when(chatClient
				.prompt()
				.system(StructuredOutPutController.NEUTRAL_SYSTEM_PROMPT)
				.user("two countries")
				.call()
				.entity(StructuredOutPutController.COUNTRY_CITIES_LIST_TYPE))
				.thenReturn(List.of(
						new CountryCities("France", List.of("Paris")),
						new CountryCities("Spain", List.of("Madrid"))));

		mvc.perform(get("/api/chat-bean-list").param("message", "two countries"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].country").value("France"))
				.andExpect(jsonPath("$[1].country").value("Spain"));
	}

}
