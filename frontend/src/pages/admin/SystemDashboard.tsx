import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { 
  BuildingOfficeIcon, 
  UserGroupIcon, 
  TicketIcon,
  ChartBarIcon,
  ExclamationTriangleIcon,
  ClockIcon
} from '@heroicons/react/24/outline';
import { getTenantStats } from '../../services/tenants';
import { ticketService } from '../../services/tickets';
import { TenantStats, TicketStats } from '../../types/api';

interface DashboardCard {
  title: string;
  value: string | number;
  change?: string;
  changeType?: 'increase' | 'decrease' | 'neutral';
  icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
  href?: string;
}

const SystemDashboard: React.FC = () => {
  const [tenantStats, setTenantStats] = useState<TenantStats | null>(null);
  const [ticketStats, setTicketStats] = useState<TicketStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setLoading(true);
        const [tenantData, ticketData] = await Promise.all([
          getTenantStats(),
          ticketService.getTicketStats(),
        ]);
        
        setTenantStats(tenantData);
        setTicketStats(ticketData);
      } catch (err: any) {
        setError(err.message || 'Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  if (loading) {
    return (
      <div className="p-6">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-1/4 mb-6"></div>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {[...Array(4)].map((_, i) => (
              <div key={i} className="bg-white p-6 rounded-lg shadow">
                <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                <div className="h-8 bg-gray-200 rounded w-1/2"></div>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6">
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <div className="flex">
            <ExclamationTriangleIcon className="h-5 w-5 text-red-400" />
            <div className="ml-3">
              <h3 className="text-sm font-medium text-red-800">Error loading dashboard</h3>
              <p className="mt-1 text-sm text-red-700">{error}</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const cards: DashboardCard[] = [
    {
      title: 'Total Tenants',
      value: tenantStats?.totalTenants || 0,
      icon: BuildingOfficeIcon,
      href: '/admin/tenants',
    },
    {
      title: 'Active Tenants',
      value: tenantStats?.activeTenants || 0,
      icon: BuildingOfficeIcon,
      href: '/admin/tenants?status=active',
    },
    {
      title: 'Total Tickets',
      value: ticketStats?.totalTickets || 0,
      icon: TicketIcon,
      href: '/tickets',
    },
    {
      title: 'Open Tickets',
      value: ticketStats?.openTickets || 0,
      icon: TicketIcon,
      href: '/tickets?status=open',
    },
  ];

  const alertCards = [
    {
      title: 'Expired Trials',
      value: tenantStats?.expiredTrials || 0,
      icon: ExclamationTriangleIcon,
      color: 'red',
      href: '/admin/tenants?status=expired-trials',
    },
    {
      title: 'Trial Tenants',
      value: tenantStats?.trialTenants || 0,
      icon: ClockIcon,
      color: 'yellow',
      href: '/admin/tenants?status=trial',
    },
    {
      title: 'Suspended Tenants',
      value: tenantStats?.suspendedTenants || 0,
      icon: ExclamationTriangleIcon,
      color: 'orange',
      href: '/admin/tenants?status=suspended',
    },
  ];

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="md:flex md:items-center md:justify-between">
        <div className="flex-1 min-w-0">
          <h2 className="text-2xl font-bold leading-7 text-gray-900 sm:text-3xl sm:truncate">
            System Dashboard
          </h2>
          <p className="mt-1 text-sm text-gray-500">
            Overview of all tenants and system-wide metrics
          </p>
        </div>
        <div className="mt-4 flex md:mt-0 md:ml-4">
          <Link
            to="/admin/tenants/new"
            className="ml-3 inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
          >
            Add Tenant
          </Link>
        </div>
      </div>

      {/* Main Stats */}
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {cards.map((card) => {
          const Icon = card.icon;
          return (
            <div key={card.title} className="bg-white overflow-hidden shadow rounded-lg">
              <div className="p-5">
                <div className="flex items-center">
                  <div className="flex-shrink-0">
                    <Icon className="h-6 w-6 text-gray-400" aria-hidden="true" />
                  </div>
                  <div className="ml-5 w-0 flex-1">
                    <dl>
                      <dt className="text-sm font-medium text-gray-500 truncate">
                        {card.title}
                      </dt>
                      <dd className="text-lg font-medium text-gray-900">
                        {card.value}
                      </dd>
                    </dl>
                  </div>
                </div>
              </div>
              {card.href && (
                <div className="bg-gray-50 px-5 py-3">
                  <div className="text-sm">
                    <Link
                      to={card.href}
                      className="font-medium text-primary-700 hover:text-primary-900"
                    >
                      View all
                    </Link>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Alert Cards */}
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-3">
        {alertCards.map((card) => {
          const Icon = card.icon;
          const colorClasses = {
            red: 'bg-red-50 border-red-200',
            yellow: 'bg-yellow-50 border-yellow-200',
            orange: 'bg-orange-50 border-orange-200',
          };
          
          return (
            <div
              key={card.title}
              className={`border rounded-lg p-4 ${colorClasses[card.color as keyof typeof colorClasses]}`}
            >
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <Icon 
                    className={`h-6 w-6 ${
                      card.color === 'red' ? 'text-red-400' :
                      card.color === 'yellow' ? 'text-yellow-400' :
                      'text-orange-400'
                    }`} 
                    aria-hidden="true" 
                  />
                </div>
                <div className="ml-3 flex-1">
                  <h3 className={`text-sm font-medium ${
                    card.color === 'red' ? 'text-red-800' :
                    card.color === 'yellow' ? 'text-yellow-800' :
                    'text-orange-800'
                  }`}>
                    {card.title}
                  </h3>
                  <p className={`text-2xl font-bold ${
                    card.color === 'red' ? 'text-red-900' :
                    card.color === 'yellow' ? 'text-yellow-900' :
                    'text-orange-900'
                  }`}>
                    {card.value}
                  </p>
                </div>
              </div>
              {card.href && (
                <div className="mt-3">
                  <Link
                    to={card.href}
                    className={`text-sm font-medium ${
                      card.color === 'red' ? 'text-red-700 hover:text-red-900' :
                      card.color === 'yellow' ? 'text-yellow-700 hover:text-yellow-900' :
                      'text-orange-700 hover:text-orange-900'
                    }`}
                  >
                    View details →
                  </Link>
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Quick Actions */}
      <div className="bg-white shadow rounded-lg">
        <div className="px-4 py-5 sm:p-6">
          <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4">
            Quick Actions
          </h3>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Link
              to="/admin/tenants"
              className="relative group bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-primary-500 rounded-lg border border-gray-300 hover:border-gray-400"
            >
              <div>
                <span className="rounded-lg inline-flex p-3 bg-primary-50 text-primary-700 ring-4 ring-white">
                  <BuildingOfficeIcon className="h-6 w-6" aria-hidden="true" />
                </span>
              </div>
              <div className="mt-8">
                <h3 className="text-lg font-medium">
                  <span className="absolute inset-0" aria-hidden="true" />
                  Manage Tenants
                </h3>
                <p className="mt-2 text-sm text-gray-500">
                  View, create, and manage tenant organizations
                </p>
              </div>
            </Link>

            <Link
              to="/analytics"
              className="relative group bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-primary-500 rounded-lg border border-gray-300 hover:border-gray-400"
            >
              <div>
                <span className="rounded-lg inline-flex p-3 bg-green-50 text-green-700 ring-4 ring-white">
                  <ChartBarIcon className="h-6 w-6" aria-hidden="true" />
                </span>
              </div>
              <div className="mt-8">
                <h3 className="text-lg font-medium">
                  <span className="absolute inset-0" aria-hidden="true" />
                  System Analytics
                </h3>
                <p className="mt-2 text-sm text-gray-500">
                  View system-wide performance and usage metrics
                </p>
              </div>
            </Link>

            <Link
              to="/tickets"
              className="relative group bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-primary-500 rounded-lg border border-gray-300 hover:border-gray-400"
            >
              <div>
                <span className="rounded-lg inline-flex p-3 bg-blue-50 text-blue-700 ring-4 ring-white">
                  <TicketIcon className="h-6 w-6" aria-hidden="true" />
                </span>
              </div>
              <div className="mt-8">
                <h3 className="text-lg font-medium">
                  <span className="absolute inset-0" aria-hidden="true" />
                  All Tickets
                </h3>
                <p className="mt-2 text-sm text-gray-500">
                  View and manage tickets across all tenants
                </p>
              </div>
            </Link>

            <Link
              to="/settings"
              className="relative group bg-white p-6 focus-within:ring-2 focus-within:ring-inset focus-within:ring-primary-500 rounded-lg border border-gray-300 hover:border-gray-400"
            >
              <div>
                <span className="rounded-lg inline-flex p-3 bg-purple-50 text-purple-700 ring-4 ring-white">
                  <UserGroupIcon className="h-6 w-6" aria-hidden="true" />
                </span>
              </div>
              <div className="mt-8">
                <h3 className="text-lg font-medium">
                  <span className="absolute inset-0" aria-hidden="true" />
                  System Settings
                </h3>
                <p className="mt-2 text-sm text-gray-500">
                  Configure system-wide settings and preferences
                </p>
              </div>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SystemDashboard;
