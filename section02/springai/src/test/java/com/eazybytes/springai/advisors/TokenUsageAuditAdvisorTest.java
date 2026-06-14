package com.eazybytes.springai.advisors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(OutputCaptureExtension.class)
class TokenUsageAuditAdvisorTest {

	private final TokenUsageAuditAdvisor advisor = new TokenUsageAuditAdvisor();

	@Test
	void returns_the_chain_response_unchanged() {
		ChatClientRequest request = mock(ChatClientRequest.class);
		ChatClientResponse chainResponse = mock(ChatClientResponse.class);
		CallAdvisorChain chain = mock(CallAdvisorChain.class);
		when(chain.nextCall(request)).thenReturn(chainResponse);

		ChatClientResponse result = advisor.adviseCall(request, chain);

		assertThat(result).isSameAs(chainResponse);
	}

	@Test
	void logs_token_usage_details_when_usage_metadata_is_present(CapturedOutput output) {
		ChatClientRequest request = mock(ChatClientRequest.class);
		CallAdvisorChain chain = mock(CallAdvisorChain.class);

		ChatResponse chatResponse = mock(ChatResponse.class);
		ChatResponseMetadata metadata = ChatResponseMetadata.builder()
				.usage(new DefaultUsage(11, 22, 33))
				.build();
		when(chatResponse.getMetadata()).thenReturn(metadata);

		ChatClientResponse chainResponse = mock(ChatClientResponse.class);
		when(chainResponse.chatResponse()).thenReturn(chatResponse);
		when(chain.nextCall(request)).thenReturn(chainResponse);

		advisor.adviseCall(request, chain);

		assertThat(output.getOut())
				.contains("Token usage details")
				.contains("TokenUsageAuditAdvisor");
	}

	@Test
	void does_not_log_when_chat_response_is_null(CapturedOutput output) {
		ChatClientRequest request = mock(ChatClientRequest.class);
		ChatClientResponse chainResponse = mock(ChatClientResponse.class);
		when(chainResponse.chatResponse()).thenReturn(null);
		CallAdvisorChain chain = mock(CallAdvisorChain.class);
		when(chain.nextCall(request)).thenReturn(chainResponse);

		advisor.adviseCall(request, chain);

		assertThat(output.getOut()).doesNotContain("Token usage details");
	}

	@Test
	void exposes_name_and_order() {
		assertThat(advisor.getName()).isEqualTo("TokenUsageAuditAdvisor");
		assertThat(advisor.getOrder()).isEqualTo(1);
	}

}
