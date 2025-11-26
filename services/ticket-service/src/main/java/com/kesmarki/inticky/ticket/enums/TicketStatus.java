package com.kesmarki.inticky.ticket.enums;

/**
 * Enumeration for ticket status values
 */
public enum TicketStatus {
    /**
     * Newly created ticket, not yet processed
     */
    NEW,
    
    /**
     * Ticket accepted and ready for processing
     */
    OPEN,
    
    /**
     * Ticket is being actively worked on
     */
    IN_PROGRESS,
    
    /**
     * Ticket is waiting for external input (customer response, tools, etc.)
     */
    PENDING,
    
    /**
     * Ticket has been resolved, waiting for customer confirmation
     */
    RESOLVED,
    
    /**
     * Ticket is permanently closed
     */
    CLOSED,
    
    /**
     * Ticket has been cancelled or is invalid
     */
    CANCELLED;

    /**
     * Check if status is active (not closed or cancelled)
     */
    public boolean isActive() {
        return this != CLOSED && this != CANCELLED;
    }

    /**
     * Check if status is in progress (being worked on)
     */
    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    /**
     * Check if status is pending (waiting for input)
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * Check if status is resolved (completed but not closed)
     */
    public boolean isResolved() {
        return this == RESOLVED;
    }

    /**
     * Check if status is final (closed or cancelled)
     */
    public boolean isFinal() {
        return this == CLOSED || this == CANCELLED;
    }

    /**
     * Get next possible statuses from current status
     */
    public TicketStatus[] getNextPossibleStatuses() {
        return switch (this) {
            case NEW -> new TicketStatus[]{OPEN, CANCELLED};
            case OPEN -> new TicketStatus[]{IN_PROGRESS, PENDING, CANCELLED};
            case IN_PROGRESS -> new TicketStatus[]{PENDING, RESOLVED, CANCELLED};
            case PENDING -> new TicketStatus[]{IN_PROGRESS, RESOLVED, CANCELLED};
            case RESOLVED -> new TicketStatus[]{CLOSED, IN_PROGRESS};
            case CLOSED -> new TicketStatus[]{IN_PROGRESS}; // Reopen
            case CANCELLED -> new TicketStatus[]{OPEN}; // Reactivate
        };
    }
}
