package com.kesmarki.inticky.ticket.enums;

import java.time.Duration;

/**
 * Enumeration for ticket priority levels
 */
public enum Priority {
    /**
     * Critical priority - system critical issues (4 hours SLA)
     */
    CRITICAL(1, Duration.ofHours(4)),
    
    /**
     * High priority - important functions not working (24 hours SLA)
     */
    HIGH(2, Duration.ofHours(24)),
    
    /**
     * Medium priority - normal problems (72 hours SLA)
     */
    MEDIUM(3, Duration.ofHours(72)),
    
    /**
     * Low priority - development requests, minor issues (7 days SLA)
     */
    LOW(4, Duration.ofDays(7));

    private final int level;
    private final Duration slaTime;

    Priority(int level, Duration slaTime) {
        this.level = level;
        this.slaTime = slaTime;
    }

    /**
     * Get priority level (1 = highest, 4 = lowest)
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get SLA time for this priority
     */
    public Duration getSlaTime() {
        return slaTime;
    }

    /**
     * Check if this priority is higher than another
     */
    public boolean isHigherThan(Priority other) {
        return this.level < other.level;
    }

    /**
     * Check if this priority is lower than another
     */
    public boolean isLowerThan(Priority other) {
        return this.level > other.level;
    }

    /**
     * Check if this is a critical priority
     */
    public boolean isCritical() {
        return this == CRITICAL;
    }

    /**
     * Check if this is a high priority
     */
    public boolean isHigh() {
        return this == HIGH;
    }

    /**
     * Check if this is urgent (critical or high)
     */
    public boolean isUrgent() {
        return this == CRITICAL || this == HIGH;
    }

    /**
     * Get priority by level
     */
    public static Priority fromLevel(int level) {
        return switch (level) {
            case 1 -> CRITICAL;
            case 2 -> HIGH;
            case 3 -> MEDIUM;
            case 4 -> LOW;
            default -> throw new IllegalArgumentException("Invalid priority level: " + level);
        };
    }

    /**
     * Get display name for priority
     */
    public String getDisplayName() {
        return switch (this) {
            case CRITICAL -> "Critical";
            case HIGH -> "High";
            case MEDIUM -> "Medium";
            case LOW -> "Low";
        };
    }

    /**
     * Get color code for UI display
     */
    public String getColorCode() {
        return switch (this) {
            case CRITICAL -> "#FF0000"; // Red
            case HIGH -> "#FF8C00";     // Dark Orange
            case MEDIUM -> "#FFD700";   // Gold
            case LOW -> "#32CD32";      // Lime Green
        };
    }
}
