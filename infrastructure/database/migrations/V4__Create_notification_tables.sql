-- Create notification templates table
CREATE TABLE notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    subject VARCHAR(500),
    body TEXT NOT NULL,
    is_system_template BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    UNIQUE(tenant_id, name, type)
);

-- Create indexes for notification_templates
CREATE INDEX idx_notification_templates_tenant_id ON notification_templates(tenant_id);
CREATE INDEX idx_notification_templates_type ON notification_templates(tenant_id, type);
CREATE INDEX idx_notification_templates_active ON notification_templates(tenant_id, is_active);

-- Create notification log table
CREATE TABLE notification_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    template_id UUID,
    recipient_id UUID,
    recipient_email VARCHAR(320),
    recipient_phone VARCHAR(20),
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    subject VARCHAR(500),
    content TEXT NOT NULL,
    metadata JSONB,
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (template_id) REFERENCES notification_templates(id) ON DELETE SET NULL,
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for notification_log
CREATE INDEX idx_notification_log_tenant_id ON notification_log(tenant_id);
CREATE INDEX idx_notification_log_recipient_id ON notification_log(recipient_id);
CREATE INDEX idx_notification_log_type ON notification_log(type);
CREATE INDEX idx_notification_log_status ON notification_log(status);
CREATE INDEX idx_notification_log_created_at ON notification_log(created_at);
CREATE INDEX idx_notification_log_retry ON notification_log(status, retry_count, max_retries);

-- Create notification preferences table
CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(tenant_id, user_id, notification_type, channel)
);

-- Create indexes for notification_preferences
CREATE INDEX idx_notification_preferences_tenant_id ON notification_preferences(tenant_id);
CREATE INDEX idx_notification_preferences_user_id ON notification_preferences(user_id);
CREATE INDEX idx_notification_preferences_type ON notification_preferences(notification_type);

-- Create webhook configurations table
CREATE TABLE webhook_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    secret VARCHAR(255),
    events TEXT[] NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    headers JSONB,
    retry_policy JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    UNIQUE(tenant_id, name)
);

-- Create indexes for webhook_configurations
CREATE INDEX idx_webhook_configurations_tenant_id ON webhook_configurations(tenant_id);
CREATE INDEX idx_webhook_configurations_active ON webhook_configurations(tenant_id, is_active);

-- Create webhook delivery log table
CREATE TABLE webhook_delivery_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    webhook_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    http_status_code INTEGER,
    response_body TEXT,
    error_message TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (webhook_id) REFERENCES webhook_configurations(id) ON DELETE CASCADE
);

-- Create indexes for webhook_delivery_log
CREATE INDEX idx_webhook_delivery_log_tenant_id ON webhook_delivery_log(tenant_id);
CREATE INDEX idx_webhook_delivery_log_webhook_id ON webhook_delivery_log(webhook_id);
CREATE INDEX idx_webhook_delivery_log_status ON webhook_delivery_log(status);
CREATE INDEX idx_webhook_delivery_log_created_at ON webhook_delivery_log(created_at);
CREATE INDEX idx_webhook_delivery_log_retry ON webhook_delivery_log(status, retry_count, max_retries);

-- Insert default notification templates for demo tenant
INSERT INTO notification_templates (tenant_id, name, type, subject, body, is_system_template) VALUES
('demo', 'Ticket Created', 'EMAIL', 'New Ticket Created: {{ticket.title}}', 
 'Hello {{user.firstName}},\n\nA new ticket has been created:\n\nTitle: {{ticket.title}}\nDescription: {{ticket.description}}\nPriority: {{ticket.priority}}\n\nYou can view the ticket at: {{ticket.url}}\n\nBest regards,\nSupport Team', 
 TRUE),

('demo', 'Ticket Assigned', 'EMAIL', 'Ticket Assigned to You: {{ticket.title}}', 
 'Hello {{assignee.firstName}},\n\nA ticket has been assigned to you:\n\nTitle: {{ticket.title}}\nPriority: {{ticket.priority}}\nReporter: {{reporter.firstName}} {{reporter.lastName}}\n\nPlease review and respond as soon as possible.\n\nView ticket: {{ticket.url}}\n\nBest regards,\nSupport Team', 
 TRUE),

('demo', 'Ticket Status Changed', 'EMAIL', 'Ticket Status Updated: {{ticket.title}}', 
 'Hello {{user.firstName}},\n\nThe status of your ticket has been updated:\n\nTitle: {{ticket.title}}\nOld Status: {{oldStatus}}\nNew Status: {{newStatus}}\n\n{{#if comment}}Latest Comment:\n{{comment}}{{/if}}\n\nView ticket: {{ticket.url}}\n\nBest regards,\nSupport Team', 
 TRUE),

('demo', 'Ticket Resolved', 'EMAIL', 'Ticket Resolved: {{ticket.title}}', 
 'Hello {{user.firstName}},\n\nYour ticket has been resolved:\n\nTitle: {{ticket.title}}\nResolution: {{resolution}}\n\nIf you are satisfied with the resolution, the ticket will be automatically closed in 24 hours. If you need further assistance, please reply to this email.\n\nView ticket: {{ticket.url}}\n\nBest regards,\nSupport Team', 
 TRUE),

('demo', 'SLA Breach Warning', 'EMAIL', 'SLA Breach Warning: {{ticket.title}}', 
 'ATTENTION: SLA Breach Warning\n\nTicket: {{ticket.title}}\nPriority: {{ticket.priority}}\nAssignee: {{assignee.firstName}} {{assignee.lastName}}\n\nThis ticket is approaching its SLA deadline:\nFirst Response Due: {{sla.firstResponseDue}}\nResolution Due: {{sla.resolutionDue}}\n\nImmediate action required.\n\nView ticket: {{ticket.url}}', 
 TRUE);

-- Insert default notification preferences for common notification types
INSERT INTO notification_preferences (tenant_id, user_id, notification_type, channel, is_enabled)
SELECT 
    u.tenant_id,
    u.id,
    notification_type,
    'EMAIL',
    TRUE
FROM users u
CROSS JOIN (
    VALUES 
    ('TICKET_CREATED'),
    ('TICKET_ASSIGNED'),
    ('TICKET_STATUS_CHANGED'),
    ('TICKET_RESOLVED'),
    ('SLA_BREACH_WARNING')
) AS nt(notification_type)
WHERE u.tenant_id IN ('demo', 'system');

-- Create triggers for updated_at
CREATE TRIGGER update_notification_templates_updated_at BEFORE UPDATE ON notification_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notification_preferences_updated_at BEFORE UPDATE ON notification_preferences
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_webhook_configurations_updated_at BEFORE UPDATE ON webhook_configurations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
