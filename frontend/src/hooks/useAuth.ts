import { useEffect } from 'react';
import { useAuthStore } from '../store/authStore';
import { tokenUtils } from '../utils/auth';

// Re-export the auth hook from store for convenience
export { useAuth } from '../store/authStore';

// Hook for automatic token validation and refresh
export const useTokenValidation = () => {
  const { token, validateToken, refreshToken, logout } = useAuthStore();

  useEffect(() => {
    if (!token) return;

    // Check if token is expired
    if (tokenUtils.isTokenExpired(token)) {
      // Try to refresh token, if that fails, logout
      refreshToken().catch(() => {
        logout();
      });
      return;
    }

    // Set up token validation interval
    const validateInterval = setInterval(async () => {
      const isValid = await validateToken();
      if (!isValid) {
        clearInterval(validateInterval);
      }
    }, 5 * 60 * 1000); // Validate every 5 minutes

    // Set up token refresh interval (refresh 5 minutes before expiry)
    const timeUntilExpiry = tokenUtils.getTimeUntilExpiration(token);
    const refreshTime = Math.max(0, (timeUntilExpiry - 5) * 60 * 1000);
    
    const refreshTimeout = setTimeout(async () => {
      try {
        await refreshToken();
      } catch (error) {
        logout();
      }
    }, refreshTime);

    return () => {
      clearInterval(validateInterval);
      clearTimeout(refreshTimeout);
    };
  }, [token, validateToken, refreshToken, logout]);
};

// Hook for role-based component rendering
export const useRoleAccess = () => {
  const {
    hasRole,
    hasAnyRole,
    hasPermission,
    hasAnyPermission,
    isSystemAdmin,
    isTenantAdmin,
    isAgent,
    canAccessAdmin,
  } = useAuthStore();

  return {
    hasRole,
    hasAnyRole,
    hasPermission,
    hasAnyPermission,
    isSystemAdmin,
    isTenantAdmin,
    isAgent,
    canAccessAdmin,
  };
};

// Hook for tenant context
export const useTenantContext = () => {
  const { user, getTenantId } = useAuthStore();

  return {
    tenantId: getTenantId(),
    tenantName: user?.tenantId, // This would need to be enhanced to get actual tenant name
    user,
  };
};
