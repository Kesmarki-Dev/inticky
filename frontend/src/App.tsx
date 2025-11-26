import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Header } from './components/Layout/Header';
import { Sidebar } from './components/Layout/Sidebar';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import TicketList from './pages/tickets/TicketList';
import Users from './pages/Users';
import Tenants from './pages/Tenants';
import { useAuth, useTokenValidation } from './hooks/useAuth';
import { initializeAuth } from './store/authStore';

function App() {
  const { isAuthenticated, isLoading } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [appInitialized, setAppInitialized] = useState(false);

  // Initialize auth and token validation
  useTokenValidation();

  useEffect(() => {
    const init = async () => {
      await initializeAuth();
      setAppInitialized(true);
    };
    init();
  }, []);

  // Show loading screen while initializing
  if (!appInitialized || isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<Login />} />
          
          {/* Protected routes */}
          <Route
            path="/*"
            element={
              <ProtectedRoute>
                <div className="flex h-screen">
                  <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
                  
                  <div className="flex-1 flex flex-col lg:pl-64">
                    <Header 
                      onMenuClick={() => setSidebarOpen(true)} 
                      showMenuButton={true}
                    />
                    
                    <main className="flex-1 overflow-y-auto">
                      <Routes>
                        <Route path="/" element={<Navigate to="/dashboard" replace />} />
                        <Route path="/dashboard" element={<Dashboard />} />
                        
                        {/* Ticket routes */}
                        <Route path="/tickets" element={<TicketList />} />
                        <Route path="/my-tickets" element={<div className="p-6">My Tickets page coming soon...</div>} />
                        <Route path="/ai-chat" element={<div className="p-6">AI Chat page coming soon...</div>} />
                        <Route path="/admin/users" element={<Users />} />
                        <Route path="/admin/tenants" element={<Tenants />} />
                        <Route path="/analytics" element={<div className="p-6">Analytics page coming soon...</div>} />
                        <Route path="/notifications" element={<div className="p-6">Notifications page coming soon...</div>} />
                        <Route path="/settings" element={<div className="p-6">Settings page coming soon...</div>} />
                        
                        {/* Catch all route */}
                        <Route path="*" element={<Navigate to="/dashboard" replace />} />
                      </Routes>
                    </main>
                  </div>
                </div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
