import { jwtDecode } from 'jwt-decode';

// JWT token interface
interface JwtPayload {
  sub: string; // subject (user email)
  tenantId: string;
  roles: string[];
  permissions: string[];
  exp: number; // expiration time
  iat: number; // issued at
}

// Token utilities
export const tokenUtils = {
  // Decode JWT token
  decodeToken: (token: string): JwtPayload | null => {
    try {
      return jwtDecode<JwtPayload>(token);
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  },

  // Check if token is expired
  isTokenExpired: (token: string): boolean => {
    const decoded = tokenUtils.decodeToken(token);
    if (!decoded) return true;
    
    const currentTime = Date.now() / 1000;
    return decoded.exp < currentTime;
  },

  // Get token expiration date
  getTokenExpiration: (token: string): Date | null => {
    const decoded = tokenUtils.decodeToken(token);
    if (!decoded) return null;
    
    return new Date(decoded.exp * 1000);
  },

  // Get time until token expires (in minutes)
  getTimeUntilExpiration: (token: string): number => {
    const decoded = tokenUtils.decodeToken(token);
    if (!decoded) return 0;
    
    const currentTime = Date.now() / 1000;
    const timeLeft = decoded.exp - currentTime;
    return Math.max(0, Math.floor(timeLeft / 60));
  },

  // Extract user email from token
  getEmailFromToken: (token: string): string | null => {
    const decoded = tokenUtils.decodeToken(token);
    return decoded?.sub || null;
  },

  // Extract tenant ID from token
  getTenantIdFromToken: (token: string): string | null => {
    const decoded = tokenUtils.decodeToken(token);
    return decoded?.tenantId || null;
  },

  // Extract roles from token
  getRolesFromToken: (token: string): string[] => {
    const decoded = tokenUtils.decodeToken(token);
    return decoded?.roles || [];
  },

  // Extract permissions from token
  getPermissionsFromToken: (token: string): string[] => {
    const decoded = tokenUtils.decodeToken(token);
    return decoded?.permissions || [];
  },
};

// Role definitions
export const ROLES = {
  SYSTEM_ADMIN: 'SYSTEM_ADMIN',
  TENANT_ADMIN: 'TENANT_ADMIN',
  AGENT: 'AGENT',
  USER: 'USER',
} as const;

export type Role = typeof ROLES[keyof typeof ROLES];

// Permission definitions
export const PERMISSIONS = {
  // Tenant permissions
  TENANT_CREATE: 'TENANT_CREATE',
  TENANT_READ: 'TENANT_READ',
  TENANT_UPDATE: 'TENANT_UPDATE',
  TENANT_DELETE: 'TENANT_DELETE',
  
  // User permissions
  USER_CREATE: 'USER_CREATE',
  USER_READ: 'USER_READ',
  USER_UPDATE: 'USER_UPDATE',
  USER_DELETE: 'USER_DELETE',
  
  // Ticket permissions
  TICKET_CREATE: 'TICKET_CREATE',
  TICKET_READ: 'TICKET_READ',
  TICKET_UPDATE: 'TICKET_UPDATE',
  TICKET_DELETE: 'TICKET_DELETE',
  TICKET_ASSIGN: 'TICKET_ASSIGN',
  
  // Comment permissions
  COMMENT_CREATE: 'COMMENT_CREATE',
  COMMENT_READ: 'COMMENT_READ',
  COMMENT_UPDATE: 'COMMENT_UPDATE',
  COMMENT_DELETE: 'COMMENT_DELETE',
  
  // Admin permissions
  SYSTEM_ADMIN: 'SYSTEM_ADMIN',
  TENANT_ADMIN: 'TENANT_ADMIN',
} as const;

export type Permission = typeof PERMISSIONS[keyof typeof PERMISSIONS];

// Role hierarchy and permissions mapping
export const ROLE_PERMISSIONS: Record<Role, Permission[]> = {
  [ROLES.SYSTEM_ADMIN]: [
    PERMISSIONS.SYSTEM_ADMIN,
    PERMISSIONS.TENANT_CREATE,
    PERMISSIONS.TENANT_READ,
    PERMISSIONS.TENANT_UPDATE,
    PERMISSIONS.TENANT_DELETE,
    PERMISSIONS.USER_CREATE,
    PERMISSIONS.USER_READ,
    PERMISSIONS.USER_UPDATE,
    PERMISSIONS.USER_DELETE,
    PERMISSIONS.TICKET_CREATE,
    PERMISSIONS.TICKET_READ,
    PERMISSIONS.TICKET_UPDATE,
    PERMISSIONS.TICKET_DELETE,
    PERMISSIONS.TICKET_ASSIGN,
    PERMISSIONS.COMMENT_CREATE,
    PERMISSIONS.COMMENT_READ,
    PERMISSIONS.COMMENT_UPDATE,
    PERMISSIONS.COMMENT_DELETE,
  ],
  [ROLES.TENANT_ADMIN]: [
    PERMISSIONS.TENANT_ADMIN,
    PERMISSIONS.TENANT_READ,
    PERMISSIONS.TENANT_UPDATE,
    PERMISSIONS.USER_CREATE,
    PERMISSIONS.USER_READ,
    PERMISSIONS.USER_UPDATE,
    PERMISSIONS.USER_DELETE,
    PERMISSIONS.TICKET_CREATE,
    PERMISSIONS.TICKET_READ,
    PERMISSIONS.TICKET_UPDATE,
    PERMISSIONS.TICKET_DELETE,
    PERMISSIONS.TICKET_ASSIGN,
    PERMISSIONS.COMMENT_CREATE,
    PERMISSIONS.COMMENT_READ,
    PERMISSIONS.COMMENT_UPDATE,
    PERMISSIONS.COMMENT_DELETE,
  ],
  [ROLES.AGENT]: [
    PERMISSIONS.TICKET_CREATE,
    PERMISSIONS.TICKET_READ,
    PERMISSIONS.TICKET_UPDATE,
    PERMISSIONS.TICKET_ASSIGN,
    PERMISSIONS.COMMENT_CREATE,
    PERMISSIONS.COMMENT_READ,
    PERMISSIONS.COMMENT_UPDATE,
    PERMISSIONS.USER_READ,
  ],
  [ROLES.USER]: [
    PERMISSIONS.TICKET_CREATE,
    PERMISSIONS.TICKET_READ,
    PERMISSIONS.COMMENT_CREATE,
    PERMISSIONS.COMMENT_READ,
  ],
};

// Permission checking utilities
export const permissionUtils = {
  // Check if user has specific permission
  hasPermission: (userRoles: string[], permission: Permission): boolean => {
    return userRoles.some(role => {
      const rolePermissions = ROLE_PERMISSIONS[role as Role];
      return rolePermissions?.includes(permission) || false;
    });
  },

  // Check if user has any of the specified permissions
  hasAnyPermission: (userRoles: string[], permissions: Permission[]): boolean => {
    return permissions.some(permission => 
      permissionUtils.hasPermission(userRoles, permission)
    );
  },

  // Check if user has all specified permissions
  hasAllPermissions: (userRoles: string[], permissions: Permission[]): boolean => {
    return permissions.every(permission => 
      permissionUtils.hasPermission(userRoles, permission)
    );
  },

  // Get all permissions for user roles
  getUserPermissions: (userRoles: string[]): Permission[] => {
    const permissions = new Set<Permission>();
    
    userRoles.forEach(role => {
      const rolePermissions = ROLE_PERMISSIONS[role as Role];
      if (rolePermissions) {
        rolePermissions.forEach(permission => permissions.add(permission));
      }
    });
    
    return Array.from(permissions);
  },

  // Check if user is system admin
  isSystemAdmin: (userRoles: string[]): boolean => {
    return userRoles.includes(ROLES.SYSTEM_ADMIN);
  },

  // Check if user is tenant admin
  isTenantAdmin: (userRoles: string[]): boolean => {
    return userRoles.includes(ROLES.TENANT_ADMIN) || 
           permissionUtils.isSystemAdmin(userRoles);
  },

  // Check if user is agent
  isAgent: (userRoles: string[]): boolean => {
    return userRoles.includes(ROLES.AGENT) || 
           permissionUtils.isTenantAdmin(userRoles);
  },

  // Check if user can access admin features
  canAccessAdmin: (userRoles: string[]): boolean => {
    return permissionUtils.isSystemAdmin(userRoles) || 
           permissionUtils.isTenantAdmin(userRoles);
  },
};

// Local storage keys
export const STORAGE_KEYS = {
  AUTH_TOKEN: 'auth_token',
  USER: 'user',
  TENANT_ID: 'tenant_id',
  REFRESH_TOKEN: 'refresh_token',
} as const;

// Auth state utilities
export const authStateUtils = {
  // Clear all auth data
  clearAuthData: (): void => {
    Object.values(STORAGE_KEYS).forEach(key => {
      localStorage.removeItem(key);
    });
  },

  // Check if user is authenticated
  isAuthenticated: (): boolean => {
    const token = localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
    if (!token) return false;
    
    return !tokenUtils.isTokenExpired(token);
  },

  // Get current user roles
  getCurrentUserRoles: (): string[] => {
    const token = localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
    if (!token) return [];
    
    return tokenUtils.getRolesFromToken(token);
  },

  // Get current user permissions
  getCurrentUserPermissions: (): Permission[] => {
    const roles = authStateUtils.getCurrentUserRoles();
    return permissionUtils.getUserPermissions(roles);
  },

  // Check if current user has permission
  currentUserHasPermission: (permission: Permission): boolean => {
    const roles = authStateUtils.getCurrentUserRoles();
    return permissionUtils.hasPermission(roles, permission);
  },

  // Get current tenant ID
  getCurrentTenantId: (): string | null => {
    return localStorage.getItem(STORAGE_KEYS.TENANT_ID);
  },
};
