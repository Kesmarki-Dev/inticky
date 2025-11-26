-- Additional performance indexes for multi-tenant queries

-- Composite indexes for common query patterns
CREATE INDEX idx_tickets_tenant_status_priority ON tickets(tenant_id, status, priority);
CREATE INDEX idx_tickets_tenant_assignee_status ON tickets(tenant_id, assignee_id, status);
CREATE INDEX idx_tickets_tenant_reporter_created ON tickets(tenant_id, reporter_id, created_at DESC);
CREATE INDEX idx_tickets_tenant_category_status ON tickets(tenant_id, category, status);

-- Indexes for ticket search and filtering
CREATE INDEX idx_tickets_tenant_created_desc ON tickets(tenant_id, created_at DESC);
CREATE INDEX idx_tickets_tenant_updated_desc ON tickets(tenant_id, updated_at DESC);
CREATE INDEX idx_tickets_tenant_due_date_asc ON tickets(tenant_id, due_date ASC) WHERE due_date IS NOT NULL;

-- Full-text search indexes for ticket content
CREATE INDEX idx_tickets_title_search ON tickets USING gin(to_tsvector('english', title));
CREATE INDEX idx_tickets_description_search ON tickets USING gin(to_tsvector('english', description));
CREATE INDEX idx_ticket_comments_content_search ON ticket_comments USING gin(to_tsvector('english', content));

-- Composite index for ticket comments with tenant filtering
CREATE INDEX idx_ticket_comments_tenant_ticket_created ON ticket_comments(tenant_id, ticket_id, created_at DESC);

-- Indexes for user management queries
CREATE INDEX idx_users_tenant_status_created ON users(tenant_id, status, created_at DESC);
CREATE INDEX idx_users_tenant_last_login ON users(tenant_id, last_login_at DESC) WHERE last_login_at IS NOT NULL;

-- Indexes for role and permission queries
CREATE INDEX idx_user_roles_user_role ON user_roles(user_id, role_id);
CREATE INDEX idx_role_permissions_role_permission ON role_permissions(role_id, permission_id);

-- Indexes for notification performance
CREATE INDEX idx_notification_log_tenant_status_created ON notification_log(tenant_id, status, created_at DESC);
CREATE INDEX idx_notification_log_recipient_type_created ON notification_log(recipient_id, type, created_at DESC);

-- Indexes for SLA tracking and reporting
CREATE INDEX idx_ticket_sla_tenant_breach ON ticket_sla_tracking(tenant_id, is_breached);
CREATE INDEX idx_ticket_sla_tenant_first_response_due ON ticket_sla_tracking(tenant_id, first_response_due) WHERE first_response_at IS NULL;
CREATE INDEX idx_ticket_sla_tenant_resolution_due ON ticket_sla_tracking(tenant_id, resolution_due) WHERE resolved_at IS NULL;

-- Indexes for audit and history queries
CREATE INDEX idx_ticket_history_tenant_ticket_created ON ticket_history(tenant_id, ticket_id, created_at DESC);
CREATE INDEX idx_ticket_history_tenant_changed_by ON ticket_history(tenant_id, changed_by, created_at DESC);

-- Indexes for session management
CREATE INDEX idx_user_sessions_tenant_user_active ON user_sessions(tenant_id, user_id, expires_at) WHERE expires_at > CURRENT_TIMESTAMP;
CREATE INDEX idx_user_sessions_cleanup ON user_sessions(expires_at) WHERE expires_at < CURRENT_TIMESTAMP;

-- Indexes for webhook delivery
CREATE INDEX idx_webhook_delivery_tenant_status_created ON webhook_delivery_log(tenant_id, status, created_at DESC);
CREATE INDEX idx_webhook_delivery_retry_pending ON webhook_delivery_log(status, retry_count, created_at) WHERE status = 'FAILED' AND retry_count < max_retries;

-- Partial indexes for active records only
CREATE INDEX idx_tenants_active ON tenants(id, name) WHERE status = 'ACTIVE';
CREATE INDEX idx_users_active ON users(tenant_id, id, email) WHERE status = 'ACTIVE';
CREATE INDEX idx_tickets_open ON tickets(tenant_id, id, assignee_id, priority) WHERE status IN ('NEW', 'OPEN', 'IN_PROGRESS');
CREATE INDEX idx_notification_templates_active ON notification_templates(tenant_id, type, name) WHERE is_active = TRUE;

-- Indexes for reporting and analytics
CREATE INDEX idx_tickets_reporting_created_monthly ON tickets(tenant_id, date_trunc('month', created_at), status, priority);
CREATE INDEX idx_tickets_reporting_resolved_monthly ON tickets(tenant_id, date_trunc('month', resolved_at), category) WHERE resolved_at IS NOT NULL;

-- Covering indexes for common SELECT queries
CREATE INDEX idx_tickets_list_covering ON tickets(tenant_id, status, created_at DESC) 
    INCLUDE (id, title, priority, category, reporter_id, assignee_id);

CREATE INDEX idx_users_list_covering ON users(tenant_id, status, created_at DESC) 
    INCLUDE (id, email, first_name, last_name, last_login_at);

-- Statistics update for better query planning
ANALYZE tenants;
ANALYZE users;
ANALYZE roles;
ANALYZE permissions;
ANALYZE user_roles;
ANALYZE role_permissions;
ANALYZE tickets;
ANALYZE ticket_comments;
ANALYZE ticket_attachments;
ANALYZE ticket_history;
ANALYZE ticket_sla_tracking;
ANALYZE sla_policies;
ANALYZE notification_templates;
ANALYZE notification_log;
ANALYZE notification_preferences;
ANALYZE webhook_configurations;
ANALYZE webhook_delivery_log;
