package com.kesmarki.inticky.ticket.enums;

/**
 * Enumeration for ticket categories
 */
public enum Category {
    /**
     * Technical problems and issues
     */
    TECHNICAL("Technical", "Technical problems and system issues"),
    
    /**
     * Account and access related problems
     */
    ACCOUNT("Account", "Account and access related problems"),
    
    /**
     * New feature requests
     */
    FEATURE_REQUEST("Feature Request", "New feature requests and enhancements"),
    
    /**
     * Software bugs and defects
     */
    BUG("Bug", "Software bugs and defects"),
    
    /**
     * General support and questions
     */
    SUPPORT("Support", "General support and questions"),
    
    /**
     * Documentation requests and issues
     */
    DOCUMENTATION("Documentation", "Documentation requests and issues"),
    
    /**
     * Security related issues
     */
    SECURITY("Security", "Security related issues and concerns"),
    
    /**
     * Performance related issues
     */
    PERFORMANCE("Performance", "Performance related issues and optimization"),
    
    /**
     * Integration related issues
     */
    INTEGRATION("Integration", "Integration related issues and setup"),
    
    /**
     * Other/miscellaneous issues
     */
    OTHER("Other", "Other or miscellaneous issues");

    private final String displayName;
    private final String description;

    Category(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Get display name for category
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get description for category
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if category is technical
     */
    public boolean isTechnical() {
        return this == TECHNICAL || this == BUG || this == PERFORMANCE || this == INTEGRATION;
    }

    /**
     * Check if category is user-facing
     */
    public boolean isUserFacing() {
        return this == ACCOUNT || this == SUPPORT || this == DOCUMENTATION;
    }

    /**
     * Check if category is development-related
     */
    public boolean isDevelopmentRelated() {
        return this == FEATURE_REQUEST || this == BUG || this == PERFORMANCE;
    }

    /**
     * Check if category is security-related
     */
    public boolean isSecurityRelated() {
        return this == SECURITY || this == ACCOUNT;
    }

    /**
     * Get default priority for this category
     */
    public Priority getDefaultPriority() {
        return switch (this) {
            case SECURITY -> Priority.HIGH;
            case BUG -> Priority.MEDIUM;
            case TECHNICAL, PERFORMANCE -> Priority.MEDIUM;
            case ACCOUNT -> Priority.MEDIUM;
            case FEATURE_REQUEST -> Priority.LOW;
            case SUPPORT, DOCUMENTATION, INTEGRATION, OTHER -> Priority.LOW;
        };
    }

    /**
     * Get color code for UI display
     */
    public String getColorCode() {
        return switch (this) {
            case SECURITY -> "#DC143C";      // Crimson
            case BUG -> "#FF6347";           // Tomato
            case TECHNICAL -> "#4682B4";     // Steel Blue
            case PERFORMANCE -> "#FF8C00";   // Dark Orange
            case ACCOUNT -> "#9370DB";       // Medium Purple
            case FEATURE_REQUEST -> "#32CD32"; // Lime Green
            case SUPPORT -> "#20B2AA";       // Light Sea Green
            case DOCUMENTATION -> "#DAA520"; // Goldenrod
            case INTEGRATION -> "#8A2BE2";   // Blue Violet
            case OTHER -> "#808080";         // Gray
        };
    }
}
