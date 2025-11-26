import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { 
  Ticket, 
  TicketCreateRequest, 
  TicketFilters, 
  TicketStats,
  PaginationParams,
  Comment,
  CommentCreateRequest
} from '../types/api';
import { ticketService, commentService } from '../services/tickets';

interface TicketState {
  // State
  tickets: Ticket[];
  currentTicket: Ticket | null;
  comments: Comment[];
  stats: TicketStats | null;
  filters: TicketFilters;
  pagination: {
    page: number;
    size: number;
    totalPages: number;
    totalElements: number;
  };
  loading: boolean;
  error: string | null;

  // Actions
  fetchTickets: (params?: PaginationParams & TicketFilters) => Promise<void>;
  fetchTicketById: (id: string) => Promise<void>;
  createTicket: (data: TicketCreateRequest) => Promise<Ticket>;
  updateTicket: (id: string, data: Partial<TicketCreateRequest>) => Promise<Ticket>;
  deleteTicket: (id: string) => Promise<void>;
  assignTicket: (ticketId: string, userId: string) => Promise<Ticket>;
  updateTicketStatus: (ticketId: string, status: string) => Promise<Ticket>;
  updateTicketPriority: (ticketId: string, priority: string) => Promise<Ticket>;
  
  // Comments
  fetchComments: (ticketId: string) => Promise<void>;
  addComment: (ticketId: string, data: CommentCreateRequest) => Promise<Comment>;
  updateComment: (commentId: string, content: string) => Promise<Comment>;
  deleteComment: (commentId: string) => Promise<void>;
  
  // Stats
  fetchStats: () => Promise<void>;
  
  // Filters and Pagination
  setFilters: (filters: Partial<TicketFilters>) => void;
  clearFilters: () => void;
  setPagination: (pagination: Partial<TicketState['pagination']>) => void;
  
  // Utility
  clearError: () => void;
  reset: () => void;
}

