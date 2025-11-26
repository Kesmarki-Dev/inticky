import React from 'react';
import { 
  TicketIcon,
  ClockIcon,
  CheckCircleIcon,
  ExclamationTriangleIcon,
  UserGroupIcon,
  ChartBarIcon
} from '@heroicons/react/24/outline';
import { TicketStats as ITicketStats } from '../../types/api';

interface StatCardProps {
  title: string;
  value: string | number;
  change?: {
    value: number;
    type: 'increase' | 'decrease' | 'neutral';
    period: string;
  };
  icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
  color?: 'blue' | 'green' | 'yellow' | 'red' | 'purple' | 'gray';
  href?: string;
}

const StatCard: React.FC<StatCardProps> = ({ 
  title, 
  value, 
  change, 
  icon: Icon, 
  color = 'blue',
  href 
}) => {
  const colorClasses = {
    blue: 'bg-blue-50 text-blue-600',
    green: 'bg-green-50 text-green-600',
    yellow: 'bg-yellow-50 text-yellow-600',
    red: 'bg-red-50 text-red-600',
    purple: 'bg-purple-50 text-purple-600',
    gray: 'bg-gray-50 text-gray-600',
  };

  const changeColorClasses = {
    increase: 'text-green-600',
    decrease: 'text-red-600',
    neutral: 'text-gray-600',
  };

  const CardContent = () => (
    <div className="bg-white overflow-hidden shadow rounded-lg">
      <div className="p-5">
        <div className="flex items-center">
          <div className="flex-shrink-0">
            <div className={`p-3 rounded-md ${colorClasses[color]}`}>
              <Icon className="h-6 w-6" aria-hidden="true" />
            </div>
          </div>
          <div className="ml-5 w-0 flex-1">
            <dl>
              <dt className="text-sm font-medium text-gray-500 truncate">
                {title}
              </dt>
              <dd className="flex items-baseline">
                <div className="text-2xl font-semibold text-gray-900">
                  {typeof value === 'number' ? value.toLocaleString() : value}
                </div>
                {change && (
                  <div className={`ml-2 flex items-baseline text-sm font-semibold ${changeColorClasses[change.type]}`}>
                    {change.type === 'increase' && (
                      <svg className="self-center flex-shrink-0 h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M5.293 9.707a1 1 0 010-1.414l4-4a1 1 0 011.414 0l4 4a1 1 0 01-1.414 1.414L11 7.414V15a1 1 0 11-2 0V7.414L6.707 9.707a1 1 0 01-1.414 0z" clipRule="evenodd" />
                      </svg>
                    )}
                    {change.type === 'decrease' && (
                      <svg className="self-center flex-shrink-0 h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M14.707 10.293a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 111.414-1.414L9 12.586V5a1 1 0 012 0v7.586l2.293-2.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                    )}
                    <span className="sr-only">
                      {change.type === 'increase' ? 'Increased' : 'Decreased'} by
                    </span>
                    {Math.abs(change.value)}%
                    <span className="text-gray-500 font-normal ml-1">
                      {change.period}
                    </span>
                  </div>
                )}
              </dd>
            </dl>
          </div>
        </div>
      </div>
    </div>
  );

  if (href) {
    return (
      <a href={href} className="block hover:bg-gray-50 transition-colors">
        <CardContent />
      </a>
    );
  }

  return <CardContent />;
};

interface TicketStatsProps {
  stats: ITicketStats;
  loading?: boolean;
  showTrends?: boolean;
}

export const TicketStatsGrid: React.FC<TicketStatsProps> = ({ 
  stats, 
  loading = false,
  showTrends = false 
}) => {
  if (loading) {
    return (
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {[...Array(4)].map((_, i) => (
          <div key={i} className="bg-white overflow-hidden shadow rounded-lg animate-pulse">
            <div className="p-5">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="p-3 rounded-md bg-gray-200 w-12 h-12"></div>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                  <div className="h-8 bg-gray-200 rounded w-1/2"></div>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    );
  }

  const statCards = [
    {
      title: 'Total Tickets',
      value: stats.totalTickets,
      icon: TicketIcon,
      color: 'blue' as const,
      change: showTrends ? {
        value: 12,
        type: 'increase' as const,
        period: 'from last month'
      } : undefined,
      href: '/tickets',
    },
    {
      title: 'Open Tickets',
      value: stats.openTickets,
      icon: ClockIcon,
      color: 'yellow' as const,
      change: showTrends ? {
        value: 5,
        type: 'decrease' as const,
        period: 'from last week'
      } : undefined,
      href: '/tickets?status=open',
    },
    {
      title: 'In Progress',
      value: stats.inProgressTickets,
      icon: UserGroupIcon,
      color: 'purple' as const,
      change: showTrends ? {
        value: 8,
        type: 'increase' as const,
        period: 'from last week'
      } : undefined,
      href: '/tickets?status=in_progress',
    },
    {
      title: 'Resolved',
      value: stats.resolvedTickets,
      icon: CheckCircleIcon,
      color: 'green' as const,
      change: showTrends ? {
        value: 15,
        type: 'increase' as const,
        period: 'from last month'
      } : undefined,
      href: '/tickets?status=resolved',
    },
  ];

  return (
    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
      {statCards.map((card) => (
        <StatCard
          key={card.title}
          title={card.title}
          value={card.value}
          icon={card.icon}
          color={card.color}
          change={card.change}
          href={card.href}
        />
      ))}
    </div>
  );
};

// Priority Stats Component
interface PriorityStatsProps {
  stats: ITicketStats;
  loading?: boolean;
}

export const PriorityStats: React.FC<PriorityStatsProps> = ({ stats, loading }) => {
  if (loading) {
    return (
      <div className="bg-white shadow rounded-lg p-6 animate-pulse">
        <div className="h-6 bg-gray-200 rounded w-1/4 mb-4"></div>
        <div className="space-y-3">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="flex items-center justify-between">
              <div className="h-4 bg-gray-200 rounded w-1/3"></div>
              <div className="h-4 bg-gray-200 rounded w-1/4"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  const priorityData = [
    {
      name: 'Low',
      count: stats.ticketsByPriority?.LOW || 0,
      color: 'bg-blue-500',
      percentage: ((stats.ticketsByPriority?.LOW || 0) / stats.totalTickets * 100).toFixed(1),
    },
    {
      name: 'Medium',
      count: stats.ticketsByPriority?.MEDIUM || 0,
      color: 'bg-yellow-500',
      percentage: ((stats.ticketsByPriority?.MEDIUM || 0) / stats.totalTickets * 100).toFixed(1),
    },
    {
      name: 'High',
      count: stats.ticketsByPriority?.HIGH || 0,
      color: 'bg-orange-500',
      percentage: ((stats.ticketsByPriority?.HIGH || 0) / stats.totalTickets * 100).toFixed(1),
    },
    {
      name: 'Urgent',
      count: stats.ticketsByPriority?.URGENT || 0,
      color: 'bg-red-500',
      percentage: ((stats.ticketsByPriority?.URGENT || 0) / stats.totalTickets * 100).toFixed(1),
    },
  ];

  return (
    <div className="bg-white shadow rounded-lg p-6">
      <h3 className="text-lg font-medium text-gray-900 mb-4">Tickets by Priority</h3>
      <div className="space-y-4">
        {priorityData.map((priority) => (
          <div key={priority.name} className="flex items-center justify-between">
            <div className="flex items-center">
              <div className={`w-3 h-3 rounded-full ${priority.color} mr-3`}></div>
              <span className="text-sm font-medium text-gray-900">{priority.name}</span>
            </div>
            <div className="flex items-center space-x-2">
              <span className="text-sm text-gray-500">{priority.percentage}%</span>
              <span className="text-sm font-semibold text-gray-900">{priority.count}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

// Performance Metrics Component
interface PerformanceMetricsProps {
  stats: ITicketStats;
  loading?: boolean;
}

export const PerformanceMetrics: React.FC<PerformanceMetricsProps> = ({ stats, loading }) => {
  if (loading) {
    return (
      <div className="bg-white shadow rounded-lg p-6 animate-pulse">
        <div className="h-6 bg-gray-200 rounded w-1/3 mb-4"></div>
        <div className="grid grid-cols-2 gap-4">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="text-center">
              <div className="h-8 bg-gray-200 rounded w-full mb-2"></div>
              <div className="h-4 bg-gray-200 rounded w-3/4 mx-auto"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  const resolutionRate = stats.totalTickets > 0 
    ? ((stats.resolvedTickets + stats.closedTickets) / stats.totalTickets * 100).toFixed(1)
    : '0';

  const metrics = [
    {
      label: 'Avg Resolution Time',
      value: `${stats.averageResolutionTime || 0}h`,
      icon: ClockIcon,
    },
    {
      label: 'Resolution Rate',
      value: `${resolutionRate}%`,
      icon: ChartBarIcon,
    },
    {
      label: 'Open Rate',
      value: `${((stats.openTickets / stats.totalTickets * 100) || 0).toFixed(1)}%`,
      icon: ExclamationTriangleIcon,
    },
    {
      label: 'Total Closed',
      value: stats.closedTickets,
      icon: CheckCircleIcon,
    },
  ];

  return (
    <div className="bg-white shadow rounded-lg p-6">
      <h3 className="text-lg font-medium text-gray-900 mb-4">Performance Metrics</h3>
      <div className="grid grid-cols-2 gap-4">
        {metrics.map((metric) => {
          const Icon = metric.icon;
          return (
            <div key={metric.label} className="text-center">
              <div className="flex justify-center mb-2">
                <Icon className="h-6 w-6 text-gray-400" />
              </div>
              <div className="text-2xl font-semibold text-gray-900">{metric.value}</div>
              <div className="text-sm text-gray-500">{metric.label}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
