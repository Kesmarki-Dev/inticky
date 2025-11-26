import { create } from 'zustand';

interface Toast {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message?: string;
  duration?: number;
  action?: {
    label: string;
    onClick: () => void;
  };
}

interface Modal {
  id: string;
  type: 'confirmation' | 'form' | 'info';
  title: string;
  content: React.ReactNode;
  onConfirm?: () => void;
  onCancel?: () => void;
  confirmText?: string;
  cancelText?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
}

interface UIState {
  // Sidebar
  sidebarOpen: boolean;
  sidebarCollapsed: boolean;
  
  // Toasts
  toasts: Toast[];
  
  // Modals
  modals: Modal[];
  
  // Loading states
  globalLoading: boolean;
  loadingStates: Record<string, boolean>;
  
  // Theme
  theme: 'light' | 'dark' | 'system';
  
  // Actions
  setSidebarOpen: (open: boolean) => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  toggleSidebar: () => void;
  
  // Toast actions
  addToast: (toast: Omit<Toast, 'id'>) => string;
  removeToast: (id: string) => void;
  clearToasts: () => void;
  
  // Modal actions
  openModal: (modal: Omit<Modal, 'id'>) => string;
  closeModal: (id: string) => void;
  closeAllModals: () => void;
  
  // Loading actions
  setGlobalLoading: (loading: boolean) => void;
  setLoading: (key: string, loading: boolean) => void;
  clearLoading: (key: string) => void;
  
  // Theme actions
  setTheme: (theme: UIState['theme']) => void;
  
  // Utility
  reset: () => void;
}

export const useUIStore = create<UIState>((set, get) => ({
  // Initial state
  sidebarOpen: false,
  sidebarCollapsed: false,
  toasts: [],
  modals: [],
  globalLoading: false,
  loadingStates: {},
  theme: 'system',

  // Sidebar actions
  setSidebarOpen: (open: boolean) => {
    set({ sidebarOpen: open });
  },

  setSidebarCollapsed: (collapsed: boolean) => {
    set({ sidebarCollapsed: collapsed });
  },

  toggleSidebar: () => {
    set((state) => ({ sidebarOpen: !state.sidebarOpen }));
  },

  // Toast actions
  addToast: (toast: Omit<Toast, 'id'>) => {
    const id = Math.random().toString(36).substr(2, 9);
    const newToast: Toast = {
      id,
      duration: 5000, // Default 5 seconds
      ...toast,
    };

    set((state) => ({
      toasts: [...state.toasts, newToast],
    }));

    // Auto-remove toast after duration
    if (newToast.duration && newToast.duration > 0) {
      setTimeout(() => {
        get().removeToast(id);
      }, newToast.duration);
    }

    return id;
  },

  removeToast: (id: string) => {
    set((state) => ({
      toasts: state.toasts.filter(toast => toast.id !== id),
    }));
  },

  clearToasts: () => {
    set({ toasts: [] });
  },

  // Modal actions
  openModal: (modal: Omit<Modal, 'id'>) => {
    const id = Math.random().toString(36).substr(2, 9);
    const newModal: Modal = {
      id,
      size: 'md',
      ...modal,
    };

    set((state) => ({
      modals: [...state.modals, newModal],
    }));

    return id;
  },

  closeModal: (id: string) => {
    set((state) => ({
      modals: state.modals.filter(modal => modal.id !== id),
    }));
  },

  closeAllModals: () => {
    set({ modals: [] });
  },

  // Loading actions
  setGlobalLoading: (loading: boolean) => {
    set({ globalLoading: loading });
  },

  setLoading: (key: string, loading: boolean) => {
    set((state) => ({
      loadingStates: {
        ...state.loadingStates,
        [key]: loading,
      },
    }));
  },

  clearLoading: (key: string) => {
    set((state) => {
      const { [key]: removed, ...rest } = state.loadingStates;
      return { loadingStates: rest };
    });
  },

  // Theme actions
  setTheme: (theme: UIState['theme']) => {
    set({ theme });
    
    // Apply theme to document
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else if (theme === 'light') {
      document.documentElement.classList.remove('dark');
    } else {
      // System theme
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      if (prefersDark) {
        document.documentElement.classList.add('dark');
      } else {
        document.documentElement.classList.remove('dark');
      }
    }
  },

  // Utility
  reset: () => {
    set({
      sidebarOpen: false,
      sidebarCollapsed: false,
      toasts: [],
      modals: [],
      globalLoading: false,
      loadingStates: {},
      theme: 'system',
    });
  },
}));

// Convenience hooks
export const useToast = () => {
  const { addToast, removeToast, clearToasts } = useUIStore();
  
  return {
    toast: {
      success: (title: string, message?: string, options?: Partial<Toast>) =>
        addToast({ type: 'success', title, message, ...options }),
      error: (title: string, message?: string, options?: Partial<Toast>) =>
        addToast({ type: 'error', title, message, ...options }),
      warning: (title: string, message?: string, options?: Partial<Toast>) =>
        addToast({ type: 'warning', title, message, ...options }),
      info: (title: string, message?: string, options?: Partial<Toast>) =>
        addToast({ type: 'info', title, message, ...options }),
    },
    removeToast,
    clearToasts,
  };
};

export const useModal = () => {
  const { openModal, closeModal, closeAllModals } = useUIStore();
  
  return {
    openModal,
    closeModal,
    closeAllModals,
    confirm: (
      title: string, 
      message: string, 
      onConfirm: () => void,
      options?: Partial<Modal>
    ) => {
      return openModal({
        type: 'confirmation',
        title,
        content: message,
        onConfirm,
        confirmText: 'Confirm',
        cancelText: 'Cancel',
        ...options,
      });
    },
  };
};

export const useLoading = () => {
  const { setLoading, clearLoading, loadingStates, globalLoading } = useUIStore();
  
  return {
    setLoading,
    clearLoading,
    isLoading: (key: string) => loadingStates[key] || false,
    globalLoading,
  };
};

export const useSidebar = () => {
  const { 
    sidebarOpen, 
    sidebarCollapsed, 
    setSidebarOpen, 
    setSidebarCollapsed, 
    toggleSidebar 
  } = useUIStore();
  
  return {
    sidebarOpen,
    sidebarCollapsed,
    setSidebarOpen,
    setSidebarCollapsed,
    toggleSidebar,
  };
};

export const useTheme = () => {
  const { theme, setTheme } = useUIStore();
  
  return {
    theme,
    setTheme,
    isDark: theme === 'dark' || (theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches),
  };
};
