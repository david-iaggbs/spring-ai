package com.eazybytes.springai.controller;

import com.eazybytes.springai.model.CountryCities;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StructuredOutPutController {

	static final String NEUTRAL_SYSTEM_PROMPT = """
			You are a helpful assistant. Always respond exactly in the
			structured format described in the user's request.
			""";

	static final ListOutputConverter LIST_CONVERTER = new ListOutputConverter();

	static final MapOutputConverter MAP_CONVERTER = new MapOutputConverter();

	static final ParameterizedTypeReference<List<CountryCities>> COUNTRY_CITIES_LIST_TYPE =
			new ParameterizedTypeReference<>() { };

	private final ChatClient chatClient;

	public StructuredOutPutController(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@GetMapping("/chat-bean")
	public ResponseEntity<CountryCities> chatBean(@RequestParam("message") String message) {
		CountryCities countryCities = chatClient
				.prompt()
				.system(NEUTRAL_SYSTEM_PROMPT)
				.user(message)
				.call()
				.entity(CountryCities.class);
		return ResponseEntity.ok(countryCities);
	}

	@GetMapping("/chat-list")
	public ResponseEntity<List<String>> chatList(@RequestParam("message") String message) {
		List<String> values = chatClient
				.prompt()
				.system(NEUTRAL_SYSTEM_PROMPT)
				.user(message)
				.call()
				.entity(LIST_CONVERTER);
		return ResponseEntity.ok(values);
	}

	@GetMapping("/chat-map")
	public ResponseEntity<Map<String, Object>> chatMap(@RequestParam("message") String message) {
		Map<String, Object> values = chatClient
				.prompt()
				.system(NEUTRAL_SYSTEM_PROMPT)
				.user(message)
				.call()
				.entity(MAP_CONVERTER);
		return ResponseEntity.ok(values);
	}

	@GetMapping("/chat-bean-list")
	public ResponseEntity<List<CountryCities>> chatBeanList(@RequestParam("message") String message) {
		List<CountryCities> values = chatClient
				.prompt()
				.system(NEUTRAL_SYSTEM_PROMPT)
				.user(message)
				.call()
				.entity(COUNTRY_CITIES_LIST_TYPE);
		return ResponseEntity.ok(values);
	}

}
