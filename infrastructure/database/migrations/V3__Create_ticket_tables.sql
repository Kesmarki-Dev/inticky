-- Create tickets table
CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    category VARCHAR(100) NOT NULL DEFAULT 'SUPPORT',
    reporter_id UUID NOT NULL,
    assignee_id UUID,
    due_date TIMESTAMP,
    resolved_at TIMESTAMP,
    closed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for tickets
CREATE INDEX idx_tickets_tenant_id ON tickets(tenant_id);
CREATE INDEX idx_tickets_status ON tickets(tenant_id, status);
CREATE INDEX idx_tickets_priority ON tickets(tenant_id, priority);
CREATE INDEX idx_tickets_category ON tickets(tenant_id, category);
CREATE INDEX idx_tickets_reporter_id ON tickets(tenant_id, reporter_id);
CREATE INDEX idx_tickets_assignee_id ON tickets(tenant_id, assignee_id);
CREATE INDEX idx_tickets_due_date ON tickets(tenant_id, due_date);
CREATE INDEX idx_tickets_created_at ON tickets(tenant_id, created_at);

-- Create ticket comments table
CREATE TABLE ticket_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    ticket_id UUID NOT NULL,
    author_id UUID NOT NULL,
    content TEXT NOT NULL,
    comment_type VARCHAR(50) NOT NULL DEFAULT 'PUBLIC',
    is_system_comment BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- Create indexes for ticket_comments
CREATE INDEX idx_ticket_comments_tenant_id ON ticket_comments(tenant_id);
CREATE INDEX idx_ticket_comments_ticket_id ON ticket_comments(ticket_id);
CREATE INDEX idx_ticket_comments_author_id ON ticket_comments(author_id);
CREATE INDEX idx_ticket_comments_type ON ticket_comments(comment_type);
CREATE INDEX idx_ticket_comments_created_at ON ticket_comments(ticket_id, created_at);

-- Create ticket attachments table
CREATE TABLE ticket_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    ticket_id UUID NOT NULL,
    comment_id UUID,
    uploaded_by UUID NOT NULL,
    file_name VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (comment_id) REFERENCES ticket_comments(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE RESTRICT
);

-- Create indexes for ticket_attachments
CREATE INDEX idx_ticket_attachments_tenant_id ON ticket_attachments(tenant_id);
CREATE INDEX idx_ticket_attachments_ticket_id ON ticket_attachments(ticket_id);
CREATE INDEX idx_ticket_attachments_comment_id ON ticket_attachments(comment_id);
CREATE INDEX idx_ticket_attachments_uploaded_by ON ticket_attachments(uploaded_by);

-- Create ticket history table for audit trail
CREATE TABLE ticket_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    ticket_id UUID NOT NULL,
    changed_by UUID NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    change_type VARCHAR(50) NOT NULL DEFAULT 'UPDATE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE RESTRICT
);

-- Create indexes for ticket_history
CREATE INDEX idx_ticket_history_tenant_id ON ticket_history(tenant_id);
CREATE INDEX idx_ticket_history_ticket_id ON ticket_history(ticket_id);
CREATE INDEX idx_ticket_history_changed_by ON ticket_history(changed_by);
CREATE INDEX idx_ticket_history_created_at ON ticket_history(ticket_id, created_at);

-- Create ticket tags table
CREATE TABLE ticket_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7) DEFAULT '#6B7280',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    UNIQUE(tenant_id, name)
);

-- Create indexes for ticket_tags
CREATE INDEX idx_ticket_tags_tenant_id ON ticket_tags(tenant_id);
CREATE INDEX idx_ticket_tags_name ON ticket_tags(tenant_id, name);

-- Create ticket_tag_assignments junction table
CREATE TABLE ticket_tag_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    assigned_by UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES ticket_tags(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE RESTRICT,
    UNIQUE(ticket_id, tag_id)
);

-- Create indexes for ticket_tag_assignments
CREATE INDEX idx_ticket_tag_assignments_ticket_id ON ticket_tag_assignments(ticket_id);
CREATE INDEX idx_ticket_tag_assignments_tag_id ON ticket_tag_assignments(tag_id);

-- Create SLA policies table
CREATE TABLE sla_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    first_response_time_minutes INTEGER NOT NULL,
    resolution_time_minutes INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    UNIQUE(tenant_id, priority)
);

-- Create indexes for sla_policies
CREATE INDEX idx_sla_policies_tenant_id ON sla_policies(tenant_id);
CREATE INDEX idx_sla_policies_priority ON sla_policies(tenant_id, priority);

-- Create ticket SLA tracking table
CREATE TABLE ticket_sla_tracking (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id VARCHAR(255) NOT NULL,
    ticket_id UUID NOT NULL,
    sla_policy_id UUID NOT NULL,
    first_response_due TIMESTAMP NOT NULL,
    resolution_due TIMESTAMP NOT NULL,
    first_response_at TIMESTAMP,
    resolved_at TIMESTAMP,
    is_breached BOOLEAN NOT NULL DEFAULT FALSE,
    breach_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (sla_policy_id) REFERENCES sla_policies(id) ON DELETE RESTRICT,
    UNIQUE(ticket_id)
);

-- Create indexes for ticket_sla_tracking
CREATE INDEX idx_ticket_sla_tracking_tenant_id ON ticket_sla_tracking(tenant_id);
CREATE INDEX idx_ticket_sla_tracking_ticket_id ON ticket_sla_tracking(ticket_id);
CREATE INDEX idx_ticket_sla_tracking_first_response_due ON ticket_sla_tracking(first_response_due);
CREATE INDEX idx_ticket_sla_tracking_resolution_due ON ticket_sla_tracking(resolution_due);
CREATE INDEX idx_ticket_sla_tracking_breached ON ticket_sla_tracking(tenant_id, is_breached);

-- Insert default SLA policies for demo tenant
INSERT INTO sla_policies (tenant_id, name, priority, first_response_time_minutes, resolution_time_minutes) VALUES
('demo', 'Critical Priority SLA', 'CRITICAL', 60, 240),
('demo', 'High Priority SLA', 'HIGH', 240, 1440),
('demo', 'Medium Priority SLA', 'MEDIUM', 1440, 4320),
('demo', 'Low Priority SLA', 'LOW', 4320, 10080);

-- Insert default SLA policies for system tenant
INSERT INTO sla_policies (tenant_id, name, priority, first_response_time_minutes, resolution_time_minutes) VALUES
('system', 'Critical Priority SLA', 'CRITICAL', 30, 120),
('system', 'High Priority SLA', 'HIGH', 120, 720),
('system', 'Medium Priority SLA', 'MEDIUM', 720, 2160),
('system', 'Low Priority SLA', 'LOW', 2160, 5040);

-- Create triggers for updated_at
CREATE TRIGGER update_tickets_updated_at BEFORE UPDATE ON tickets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ticket_comments_updated_at BEFORE UPDATE ON ticket_comments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sla_policies_updated_at BEFORE UPDATE ON sla_policies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
