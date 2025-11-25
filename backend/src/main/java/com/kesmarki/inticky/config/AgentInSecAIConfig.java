package com.kesmarki.inticky.config;

import com.agentinsec.ai.AIChat;
import com.agentinsec.ai.AIChatBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * AgentInSec-AI konfigurációs osztály
 * 
 * Ez az osztály felelős az AgentInSec-AI library inicializálásáért és konfigurálásáért.
 * Az AgentInSec-AI egy modern AI chat library Router Pattern architektúrával,
 * amely kétfázisú AI pipeline-t használ (Router AI + Agent AI).
 */
@Configuration
public class AgentInSecAIConfig {

    @Value("${agentinsec.api-key}")
    private String apiKey;

    @Value("${agentinsec.provider:openai}")
    private String provider;

    @Value("${agentinsec.azure.endpoint:}")
    private String azureEndpoint;

    @Value("${agentinsec.azure.router-deployment:gpt-router}")
    private String routerDeployment;

    @Value("${agentinsec.azure.agent-deployment:gpt-agent}")
    private String agentDeployment;

    @Value("${agentinsec.azure.embedding-deployment:gpt-embedding}")
    private String embeddingDeployment;

    /**
     * AIChat Bean létrehozása
     * Factory metódusokat használ az egyszerű inicializáláshoz
     */
    @Bean
    @Profile("!test")
    public AIChat aiChat() {
        if ("azure".equalsIgnoreCase(provider) && !azureEndpoint.isEmpty()) {
            // Azure OpenAI használata külön deployment nevekkel
            return AIChatBuilder.forAzure(
                azureEndpoint,
                apiKey,
                routerDeployment,
                agentDeployment,
                embeddingDeployment
            );
        } else {
            // OpenAI használata (alapértelmezett)
            return AIChatBuilder.forOpenAI(apiKey);
        }
    }

    /**
     * Test környezethez mock AIChat
     */
    @Bean
    @Profile("test")
    public AIChat testAIChat() {
        // Test környezetben mock implementáció
        return AIChatBuilder.forOpenAI("test-key");
    }
}
