// Base types
export interface ApiResponse<T> {
  data?: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// User types
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  status: UserStatus;
  tenantId: string;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
}

export interface UserResponse extends User {}

export interface Role {
  id: string;
  name: string;
  description?: string;
  tenantId: string;
  permissions: Permission[];
  createdAt: string;
  updatedAt: string;
}

export interface Permission {
  id: string;
  name: string;
  description: string;
  resource?: string;
  action?: string;
  createdAt: string;
  updatedAt: string;
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED'
}

export interface UserCreateRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  roleIds?: string[];
}

export interface UserUpdateRequest {
  email?: string;
  firstName?: string;
  lastName?: string;
  status?: UserStatus;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
  expiresAt: string;
}

// Ticket types
export interface Ticket {
  id: string;
  title: string;
  description: string;
  status: TicketStatus;
  priority: Priority;
  category: Category;
  reporterId: string;
  assigneeId?: string;
  dueDate?: string;
  tenantId: string;
  createdAt: string;
  updatedAt: string;
  comments: Comment[];
  attachments: Attachment[];
  reporter?: User;
  assignee?: User;
}

export enum TicketStatus {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  WAITING_FOR_CUSTOMER = 'WAITING_FOR_CUSTOMER',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED'
}

export enum Priority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  URGENT = 'URGENT'
}

export enum Category {
  TECHNICAL = 'TECHNICAL',
  BILLING = 'BILLING',
  GENERAL = 'GENERAL',
  FEATURE_REQUEST = 'FEATURE_REQUEST',
  BUG_REPORT = 'BUG_REPORT'
}

export interface TicketCreateRequest {
  title: string;
  description: string;
  priority: Priority;
  category: Category;
  dueDate?: string;
  assigneeId?: string;
}

export interface Comment {
  id: string;
  content: string;
  authorId: string;
  ticketId: string;
  type: CommentType;
  tenantId: string;
  createdAt: string;
  updatedAt: string;
  author?: User;
}

export enum CommentType {
  PUBLIC = 'PUBLIC',
  INTERNAL = 'INTERNAL'
}

export interface CommentCreateRequest {
  content: string;
  type: CommentType;
}

export interface Attachment {
  id: string;
  fileName: string;
  fileSize: number;
  contentType: string;
  uploadedBy: string;
  ticketId: string;
  tenantId: string;
  createdAt: string;
}

// Tenant types
export interface Tenant {
  id: string;
  name: string;
  domain?: string;
  displayName?: string;
  description?: string;
  status: TenantStatus;
  plan: TenantPlan;
  contactEmail?: string;
  contactPhone?: string;
  address?: string;
  logoUrl?: string;
  primaryColor?: string;
  secondaryColor?: string;
  trialExpiresAt?: string;
  subscriptionExpiresAt?: string;
  maxUsers?: number;
  maxTickets?: number;
  storageQuotaBytes?: number;
  settings: Record<string, string>;
  enabledFeatures: string[];
  createdAt: string;
  updatedAt: string;
  isActive: boolean;
  isTrial: boolean;
  isSuspended: boolean;
  isTrialExpired: boolean;
  isSubscriptionExpired: boolean;
  daysUntilTrialExpires?: number;
  daysUntilSubscriptionExpires?: number;
}

export enum TenantStatus {
  TRIAL = 'TRIAL',
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED',
  CANCELLED = 'CANCELLED',
  EXPIRED = 'EXPIRED'
}

export enum TenantPlan {
  BASIC = 'BASIC',
  PREMIUM = 'PREMIUM',
  ENTERPRISE = 'ENTERPRISE',
  CUSTOM = 'CUSTOM'
}

export interface TenantCreateRequest {
  name: string;
  domain?: string;
  displayName?: string;
  description?: string;
  plan: TenantPlan;
  contactEmail?: string;
  contactPhone?: string;
  address?: string;
  logoUrl?: string;
  primaryColor?: string;
  secondaryColor?: string;
  trialExpiresAt?: string;
  maxUsers?: number;
  maxTickets?: number;
  storageQuotaBytes?: number;
  settings?: Record<string, string>;
  enabledFeatures?: string[];
}

export interface TenantUpdateRequest {
  displayName?: string;
  description?: string;
  status?: TenantStatus;
  plan?: TenantPlan;
  contactEmail?: string;
  contactPhone?: string;
  address?: string;
  logoUrl?: string;
  primaryColor?: string;
  secondaryColor?: string;
  trialExpiresAt?: string;
  subscriptionExpiresAt?: string;
  maxUsers?: number;
  maxTickets?: number;
  storageQuotaBytes?: number;
  settings?: Record<string, string>;
  enabledFeatures?: string[];
}

// Statistics and Analytics types
export interface TicketStats {
  totalTickets: number;
  openTickets: number;
  inProgressTickets: number;
  resolvedTickets: number;
  closedTickets: number;
  averageResolutionTime: number;
  ticketsByPriority: Record<Priority, number>;
  ticketsByCategory: Record<Category, number>;
}

export interface TenantStats {
  totalTenants: number;
  activeTenants: number;
  trialTenants: number;
  suspendedTenants: number;
  expiredTrials: number;
  expiredSubscriptions: number;
}

// Filter and search types
export interface TicketFilters {
  status?: TicketStatus[];
  priority?: Priority[];
  category?: Category[];
  assigneeId?: string;
  reporterId?: string;
  search?: string;
  dateFrom?: string;
  dateTo?: string;
}

export interface UserFilters {
  status?: UserStatus[];
  roleId?: string;
  search?: string;
}

export interface TenantFilters {
  status?: TenantStatus[];
  plan?: TenantPlan[];
  search?: string;
}

// Pagination
export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'asc' | 'desc';
}
