import axios from 'axios';

// Type definitions
type AxiosInstance = typeof axios;
type AxiosRequestConfig = Parameters<typeof axios.get>[1];
type AxiosResponse<T = any> = Awaited<ReturnType<typeof axios.get<T>>>;

// Create axios instance with default configuration
const api: AxiosInstance = axios.create({
  baseURL: '/api', // Use Vite proxy
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token and tenant ID
api.interceptors.request.use(
  (config) => {
    // Add auth token if available
    const token = localStorage.getItem('jwt_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Add tenant ID if available and not already set
    const userData = localStorage.getItem('user_data');
    if (userData) {
      try {
        const user = JSON.parse(userData);
        if (user.tenantId && !config.headers['X-Tenant-ID']) {
          config.headers['X-Tenant-ID'] = user.tenantId;
        }
      } catch (e) {
        console.warn('Failed to parse user data from localStorage:', e);
        // Clear invalid user data
        localStorage.removeItem('user_data');
      }
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle common errors
api.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  (error) => {
    // Handle 401 Unauthorized - redirect to login
    if (error.response?.status === 401) {
      localStorage.removeItem('jwt_token');
      localStorage.removeItem('user_data');
      localStorage.removeItem('tenant_id');
      window.location.href = '/login';
    }

    // Handle 403 Forbidden - show access denied message
    if (error.response?.status === 403) {
      console.error('Access denied:', error.response.data?.message);
    }

    // Handle network errors
    if (!error.response) {
      console.error('Network error:', error.message);
    }

    return Promise.reject(error);
  }
);

// Generic API methods
export const apiService = {
  // GET request
  get: async <T>(url: string, config?: AxiosRequestConfig): Promise<T> => {
    const response = await api.get<any>(url, config);
    // Handle ApiResponse wrapper from backend
    if (response.data && typeof response.data === 'object' && 'success' in response.data && 'data' in response.data) {
      return response.data.data as T;
    }
    return response.data as T;
  },

  // POST request
  post: async <T, D = any>(url: string, data?: D, config?: AxiosRequestConfig): Promise<T> => {
    const response = await api.post<any>(url, data, config);
    // Handle ApiResponse wrapper from backend
    if (response.data && typeof response.data === 'object' && 'success' in response.data && 'data' in response.data) {
      return response.data.data as T;
    }
    return response.data as T;
  },

  // PUT request
  put: async <T, D = any>(url: string, data?: D, config?: AxiosRequestConfig): Promise<T> => {
    const response = await api.put<any>(url, data, config);
    // Handle ApiResponse wrapper from backend
    if (response.data && typeof response.data === 'object' && 'success' in response.data && 'data' in response.data) {
      return response.data.data as T;
    }
    return response.data as T;
  },

  // PATCH request
  patch: async <T, D = any>(url: string, data?: D, config?: AxiosRequestConfig): Promise<T> => {
    const response = await api.patch<any>(url, data, config);
    // Handle ApiResponse wrapper from backend
    if (response.data && typeof response.data === 'object' && 'success' in response.data && 'data' in response.data) {
      return response.data.data as T;
    }
    return response.data as T;
  },

  // DELETE request
  delete: async <T>(url: string, config?: AxiosRequestConfig): Promise<T> => {
    const response = await api.delete<any>(url, config);
    // Handle ApiResponse wrapper from backend
    if (response.data && typeof response.data === 'object' && 'success' in response.data && 'data' in response.data) {
      return response.data.data as T;
    }
    return response.data as T;
  },

  // Upload file
  upload: async <T>(url: string, file: File, onProgress?: (progress: number) => void): Promise<T> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await api.post<T>(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(progress);
        }
      },
    });

    return response.data;
  },

  // Download file
  download: async (url: string, filename?: string): Promise<void> => {
    const response = await api.get(url, {
      responseType: 'blob',
    });

    // Create blob link to download
    const blob = new Blob([response.data]);
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = filename || 'download';
    link.click();
    
    // Clean up
    window.URL.revokeObjectURL(link.href);
  },
};

// Helper function to build query parameters
export const buildQueryParams = (params: Record<string, any>): string => {
  const searchParams = new URLSearchParams();
  
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      if (Array.isArray(value)) {
        value.forEach((item) => searchParams.append(key, item.toString()));
      } else {
        searchParams.append(key, value.toString());
      }
    }
  });
  
  return searchParams.toString();
};

// Helper function to set tenant context
export const setTenantContext = (tenantId: string): void => {
  localStorage.setItem('tenant_id', tenantId);
};

// Helper function to get tenant context
export const getTenantContext = (): string | null => {
  return localStorage.getItem('tenant_id');
};

// Helper function to clear tenant context
export const clearTenantContext = (): void => {
  localStorage.removeItem('tenant_id');
};

export { api };
export default api;
