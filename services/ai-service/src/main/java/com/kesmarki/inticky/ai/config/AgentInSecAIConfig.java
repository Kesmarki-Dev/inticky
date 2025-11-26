package com.kesmarki.inticky.ai.config;

import com.agentinsec.ai.AIChat;
import com.agentinsec.ai.AIChatBuilder;
import com.agentinsec.context.InfoBlock;
import com.agentinsec.registry.ToolBuilder;
import com.agentinsec.security.impl.DefaultPermissionChecker;
import com.agentinsec.security.impl.DefaultConfirmationProvider;
import com.agentinsec.vector.VectorStore;
import com.agentinsec.vector.impl.InMemoryVectorStore;
import com.agentinsec.vector.impl.QdrantVectorStore;
import com.agentinsec.vector.EmbeddingProvider;
import com.agentinsec.vector.impl.OpenAIEmbeddingProvider;
import com.agentinsec.vector.impl.AzureEmbeddingProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Configuration for AgentInSec-AI with multi-tenant support
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AgentInSecAIConfig {

    @Value("${agentinsec.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${agentinsec.ai.azure.enabled:false}")
    private boolean azureEnabled;

    @Value("${agentinsec.ai.azure.endpoint:}")
    private String azureEndpoint;

    @Value("${agentinsec.ai.azure.api-key:}")
    private String azureApiKey;

    @Value("${agentinsec.ai.azure.router-deployment:gpt-router}")
    private String azureRouterDeployment;

    @Value("${agentinsec.ai.azure.agent-deployment:gpt-agent}")
    private String azureAgentDeployment;

    @Value("${agentinsec.ai.azure.embedding-deployment:gpt-embedding}")
    private String azureEmbeddingDeployment;

    @Value("${agentinsec.ai.vector-store.type:in-memory}")
    private String vectorStoreType;

    @Value("${agentinsec.ai.vector-store.qdrant.host:localhost}")
    private String qdrantHost;

    @Value("${agentinsec.ai.vector-store.qdrant.port:6333}")
    private int qdrantPort;

    @Value("${agentinsec.ai.vector-store.qdrant.collection-name:inticky_vectors}")
    private String qdrantCollectionName;

    // Cache for tenant-specific AI instances
    private final Map<String, AIChat> tenantAIInstances = new ConcurrentHashMap<>();

    /**
     * Create default AIChat instance (for system-level operations)
     */
    @Bean
    @Primary
    public AIChat defaultAIChat() {
        log.info("Creating default AIChat instance");
        
        try {
            AIChat aiChat = createAIChatInstance("system");
            initializeSystemInfoBlocks(aiChat);
            registerSystemFunctions(aiChat);
            
            log.info("Default AIChat instance created successfully");
            return aiChat;
        } catch (Exception e) {
            log.error("Failed to create default AIChat instance: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize AgentInSec-AI", e);
        }
    }

    /**
     * Get or create tenant-specific AIChat instance
     */
    public AIChat getTenantAIChat(String tenantId) {
        return tenantAIInstances.computeIfAbsent(tenantId, this::createTenantAIChat);
    }

    /**
     * Create tenant-specific AIChat instance
     */
    private AIChat createTenantAIChat(String tenantId) {
        log.info("Creating AIChat instance for tenant: {}", tenantId);
        
        try {
            AIChat aiChat = createAIChatInstance(tenantId);
            initializeTenantInfoBlocks(aiChat, tenantId);
            registerTenantFunctions(aiChat, tenantId);
            
            log.info("AIChat instance created successfully for tenant: {}", tenantId);
            return aiChat;
        } catch (Exception e) {
            log.error("Failed to create AIChat instance for tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Failed to initialize tenant AI chat", e);
        }
    }

    /**
     * Create base AIChat instance
     */
    private AIChat createAIChatInstance(String instanceId) {
        // Create embedding provider
        EmbeddingProvider embeddingProvider = createEmbeddingProvider();
        
        // Create vector store
        VectorStore vectorStore = createVectorStore(embeddingProvider, instanceId);
        
        // Create permission checker
        DefaultPermissionChecker permissionChecker = new DefaultPermissionChecker();
        
        // Create confirmation provider
        DefaultConfirmationProvider confirmationProvider = new DefaultConfirmationProvider(
                (userId, action, reason) -> {
                    log.warn("Dangerous operation requested by user {}: {} - {}", userId, action, reason);
                    // In production, implement proper confirmation mechanism
                    return true; // For now, allow all operations
                }
        );

        // Build AIChat instance
        if (azureEnabled && azureEndpoint != null && !azureEndpoint.trim().isEmpty()) {
            log.info("Creating Azure-based AIChat instance for: {}", instanceId);
            return AIChatBuilder.forAzure(
                    azureEndpoint,
                    azureApiKey,
                    azureRouterDeployment,
                    azureAgentDeployment,
                    azureEmbeddingDeployment
            )
            .withVectorStore(vectorStore)
            .withPermissionChecker(permissionChecker)
            .withConfirmationProvider(confirmationProvider)
            .build();
        } else {
            log.info("Creating OpenAI-based AIChat instance for: {}", instanceId);
            return AIChatBuilder.forOpenAI(openAiApiKey)
                    .withVectorStore(vectorStore)
                    .withPermissionChecker(permissionChecker)
                    .withConfirmationProvider(confirmationProvider)
                    .build();
        }
    }

    /**
     * Create embedding provider
     */
    private EmbeddingProvider createEmbeddingProvider() {
        if (azureEnabled && azureEndpoint != null && !azureEndpoint.trim().isEmpty()) {
            return new AzureEmbeddingProvider(azureEndpoint, azureApiKey, azureEmbeddingDeployment);
        } else {
            return new OpenAIEmbeddingProvider(openAiApiKey);
        }
    }

    /**
     * Create vector store
     */
    private VectorStore createVectorStore(EmbeddingProvider embeddingProvider, String instanceId) {
        if ("qdrant".equalsIgnoreCase(vectorStoreType)) {
            log.info("Creating Qdrant vector store for instance: {}", instanceId);
            String collectionName = qdrantCollectionName + "_" + instanceId;
            return new QdrantVectorStore(
                    embeddingProvider,
                    "http://" + qdrantHost + ":" + qdrantPort,
                    collectionName
            );
        } else {
            log.info("Creating in-memory vector store for instance: {}", instanceId);
            return new InMemoryVectorStore(embeddingProvider);
        }
    }

    /**
     * Initialize system-level info blocks
     */
    private void initializeSystemInfoBlocks(AIChat aiChat) {
        log.debug("Initializing system info blocks");

        // System overview
        aiChat.addInfoBlock(InfoBlock.builder()
                .id("system_overview")
                .title("Inticky Ticketing System Overview")
                .content("""
                        Inticky is a modern, AI-powered ticketing system built with microservices architecture.
                        
                        Key Features:
                        - Multi-tenant support for multiple organizations
                        - AI-powered ticket analysis and suggestions
                        - Comprehensive workflow management
                        - Real-time notifications and alerts
                        - Advanced reporting and analytics
                        - RESTful API with comprehensive documentation
                        
                        Architecture:
                        - API Gateway: Central routing and security
                        - User Service: Authentication and user management
                        - Ticket Service: Core ticket operations
                        - AI Service: AI-powered features and chat
                        - Notification Service: Email and push notifications
                        - Tenant Service: Multi-tenant management
                        """)
                .category("system")
                .tag("overview", "architecture")
                .shouldChunk(true)
                .build());

        // AI capabilities
        aiChat.addInfoBlock(InfoBlock.builder()
                .id("ai_capabilities")
                .title("AI Assistant Capabilities")
                .content("""
                        As an AI assistant for the Inticky ticketing system, I can help you with:
                        
                        Ticket Management:
                        - Create, update, and manage tickets
                        - Analyze ticket content and suggest categorization
                        - Recommend priority levels based on content
                        - Suggest appropriate assignees
                        - Track SLA compliance and deadlines
                        
                        Data Analysis:
                        - Generate reports and statistics
                        - Identify trends and patterns
                        - Provide insights on team performance
                        - Analyze customer satisfaction metrics
                        
                        Workflow Assistance:
                        - Guide through ticket resolution processes
                        - Suggest next steps and actions
                        - Automate routine tasks
                        - Provide best practice recommendations
                        
                        Communication:
                        - Draft responses to customers
                        - Summarize ticket conversations
                        - Translate content between languages
                        - Format and structure communications
                        """)
                .category("ai")
                .tag("capabilities", "features")
                .shouldChunk(true)
                .build());
    }

    /**
     * Initialize tenant-specific info blocks
     */
    private void initializeTenantInfoBlocks(AIChat aiChat, String tenantId) {
        log.debug("Initializing info blocks for tenant: {}", tenantId);

        // Tenant context
        aiChat.addInfoBlock(InfoBlock.builder()
                .id("tenant_context")
                .title("Tenant Context")
                .content(String.format("""
                        Current tenant context: %s
                        
                        All operations are performed within this tenant's scope.
                        Data isolation is enforced at all levels.
                        
                        Tenant-specific features:
                        - Custom workflows and business rules
                        - Personalized dashboards and reports
                        - Tenant-specific user roles and permissions
                        - Custom fields and ticket categories
                        - Branded notifications and communications
                        """, tenantId))
                .category("tenant")
                .tag("context", "isolation")
                .build());

        // TODO: Load tenant-specific configuration and info blocks from database
        // This would include:
        // - Custom workflow rules
        // - Business-specific terminology
        // - Integration configurations
        // - Custom field definitions
        // - SLA policies
    }

    /**
     * Register system-level functions
     */
    private void registerSystemFunctions(AIChat aiChat) {
        log.debug("Registering system functions");

        // System health check
        aiChat.registerTool(
                ToolBuilder.create("check_system_health")
                        .description("Check the health status of all system services")
                        .category("system")
                        .requiredPermission("admin")
                        .execute(params -> {
                            // TODO: Implement actual health check
                            return Map.of(
                                    "status", "healthy",
                                    "services", Map.of(
                                            "api-gateway", "UP",
                                            "user-service", "UP",
                                            "ticket-service", "UP",
                                            "ai-service", "UP"
                                    ),
                                    "timestamp", System.currentTimeMillis()
                            );
                        })
        );
    }

    /**
     * Register tenant-specific functions
     */
    private void registerTenantFunctions(AIChat aiChat, String tenantId) {
        log.debug("Registering tenant functions for: {}", tenantId);

        // Get tenant statistics
        aiChat.registerTool(
                ToolBuilder.create("get_tenant_statistics")
                        .description("Get comprehensive statistics for the current tenant")
                        .category("analytics")
                        .requiredPermission("user")
                        .execute(params -> {
                            // TODO: Implement actual statistics gathering
                            return Map.of(
                                    "tenantId", tenantId,
                                    "totalTickets", 0,
                                    "activeTickets", 0,
                                    "resolvedTickets", 0,
                                    "averageResolutionTime", "N/A",
                                    "customerSatisfaction", "N/A",
                                    "timestamp", System.currentTimeMillis()
                            );
                        })
        );

        // TODO: Register more tenant-specific functions:
        // - create_ticket
        // - update_ticket
        // - search_tickets
        // - assign_ticket
        // - analyze_ticket_sentiment
        // - suggest_ticket_priority
        // - generate_ticket_report
        // - etc.
    }

    /**
     * Clear tenant AI instance (for cleanup)
     */
    public void clearTenantAIChat(String tenantId) {
        log.info("Clearing AIChat instance for tenant: {}", tenantId);
        tenantAIInstances.remove(tenantId);
    }

    /**
     * Get all active tenant instances
     */
    public Map<String, AIChat> getAllTenantInstances() {
        return Map.copyOf(tenantAIInstances);
    }
}
