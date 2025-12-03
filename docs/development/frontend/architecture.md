# InTicky - Frontend Architektúra

## Célközönség

Ez a dokumentum a frontend architektúráját és komponens struktúráját mutatja be. Frontend fejlesztők számára készült.

## Komponens Struktúra

### Mappa Struktúra

```
frontend/src/
├── components/        # Újrafelhasználható komponensek
│   ├── common/       # Általános komponensek (Button, Input, stb.)
│   ├── layout/       # Layout komponensek (Header, Sidebar, stb.)
│   └── ticket/       # Ticket specifikus komponensek
├── pages/            # Oldalak/routes
│   ├── auth/         # Autentikáció oldalak
│   ├── tickets/      # Ticket oldalak
│   ├── projects/     # Projekt oldalak
│   └── dashboard/    # Dashboard
├── services/         # API hívások
│   ├── api.ts        # Axios instance
│   ├── ticketService.ts
│   └── projectService.ts
├── store/            # State management
│   ├── slices/       # Redux slices (ha Redux Toolkit)
│   └── store.ts
├── hooks/            # Custom hooks
│   ├── useAuth.ts
│   ├── useTenant.ts
│   └── useModule.ts
├── utils/            # Segédfunkciók
│   ├── formatters.ts
│   └── validators.ts
├── types/            # TypeScript típusok
│   ├── ticket.ts
│   ├── project.ts
│   └── user.ts
└── App.tsx           # Fő komponens
```

## State Management

### Redux Toolkit (Ajánlott)

**Store setup:**
```typescript
// store/store.ts
import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import tenantReducer from './slices/tenantSlice';
import ticketReducer from './slices/ticketSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    tenant: tenantReducer,
    tickets: ticketReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
```

**Slice példa:**
```typescript
// store/slices/ticketSlice.ts
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { ticketService } from '../../services/ticketService';

export const fetchTickets = createAsyncThunk(
  'tickets/fetchTickets',
  async () => {
    return await ticketService.getTickets();
  }
);

const ticketSlice = createSlice({
  name: 'tickets',
  initialState: {
    items: [],
    loading: false,
    error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchTickets.pending, (state) => {
        state.loading = true;
      })
      .addCase(fetchTickets.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload;
      });
  },
});

export default ticketSlice.reducer;
```

### Zustand (Alternatíva)

**Store példa:**
```typescript
// store/ticketStore.ts
import { create } from 'zustand';
import { ticketService } from '../services/ticketService';

interface TicketStore {
  tickets: Ticket[];
  loading: boolean;
  fetchTickets: () => Promise<void>;
}

export const useTicketStore = create<TicketStore>((set) => ({
  tickets: [],
  loading: false,
  fetchTickets: async () => {
    set({ loading: true });
    const tickets = await ticketService.getTickets();
    set({ tickets, loading: false });
  },
}));
```

## Routing

### React Router

**Router setup:**
```typescript
// App.tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import LoginPage from './pages/auth/LoginPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import TicketListPage from './pages/tickets/TicketListPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/tickets"
          element={
            <ProtectedRoute>
              <TicketListPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}
```

**Protected Route:**
```typescript
// components/auth/ProtectedRoute.tsx
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  return <>{children}</>;
}
```

## API Integráció

### Axios Instance

**API client:**
```typescript
// services/api.ts
import axios from 'axios';
import { useAuthStore } from '../store/authStore';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - Token hozzáadása
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  
  // Tenant ID hozzáadása
  const tenantId = useTenantStore.getState().tenantId;
  if (tenantId) {
    config.headers['X-Tenant-ID'] = tenantId;
  }
  
  return config;
});

// Response interceptor - Error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirect to login
    }
    return Promise.reject(error);
  }
);

export default api;
```

**Service példa:**
```typescript
// services/ticketService.ts
import api from './api';
import { Ticket, CreateTicketDTO } from '../types/ticket';

export const ticketService = {
  getTickets: async (): Promise<Ticket[]> => {
    const response = await api.get('/tickets');
    return response.data.data;
  },
  
  getTicket: async (id: string): Promise<Ticket> => {
    const response = await api.get(`/tickets/${id}`);
    return response.data.data;
  },
  
  createTicket: async (dto: CreateTicketDTO): Promise<Ticket> => {
    const response = await api.post('/tickets', dto);
    return response.data.data;
  },
};
```

## Modul Check Implementáció

### Custom Hook

```typescript
// hooks/useModule.ts
import { useState, useEffect } from 'react';
import { useTenant } from './useTenant';
import { moduleService } from '../services/moduleService';

export function useModule(moduleName: string) {
  const { tenant } = useTenant();
  const [isActive, setIsActive] = useState(false);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    if (tenant?.id) {
      moduleService
        .checkModuleActive(moduleName, tenant.id)
        .then(setIsActive)
        .finally(() => setLoading(false));
    }
  }, [moduleName, tenant?.id]);
  
  return { isActive, loading };
}
```

**Használat:**
```typescript
// pages/projects/ProjectListPage.tsx
import { useModule } from '../../hooks/useModule';

function ProjectListPage() {
  const { isActive, loading } = useModule('project');
  
  if (loading) {
    return <Loading />;
  }
  
  if (!isActive) {
    return <ModuleNotAvailable module="project" />;
  }
  
  return <ProjectList />;
}
```

## Komponens Best Practices

### Function Components

```typescript
// ✅ Jó - Function component
interface TicketCardProps {
  ticket: Ticket;
  onSelect: (ticket: Ticket) => void;
}

export function TicketCard({ ticket, onSelect }: TicketCardProps) {
  return (
    <div onClick={() => onSelect(ticket)}>
      <h3>{ticket.title}</h3>
      <p>{ticket.description}</p>
    </div>
  );
}
```

### Hooks Használata

```typescript
// ✅ Jó - Custom hook
export function useTickets() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(false);
  
  useEffect(() => {
    setLoading(true);
    ticketService
      .getTickets()
      .then(setTickets)
      .finally(() => setLoading(false));
  }, []);
  
  return { tickets, loading };
}
```

## További Információk

- [Frontend Setup](./setup.md)
- [Coding Standards](./coding-standards.md)
- [Testing](./testing.md)
- [React dokumentáció](https://react.dev/)

