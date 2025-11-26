package com.kesmarki.inticky.ticket.service;

import com.kesmarki.inticky.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for generating unique ticket numbers
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketNumberService {

    private final TicketRepository ticketRepository;

    @Value("${inticky.ticket.number.prefix:TICK}")
    private String ticketPrefix;

    @Value("${inticky.ticket.number.format:SEQUENTIAL}")
    private String numberFormat; // SEQUENTIAL, DATE_BASED, UUID

    @Value("${inticky.ticket.number.padding:6}")
    private int numberPadding;

    // In-memory counter for performance (reset on restart)
    private final ConcurrentHashMap<String, AtomicInteger> tenantCounters = new ConcurrentHashMap<>();

    /**
     * Generate unique ticket number for tenant
     */
    public String generateTicketNumber(String tenantId) {
        return switch (numberFormat.toUpperCase()) {
            case "DATE_BASED" -> generateDateBasedTicketNumber(tenantId);
            case "UUID" -> generateUUIDBasedTicketNumber(tenantId);
            default -> generateSequentialTicketNumber(tenantId);
        };
    }

    /**
     * Generate sequential ticket number (TICK-000001)
     */
    private String generateSequentialTicketNumber(String tenantId) {
        log.debug("Generating sequential ticket number for tenant: {}", tenantId);

        // Get or create counter for tenant
        AtomicInteger counter = tenantCounters.computeIfAbsent(tenantId, k -> {
            // Initialize counter from database
            Integer lastNumber = ticketRepository.getNextTicketNumber(tenantId, ticketPrefix);
            return new AtomicInteger(lastNumber != null ? lastNumber : 1);
        });

        int nextNumber = counter.getAndIncrement();
        String ticketNumber = String.format("%s-%0" + numberPadding + "d", ticketPrefix, nextNumber);

        // Ensure uniqueness (in case of concurrent access or restart)
        while (ticketRepository.existsByTicketNumberAndTenantId(ticketNumber, tenantId)) {
            nextNumber = counter.getAndIncrement();
            ticketNumber = String.format("%s-%0" + numberPadding + "d", ticketPrefix, nextNumber);
        }

        log.debug("Generated sequential ticket number: {} for tenant: {}", ticketNumber, tenantId);
        return ticketNumber;
    }

    /**
     * Generate date-based ticket number (TICK-20241125-001)
     */
    private String generateDateBasedTicketNumber(String tenantId) {
        log.debug("Generating date-based ticket number for tenant: {}", tenantId);

        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String datePrefix = ticketPrefix + "-" + dateStr;

        // Get or create counter for tenant and date
        String counterKey = tenantId + ":" + dateStr;
        AtomicInteger counter = tenantCounters.computeIfAbsent(counterKey, k -> {
            // Initialize counter from database for today
            Integer lastNumber = ticketRepository.getNextTicketNumber(tenantId, datePrefix);
            return new AtomicInteger(lastNumber != null ? lastNumber : 1);
        });

        int nextNumber = counter.getAndIncrement();
        String ticketNumber = String.format("%s-%03d", datePrefix, nextNumber);

        // Ensure uniqueness
        while (ticketRepository.existsByTicketNumberAndTenantId(ticketNumber, tenantId)) {
            nextNumber = counter.getAndIncrement();
            ticketNumber = String.format("%s-%03d", datePrefix, nextNumber);
        }

        log.debug("Generated date-based ticket number: {} for tenant: {}", ticketNumber, tenantId);
        return ticketNumber;
    }

    /**
     * Generate UUID-based ticket number (TICK-A1B2C3D4)
     */
    private String generateUUIDBasedTicketNumber(String tenantId) {
        log.debug("Generating UUID-based ticket number for tenant: {}", tenantId);

        String ticketNumber;
        do {
            // Generate short UUID (8 characters)
            String shortUuid = java.util.UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();
            
            ticketNumber = ticketPrefix + "-" + shortUuid;
        } while (ticketRepository.existsByTicketNumberAndTenantId(ticketNumber, tenantId));

        log.debug("Generated UUID-based ticket number: {} for tenant: {}", ticketNumber, tenantId);
        return ticketNumber;
    }

    /**
     * Validate ticket number format
     */
    public boolean isValidTicketNumber(String ticketNumber) {
        if (ticketNumber == null || ticketNumber.trim().isEmpty()) {
            return false;
        }

        // Basic validation - starts with prefix and has proper format
        return ticketNumber.startsWith(ticketPrefix + "-") && 
               ticketNumber.length() > ticketPrefix.length() + 1;
    }

    /**
     * Extract ticket ID from ticket number (for display purposes)
     */
    public String extractTicketId(String ticketNumber) {
        if (!isValidTicketNumber(ticketNumber)) {
            return null;
        }

        return ticketNumber.substring(ticketPrefix.length() + 1);
    }

    /**
     * Get ticket number statistics for tenant
     */
    public TicketNumberStats getTicketNumberStats(String tenantId) {
        log.debug("Getting ticket number statistics for tenant: {}", tenantId);

        AtomicInteger counter = tenantCounters.get(tenantId);
        int currentCounter = counter != null ? counter.get() : 0;

        // Get last ticket number from database
        Integer lastDbNumber = ticketRepository.getNextTicketNumber(tenantId, ticketPrefix);
        int lastNumber = lastDbNumber != null ? lastDbNumber - 1 : 0;

        return TicketNumberStats.builder()
                .tenantId(tenantId)
                .prefix(ticketPrefix)
                .format(numberFormat)
                .padding(numberPadding)
                .currentCounter(currentCounter)
                .lastTicketNumber(lastNumber)
                .totalTickets(ticketRepository.countByTenantId(tenantId))
                .build();
    }

    /**
     * Reset counter for tenant (admin operation)
     */
    public void resetCounter(String tenantId) {
        log.warn("Resetting ticket number counter for tenant: {}", tenantId);
        tenantCounters.remove(tenantId);
    }

    /**
     * Ticket number statistics
     */
    public static class TicketNumberStats {
        public String tenantId;
        public String prefix;
        public String format;
        public int padding;
        public int currentCounter;
        public int lastTicketNumber;
        public long totalTickets;

        public static TicketNumberStatsBuilder builder() {
            return new TicketNumberStatsBuilder();
        }

        public static class TicketNumberStatsBuilder {
            private String tenantId;
            private String prefix;
            private String format;
            private int padding;
            private int currentCounter;
            private int lastTicketNumber;
            private long totalTickets;

            public TicketNumberStatsBuilder tenantId(String tenantId) {
                this.tenantId = tenantId;
                return this;
            }

            public TicketNumberStatsBuilder prefix(String prefix) {
                this.prefix = prefix;
                return this;
            }

            public TicketNumberStatsBuilder format(String format) {
                this.format = format;
                return this;
            }

            public TicketNumberStatsBuilder padding(int padding) {
                this.padding = padding;
                return this;
            }

            public TicketNumberStatsBuilder currentCounter(int currentCounter) {
                this.currentCounter = currentCounter;
                return this;
            }

            public TicketNumberStatsBuilder lastTicketNumber(int lastTicketNumber) {
                this.lastTicketNumber = lastTicketNumber;
                return this;
            }

            public TicketNumberStatsBuilder totalTickets(long totalTickets) {
                this.totalTickets = totalTickets;
                return this;
            }

            public TicketNumberStats build() {
                TicketNumberStats stats = new TicketNumberStats();
                stats.tenantId = this.tenantId;
                stats.prefix = this.prefix;
                stats.format = this.format;
                stats.padding = this.padding;
                stats.currentCounter = this.currentCounter;
                stats.lastTicketNumber = this.lastTicketNumber;
                stats.totalTickets = this.totalTickets;
                return stats;
            }
        }
    }
}
