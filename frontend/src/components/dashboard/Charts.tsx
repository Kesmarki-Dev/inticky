import React from 'react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
  ResponsiveContainer,
  Area,
  AreaChart,
} from 'recharts';

// Color palette
const COLORS = {
  primary: '#3B82F6',
  secondary: '#10B981',
  warning: '#F59E0B',
  danger: '#EF4444',
  info: '#6366F1',
  success: '#059669',
};

const PIE_COLORS = [COLORS.primary, COLORS.secondary, COLORS.warning, COLORS.danger, COLORS.info];

// Ticket Status Chart
interface TicketStatusChartProps {
  data: Array<{
    name: string;
    value: number;
  }>;
}

export const TicketStatusChart: React.FC<TicketStatusChartProps> = ({ data }) => {
  return (
    <div className="bg-white p-6 rounded-lg shadow">
      <h3 className="text-lg font-medium text-gray-900 mb-4">Tickets by Status</h3>
      <ResponsiveContainer width="100%" height={300}>
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            labelLine={false}
            label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
            outerRadius={80}
            fill="#8884d8"
            dataKey="value"
          >
            {data.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={PIE_COLORS[index % PIE_COLORS.length]} />
            ))}
          </Pie>
          <Tooltip />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
};

// Ticket Priority Chart
interface TicketPriorityChartProps {
  data: Array<{
    priority: string;
    count: number;
  }>;
}

export const TicketPriorityChart: React.FC<TicketPriorityChartProps> = ({ data }) => {
  return (
    <div className="bg-white p-6 rounded-lg shadow">
      <h3 className="text-lg font-medium text-gray-900 mb-4">Tickets by Priority</h3>
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="priority" />
          <YAxis />
          <Tooltip />
          <Bar dataKey="count" fill={COLORS.primary} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};

// Ticket Trends Chart
interface TicketTrendsChartProps {
  data: Array<{
    date: string;
    created: number;
    resolved: number;
    open: number;
  }>;
}

export const TicketTrendsChart: React.FC<TicketTrendsChartProps> = ({ data }) => {
  return (
    <div className="bg-white p-6 rounded-lg shadow">
      <h3 className="text-lg font-medium text-gray-900 mb-4">Ticket Trends (Last 30 Days)</h3>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Line 
            type="monotone" 
            dataKey="created" 
            stroke={COLORS.primary} 
            strokeWidth={2}
            name="Created"
          />
          <Line 
            type="monotone" 
            dataKey="resolved" 
            stroke={COLORS.secondary} 
            strokeWidth={2}
            name="Resolved"
          />
          <Line 
            type="monotone" 
            dataKey="open" 
            stroke={COLORS.warning} 
            strokeWidth={2}
            name="Open"
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

// Response Time Chart
interface ResponseTimeChartProps {
  data: Array<{
    category: string;
    avgResponseTime: number;
    target: number;
  }>;
}

export const ResponseTimeChart: React.FC<ResponseTimeChartProps> = ({ data }) => {
  return (
    <div className="bg-white p-6 rounded-lg shadow">
      <h3 className="text-lg font-medium text-gray-900 mb-4">Average Response Time (Hours)</h3>
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="category" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Bar dataKey="avgResponseTime" fill={COLORS.primary} name="Actual" />
          <Bar dataKey="target" fill={COLORS.warning} name="Target" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};

// Agent Performance Chart
interface AgentPerformanceChartProps {
  data: Array<{
    agent: string;
    resolved: number;
    avgRating: number;
  }>;
}

export const AgentPerformanceChart: React.FC<AgentPerformanceChartProps> = ({ data }) => {
  return (
    <div className="bg-white p-6 rounded-lg shadow">
      <h3 className="text-lg font-medium text-gray-900 mb-4">Agent Performance</h3>
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="agent" />
          <YAxis yAxisId="left" />
          <YAxis yAxisId="right" orientation="right" />
          <Tooltip />
          <Legend />
          <Bar yAxisId="left" dataKey="resolved" fill={COLORS.primary} name="Tickets Resolved" />
          <Line yAxisId="right" dataKey="avgRating" stroke={COLORS.secondary} name="Avg Rating" />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};

// Volume Chart (Area Chart)
interface VolumeChartProps {
  data: Array<{
    date: string;
    tickets: number;
  }>;
}

export const VolumeChart: React.FC<VolumeChartProps> = ({ data }) => {
  return (
    <div className="bg-white p-6 rounded-lg shadow">
      <h3 className="text-lg font-medium text-gray-900 mb-4">Ticket Volume</h3>
      <ResponsiveContainer width="100%" height={300}>
        <AreaChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" />
          <YAxis />
          <Tooltip />
          <Area 
            type="monotone" 
            dataKey="tickets" 
            stroke={COLORS.primary} 
            fill={COLORS.primary}
            fillOpacity={0.3}
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
};

// SLA Compliance Chart
interface SLAComplianceChartProps {
  data: Array<{
    category: string;
    compliance: number;
    target: number;
  }>;
}

export const SLAComplianceChart: React.FC<SLAComplianceChartProps> = ({ data }) => {
  return (
    <div className="bg-white p-6 rounded-lg shadow">
      <h3 className="text-lg font-medium text-gray-900 mb-4">SLA Compliance (%)</h3>
      <ResponsiveContainer width="100%" height={300}>
        <BarChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="category" />
          <YAxis domain={[0, 100]} />
          <Tooltip formatter={(value) => `${value}%`} />
          <Legend />
          <Bar 
            dataKey="compliance" 
            fill={COLORS.secondary} 
            name="Actual Compliance"
          />
          <Bar 
            dataKey="target" 
            fill={COLORS.warning} 
            name="Target"
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};

// Customer Satisfaction Chart
interface CustomerSatisfactionChartProps {
  data: Array<{
    period: string;
    satisfaction: number;
    responses: number;
  }>;
}

export const CustomerSatisfactionChart: React.FC<CustomerSatisfactionChartProps> = ({ data }) => {
  return (
    <div className="bg-white p-6 rounded-lg shadow">
      <h3 className="text-lg font-medium text-gray-900 mb-4">Customer Satisfaction</h3>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="period" />
          <YAxis domain={[0, 5]} />
          <Tooltip formatter={(value, name) => [
            name === 'satisfaction' ? `${value}/5` : value,
            name === 'satisfaction' ? 'Rating' : 'Responses'
          ]} />
          <Legend />
          <Line 
            type="monotone" 
            dataKey="satisfaction" 
            stroke={COLORS.secondary} 
            strokeWidth={3}
            name="Avg Rating"
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};