export const useTicketStore = create<TicketState>()(
  persist(
    (set, get) => ({
      // Initial state
      tickets: [],
      currentTicket: null,
      comments: [],
      stats: null,
      filters: {},
      pagination: {
        page: 0,
        size: 20,
        totalPages: 0,
        totalElements: 0,
      },
      loading: false,
      error: null,

      // Actions
      fetchTickets: async (params) => {
        set({ loading: true, error: null });
        
        try {
          const { pagination, filters } = get();
          const searchParams = {
            page: pagination.page,
            size: pagination.size,
            ...filters,
            ...params,
          };
          
          const response = await ticketService.getTickets(searchParams);
          
          set({
            tickets: response.content,
            pagination: {
              ...pagination,
              totalPages: response.totalPages,
              totalElements: response.totalElements,
            },
            loading: false,
          });
        } catch (error: any) {
          set({
            loading: false,
            error: error.message || 'Failed to fetch tickets',
          });
        }
      },

      fetchTicketById: async (id: string) => {
        set({ loading: true, error: null });
        
        try {
          const ticket = await ticketService.getTicketById(id);
          set({
            currentTicket: ticket,
            loading: false,
          });
        } catch (error: any) {
          set({
            loading: false,
            error: error.message || 'Failed to fetch ticket',
          });
        }
      },

      createTicket: async (data: TicketCreateRequest) => {
        set({ loading: true, error: null });
        
        try {
          const newTicket = await ticketService.createTicket(data);
          
          // Add to tickets list
          set((state) => ({
            tickets: [newTicket, ...state.tickets],
            loading: false,
          }));
          
          // Refresh stats
          get().fetchStats();
          
          return newTicket;
        } catch (error: any) {
          set({
            loading: false,
            error: error.message || 'Failed to create ticket',
          });
          throw error;
        }
      },

      updateTicket: async (id: string, data: Partial<TicketCreateRequest>) => {
        set({ loading: true, error: null });
        
        try {
          const updatedTicket = await ticketService.updateTicket(id, data);
          
          // Update in tickets list
          set((state) => ({
            tickets: state.tickets.map(ticket => 
              ticket.id === id ? updatedTicket : ticket
            ),
            currentTicket: state.currentTicket?.id === id ? updatedTicket : state.currentTicket,
            loading: false,
          }));
          
          return updatedTicket;
        } catch (error: any) {
          set({
            loading: false,
            error: error.message || 'Failed to update ticket',
          });
          throw error;
        }
      },

      deleteTicket: async (id: string) => {
        set({ loading: true, error: null });
        
        try {
          await ticketService.deleteTicket(id);
          
          // Remove from tickets list
          set((state) => ({
            tickets: state.tickets.filter(ticket => ticket.id !== id),
            currentTicket: state.currentTicket?.id === id ? null : state.currentTicket,
            loading: false,
          }));
          
          // Refresh stats
          get().fetchStats();
        } catch (error: any) {
          set({
            loading: false,
            error: error.message || 'Failed to delete ticket',
          });
          throw error;
        }
      },

      assignTicket: async (ticketId: string, userId: string) => {
        try {
          const updatedTicket = await ticketService.assignTicket(ticketId, userId);
          
          // Update in state
          set((state) => ({
            tickets: state.tickets.map(ticket => 
              ticket.id === ticketId ? updatedTicket : ticket
            ),
            currentTicket: state.currentTicket?.id === ticketId ? updatedTicket : state.currentTicket,
          }));
          
          return updatedTicket;
        } catch (error: any) {
          set({ error: error.message || 'Failed to assign ticket' });
          throw error;
        }
      },

      updateTicketStatus: async (ticketId: string, status: string) => {
        try {
          const updatedTicket = await ticketService.updateTicketStatus(ticketId, status);
          
          // Update in state
          set((state) => ({
            tickets: state.tickets.map(ticket => 
              ticket.id === ticketId ? updatedTicket : ticket
            ),
            currentTicket: state.currentTicket?.id === ticketId ? updatedTicket : state.currentTicket,
          }));
          
          // Refresh stats
          get().fetchStats();
          
          return updatedTicket;
        } catch (error: any) {
          set({ error: error.message || 'Failed to update ticket status' });
          throw error;
        }
      },

      updateTicketPriority: async (ticketId: string, priority: string) => {
        try {
          const updatedTicket = await ticketService.updateTicketPriority(ticketId, priority);
          
          // Update in state
          set((state) => ({
            tickets: state.tickets.map(ticket => 
              ticket.id === ticketId ? updatedTicket : ticket
            ),
            currentTicket: state.currentTicket?.id === ticketId ? updatedTicket : state.currentTicket,
          }));
          
          return updatedTicket;
        } catch (error: any) {
          set({ error: error.message || 'Failed to update ticket priority' });
          throw error;
        }
      },

      // Comments
      fetchComments: async (ticketId: string) => {
        set({ loading: true, error: null });
        
        try {
          const response = await commentService.getComments(ticketId);
          set({
            comments: response.content,
            loading: false,
          });
        } catch (error: any) {
          set({
            loading: false,
            error: error.message || 'Failed to fetch comments',
          });
        }
      },

      addComment: async (ticketId: string, data: CommentCreateRequest) => {
        try {
          const newComment = await commentService.createComment(ticketId, data);
          
          // Add to comments list
          set((state) => ({
            comments: [...state.comments, newComment],
          }));
          
          return newComment;
        } catch (error: any) {
          set({ error: error.message || 'Failed to add comment' });
          throw error;
        }
      },

      updateComment: async (commentId: string, content: string) => {
        try {
          const updatedComment = await commentService.updateComment(commentId, content);
          
          // Update in comments list
          set((state) => ({
            comments: state.comments.map(comment => 
              comment.id === commentId ? updatedComment : comment
            ),
          }));
          
          return updatedComment;
        } catch (error: any) {
          set({ error: error.message || 'Failed to update comment' });
          throw error;
        }
      },

      deleteComment: async (commentId: string) => {
        try {
          await commentService.deleteComment(commentId);
          
          // Remove from comments list
          set((state) => ({
            comments: state.comments.filter(comment => comment.id !== commentId),
          }));
        } catch (error: any) {
          set({ error: error.message || 'Failed to delete comment' });
          throw error;
        }
      },

      // Stats
      fetchStats: async () => {
        try {
          const stats = await ticketService.getTicketStats();
          set({ stats });
        } catch (error: any) {
          console.error('Failed to fetch ticket stats:', error);
        }
      },

      // Filters and Pagination
      setFilters: (newFilters: Partial<TicketFilters>) => {
        set((state) => ({
          filters: { ...state.filters, ...newFilters },
          pagination: { ...state.pagination, page: 0 }, // Reset to first page
        }));
      },

      clearFilters: () => {
        set((state) => ({
          filters: {},
          pagination: { ...state.pagination, page: 0 },
        }));
      },

      setPagination: (newPagination: Partial<TicketState['pagination']>) => {
        set((state) => ({
          pagination: { ...state.pagination, ...newPagination },
        }));
      },

      // Utility
      clearError: () => {
        set({ error: null });
      },

      reset: () => {
        set({
          tickets: [],
          currentTicket: null,
          comments: [],
          stats: null,
          filters: {},
          pagination: {
            page: 0,
            size: 20,
            totalPages: 0,
            totalElements: 0,
          },
          loading: false,
          error: null,
        });
      },
    }),
    {
      name: 'ticket-storage',
      partialize: (state) => ({
        filters: state.filters,
        pagination: state.pagination,
      }),
    }
  )
);
