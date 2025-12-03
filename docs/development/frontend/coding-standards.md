# InTicky - Frontend Coding Standards

## Célközönség

Ez a dokumentum a frontend kódolási szabályokat és best practice-eket mutatja be. Frontend fejlesztők számára készült.

## TypeScript Coding Conventions

### Naming Conventions

**Komponensek:**
- PascalCase
- Főnév vagy főnév + melléknév
- `TicketCard`, `ProjectList`, `UserProfile`

**Fájlok:**
- Komponensek: PascalCase (`TicketCard.tsx`)
- Utilities: camelCase (`formatters.ts`)
- Types: camelCase (`ticket.ts`)

**Változók és függvények:**
- camelCase
- `ticketId`, `getTickets()`, `isLoading`

**Konstansok:**
- UPPER_SNAKE_CASE
- `API_BASE_URL`, `MAX_RETRY_COUNT`

### Type Definitions

**Interface vs Type:**
```typescript
// ✅ Jó - Interface objektumokhoz
interface Ticket {
  id: string;
  title: string;
  status: string;
}

// ✅ Jó - Type union-okhoz, mapped types-hoz
type TicketStatus = 'new' | 'in_progress' | 'resolved' | 'closed';
type TicketWithStatus = Ticket & { status: TicketStatus };
```

## React Best Practices

### Function Components

**Mindig function component használata:**
```typescript
// ✅ Jó
export function TicketCard({ ticket }: TicketCardProps) {
  return <div>{ticket.title}</div>;
}

// ❌ Rossz - Class component
export class TicketCard extends React.Component {
  // ...
}
```

### Props Interface

**Mindig típusozott props:**
```typescript
// ✅ Jó
interface TicketCardProps {
  ticket: Ticket;
  onSelect?: (ticket: Ticket) => void;
  className?: string;
}

export function TicketCard({ ticket, onSelect, className }: TicketCardProps) {
  // ...
}

// ❌ Rossz - Any használata
export function TicketCard(props: any) {
  // ...
}
```

### Hooks Használata

**Custom hooks:**
```typescript
// ✅ Jó - Custom hook
export function useTickets() {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  // ...
  return { tickets, loading };
}

// Használat
function TicketList() {
  const { tickets, loading } = useTickets();
  // ...
}
```

**Hooks szabályok:**
- Ne használj hooks-ot feltételesen
- Ne használj hooks-ot loop-okban
- Custom hooks `use` prefix-szel kezdődjenek

### State Management

**Local state vs Global state:**
```typescript
// ✅ Jó - Local state egyszerű esetekhez
function TicketCard({ ticket }: TicketCardProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  // ...
}

// ✅ Jó - Global state komplex esetekhez
function TicketList() {
  const tickets = useTicketStore((state) => state.tickets);
  // ...
}
```

## Code Style

### Formázás

**Indentáció:**
- 2 spaces (nem tab)
- Konzisztens formázás

**Sor hossz:**
- Maximum 100 karakter
- Hosszabb sorok tördelése

**Példa:**
```typescript
// ✅ Jó
const handleSubmit = async (
  ticketData: CreateTicketDTO
): Promise<void> => {
  // ...
};

// ❌ Rossz
const handleSubmit = async (ticketData: CreateTicketDTO): Promise<void> => { // ...
};
```

### Import Sorrend

**Konvenció:**
1. React és React-related
2. Third-party libraries
3. Internal modules (components, services, utils)
4. Types
5. Styles

**Példa:**
```typescript
// React
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

// Third-party
import axios from 'axios';
import { format } from 'date-fns';

// Internal
import { ticketService } from '../../services/ticketService';
import { TicketCard } from '../../components/ticket/TicketCard';

// Types
import type { Ticket } from '../../types/ticket';

// Styles
import './TicketList.css';
```

## Komponens Struktúra

### Komponens Template

```typescript
import { useState, useEffect } from 'react';
import type { Ticket } from '../../types/ticket';
import { ticketService } from '../../services/ticketService';
import './TicketList.css';

interface TicketListProps {
  tenantId: string;
  onTicketSelect?: (ticket: Ticket) => void;
}

export function TicketList({ tenantId, onTicketSelect }: TicketListProps) {
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(false);
  
  useEffect(() => {
    loadTickets();
  }, [tenantId]);
  
  const loadTickets = async () => {
    setLoading(true);
    try {
      const data = await ticketService.getTickets();
      setTickets(data);
    } catch (error) {
      console.error('Failed to load tickets:', error);
    } finally {
      setLoading(false);
    }
  };
  
  if (loading) {
    return <div>Loading...</div>;
  }
  
  return (
    <div className="ticket-list">
      {tickets.map((ticket) => (
        <TicketCard
          key={ticket.id}
          ticket={ticket}
          onClick={() => onTicketSelect?.(ticket)}
        />
      ))}
    </div>
  );
}
```

## Error Handling

### Try-Catch Használata

```typescript
// ✅ Jó
const handleSubmit = async (data: CreateTicketDTO) => {
  try {
    setLoading(true);
    const ticket = await ticketService.createTicket(data);
    onSuccess(ticket);
  } catch (error) {
    if (error instanceof Error) {
      setError(error.message);
    } else {
      setError('An unexpected error occurred');
    }
  } finally {
    setLoading(false);
  }
};
```

### Error Boundaries

```typescript
// ErrorBoundary.tsx
import { Component, ReactNode } from 'react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
}

export class ErrorBoundary extends Component<Props, State> {
  state = { hasError: false };
  
  static getDerivedStateFromError() {
    return { hasError: true };
  }
  
  render() {
    if (this.state.hasError) {
      return <div>Something went wrong</div>;
    }
    return this.props.children;
  }
}
```

## Performance

### Memoization

**React.memo:**
```typescript
// ✅ Jó - Memoization ha szükséges
export const TicketCard = React.memo(function TicketCard({ 
  ticket 
}: TicketCardProps) {
  return <div>{ticket.title}</div>;
});
```

**useMemo, useCallback:**
```typescript
// ✅ Jó - useMemo drága számításokhoz
const expensiveValue = useMemo(() => {
  return computeExpensiveValue(data);
}, [data]);

// ✅ Jó - useCallback callback-ekhez
const handleClick = useCallback(() => {
  onSelect(ticket);
}, [ticket, onSelect]);
```

## További Információk

- [Frontend Setup](./setup.md)
- [Frontend Architektúra](./architecture.md)
- [Testing](./testing.md)
- [React Best Practices](https://react.dev/learn)

