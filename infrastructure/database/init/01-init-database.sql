-- Initialize Inticky database with required extensions and settings

-- Enable required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";

-- Set timezone to UTC for consistency
SET timezone = 'UTC';

-- Create database user for application (if not exists)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'inticky_app') THEN
        CREATE ROLE inticky_app WITH LOGIN PASSWORD 'inticky_app_password';
    END IF;
END
$$;

-- Grant necessary permissions
GRANT CONNECT ON DATABASE inticky TO inticky_app;
GRANT USAGE ON SCHEMA public TO inticky_app;
GRANT CREATE ON SCHEMA public TO inticky_app;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO inticky_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO inticky_app;

-- Configure PostgreSQL settings for better performance
-- These settings are for development; adjust for production
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET max_connections = 200;
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;
ALTER SYSTEM SET random_page_cost = 1.1;
ALTER SYSTEM SET effective_io_concurrency = 200;

-- Enable query statistics collection
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- Create a function to generate tenant-aware UUIDs (optional enhancement)
CREATE OR REPLACE FUNCTION generate_tenant_uuid(tenant_prefix TEXT DEFAULT '')
RETURNS UUID AS $$
BEGIN
    -- This is a simple implementation; can be enhanced for better tenant isolation
    RETURN gen_random_uuid();
END;
$$ LANGUAGE plpgsql;

-- Create a function to validate tenant access (used in RLS policies if needed)
CREATE OR REPLACE FUNCTION current_tenant_id()
RETURNS TEXT AS $$
BEGIN
    -- This would be set by the application context
    -- For now, return a default value
    RETURN current_setting('app.current_tenant_id', true);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Create a function to log tenant operations
CREATE OR REPLACE FUNCTION log_tenant_operation(
    p_tenant_id TEXT,
    p_operation TEXT,
    p_table_name TEXT,
    p_record_id UUID DEFAULT NULL
)
RETURNS VOID AS $$
BEGIN
    -- This function can be enhanced to log operations to an audit table
    -- For now, it's a placeholder for future audit functionality
    RAISE NOTICE 'Tenant Operation: % - % on % (ID: %)', p_tenant_id, p_operation, p_table_name, p_record_id;
END;
$$ LANGUAGE plpgsql;

-- Create indexes on system catalogs for better performance monitoring
-- (These are typically created automatically, but ensuring they exist)

-- Ensure proper collation for text searches
-- This helps with case-insensitive searches and proper sorting
CREATE COLLATION IF NOT EXISTS case_insensitive (
    provider = icu,
    locale = 'und-u-ks-level2',
    deterministic = false
);

COMMENT ON DATABASE inticky IS 'Inticky Multi-tenant Ticketing System Database';
COMMENT ON EXTENSION "uuid-ossp" IS 'UUID generation functions';
COMMENT ON EXTENSION "pg_trgm" IS 'Trigram matching for full-text search';
COMMENT ON EXTENSION "btree_gin" IS 'GIN indexes for better performance';

-- Log successful initialization
DO $$
BEGIN
    RAISE NOTICE 'Inticky database initialized successfully at %', CURRENT_TIMESTAMP;
END
$$;
