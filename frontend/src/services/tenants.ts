import { api } from './api';
import type { 
  Tenant, 
  TenantCreateRequest, 
  TenantUpdateRequest, 
  PaginatedResponse 
} from '../types/api';

// Enhanced mock data for development
const mockTenants: Tenant[] = [
  {
    id: '550e8400-e29b-41d4-a716-446655440000',
    name: 'Demo Company Ltd.',
    displayName: 'Demo Company',
    domain: 'demo.inticky.com',
    description: 'Leading software development company specializing in enterprise solutions',
    status: 'ACTIVE',
    plan: 'PREMIUM',
    contactEmail: 'contact@demo.inticky.com',
    contactPhone: '+1-555-0123',
    address: '123 Demo Street, Demo City, DC 12345',
    logoUrl: null,
    primaryColor: '#3B82F6',
    secondaryColor: '#1E40AF',
    maxUsers: 50,
    maxTickets: 1000,
    storageQuotaBytes: 1073741824, // 1GB
    settings: {
      timezone: 'UTC',
      dateFormat: 'YYYY-MM-DD',
      language: 'en',
      currency: 'USD',
      allowSelfRegistration: 'true',
      requireEmailVerification: 'true',
      sessionTimeout: '3600'
    },
    enabledFeatures: ['TICKETS', 'USERS', 'ANALYTICS', 'API_ACCESS', 'CUSTOM_BRANDING'],
    createdAt: '2024-05-26T10:30:00Z',
    updatedAt: '2024-11-21T14:22:00Z',
    isActive: true,
    isTrial: false,
    isSuspended: false,
    isTrialExpired: false,
    isSubscriptionExpired: false,
    subscriptionExpiresAt: '2025-05-26T10:30:00Z',
    daysUntilSubscriptionExpires: 181
  },
  {
    id: 'a1b2c3d4-e5f6-7890-1234-567890abcdef',
    name: 'Trial Corporation',
    displayName: 'Trial Corp',
    domain: 'trial.inticky.com',
    description: 'Startup company evaluating our ticketing solution',
    status: 'TRIAL',
    plan: 'BASIC',
    contactEmail: 'info@trial.inticky.com',
    contactPhone: '+1-555-0456',
    address: '456 Trial Avenue, Trial Town, TT 45678',
    logoUrl: null,
    primaryColor: '#10B981',
    secondaryColor: '#047857',
    maxUsers: 10,
    maxTickets: 100,
    storageQuotaBytes: 104857600, // 100MB
    settings: {
      timezone: 'America/New_York',
      dateFormat: 'MM/DD/YYYY',
      language: 'en',
      currency: 'USD',
      allowSelfRegistration: 'false',
      requireEmailVerification: 'true',
      sessionTimeout: '1800'
    },
    enabledFeatures: ['TICKETS', 'USERS'],
    createdAt: '2024-11-16T09:15:00Z',
    updatedAt: '2024-11-25T16:45:00Z',
    isActive: true,
    isTrial: true,
    isSuspended: false,
    isTrialExpired: false,
    isSubscriptionExpired: false,
    trialExpiresAt: '2024-12-16T09:15:00Z',
    daysUntilTrialExpires: 20
  },
  {
    id: 'f0e9d8c7-b6a5-4321-fedc-ba9876543210',
    name: 'Inactive Solutions Inc.',
    displayName: 'Inactive Solutions',
    domain: 'inactive.inticky.com',
    description: 'Previously active customer - payment issues',
    status: 'SUSPENDED',
    plan: 'BASIC',
    contactEmail: 'support@inactive.inticky.com',
    contactPhone: '+1-555-0789',
    address: '789 Inactive Boulevard, Suspended City, SC 78901',
    logoUrl: null,
    primaryColor: '#EF4444',
    secondaryColor: '#DC2626',
    maxUsers: 25,
    maxTickets: 500,
    storageQuotaBytes: 524288000, // 500MB
    settings: {
      timezone: 'Europe/London',
      dateFormat: 'DD/MM/YYYY',
      language: 'en',
      currency: 'GBP',
      allowSelfRegistration: 'true',
      requireEmailVerification: 'true',
      sessionTimeout: '3600'
    },
    enabledFeatures: ['TICKETS', 'USERS', 'ANALYTICS'],
    createdAt: '2023-11-26T08:00:00Z',
    updatedAt: '2024-08-26T12:30:00Z',
    isActive: false,
    isTrial: false,
    isSuspended: true,
    isTrialExpired: false,
    isSubscriptionExpired: true,
    subscriptionExpiresAt: '2024-08-26T08:00:00Z',
    daysUntilSubscriptionExpires: -92
  },
  {
    id: '12345678-90ab-cdef-1234-567890abcdef',
    name: 'Enterprise Solutions Global',
    displayName: 'Enterprise Global',
    domain: 'enterprise.inticky.com',
    description: 'Large enterprise customer with complex requirements',
    status: 'ACTIVE',
    plan: 'ENTERPRISE',
    contactEmail: 'admin@enterprise.inticky.com',
    contactPhone: '+1-555-1000',
    address: '1000 Enterprise Plaza, Global City, GC 10001',
    logoUrl: 'https://via.placeholder.com/150x50/4F46E5/FFFFFF?text=ENT',
    primaryColor: '#4F46E5',
    secondaryColor: '#3730A3',
    maxUsers: 500,
    maxTickets: 10000,
    storageQuotaBytes: 10737418240, // 10GB
    settings: {
      timezone: 'America/Los_Angeles',
      dateFormat: 'YYYY-MM-DD',
      language: 'en',
      currency: 'USD',
      allowSelfRegistration: 'false',
      requireEmailVerification: 'true',
      sessionTimeout: '7200'
    },
    enabledFeatures: ['TICKETS', 'USERS', 'ANALYTICS', 'API_ACCESS', 'CUSTOM_BRANDING', 'SSO', 'ADVANCED_REPORTING'],
    createdAt: '2023-01-15T10:00:00Z',
    updatedAt: '2024-11-20T15:30:00Z',
    isActive: true,
    isTrial: false,
    isSuspended: false,
    isTrialExpired: false,
    isSubscriptionExpired: false,
    subscriptionExpiresAt: '2025-01-15T10:00:00Z',
    daysUntilSubscriptionExpires: 50
  },
  {
    id: '98765432-10fe-dcba-9876-543210fedcba',
    name: 'Expired Trial Co.',
    displayName: 'Expired Trial',
    domain: 'expired.inticky.com',
    description: 'Trial period has expired - needs conversion',
    status: 'TRIAL',
    plan: 'BASIC',
    contactEmail: 'contact@expired.inticky.com',
    contactPhone: '+1-555-9999',
    address: '999 Expired Lane, Trial City, TC 99999',
    logoUrl: null,
    primaryColor: '#F59E0B',
    secondaryColor: '#D97706',
    maxUsers: 5,
    maxTickets: 50,
    storageQuotaBytes: 52428800, // 50MB
    settings: {
      timezone: 'UTC',
      dateFormat: 'YYYY-MM-DD',
      language: 'en',
      currency: 'USD',
      allowSelfRegistration: 'true',
      requireEmailVerification: 'false',
      sessionTimeout: '1800'
    },
    enabledFeatures: ['TICKETS'],
    createdAt: '2024-10-01T12:00:00Z',
    updatedAt: '2024-10-31T12:00:00Z',
    isActive: false,
    isTrial: true,
    isSuspended: false,
    isTrialExpired: true,
    isSubscriptionExpired: false,
    trialExpiresAt: '2024-10-31T12:00:00Z',
    daysUntilTrialExpires: -26
  }
];

