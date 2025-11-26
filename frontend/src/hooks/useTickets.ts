import { useState, useEffect, useCallback } from 'react';
import { ticketService } from '../services/tickets';
import { 
  Ticket, 
  TicketCreateRequest, 
  PaginatedResponse, 
  PaginationParams, 
  TicketFilters,
  TicketStats
} from '../types/api';

interface UseTicketsOptions {
  initialFilters?: TicketFilters;
  initialPagination?: PaginationParams;
  autoFetch?: boolean;
}

interface UseTicketsReturn {
  tickets: Ticket[];
  pagination: {
    currentPage: number;
    totalPages: number;
    totalItems: number;
    itemsPerPage: number;
  };
  loading: boolean;
  error: string | null;
  filters: TicketFilters;
  stats: TicketStats | null;
  
  // Actions
  fetchTickets: () => Promise<void>;
  createTicket: (data: TicketCreateRequest) => Promise<Ticket>;
  updateTicket: (id: string, data: Partial<TicketCreateRequest>) => Promise<Ticket>;
  deleteTicket: (id: string) => Promise<void>;
  assignTicket: (ticketId: string, userId: string) => Promise<Ticket>;
  updateTicketStatus: (ticketId: string, status: string) => Promise<Ticket>;
  
  // Pagination
  setPage: (page: number) => void;
  setItemsPerPage: (size: number) => void;
  
  // Filters
  setFilters: (filters: Partial<TicketFilters>) => void;
  clearFilters: () => void;
  
  // Refresh
  refresh: () => Promise<void>;
}

