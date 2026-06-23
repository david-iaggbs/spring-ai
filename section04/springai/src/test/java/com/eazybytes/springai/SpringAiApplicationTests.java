package com.eazybytes.springai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.ai.ollama.init.pull-model-strategy=never",
		"spring.ai.ollama.init.chat.include=false",
		"spring.datasource.url=jdbc:h2:mem:chatmemory-ctx;DB_CLOSE_DELAY=-1"
})
class SpringAiApplicationTests {

	@Test
	void contextLoads() {
	}

}
