import React from 'react';
import { useAuth } from '../hooks/useAuth';
import SystemDashboard from './admin/SystemDashboard';
import TenantDashboard from './tenant-admin/TenantDashboard';
import AgentDashboard from './agent/AgentDashboard';
import UserDashboard from './user/UserDashboard';

const Dashboard: React.FC = () => {
  const { isSystemAdmin, isTenantAdmin, isAgent } = useAuth();

  // Route to appropriate dashboard based on user role
  if (isSystemAdmin()) {
    return <SystemDashboard />;
  }
  
  if (isTenantAdmin()) {
    return <TenantDashboard />;
  }
  
  if (isAgent()) {
    return <AgentDashboard />;
  }
  
  // Default to user dashboard
  return <UserDashboard />;
};

export default Dashboard;
