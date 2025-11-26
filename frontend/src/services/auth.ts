import { apiService } from './api';
import type { LoginRequest, LoginResponse, User } from '../types/api';

export const authService = {
  // Login user
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await apiService.post<LoginResponse>('/auth/login', credentials);
    
    // Store token and user data
    if (response.token) {
      localStorage.setItem('jwt_token', response.token);
      localStorage.setItem('user_data', JSON.stringify(response.user));
      localStorage.setItem('tenant_id', response.user.tenantId);
    }
    
    return response;
  },

  // Logout user
  logout: (): void => {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_data');
    localStorage.removeItem('tenant_id');
  },

  // Validate token
  validateToken: async (): Promise<{ valid: boolean; email?: string; tenantId?: string }> => {
    try {
      const token = localStorage.getItem('jwt_token');
      if (!token) {
        return { valid: false };
      }
      
      // The token is automatically added by the axios interceptor in Authorization header
      const response = await apiService.post<{ valid: boolean; email: string; tenantId: string }>('/auth/validate');
      return response;
    } catch (error) {
      return { valid: false };
    }
  },

  // Get current user info from token
  getCurrentUser: async (): Promise<{ email: string; tenantId: string; tokenValid: boolean }> => {
    const response = await apiService.get<{ email: string; tenantId: string; tokenValid: boolean }>('/auth/me');
    return response;
  },

  // Get stored user
  getStoredUser: (): User | null => {
    const userStr = localStorage.getItem('user_data');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch (error) {
        console.error('Error parsing stored user:', error);
        return null;
      }
    }
    return null;
  },

  // Get stored token
  getStoredToken: (): string | null => {
    return localStorage.getItem('jwt_token');
  },

  // Check if user is authenticated
  isAuthenticated: (): boolean => {
    const token = localStorage.getItem('jwt_token');
    const user = localStorage.getItem('user_data');
    return !!(token && user);
  },

  // Get user roles
  getUserRoles: (): string[] => {
    const user = authService.getStoredUser();
    return user?.roles?.map(role => role.name) || [];
  },

  // Check if user has specific role
  hasRole: (roleName: string): boolean => {
    const roles = authService.getUserRoles();
    return roles.includes(roleName);
  },

  // Check if user has any of the specified roles
  hasAnyRole: (roleNames: string[]): boolean => {
    const roles = authService.getUserRoles();
    return roleNames.some(role => roles.includes(role));
  },

  // Get user permissions
  getUserPermissions: (): string[] => {
    const user = authService.getStoredUser();
    const permissions: string[] = [];
    
    user?.roles?.forEach(role => {
      role.permissions?.forEach(permission => {
        if (!permissions.includes(permission.name)) {
          permissions.push(permission.name);
        }
      });
    });
    
    return permissions;
  },

  // Check if user has specific permission
  hasPermission: (permissionName: string): boolean => {
    const permissions = authService.getUserPermissions();
    return permissions.includes(permissionName);
  },

  // Get tenant ID
  getTenantId: (): string | null => {
    return localStorage.getItem('tenant_id');
  },

  // Refresh token (if backend supports it)
  refreshToken: async (): Promise<LoginResponse> => {
    const response = await apiService.post<LoginResponse>('/auth/refresh');
    
    if (response.token) {
      localStorage.setItem('jwt_token', response.token);
      localStorage.setItem('user_data', JSON.stringify(response.user));
    }
    
    return response;
  },
};