// Helper function to simulate API delay
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

// Helper function to filter tenants
const filterTenants = (tenants: Tenant[], query?: string) => {
  if (!query) return tenants;
  const lowerQuery = query.toLowerCase();
  return tenants.filter(tenant => 
    tenant.name.toLowerCase().includes(lowerQuery) ||
    tenant.displayName.toLowerCase().includes(lowerQuery) ||
    tenant.domain.toLowerCase().includes(lowerQuery) ||
    tenant.contactEmail.toLowerCase().includes(lowerQuery)
  );
};

// Helper function to paginate results
const paginate = <T>(items: T[], page: number, size: number): PaginatedResponse<T> => {
  const start = page * size;
  const end = start + size;
  const content = items.slice(start, end);
  
  return {
    content,
    totalElements: items.length,
    totalPages: Math.ceil(items.length / size),
    size,
    number: page,
    first: page === 0,
    last: page >= Math.ceil(items.length / size) - 1,
    numberOfElements: content.length,
    empty: content.length === 0
  };
};

export const getTenants = async (page = 0, size = 20): Promise<PaginatedResponse<Tenant>> => {
  await delay(300); // Simulate API delay
  return paginate(mockTenants, page, size);
};

export const getTenantById = async (id: string): Promise<Tenant> => {
  await delay(200);
  const tenant = mockTenants.find(t => t.id === id);
  if (!tenant) {
    throw new Error(`Tenant with id ${id} not found`);
  }
  return tenant;
};

export const getTenantByDomain = async (domain: string): Promise<Tenant> => {
  await delay(200);
  const tenant = mockTenants.find(t => t.domain === domain);
  if (!tenant) {
    throw new Error(`Tenant with domain ${domain} not found`);
  }
  return tenant;
};

