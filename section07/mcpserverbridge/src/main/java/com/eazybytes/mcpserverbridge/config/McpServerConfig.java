package com.eazybytes.mcpserverbridge.config;

import com.eazybytes.mcpserverbridge.tool.HelpDeskBridgeTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class McpServerConfig {

    @Bean
    List<ToolCallback> toolCallbacks(HelpDeskBridgeTools helpDeskBridgeTools) {
        return List.of(ToolCallbacks.from(helpDeskBridgeTools));
    }
}