export const useTickets = (options: UseTicketsOptions = {}): UseTicketsReturn => {
  const {
    initialFilters = {},
    initialPagination = { page: 0, size: 20 },
    autoFetch = true
  } = options;

  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [pagination, setPagination] = useState({
    currentPage: initialPagination.page || 0,
    totalPages: 0,
    totalItems: 0,
    itemsPerPage: initialPagination.size || 20,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFiltersState] = useState<TicketFilters>(initialFilters);
  const [stats, setStats] = useState<TicketStats | null>(null);

  const fetchTickets = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const params: PaginationParams & TicketFilters = {
        page: pagination.currentPage,
        size: pagination.itemsPerPage,
        ...filters,
      };

      const response: PaginatedResponse<Ticket> = await ticketService.getTickets(params);
      
      setTickets(response.content);
      setPagination(prev => ({
        ...prev,
        totalPages: response.totalPages,
        totalItems: response.totalElements,
      }));
    } catch (err: any) {
      setError(err.message || 'Failed to fetch tickets');
    } finally {
      setLoading(false);
    }
  }, [pagination.currentPage, pagination.itemsPerPage, filters]);

  const fetchStats = useCallback(async () => {
    try {
      const statsData = await ticketService.getTicketStats();
      setStats(statsData);
    } catch (err) {
      console.error('Failed to fetch ticket stats:', err);
    }
  }, []);

  const createTicket = useCallback(async (data: TicketCreateRequest): Promise<Ticket> => {
    try {
      setLoading(true);
      const newTicket = await ticketService.createTicket(data);
      
      // Refresh the list to include the new ticket
      await fetchTickets();
      await fetchStats();
      
      return newTicket;
    } catch (err: any) {
      setError(err.message || 'Failed to create ticket');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [fetchTickets, fetchStats]);

  const updateTicket = useCallback(async (
    id: string, 
    data: Partial<TicketCreateRequest>
  ): Promise<Ticket> => {
    try {
      setLoading(true);
      const updatedTicket = await ticketService.updateTicket(id, data);
      
      // Update the ticket in the local state
      setTickets(prev => prev.map(ticket => 
        ticket.id === id ? updatedTicket : ticket
      ));
      
      await fetchStats();
      
      return updatedTicket;
    } catch (err: any) {
      setError(err.message || 'Failed to update ticket');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [fetchStats]);

  const deleteTicket = useCallback(async (id: string): Promise<void> => {
    try {
      setLoading(true);
      await ticketService.deleteTicket(id);
      
      // Remove the ticket from local state
      setTickets(prev => prev.filter(ticket => ticket.id !== id));
      
      await fetchStats();
    } catch (err: any) {
      setError(err.message || 'Failed to delete ticket');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [fetchStats]);

  const assignTicket = useCallback(async (ticketId: string, userId: string): Promise<Ticket> => {
    try {
      setLoading(true);
      const updatedTicket = await ticketService.assignTicket(ticketId, userId);
      
      // Update the ticket in local state
      setTickets(prev => prev.map(ticket => 
        ticket.id === ticketId ? updatedTicket : ticket
      ));
      
      return updatedTicket;
    } catch (err: any) {
      setError(err.message || 'Failed to assign ticket');
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const updateTicketStatus = useCallback(async (
    ticketId: string, 
    status: string
  ): Promise<Ticket> => {
    try {
      setLoading(true);
      const updatedTicket = await ticketService.updateTicketStatus(ticketId, status);
      
      // Update the ticket in local state
      setTickets(prev => prev.map(ticket => 
        ticket.id === ticketId ? updatedTicket : ticket
      ));
      
      await fetchStats();
      
      return updatedTicket;
    } catch (err: any) {
      setError(err.message || 'Failed to update ticket status');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [fetchStats]);

  const setPage = useCallback((page: number) => {
    setPagination(prev => ({ ...prev, currentPage: page }));
  }, []);

  const setItemsPerPage = useCallback((size: number) => {
    setPagination(prev => ({ 
      ...prev, 
      itemsPerPage: size,
      currentPage: 0 // Reset to first page when changing page size
    }));
  }, []);

  const setFilters = useCallback((newFilters: Partial<TicketFilters>) => {
    setFiltersState(prev => ({ ...prev, ...newFilters }));
    setPagination(prev => ({ ...prev, currentPage: 0 })); // Reset to first page
  }, []);

  const clearFilters = useCallback(() => {
    setFiltersState({});
    setPagination(prev => ({ ...prev, currentPage: 0 }));
  }, []);

  const refresh = useCallback(async () => {
    await Promise.all([fetchTickets(), fetchStats()]);
  }, [fetchTickets, fetchStats]);

  // Auto-fetch on mount and when dependencies change
  useEffect(() => {
    if (autoFetch) {
      fetchTickets();
    }
  }, [fetchTickets, autoFetch]);

  // Fetch stats on mount
  useEffect(() => {
    if (autoFetch) {
      fetchStats();
    }
  }, [fetchStats, autoFetch]);

  return {
    tickets,
    pagination,
    loading,
    error,
    filters,
    stats,
    
    // Actions
    fetchTickets,
    createTicket,
    updateTicket,
    deleteTicket,
    assignTicket,
    updateTicketStatus,
    
    // Pagination
    setPage,
    setItemsPerPage,
    
    // Filters
    setFilters,
    clearFilters,
    
    // Refresh
    refresh,
  };
};

// Hook for a single ticket
export const useTicket = (ticketId: string) => {
  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchTicket = useCallback(async () => {
    if (!ticketId) return;
    
    try {
      setLoading(true);
      setError(null);
      const ticketData = await ticketService.getTicketById(ticketId);
      setTicket(ticketData);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch ticket');
    } finally {
      setLoading(false);
    }
  }, [ticketId]);

  const updateTicket = useCallback(async (data: Partial<TicketCreateRequest>) => {
    if (!ticketId) return;
    
    try {
      setLoading(true);
      const updatedTicket = await ticketService.updateTicket(ticketId, data);
      setTicket(updatedTicket);
      return updatedTicket;
    } catch (err: any) {
      setError(err.message || 'Failed to update ticket');
      throw err;
    } finally {
      setLoading(false);
    }
  }, [ticketId]);

  useEffect(() => {
    fetchTicket();
  }, [fetchTicket]);

  return {
    ticket,
    loading,
    error,
    fetchTicket,
    updateTicket,
  };
};
