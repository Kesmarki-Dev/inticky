import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User, LoginRequest, LoginResponse } from '../types/api';
import { authService } from '../services/auth';
import { authStateUtils, permissionUtils, Permission, Role } from '../utils/auth';

interface AuthState {
  // State
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  validateToken: () => Promise<boolean>;
  refreshToken: () => Promise<void>;
  clearError: () => void;
  
  // Getters
  getUserRoles: () => string[];
  getUserPermissions: () => Permission[];
  hasPermission: (permission: Permission) => boolean;
  hasAnyPermission: (permissions: Permission[]) => boolean;
  hasRole: (role: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
  isSystemAdmin: () => boolean;
  isTenantAdmin: () => boolean;
  isAgent: () => boolean;
  canAccessAdmin: () => boolean;
  getTenantId: () => string | null;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      // Actions
      login: async (credentials: LoginRequest) => {
        set({ isLoading: true, error: null });
        
        try {
          const response: LoginResponse = await authService.login(credentials);
          
          // Save to localStorage
          localStorage.setItem('jwt_token', response.token);
          localStorage.setItem('user_data', JSON.stringify(response.user));
          
          set({
            user: response.user,
            token: response.token,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
        } catch (error: any) {
          const errorMessage = error.response?.data?.message || 'Login failed';
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
            error: errorMessage,
          });
          throw error;
        }
      },

      logout: () => {
        authService.logout();
        set({
          user: null,
          token: null,
          isAuthenticated: false,
          isLoading: false,
          error: null,
        });
      },

      validateToken: async (): Promise<boolean> => {
        const { token } = get();
        
        if (!token) {
          set({ isAuthenticated: false });
          return false;
        }

        try {
          const result = await authService.validateToken();
          
          if (result.valid) {
            set({ isAuthenticated: true, error: null });
            return true;
          } else {
            get().logout();
            return false;
          }
        } catch (error) {
          get().logout();
          return false;
        }
      },

      refreshToken: async () => {
        set({ isLoading: true });
        
        try {
          const response = await authService.refreshToken();
          
          set({
            user: response.user,
            token: response.token,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
        } catch (error: any) {
          const errorMessage = error.response?.data?.message || 'Token refresh failed';
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
            error: errorMessage,
          });
          throw error;
        }
      },

      clearError: () => {
        set({ error: null });
      },

      // Getters
      getUserRoles: (): string[] => {
        const { user } = get();
        return user?.roles?.map(role => role.name) || [];
      },

      getUserPermissions: (): Permission[] => {
        const roles = get().getUserRoles();
        return permissionUtils.getUserPermissions(roles);
      },

      hasPermission: (permission: Permission): boolean => {
        const roles = get().getUserRoles();
        return permissionUtils.hasPermission(roles, permission);
      },

      hasAnyPermission: (permissions: Permission[]): boolean => {
        const roles = get().getUserRoles();
        return permissionUtils.hasAnyPermission(roles, permissions);
      },

      hasRole: (role: string): boolean => {
        const roles = get().getUserRoles();
        return roles.includes(role);
      },

      hasAnyRole: (roles: string[]): boolean => {
        const userRoles = get().getUserRoles();
        return roles.some(role => userRoles.includes(role));
      },

      isSystemAdmin: (): boolean => {
        const roles = get().getUserRoles();
        return permissionUtils.isSystemAdmin(roles);
      },

      isTenantAdmin: (): boolean => {
        const roles = get().getUserRoles();
        return permissionUtils.isTenantAdmin(roles);
      },

      isAgent: (): boolean => {
        const roles = get().getUserRoles();
        return permissionUtils.isAgent(roles);
      },

      canAccessAdmin: (): boolean => {
        const roles = get().getUserRoles();
        return permissionUtils.canAccessAdmin(roles);
      },

      getTenantId: (): string | null => {
        const { user } = get();
        return user?.tenantId || null;
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

// Initialize auth state from localStorage on app start
export const initializeAuth = async () => {
  const authStore = useAuthStore.getState();
  
  // Check if we have stored auth data
  if (authStore.token && authStore.user) {
    // Validate the stored token
    const isValid = await authStore.validateToken();
    
    if (!isValid) {
      authStore.logout();
    }
  } else {
    // Clear any invalid stored data
    authStore.logout();
  }
};

// Auth hook for components
export const useAuth = () => {
  const authStore = useAuthStore();
  
  return {
    // State
    user: authStore.user,
    token: authStore.token,
    isAuthenticated: authStore.isAuthenticated,
    isLoading: authStore.isLoading,
    error: authStore.error,
    
    // Actions
    login: authStore.login,
    logout: authStore.logout,
    validateToken: authStore.validateToken,
    refreshToken: authStore.refreshToken,
    clearError: authStore.clearError,
    
    // Permission checks
    getUserRoles: authStore.getUserRoles,
    getUserPermissions: authStore.getUserPermissions,
    hasPermission: authStore.hasPermission,
    hasAnyPermission: authStore.hasAnyPermission,
    hasRole: authStore.hasRole,
    hasAnyRole: authStore.hasAnyRole,
    isSystemAdmin: authStore.isSystemAdmin,
    isTenantAdmin: authStore.isTenantAdmin,
    isAgent: authStore.isAgent,
    canAccessAdmin: authStore.canAccessAdmin,
    getTenantId: authStore.getTenantId,
  };
};
