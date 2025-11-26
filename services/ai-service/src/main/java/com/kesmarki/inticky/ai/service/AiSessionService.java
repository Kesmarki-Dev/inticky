package com.kesmarki.inticky.ai.service;

import com.kesmarki.inticky.ai.entity.AiSession;
import com.kesmarki.inticky.ai.repository.AiSessionRepository;
import com.kesmarki.inticky.tenant.context.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for AI session management
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiSessionService {

    private final AiSessionRepository aiSessionRepository;

    @Value("${agentinsec.ai.session.timeout-minutes:60}")
    private int sessionTimeoutMinutes;

    @Value("${agentinsec.ai.session.max-sessions-per-user:5}")
    private int maxSessionsPerUser;

    @Value("${agentinsec.ai.session.cleanup-interval-minutes:30}")
    private int cleanupIntervalMinutes;

    /**
     * Get all sessions for user within tenant
     */
    public Page<AiSession> getUserSessions(UUID userId, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching sessions for user: {} in tenant: {}", userId, tenantId);
        
        return aiSessionRepository.findSessionsByUserIdAndTenantId(userId, tenantId, pageable);
    }

    /**
     * Get active sessions for user within tenant
     */
    public List<AiSession> getActiveUserSessions(UUID userId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching active sessions for user: {} in tenant: {}", userId, tenantId);
        
        return aiSessionRepository.findActiveSessionsByUserIdAndTenantId(userId, tenantId);
    }

    /**
     * Get session by ID within tenant
     */
    @Cacheable(value = "ai-sessions", key = "#sessionId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public AiSession getSessionById(UUID sessionId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching session: {} in tenant: {}", sessionId, tenantId);
        
        return aiSessionRepository.findByIdAndTenantId(sessionId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("AI session not found with ID: " + sessionId));
    }

    /**
     * Get sessions for ticket within tenant
     */
    public List<AiSession> getTicketSessions(UUID ticketId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching sessions for ticket: {} in tenant: {}", ticketId, tenantId);
        
        return aiSessionRepository.findSessionsByTicketIdAndTenantId(ticketId, tenantId);
    }

    /**
     * Create new AI session
     */
    @Transactional
    @CacheEvict(value = "ai-sessions", allEntries = true)
    public AiSession createSession(UUID userId, String userName, String userEmail, AiSession.SessionType sessionType, UUID ticketId, String title, String description) {
        String tenantId = TenantContext.getTenantId();
        log.info("Creating AI session for user: {} in tenant: {}", userId, tenantId);

        // Check session limits
        long activeSessionCount = aiSessionRepository.countActiveSessionsByUserIdAndTenantId(userId, tenantId);
        if (activeSessionCount >= maxSessionsPerUser) {
            log.warn("User {} has reached maximum session limit ({}) in tenant: {}", userId, maxSessionsPerUser, tenantId);
            
            // Deactivate oldest session
            List<AiSession> activeSessions = aiSessionRepository.findActiveSessionsByUserIdAndTenantId(userId, tenantId);
            if (!activeSessions.isEmpty()) {
                AiSession oldestSession = activeSessions.get(activeSessions.size() - 1);
                oldestSession.deactivate();
                aiSessionRepository.save(oldestSession);
                log.info("Deactivated oldest session {} for user {} in tenant: {}", oldestSession.getId(), userId, tenantId);
            }
        }

        // Create new session
        AiSession session = AiSession.builder()
                .userId(userId)
                .userName(userName)
                .userEmail(userEmail)
                .title(title)
                .description(description)
                .sessionType(sessionType)
                .ticketId(ticketId)
                .isActive(true)
                .lastActivityAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(sessionTimeoutMinutes))
                .messageCount(0)
                .priority(AiSession.Priority.NORMAL)
                .build();

        session.setTenantId(tenantId);
        session = aiSessionRepository.save(session);

        log.info("AI session created successfully: {} for user: {} in tenant: {}", session.getId(), userId, tenantId);

        return session;
    }

    /**
     * Update session activity
     */
    @Transactional
    @CacheEvict(value = "ai-sessions", key = "#sessionId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public void updateSessionActivity(UUID sessionId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Updating activity for session: {} in tenant: {}", sessionId, tenantId);

        AiSession session = getSessionById(sessionId);
        session.updateActivity();
        session.extendExpiry(sessionTimeoutMinutes);
        
        aiSessionRepository.save(session);
    }

    /**
     * Increment message count for session
     */
    @Transactional
    @CacheEvict(value = "ai-sessions", key = "#sessionId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public void incrementMessageCount(UUID sessionId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Incrementing message count for session: {} in tenant: {}", sessionId, tenantId);

        AiSession session = getSessionById(sessionId);
        session.incrementMessageCount();
        
        aiSessionRepository.save(session);
    }

    /**
     * Deactivate session
     */
    @Transactional
    @CacheEvict(value = "ai-sessions", key = "#sessionId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public void deactivateSession(UUID sessionId) {
        String tenantId = TenantContext.getTenantId();
        log.info("Deactivating session: {} in tenant: {}", sessionId, tenantId);

        AiSession session = getSessionById(sessionId);
        session.deactivate();
        
        aiSessionRepository.save(session);
    }

    /**
     * Update session title and description
     */
    @Transactional
    @CacheEvict(value = "ai-sessions", key = "#sessionId + '_' + T(com.kesmarki.inticky.tenant.context.TenantContext).getTenantId()")
    public AiSession updateSession(UUID sessionId, String title, String description) {
        String tenantId = TenantContext.getTenantId();
        log.info("Updating session: {} in tenant: {}", sessionId, tenantId);

        AiSession session = getSessionById(sessionId);
        
        if (title != null && !title.trim().isEmpty()) {
            session.setTitle(title);
        }
        
        if (description != null && !description.trim().isEmpty()) {
            session.setDescription(description);
        }
        
        session.updateActivity();
        
        return aiSessionRepository.save(session);
    }

    /**
     * Search sessions by keyword
     */
    public Page<AiSession> searchSessions(String keyword, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Searching sessions with keyword: {} in tenant: {}", keyword, tenantId);
        
        return aiSessionRepository.searchSessionsByKeywordAndTenantId(keyword, tenantId, pageable);
    }

    /**
     * Get session statistics for tenant
     */
    public Map<String, Object> getSessionStatistics() {
        String tenantId = TenantContext.getTenantId();
        log.debug("Fetching session statistics for tenant: {}", tenantId);

        long totalSessions = aiSessionRepository.countByTenantId(tenantId);
        long activeSessions = aiSessionRepository.countActiveSessionsByTenantId(tenantId);
        
        List<Object[]> typeStats = aiSessionRepository.getSessionStatisticsByTypeAndTenantId(tenantId);
        List<Object[]> userStats = aiSessionRepository.getSessionStatisticsByUserAndTenantId(tenantId);

        return Map.of(
                "totalSessions", totalSessions,
                "activeSessions", activeSessions,
                "inactiveSessions", totalSessions - activeSessions,
                "sessionsByType", typeStats,
                "sessionsByUser", userStats,
                "tenantId", tenantId,
                "generatedAt", LocalDateTime.now()
        );
    }

    /**
     * Get recent sessions within tenant
     */
    public List<AiSession> getRecentSessions(int hours) {
        String tenantId = TenantContext.getTenantId();
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        
        log.debug("Fetching recent sessions since {} in tenant: {}", since, tenantId);
        
        return aiSessionRepository.findRecentSessionsByTenantId(since, tenantId);
    }

    /**
     * Check if user can create new session
     */
    public boolean canCreateSession(UUID userId) {
        String tenantId = TenantContext.getTenantId();
        long activeSessionCount = aiSessionRepository.countActiveSessionsByUserIdAndTenantId(userId, tenantId);
        
        return activeSessionCount < maxSessionsPerUser;
    }

    /**
     * Get user session limit info
     */
    public Map<String, Object> getUserSessionLimits(UUID userId) {
        String tenantId = TenantContext.getTenantId();
        long activeSessionCount = aiSessionRepository.countActiveSessionsByUserIdAndTenantId(userId, tenantId);
        long totalSessionCount = aiSessionRepository.countSessionsByUserIdAndTenantId(userId, tenantId);

        return Map.of(
                "userId", userId,
                "activeSessions", activeSessionCount,
                "totalSessions", totalSessionCount,
                "maxSessionsAllowed", maxSessionsPerUser,
                "canCreateNew", activeSessionCount < maxSessionsPerUser,
                "tenantId", tenantId
        );
    }

    /**
     * Scheduled cleanup of expired and inactive sessions
     */
    @Scheduled(fixedRateString = "#{${agentinsec.ai.session.cleanup-interval-minutes:30} * 60 * 1000}")
    @Transactional
    public void cleanupSessions() {
        log.info("Starting scheduled session cleanup");

        // Get all tenants that have sessions (this would need to be implemented)
        // For now, we'll skip tenant-specific cleanup in scheduled method
        // In production, this should iterate through all tenants
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oldThreshold = now.minusDays(7); // Sessions older than 7 days
        LocalDateTime inactiveThreshold = now.minusHours(24); // Sessions inactive for 24 hours

        // This cleanup would need to be enhanced for multi-tenant support
        log.debug("Session cleanup completed");
    }

    /**
     * Cleanup sessions for specific tenant
     */
    @Transactional
    public int cleanupTenantSessions(String tenantId) {
        log.info("Cleaning up sessions for tenant: {}", tenantId);

        LocalDateTime now = LocalDateTime.now();
        
        // Deactivate expired sessions
        int expiredCount = aiSessionRepository.deactivateExpiredSessions(now, tenantId);
        
        // Find sessions that need cleanup
        LocalDateTime oldThreshold = now.minusDays(7);
        LocalDateTime inactiveThreshold = now.minusHours(24);
        
        List<AiSession> sessionsToCleanup = aiSessionRepository.findSessionsForCleanup(tenantId, oldThreshold, inactiveThreshold);
        
        // Deactivate old inactive sessions
        int cleanedCount = 0;
        for (AiSession session : sessionsToCleanup) {
            if (session.getIsActive()) {
                session.deactivate();
                aiSessionRepository.save(session);
                cleanedCount++;
            }
        }

        log.info("Session cleanup completed for tenant {}: {} expired, {} cleaned up", tenantId, expiredCount, cleanedCount);
        
        return expiredCount + cleanedCount;
    }
}
