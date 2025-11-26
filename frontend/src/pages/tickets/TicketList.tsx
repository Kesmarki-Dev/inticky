import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { 
  PlusIcon, 
  FunnelIcon,
  MagnifyingGlassIcon,
  EllipsisVerticalIcon
} from '@heroicons/react/24/outline';
import { Menu } from '@headlessui/react';
import { useTickets } from '../../hooks/useTickets';
import { 
  Table, 
  TableHeader, 
  TableBody, 
  TableHead, 
  TableRow, 
  TableCell,
  TableEmptyState,
  TableLoadingState,
  TablePagination
} from '../../components/ui/Table';
import { Badge, StatusBadge, PriorityBadge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { TicketFilters as ITicketFilters, TicketStatus, Priority, Category } from '../../types/api';
import { cn } from '../../utils/cn';
import { format } from 'date-fns';

const TicketList: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  
  const {
    tickets,
    pagination,
    loading,
    error,
    filters,
    setFilters,
    clearFilters,
    setPage,
    setItemsPerPage,
    refresh,
    updateTicketStatus,
    assignTicket,
  } = useTickets();

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    setFilters({ search: query || undefined });
  };

  const handleFilterChange = (newFilters: Partial<ITicketFilters>) => {
    setFilters(newFilters);
  };

  const handleStatusChange = async (ticketId: string, status: string) => {
    try {
      await updateTicketStatus(ticketId, status);
    } catch (error) {
      console.error('Failed to update ticket status:', error);
    }
  };

  const getStatusColor = (status: TicketStatus): 'success' | 'warning' | 'error' | 'info' => {
    switch (status) {
      case TicketStatus.RESOLVED:
      case TicketStatus.CLOSED:
        return 'success';
      case TicketStatus.IN_PROGRESS:
        return 'warning';
      case TicketStatus.WAITING_FOR_CUSTOMER:
        return 'info';
      default:
        return 'info';
    }
  };

  const getPriorityColor = (priority: Priority): 'low' | 'medium' | 'high' | 'urgent' => {
    return priority.toLowerCase() as 'low' | 'medium' | 'high' | 'urgent';
  };

  if (error) {
    return (
      <div className="p-6">
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <div className="flex">
            <div className="ml-3">
              <h3 className="text-sm font-medium text-red-800">Error loading tickets</h3>
              <p className="mt-1 text-sm text-red-700">{error}</p>
              <div className="mt-4">
                <Button onClick={refresh} variant="outline" size="sm">
                  Try Again
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="md:flex md:items-center md:justify-between">
        <div className="flex-1 min-w-0">
          <h2 className="text-2xl font-bold leading-7 text-gray-900 sm:text-3xl sm:truncate">
            Tickets
          </h2>
          <p className="mt-1 text-sm text-gray-500">
            Manage and track all support tickets
          </p>
        </div>
        <div className="mt-4 flex md:mt-0 md:ml-4 space-x-3">
          <Button
            onClick={() => setShowFilters(!showFilters)}
            variant="outline"
            leftIcon={<FunnelIcon className="h-4 w-4" />}
          >
            Filters
          </Button>
          <Link to="/tickets/new">
            <Button leftIcon={<PlusIcon className="h-4 w-4" />}>
              Create Ticket
            </Button>
          </Link>
        </div>
      </div>

      {/* Search and Filters */}
      <div className="space-y-4">
        {/* Search */}
        <div className="max-w-md">
          <Input
            placeholder="Search tickets..."
            value={searchQuery}
            onChange={(e) => handleSearch(e.target.value)}
            leftIcon={<MagnifyingGlassIcon className="h-4 w-4" />}
          />
        </div>

        {/* Filters Panel */}
        {showFilters && (
          <div className="bg-white p-4 rounded-lg border border-gray-200 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-medium text-gray-900">Filters</h3>
              <Button
                onClick={clearFilters}
                variant="ghost"
                size="sm"
              >
                Clear All
              </Button>
            </div>
            
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
              {/* Status Filter */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Status
                </label>
                <select
                  value={filters.status?.[0] || ''}
                  onChange={(e) => handleFilterChange({ 
                    status: e.target.value ? [e.target.value as TicketStatus] : undefined 
                  })}
                  className="input"
                >
                  <option value="">All Statuses</option>
                  {Object.values(TicketStatus).map(status => (
                    <option key={status} value={status}>
                      {status.replace('_', ' ')}
                    </option>
                  ))}
                </select>
              </div>

              {/* Priority Filter */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Priority
                </label>
                <select
                  value={filters.priority?.[0] || ''}
                  onChange={(e) => handleFilterChange({ 
                    priority: e.target.value ? [e.target.value as Priority] : undefined 
                  })}
                  className="input"
                >
                  <option value="">All Priorities</option>
                  {Object.values(Priority).map(priority => (
                    <option key={priority} value={priority}>
                      {priority}
                    </option>
                  ))}
                </select>
              </div>

              {/* Category Filter */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Category
                </label>
                <select
                  value={filters.category?.[0] || ''}
                  onChange={(e) => handleFilterChange({ 
                    category: e.target.value ? [e.target.value as Category] : undefined 
                  })}
                  className="input"
                >
                  <option value="">All Categories</option>
                  {Object.values(Category).map(category => (
                    <option key={category} value={category}>
                      {category.replace('_', ' ')}
                    </option>
                  ))}
                </select>
              </div>

              {/* Date Range */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Date From
                </label>
                <input
                  type="date"
                  value={filters.dateFrom || ''}
                  onChange={(e) => handleFilterChange({ dateFrom: e.target.value || undefined })}
                  className="input"
                />
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Tickets Table */}
      <div className="bg-white shadow rounded-lg overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Title</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Priority</TableHead>
              <TableHead>Category</TableHead>
              <TableHead>Assignee</TableHead>
              <TableHead>Reporter</TableHead>
              <TableHead>Created</TableHead>
              <TableHead>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              <TableLoadingState rows={5} columns={8} />
            ) : tickets.length === 0 ? (
              <TableEmptyState
                message="No tickets found"
                description="Get started by creating your first ticket"
                action={
                  <Link to="/tickets/new">
                    <Button leftIcon={<PlusIcon className="h-4 w-4" />}>
                      Create Ticket
                    </Button>
                  </Link>
                }
              />
            ) : (
              tickets.map((ticket) => (
                <TableRow key={ticket.id} clickable>
                  <TableCell>
                    <div>
                      <Link
                        to={`/tickets/${ticket.id}`}
                        className="text-sm font-medium text-gray-900 hover:text-primary-600"
                      >
                        {ticket.title}
                      </Link>
                      <p className="text-sm text-gray-500 truncate max-w-xs">
                        {ticket.description}
                      </p>
                    </div>
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={getStatusColor(ticket.status)} />
                  </TableCell>
                  <TableCell>
                    <PriorityBadge priority={getPriorityColor(ticket.priority)} />
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline">
                      {ticket.category.replace('_', ' ')}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    {ticket.assignee ? (
                      <div className="flex items-center">
                        <div className="h-6 w-6 rounded-full bg-primary-100 flex items-center justify-center mr-2">
                          <span className="text-xs font-medium text-primary-700">
                            {ticket.assignee.firstName.charAt(0)}{ticket.assignee.lastName?.charAt(0)}
                          </span>
                        </div>
                        <span className="text-sm text-gray-900">
                          {ticket.assignee.firstName} {ticket.assignee.lastName}
                        </span>
                      </div>
                    ) : (
                      <span className="text-sm text-gray-500">Unassigned</span>
                    )}
                  </TableCell>
                  <TableCell>
                    {ticket.reporter && (
                      <div className="flex items-center">
                        <div className="h-6 w-6 rounded-full bg-gray-100 flex items-center justify-center mr-2">
                          <span className="text-xs font-medium text-gray-700">
                            {ticket.reporter.firstName.charAt(0)}{ticket.reporter.lastName?.charAt(0)}
                          </span>
                        </div>
                        <span className="text-sm text-gray-900">
                          {ticket.reporter.firstName} {ticket.reporter.lastName}
                        </span>
                      </div>
                    )}
                  </TableCell>
                  <TableCell>
                    <span className="text-sm text-gray-500">
                      {format(new Date(ticket.createdAt), 'MMM d, yyyy')}
                    </span>
                  </TableCell>
                  <TableCell>
                    <Menu as="div" className="relative inline-block text-left">
                      <Menu.Button className="p-2 rounded-full hover:bg-gray-100">
                        <EllipsisVerticalIcon className="h-4 w-4 text-gray-400" />
                      </Menu.Button>
                      <Menu.Items className="absolute right-0 z-10 mt-2 w-48 origin-top-right rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
                        <div className="py-1">
                          <Menu.Item>
                            {({ active }) => (
                              <Link
                                to={`/tickets/${ticket.id}`}
                                className={cn(
                                  active ? 'bg-gray-100' : '',
                                  'block px-4 py-2 text-sm text-gray-700'
                                )}
                              >
                                View Details
                              </Link>
                            )}
                          </Menu.Item>
                          <Menu.Item>
                            {({ active }) => (
                              <Link
                                to={`/tickets/${ticket.id}/edit`}
                                className={cn(
                                  active ? 'bg-gray-100' : '',
                                  'block px-4 py-2 text-sm text-gray-700'
                                )}
                              >
                                Edit Ticket
                              </Link>
                            )}
                          </Menu.Item>
                          {ticket.status !== TicketStatus.CLOSED && (
                            <Menu.Item>
                              {({ active }) => (
                                <button
                                  onClick={() => handleStatusChange(ticket.id, TicketStatus.CLOSED)}
                                  className={cn(
                                    active ? 'bg-gray-100' : '',
                                    'block w-full text-left px-4 py-2 text-sm text-gray-700'
                                  )}
                                >
                                  Close Ticket
                                </button>
                              )}
                            </Menu.Item>
                          )}
                        </div>
                      </Menu.Items>
                    </Menu>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>

        {/* Pagination */}
        {!loading && tickets.length > 0 && (
          <TablePagination
            currentPage={pagination.currentPage}
            totalPages={pagination.totalPages}
            totalItems={pagination.totalItems}
            itemsPerPage={pagination.itemsPerPage}
            onPageChange={setPage}
            onItemsPerPageChange={setItemsPerPage}
          />
        )}
      </div>
    </div>
  );
};

export default TicketList;
