-- Demo data for User Service
-- Inserts basic permissions, roles and demo users

-- Insert system permissions
INSERT INTO permissions (id, name, display_name, description, resource, action, is_system_permission, is_active) VALUES
('00000000-0000-0000-0000-000000000001', 'user:read', 'Read Users', 'View user information', 'user', 'read', true, true),
('00000000-0000-0000-0000-000000000002', 'user:write', 'Write Users', 'Create and update users', 'user', 'write', true, true),
('00000000-0000-0000-0000-000000000003', 'user:delete', 'Delete Users', 'Delete users', 'user', 'delete', true, true),
('00000000-0000-0000-0000-000000000004', 'role:read', 'Read Roles', 'View roles', 'role', 'read', true, true),
('00000000-0000-0000-0000-000000000005', 'role:write', 'Write Roles', 'Create and update roles', 'role', 'write', true, true),
('00000000-0000-0000-0000-000000000006', 'ticket:read', 'Read Tickets', 'View tickets', 'ticket', 'read', true, true),
('00000000-0000-0000-0000-000000000007', 'ticket:write', 'Write Tickets', 'Create and update tickets', 'ticket', 'write', true, true),
('00000000-0000-0000-0000-000000000008', 'ticket:delete', 'Delete Tickets', 'Delete tickets', 'ticket', 'delete', true, true),
('00000000-0000-0000-0000-000000000009', 'tenant:read', 'Read Tenants', 'View tenant information', 'tenant', 'read', true, true),
('00000000-0000-0000-0000-000000000010', 'tenant:write', 'Write Tenants', 'Create and update tenants', 'tenant', 'write', true, true);

-- Insert system roles
INSERT INTO roles (id, tenant_id, name, display_name, description, is_system_role, is_active) VALUES
('00000000-0000-0000-0000-000000000101', NULL, 'SYSTEM_ADMIN', 'System Administrator', 'Full system access across all tenants', true, true),
('00000000-0000-0000-0000-000000000102', '550e8400-e29b-41d4-a716-446655440000', 'TENANT_ADMIN', 'Tenant Administrator', 'Full access within tenant', false, true),
('00000000-0000-0000-0000-000000000103', '550e8400-e29b-41d4-a716-446655440000', 'AGENT', 'Support Agent', 'Handle tickets and support requests', false, true),
('00000000-0000-0000-0000-000000000104', '550e8400-e29b-41d4-a716-446655440000', 'USER', 'End User', 'Create and view own tickets', false, true);

-- Assign permissions to roles
-- SYSTEM_ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000003'),
('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000006'),
('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000007'),
('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000008'),
('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000009'),
('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000010');

-- TENANT_ADMIN gets user and ticket permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000002'),
('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000004'),
('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000005'),
('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000006'),
('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000007'),
('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000008');

-- AGENT gets ticket permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
('00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000006'),
('00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000007');

-- USER gets basic ticket read permission
INSERT INTO role_permissions (role_id, permission_id) VALUES
('00000000-0000-0000-0000-000000000104', '00000000-0000-0000-0000-000000000006');

-- Insert demo users with BCrypt hashed passwords (password123)
INSERT INTO users (id, tenant_id, email, password, first_name, last_name, phone, department, job_title, status, email_verified) VALUES
('00000000-0000-0000-0000-000000000201', '550e8400-e29b-41d4-a716-446655440000', 'admin@system.inticky.com', '$2a$10$OrsqFC1M5U4Jt4FrpfQyqOU47ogAuvlMYNu5/H5TzjyXxNAz6B3Ei', 'System', 'Administrator', '+1-555-0001', 'IT', 'System Admin', 'ACTIVE', true),
('00000000-0000-0000-0000-000000000202', '550e8400-e29b-41d4-a716-446655440000', 'admin@demo.example.com', '$2a$10$OrsqFC1M5U4Jt4FrpfQyqOU47ogAuvlMYNu5/H5TzjyXxNAz6B3Ei', 'Demo', 'Admin', '+1-555-0002', 'Management', 'Tenant Admin', 'ACTIVE', true),
('00000000-0000-0000-0000-000000000203', '550e8400-e29b-41d4-a716-446655440000', 'agent@demo.example.com', '$2a$10$OrsqFC1M5U4Jt4FrpfQyqOU47ogAuvlMYNu5/H5TzjyXxNAz6B3Ei', 'Support', 'Agent', '+1-555-0003', 'Support', 'Support Agent', 'ACTIVE', true),
('00000000-0000-0000-0000-000000000204', '550e8400-e29b-41d4-a716-446655440000', 'user@demo.example.com', '$2a$10$OrsqFC1M5U4Jt4FrpfQyqOU47ogAuvlMYNu5/H5TzjyXxNAz6B3Ei', 'Demo', 'User', '+1-555-0004', 'Sales', 'Sales Rep', 'ACTIVE', true);

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id, tenant_id) VALUES
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000101', '550e8400-e29b-41d4-a716-446655440000'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000102', '550e8400-e29b-41d4-a716-446655440000'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000103', '550e8400-e29b-41d4-a716-446655440000'),
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000104', '550e8400-e29b-41d4-a716-446655440000');
