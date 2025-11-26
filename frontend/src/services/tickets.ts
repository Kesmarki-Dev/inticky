import { apiService, buildQueryParams } from './api';
import { 
  Ticket, 
  TicketCreateRequest, 
  Comment, 
  CommentCreateRequest, 
  PaginatedResponse, 
  PaginationParams, 
  TicketFilters,
  TicketStats,
  Attachment
} from '../types/api';

export const ticketService = {
  // Get all tickets with pagination and filters
  getTickets: async (
    params: PaginationParams & TicketFilters = {}
  ): Promise<PaginatedResponse<Ticket>> => {
    const queryString = buildQueryParams(params);
    return apiService.get<PaginatedResponse<Ticket>>(`/tickets?${queryString}`);
  },

  // Get ticket by ID
  getTicketById: async (id: string): Promise<Ticket> => {
    return apiService.get<Ticket>(`/tickets/${id}`);
  },

  // Create new ticket
  createTicket: async (ticketData: TicketCreateRequest): Promise<Ticket> => {
    return apiService.post<Ticket>('/tickets', ticketData);
  },

  // Update ticket
  updateTicket: async (id: string, ticketData: Partial<TicketCreateRequest>): Promise<Ticket> => {
    return apiService.put<Ticket>(`/tickets/${id}`, ticketData);
  },

  // Delete ticket
  deleteTicket: async (id: string): Promise<void> => {
    return apiService.delete<void>(`/tickets/${id}`);
  },

  // Get tickets by status
  getTicketsByStatus: async (
    status: string, 
    params: PaginationParams = {}
  ): Promise<PaginatedResponse<Ticket>> => {
    const queryString = buildQueryParams(params);
    return apiService.get<PaginatedResponse<Ticket>>(`/tickets/status/${status}?${queryString}`);
  },

  // Get tickets by priority
  getTicketsByPriority: async (
    priority: string, 
    params: PaginationParams = {}
  ): Promise<PaginatedResponse<Ticket>> => {
    const queryString = buildQueryParams(params);
    return apiService.get<PaginatedResponse<Ticket>>(`/tickets/priority/${priority}?${queryString}`);
  },

  // Get tickets by category
  getTicketsByCategory: async (
    category: string, 
    params: PaginationParams = {}
  ): Promise<PaginatedResponse<Ticket>> => {
    const queryString = buildQueryParams(params);
    return apiService.get<PaginatedResponse<Ticket>>(`/tickets/category/${category}?${queryString}`);
  },

  // Get tickets assigned to user
  getAssignedTickets: async (
    userId: string, 
    params: PaginationParams = {}
  ): Promise<PaginatedResponse<Ticket>> => {
    const queryString = buildQueryParams(params);
    return apiService.get<PaginatedResponse<Ticket>>(`/tickets/assigned/${userId}?${queryString}`);
  },

  // Get tickets reported by user
  getReportedTickets: async (
    userId: string, 
    params: PaginationParams = {}
  ): Promise<PaginatedResponse<Ticket>> => {
    const queryString = buildQueryParams(params);
    return apiService.get<PaginatedResponse<Ticket>>(`/tickets/reported/${userId}?${queryString}`);
  },

  // Search tickets
  searchTickets: async (
    query: string, 
    params: PaginationParams = {}
  ): Promise<PaginatedResponse<Ticket>> => {
    const searchParams = { ...params, search: query };
    const queryString = buildQueryParams(searchParams);
    return apiService.get<PaginatedResponse<Ticket>>(`/tickets/search?${queryString}`);
  },

  // Assign ticket to user
  assignTicket: async (ticketId: string, userId: string): Promise<Ticket> => {
    return apiService.post<Ticket>(`/tickets/${ticketId}/assign`, { assigneeId: userId });
  },

  // Unassign ticket
  unassignTicket: async (ticketId: string): Promise<Ticket> => {
    return apiService.post<Ticket>(`/tickets/${ticketId}/unassign`);
  },

  // Update ticket status
  updateTicketStatus: async (ticketId: string, status: string): Promise<Ticket> => {
    return apiService.patch<Ticket>(`/tickets/${ticketId}/status`, { status });
  },

  // Update ticket priority
  updateTicketPriority: async (ticketId: string, priority: string): Promise<Ticket> => {
    return apiService.patch<Ticket>(`/tickets/${ticketId}/priority`, { priority });
  },

  // Get ticket statistics
  getTicketStats: async (): Promise<TicketStats> => {
    return apiService.get<TicketStats>('/tickets/stats');
  },

  // Get tickets due soon
  getTicketsDueSoon: async (days: number = 7): Promise<Ticket[]> => {
    return apiService.get<Ticket[]>(`/tickets/due-soon?days=${days}`);
  },

  // Get overdue tickets
  getOverdueTickets: async (): Promise<Ticket[]> => {
    return apiService.get<Ticket[]>('/tickets/overdue');
  },

  // Bulk update tickets
  bulkUpdateTickets: async (
    ticketIds: string[], 
    updates: Partial<TicketCreateRequest>
  ): Promise<Ticket[]> => {
    return apiService.post<Ticket[]>('/tickets/bulk-update', {
      ticketIds,
      updates,
    });
  },

  // Export tickets
  exportTickets: async (filters: TicketFilters = {}, format: 'csv' | 'xlsx' = 'csv'): Promise<void> => {
    const queryString = buildQueryParams({ ...filters, format });
    return apiService.download(`/tickets/export?${queryString}`, `tickets.${format}`);
  },
};

// Comment service
export const commentService = {
  // Get comments for ticket
  getComments: async (
    ticketId: string, 
    params: PaginationParams = {}
  ): Promise<PaginatedResponse<Comment>> => {
    const queryString = buildQueryParams(params);
    return apiService.get<PaginatedResponse<Comment>>(`/comments/ticket/${ticketId}?${queryString}`);
  },

  // Create comment
  createComment: async (ticketId: string, commentData: CommentCreateRequest): Promise<Comment> => {
    return apiService.post<Comment>(`/comments/ticket/${ticketId}`, commentData);
  },

  // Update comment
  updateComment: async (commentId: string, content: string): Promise<Comment> => {
    return apiService.put<Comment>(`/comments/${commentId}`, { content });
  },

  // Delete comment
  deleteComment: async (commentId: string): Promise<void> => {
    return apiService.delete<void>(`/comments/${commentId}`);
  },

  // Get comments by author
  getCommentsByAuthor: async (
    authorId: string, 
    params: PaginationParams = {}
  ): Promise<PaginatedResponse<Comment>> => {
    const queryString = buildQueryParams(params);
    return apiService.get<PaginatedResponse<Comment>>(`/comments/author/${authorId}?${queryString}`);
  },
};

// Attachment service
export const attachmentService = {
  // Upload attachment
  uploadAttachment: async (
    ticketId: string, 
    file: File, 
    onProgress?: (progress: number) => void
  ): Promise<Attachment> => {
    return apiService.upload<Attachment>(`/tickets/${ticketId}/attachments`, file, onProgress);
  },

  // Get attachments for ticket
  getAttachments: async (ticketId: string): Promise<Attachment[]> => {
    return apiService.get<Attachment[]>(`/tickets/${ticketId}/attachments`);
  },

  // Download attachment
  downloadAttachment: async (attachmentId: string, filename?: string): Promise<void> => {
    return apiService.download(`/attachments/${attachmentId}/download`, filename);
  },

  // Delete attachment
  deleteAttachment: async (attachmentId: string): Promise<void> => {
    return apiService.delete<void>(`/attachments/${attachmentId}`);
  },
};
