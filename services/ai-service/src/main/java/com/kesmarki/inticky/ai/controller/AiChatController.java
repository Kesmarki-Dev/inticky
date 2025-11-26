package com.kesmarki.inticky.ai.controller;

import com.kesmarki.inticky.ai.dto.ChatRequest;
import com.kesmarki.inticky.ai.dto.ChatResponse;
import com.kesmarki.inticky.ai.entity.AiSession;
import com.kesmarki.inticky.ai.service.AiChatService;
import com.kesmarki.inticky.ai.service.AiSessionService;
import com.kesmarki.inticky.common.dto.ApiResponse;
import com.kesmarki.inticky.tenant.annotation.TenantAware;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for AI chat operations
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
@TenantAware
@Tag(name = "AI Chat", description = "AI-powered chat and conversation management")
public class AiChatController {

    private final AiChatService aiChatService;
    private final AiSessionService sessionService;

    /**
     * Send message to AI chat
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ai:chat')")
    @Operation(summary = "Send chat message", description = "Send a message to AI chat with optional session context")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request) {
        
        UUID currentUserId = getCurrentUserId();
        String currentUserName = getCurrentUserName();
        String currentUserEmail = getCurrentUserEmail();
        
        log.info("Processing AI chat request from user: {}", currentUserId);
        
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        
        try {
            String aiResponse;
            UUID sessionId = request.getSessionId();
            boolean sessionCreated = false;
            
            // Create session if requested and not provided
            if (request.getCreateSession() && sessionId == null) {
                AiSession.SessionType sessionType = request.getTicketId() != null ? 
                        AiSession.SessionType.TICKET_SPECIFIC : AiSession.SessionType.GENERAL;
                
                AiSession session = sessionService.createSession(
                        currentUserId, 
                        currentUserName, 
                        currentUserEmail, 
                        sessionType, 
                        request.getTicketId(),
                        request.getSessionTitle(),
                        request.getSessionDescription()
                );
                
                sessionId = session.getId();
                sessionCreated = true;
                
                log.info("Created new AI session: {} for user: {}", sessionId, currentUserId);
            }
            
            // Process chat message
            if (sessionId != null) {
                aiResponse = aiChatService.chatInSession(sessionId, currentUserId, request.getMessage());
            } else {
                aiResponse = aiChatService.chat(currentUserId, request.getMessage());
            }
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Get session info if available
            String sessionTitle = null;
            Integer messageCount = null;
            if (sessionId != null) {
                try {
                    AiSession session = sessionService.getSessionById(sessionId);
                    sessionTitle = session.getDisplayTitle();
                    messageCount = session.getMessageCount();
                } catch (Exception e) {
                    log.warn("Could not retrieve session info for {}: {}", sessionId, e.getMessage());
                }
            }
            
            ChatResponse response = ChatResponse.builder()
                    .response(aiResponse)
                    .sessionId(sessionId)
                    .sessionTitle(sessionTitle)
                    .sessionCreated(sessionCreated)
                    .messageCount(messageCount)
                    .timestamp(LocalDateTime.now())
                    .responseTimeMs(responseTime)
                    .requestId(requestId)
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(response, "AI chat response generated successfully"));
            
        } catch (Exception e) {
            log.error("Error processing AI chat request {}: {}", requestId, e.getMessage(), e);
            
            ChatResponse errorResponse = ChatResponse.builder()
                    .response("I apologize, but I'm experiencing technical difficulties. Please try again later.")
                    .sessionId(request.getSessionId())
                    .timestamp(LocalDateTime.now())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .requestId(requestId)
                    .build();
            
            return ResponseEntity.ok(ApiResponse.success(errorResponse, "AI chat completed with fallback response"));
        }
    }

    /**
     * Send message to AI chat asynchronously
     */
    @PostMapping("/async")
    @PreAuthorize("hasAuthority('ai:chat')")
    @Operation(summary = "Send async chat message", description = "Send a message to AI chat asynchronously")
    public ResponseEntity<ApiResponse<CompletableFuture<String>>> chatAsync(
            @Valid @RequestBody ChatRequest request) {
        
        UUID currentUserId = getCurrentUserId();
        log.info("Processing async AI chat request from user: {}", currentUserId);
        
        if (request.getSessionId() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Session ID is required for async chat"));
        }
        
        CompletableFuture<String> futureResponse = aiChatService.chatAsync(
                request.getSessionId(), 
                currentUserId, 
                request.getMessage()
        );
        
        return ResponseEntity.ok(ApiResponse.success(futureResponse, "Async AI chat request submitted"));
    }

    /**
     * Get AI capabilities
     */
    @GetMapping("/capabilities")
    @PreAuthorize("hasAuthority('ai:read')")
    @Operation(summary = "Get AI capabilities", description = "Get available AI capabilities for current tenant")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCapabilities() {
        
        log.debug("Fetching AI capabilities");
        
        Map<String, Object> capabilities = aiChatService.getAICapabilities();
        
        return ResponseEntity.ok(ApiResponse.success(capabilities, "AI capabilities retrieved successfully"));
    }

    /**
     * Test AI connection
     */
    @PostMapping("/test")
    @PreAuthorize("hasAuthority('ai:admin')")
    @Operation(summary = "Test AI connection", description = "Test AI service connectivity and response")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testConnection() {
        
        log.info("Testing AI connection");
        
        Map<String, Object> testResult = aiChatService.testAIConnection();
        
        return ResponseEntity.ok(ApiResponse.success(testResult, "AI connection test completed"));
    }

    /**
     * Get AI usage statistics
     */
    @GetMapping("/usage")
    @PreAuthorize("hasAuthority('ai:read')")
    @Operation(summary = "Get AI usage statistics", description = "Get AI usage statistics for current tenant")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUsageStatistics() {
        
        log.debug("Fetching AI usage statistics");
        
        Map<String, Object> usage = aiChatService.getUsageStatistics();
        
        return ResponseEntity.ok(ApiResponse.success(usage, "AI usage statistics retrieved successfully"));
    }

    /**
     * Clear AI cache
     */
    @PostMapping("/cache/clear")
    @PreAuthorize("hasAuthority('ai:admin')")
    @Operation(summary = "Clear AI cache", description = "Clear AI cache for current tenant")
    public ResponseEntity<ApiResponse<Void>> clearCache() {
        
        log.info("Clearing AI cache");
        
        aiChatService.clearTenantAICache();
        
        return ResponseEntity.ok(ApiResponse.success(null, "AI cache cleared successfully"));
    }

    /**
     * Get current user ID from security context
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return UUID.fromString((String) authentication.getPrincipal());
        }
        throw new IllegalStateException("No authenticated user found");
    }

    /**
     * Get current user name from security context
     */
    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        }
        return "Unknown User";
    }

    /**
     * Get current user email from security context
     */
    private String getCurrentUserEmail() {
        // TODO: Extract email from JWT token or user details
        return null;
    }
}
