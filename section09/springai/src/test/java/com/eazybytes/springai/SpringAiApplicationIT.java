package com.eazybytes.springai;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.ai.ollama.init.pull-model-strategy=when_missing",
		"spring.main.lazy-initialization=true"
})
@Tag("integration")
class SpringAiApplicationIT {

	@Test
	void contextLoads() {
	}

}
