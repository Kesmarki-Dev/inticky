package com.kesmarki.inticky.ai.service;

import com.agentinsec.ai.AIChat;
import com.kesmarki.inticky.ai.config.AgentInSecAIConfig;
import com.kesmarki.inticky.ai.entity.AiSession;
import com.kesmarki.inticky.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for AI chat operations with multi-tenant support
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiChatService {

    private final AgentInSecAIConfig aiConfig;
    private final AiSessionService sessionService;
    private final TicketAnalysisService ticketAnalysisService;

    /**
     * Send message to AI chat in session context
     */
    @Transactional
    public String chatInSession(UUID sessionId, UUID userId, String message) {
        String tenantId = TenantContext.getTenantId();
        log.info("Processing AI chat message in session: {} for user: {} in tenant: {}", sessionId, userId, tenantId);

        // Validate session
        AiSession session = sessionService.getSessionById(sessionId);
        if (!session.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Session does not belong to user");
        }

        if (!session.isActiveAndValid()) {
            throw new IllegalStateException("Session is not active or has expired");
        }

        try {
            // Get tenant-specific AI instance
            AIChat aiChat = aiConfig.getTenantAIChat(tenantId);

            // Update session activity
            sessionService.updateSessionActivity(sessionId);
            sessionService.incrementMessageCount(sessionId);

            // Process message with session context
            String sessionKey = generateSessionKey(sessionId, userId, tenantId);
            String response = aiChat.chatInSession(sessionKey, userId.toString(), message);

            log.info("AI chat response generated successfully for session: {} in tenant: {}", sessionId, tenantId);

            return response;

        } catch (Exception e) {
            log.error("Error processing AI chat message in session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to process AI chat message", e);
        }
    }

    /**
     * Send message to AI chat without session (stateless)
     */
    public String chat(UUID userId, String message) {
        String tenantId = TenantContext.getTenantId();
        log.info("Processing stateless AI chat message for user: {} in tenant: {}", userId, tenantId);

        try {
            // Get tenant-specific AI instance
            AIChat aiChat = aiConfig.getTenantAIChat(tenantId);

            // Process message without session
            String response = aiChat.chat(message);

            log.info("Stateless AI chat response generated successfully for user: {} in tenant: {}", userId, tenantId);

            return response;

        } catch (Exception e) {
            log.error("Error processing stateless AI chat message for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to process AI chat message", e);
        }
    }

    /**
     * Analyze ticket content using AI
     */
    public Map<String, Object> analyzeTicket(UUID ticketId, String title, String description) {
        String tenantId = TenantContext.getTenantId();
        log.info("Analyzing ticket: {} in tenant: {}", ticketId, tenantId);

        return ticketAnalysisService.analyzeTicket(ticketId, title, description);
    }

    /**
     * Suggest ticket category using AI
     */
    public String suggestTicketCategory(String title, String description) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Suggesting category for ticket in tenant: {}", tenantId);

        return ticketAnalysisService.suggestCategory(title, description);
    }

    /**
     * Suggest ticket priority using AI
     */
    public String suggestTicketPriority(String title, String description) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Suggesting priority for ticket in tenant: {}", tenantId);

        return ticketAnalysisService.suggestPriority(title, description);
    }

    /**
     * Suggest ticket assignee using AI
     */
    public Map<String, Object> suggestTicketAssignee(String title, String description, String category) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Suggesting assignee for ticket in tenant: {}", tenantId);

        return ticketAnalysisService.suggestAssignee(title, description, category);
    }

    /**
     * Generate ticket summary using AI
     */
    public String generateTicketSummary(UUID ticketId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Generating summary for ticket: {} in tenant: {}", ticketId, tenantId);

        return ticketAnalysisService.generateSummary(ticketId);
    }

    /**
     * Analyze sentiment of ticket content
     */
    public Map<String, Object> analyzeSentiment(String content) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Analyzing sentiment for content in tenant: {}", tenantId);

        return ticketAnalysisService.analyzeSentiment(content);
    }

    /**
     * Extract keywords from ticket content
     */
    public String[] extractKeywords(String content) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Extracting keywords from content in tenant: {}", tenantId);

        return ticketAnalysisService.extractKeywords(content);
    }

    /**
     * Generate response suggestion for ticket
     */
    public String generateResponseSuggestion(UUID ticketId, String context) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Generating response suggestion for ticket: {} in tenant: {}", ticketId, tenantId);

        return ticketAnalysisService.generateResponseSuggestion(ticketId, context);
    }

    /**
     * Process AI chat asynchronously
     */
    public CompletableFuture<String> chatAsync(UUID sessionId, UUID userId, String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return chatInSession(sessionId, userId, message);
            } catch (Exception e) {
                log.error("Async chat processing failed for session {}: {}", sessionId, e.getMessage(), e);
                throw new RuntimeException("Async chat processing failed", e);
            }
        });
    }

    /**
     * Get AI capabilities for tenant
     */
    @Cacheable(value = "ai-capabilities", key = "T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public Map<String, Object> getAICapabilities() {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching AI capabilities for tenant: {}", tenantId);

        return Map.of(
                "chatSupport", true,
                "ticketAnalysis", true,
                "sentimentAnalysis", true,
                "keywordExtraction", true,
                "categoryPrediction", true,
                "priorityPrediction", true,
                "assigneeSuggestion", true,
                "responseSuggestion", true,
                "summaryGeneration", true,
                "multiLanguageSupport", true,
                "sessionManagement", true,
                "contextAwareness", true,
                "functionCalling", true,
                "tenantId", tenantId,
                "maxMessageLength", 4000,
                "maxSessionDuration", "60 minutes",
                "maxSessionsPerUser", 5
        );
    }

    /**
     * Get AI usage statistics for tenant
     */
    public Map<String, Object> getUsageStatistics() {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching AI usage statistics for tenant: {}", tenantId);

        // Get session statistics
        Map<String, Object> sessionStats = sessionService.getSessionStatistics();

        // TODO: Add more detailed usage statistics
        // - Total messages processed
        // - Average response time
        // - Most used features
        // - Error rates
        // - Token usage (if available)

        return Map.of(
                "sessionStatistics", sessionStats,
                "totalRequests", "N/A", // TODO: Implement request counting
                "averageResponseTime", "N/A", // TODO: Implement response time tracking
                "errorRate", "N/A", // TODO: Implement error rate calculation
                "mostUsedFeatures", "N/A", // TODO: Implement feature usage tracking
                "tenantId", tenantId,
                "generatedAt", java.time.LocalDateTime.now()
        );
    }

    /**
     * Test AI connectivity for tenant
     */
    public Map<String, Object> testAIConnection() {
        String tenantId = TenantContext.getTenantId();
        log.debug("Testing AI connection for tenant: {}", tenantId);

        try {
            AIChat aiChat = aiConfig.getTenantAIChat(tenantId);
            
            // Send a simple test message
            long startTime = System.currentTimeMillis();
            String response = aiChat.chat("Hello, this is a connection test. Please respond with 'Connection successful'.");
            long responseTime = System.currentTimeMillis() - startTime;

            boolean isSuccessful = response != null && response.toLowerCase().contains("connection");

            return Map.of(
                    "status", isSuccessful ? "SUCCESS" : "FAILED",
                    "responseTime", responseTime + "ms",
                    "response", response != null ? response.substring(0, Math.min(response.length(), 100)) : "No response",
                    "tenantId", tenantId,
                    "timestamp", java.time.LocalDateTime.now()
            );

        } catch (Exception e) {
            log.error("AI connection test failed for tenant {}: {}", tenantId, e.getMessage(), e);
            
            return Map.of(
                    "status", "ERROR",
                    "error", e.getMessage(),
                    "tenantId", tenantId,
                    "timestamp", java.time.LocalDateTime.now()
            );
        }
    }

    /**
     * Generate session key for AgentInSec-AI
     */
    private String generateSessionKey(UUID sessionId, UUID userId, String tenantId) {
        return String.format("%s:%s:%s", tenantId, userId, sessionId);
    }

    /**
     * Clear tenant AI cache (for admin operations)
     */
    public void clearTenantAICache() {
        String tenantId = TenantContext.getTenantId();
        log.info("Clearing AI cache for tenant: {}", tenantId);
        
        aiConfig.clearTenantAIChat(tenantId);
    }
}
