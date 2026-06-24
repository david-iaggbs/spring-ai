package com.eazybytes.springai;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.qdrant.QdrantContainer;
import org.testcontainers.utility.DockerImageName;

@Tag("e2e")
@Testcontainers
@SpringBootTest
@TestPropertySource(properties = {
		"spring.docker.compose.enabled=false",
		"spring.ai.ollama.chat.options.model=" + SpringAiApplicationTests.CHAT_MODEL,
		"spring.ai.ollama.embedding.options.model=" + SpringAiApplicationTests.EMBED_MODEL,
		"spring.ai.ollama.init.pull-model-strategy=when_missing",
		"spring.datasource.url=jdbc:h2:mem:chatmemory-smoke;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
class SpringAiApplicationTests {

	static final String CHAT_MODEL = "llama3.2:1b";
	static final String EMBED_MODEL = "nomic-embed-text";

	@Container
	@ServiceConnection
	static OllamaContainer ollama =
			new OllamaContainer(DockerImageName.parse("ollama/ollama:latest"));

	@Container
	@ServiceConnection
	static QdrantContainer qdrant =
			new QdrantContainer(DockerImageName.parse("qdrant/qdrant:latest"));

	@Test
	void contextLoads() {
	}

}