export const searchTenants = async (query: string, page = 0, size = 20): Promise<PaginatedResponse<Tenant>> => {
  await delay(400);
  const filtered = filterTenants(mockTenants, query);
  return paginate(filtered, page, size);
};

export const createTenant = async (tenantData: TenantCreateRequest): Promise<Tenant> => {
  await delay(500);
  
  const newTenant: Tenant = {
    id: `new-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
    name: tenantData.name,
    displayName: tenantData.displayName || tenantData.name,
    domain: tenantData.domain,
    description: tenantData.description || '',
    status: 'TRIAL',
    plan: tenantData.plan || 'BASIC',
    contactEmail: tenantData.contactEmail,
    contactPhone: tenantData.contactPhone || '',
    address: tenantData.address || '',
    logoUrl: null,
    primaryColor: '#3B82F6',
    secondaryColor: '#1E40AF',
    maxUsers: tenantData.plan === 'ENTERPRISE' ? 500 : tenantData.plan === 'PREMIUM' ? 50 : 10,
    maxTickets: tenantData.plan === 'ENTERPRISE' ? 10000 : tenantData.plan === 'PREMIUM' ? 1000 : 100,
    storageQuotaBytes: tenantData.plan === 'ENTERPRISE' ? 10737418240 : tenantData.plan === 'PREMIUM' ? 1073741824 : 104857600,
    settings: {
      timezone: 'UTC',
      dateFormat: 'YYYY-MM-DD',
      language: 'en',
      currency: 'USD',
      allowSelfRegistration: 'true',
      requireEmailVerification: 'true',
      sessionTimeout: '3600'
    },
    enabledFeatures: tenantData.plan === 'ENTERPRISE' 
      ? ['TICKETS', 'USERS', 'ANALYTICS', 'API_ACCESS', 'CUSTOM_BRANDING', 'SSO', 'ADVANCED_REPORTING']
      : tenantData.plan === 'PREMIUM'
      ? ['TICKETS', 'USERS', 'ANALYTICS', 'API_ACCESS', 'CUSTOM_BRANDING']
      : ['TICKETS', 'USERS'],
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    isActive: true,
    isTrial: true,
    isSuspended: false,
    isTrialExpired: false,
    isSubscriptionExpired: false,
    trialExpiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(), // 30 days from now
    daysUntilTrialExpires: 30
  };
  
  // Add to mock data (in real app, this would be handled by the backend)
  mockTenants.unshift(newTenant);
  
  return newTenant;
};

export const updateTenant = async (id: string, tenantData: TenantUpdateRequest): Promise<Tenant> => {
  await delay(400);
  
  const tenantIndex = mockTenants.findIndex(t => t.id === id);
  if (tenantIndex === -1) {
    throw new Error(`Tenant with id ${id} not found`);
  }
  
  const updatedTenant = {
    ...mockTenants[tenantIndex],
    ...tenantData,
    updatedAt: new Date().toISOString()
  };
  
  mockTenants[tenantIndex] = updatedTenant;
  return updatedTenant;
};

export const updateTenantStatus = async (id: string, status: string): Promise<Tenant> => {
  await delay(300);
  
  const tenantIndex = mockTenants.findIndex(t => t.id === id);
  if (tenantIndex === -1) {
    throw new Error(`Tenant with id ${id} not found`);
  }
  
  const updatedTenant = {
    ...mockTenants[tenantIndex],
    status: status as any,
    isActive: status === 'ACTIVE' || status === 'TRIAL',
    isSuspended: status === 'SUSPENDED',
    updatedAt: new Date().toISOString()
  };
  
  mockTenants[tenantIndex] = updatedTenant;
  return updatedTenant;
};

export const deleteTenant = async (id: string): Promise<void> => {
  await delay(300);
  
  const tenantIndex = mockTenants.findIndex(t => t.id === id);
  if (tenantIndex === -1) {
    throw new Error(`Tenant with id ${id} not found`);
  }
  
  mockTenants.splice(tenantIndex, 1);
};

export const getTenantStats = async () => {
  await delay(250);
  
  const totalTenants = mockTenants.length;
  const activeTenants = mockTenants.filter(t => t.isActive).length;
  const trialTenants = mockTenants.filter(t => t.isTrial).length;
  const suspendedTenants = mockTenants.filter(t => t.isSuspended).length;
  const expiredTrials = mockTenants.filter(t => t.isTrialExpired).length;
  const expiredSubscriptions = mockTenants.filter(t => t.isSubscriptionExpired).length;
  
  return {
    totalTenants,
    activeTenants,
    trialTenants,
    suspendedTenants,
    expiredTrials,
    expiredSubscriptions,
    timestamp: new Date().toISOString()
  };
};