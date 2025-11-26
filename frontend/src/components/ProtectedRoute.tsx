import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth, useRoleAccess } from '../hooks/useAuth';
import { Permission, Role } from '../utils/auth';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAuth?: boolean;
  requiredRoles?: string[];
  requiredPermissions?: Permission[];
  requireAnyRole?: boolean; // If true, user needs ANY of the roles, if false, user needs ALL roles
  requireAnyPermission?: boolean; // If true, user needs ANY of the permissions, if false, user needs ALL permissions
  fallbackPath?: string;
  unauthorizedComponent?: React.ComponentType;
}

const UnauthorizedComponent: React.FC = () => (
  <div className="min-h-screen flex items-center justify-center bg-gray-50">
    <div className="max-w-md w-full bg-white shadow-lg rounded-lg p-6 text-center">
      <div className="w-16 h-16 mx-auto mb-4 bg-red-100 rounded-full flex items-center justify-center">
        <svg
          className="w-8 h-8 text-red-600"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"
          />
        </svg>
      </div>
      <h2 className="text-xl font-semibold text-gray-900 mb-2">Access Denied</h2>
      <p className="text-gray-600 mb-4">
        You don't have permission to access this page.
      </p>
      <button
        onClick={() => window.history.back()}
        className="btn btn-primary"
      >
        Go Back
      </button>
    </div>
  </div>
);

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requireAuth = true,
  requiredRoles = [],
  requiredPermissions = [],
  requireAnyRole = false,
  requireAnyPermission = false,
  fallbackPath = '/login',
  unauthorizedComponent: UnauthorizedComponentProp = UnauthorizedComponent,
}) => {
  const { isAuthenticated, isLoading } = useAuth();
  const { hasRole, hasAnyRole, hasPermission, hasAnyPermission } = useRoleAccess();
  const location = useLocation();

  // Show loading state while checking authentication
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  // Check authentication requirement
  if (requireAuth && !isAuthenticated) {
    return <Navigate to={fallbackPath} state={{ from: location }} replace />;
  }

  // Check role requirements
  if (requiredRoles.length > 0) {
    const hasRequiredRoles = requireAnyRole
      ? hasAnyRole(requiredRoles)
      : requiredRoles.every(role => hasRole(role));

    if (!hasRequiredRoles) {
      return <UnauthorizedComponentProp />;
    }
  }

  // Check permission requirements
  if (requiredPermissions.length > 0) {
    const hasRequiredPermissions = requireAnyPermission
      ? hasAnyPermission(requiredPermissions)
      : requiredPermissions.every(permission => hasPermission(permission));

    if (!hasRequiredPermissions) {
      return <UnauthorizedComponentProp />;
    }
  }

  return <>{children}</>;
};

// Convenience components for common protection patterns
export const AdminRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <ProtectedRoute requiredRoles={['SYSTEM_ADMIN', 'TENANT_ADMIN']} requireAnyRole>
    {children}
  </ProtectedRoute>
);

export const SystemAdminRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <ProtectedRoute requiredRoles={['SYSTEM_ADMIN']}>
    {children}
  </ProtectedRoute>
);

export const TenantAdminRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <ProtectedRoute requiredRoles={['SYSTEM_ADMIN', 'TENANT_ADMIN']} requireAnyRole>
    {children}
  </ProtectedRoute>
);

export const AgentRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <ProtectedRoute requiredRoles={['SYSTEM_ADMIN', 'TENANT_ADMIN', 'AGENT']} requireAnyRole>
    {children}
  </ProtectedRoute>
);

// Higher-order component for role-based access
export const withRoleAccess = <P extends object>(
  Component: React.ComponentType<P>,
  requiredRoles: string[],
  requireAnyRole = false
) => {
  return (props: P) => (
    <ProtectedRoute requiredRoles={requiredRoles} requireAnyRole={requireAnyRole}>
      <Component {...props} />
    </ProtectedRoute>
  );
};

// Higher-order component for permission-based access
export const withPermissionAccess = <P extends object>(
  Component: React.ComponentType<P>,
  requiredPermissions: Permission[],
  requireAnyPermission = false
) => {
  return (props: P) => (
    <ProtectedRoute 
      requiredPermissions={requiredPermissions} 
      requireAnyPermission={requireAnyPermission}
    >
      <Component {...props} />
    </ProtectedRoute>
  );
};

export default ProtectedRoute;
