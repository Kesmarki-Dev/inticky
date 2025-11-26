-- Create tenant table
CREATE TABLE IF NOT EXISTS tenants (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    domain VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    plan VARCHAR(50) NOT NULL DEFAULT 'BASIC',
    settings TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255) NOT NULL DEFAULT 'system'
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_tenant_domain ON tenants(domain);
CREATE INDEX IF NOT EXISTS idx_tenant_status ON tenants(status);
CREATE INDEX IF NOT EXISTS idx_tenant_plan ON tenants(plan);
CREATE INDEX IF NOT EXISTS idx_tenant_created ON tenants(created_at);

-- Insert system tenant
INSERT INTO tenants (id, name, domain, status, plan, tenant_id) 
VALUES ('system', 'System Tenant', 'system.inticky.com', 'ACTIVE', 'ENTERPRISE', 'system')
ON CONFLICT (id) DO NOTHING;
