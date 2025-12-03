# InTicky - Frontend Testing

## Célközönség

Ez a dokumentum a frontend tesztelési stratégiáját mutatja be. Frontend fejlesztők számára készült.

## Testing Stack

**Választott eszközök:**
- **Vitest** - Unit és component tesztek
- **React Testing Library** - Komponens tesztelés
- **Playwright** vagy **Cypress** - E2E tesztek

## Test Típusok

### 1. Unit Tests

**Cél:** Egyedi függvények/utilities tesztelése.

**Példa:**
```typescript
// utils/formatters.test.ts
import { describe, it, expect } from 'vitest';
import { formatDate, formatCurrency } from './formatters';

describe('formatters', () => {
  it('should format date correctly', () => {
    const date = new Date('2024-01-15');
    expect(formatDate(date)).toBe('2024-01-15');
  });
  
  it('should format currency correctly', () => {
    expect(formatCurrency(1000)).toBe('1,000.00 HUF');
  });
});
```

### 2. Component Tests

**Cél:** React komponensek tesztelése.

**Példa:**
```typescript
// components/ticket/TicketCard.test.tsx
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { TicketCard } from './TicketCard';
import type { Ticket } from '../../../types/ticket';

describe('TicketCard', () => {
  const mockTicket: Ticket = {
    id: '1',
    title: 'Test Ticket',
    status: 'new',
    // ...
  };
  
  it('should render ticket title', () => {
    render(<TicketCard ticket={mockTicket} />);
    expect(screen.getByText('Test Ticket')).toBeInTheDocument();
  });
  
  it('should call onSelect when clicked', () => {
    const onSelect = vi.fn();
    render(<TicketCard ticket={mockTicket} onSelect={onSelect} />);
    
    fireEvent.click(screen.getByText('Test Ticket'));
    expect(onSelect).toHaveBeenCalledWith(mockTicket);
  });
});
```

### 3. Integration Tests

**Cél:** Több komponens együttműködésének tesztelése.

**Példa:**
```typescript
// pages/tickets/TicketListPage.test.tsx
import { describe, it, expect } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { TicketListPage } from './TicketListPage';
import { ticketService } from '../../../services/ticketService';

vi.mock('../../../services/ticketService');

describe('TicketListPage', () => {
  it('should load and display tickets', async () => {
    const mockTickets = [
      { id: '1', title: 'Ticket 1' },
      { id: '2', title: 'Ticket 2' },
    ];
    
    vi.mocked(ticketService.getTickets).mockResolvedValue(mockTickets);
    
    render(<TicketListPage />);
    
    await waitFor(() => {
      expect(screen.getByText('Ticket 1')).toBeInTheDocument();
      expect(screen.getByText('Ticket 2')).toBeInTheDocument();
    });
  });
});
```

### 4. E2E Tests

**Cél:** Teljes felhasználói folyamatok tesztelése.

**Playwright példa:**
```typescript
// e2e/tickets.spec.ts
import { test, expect } from '@playwright/test';

test('should create a new ticket', async ({ page }) => {
  await page.goto('/login');
  await page.fill('[name="email"]', 'test@example.com');
  await page.fill('[name="password"]', 'password');
  await page.click('button[type="submit"]');
  
  await page.goto('/tickets');
  await page.click('button:has-text("New Ticket")');
  await page.fill('[name="title"]', 'Test Ticket');
  await page.fill('[name="description"]', 'Test Description');
  await page.click('button:has-text("Create")');
  
  await expect(page.locator('text=Test Ticket')).toBeVisible();
});
```

## Test Coverage

### Követelmények

**Minimum coverage:** 70%

**Kritikus részek:** 90%+
- Business logika
- Form validáció
- API integráció

### Coverage Mérés

**Vitest coverage:**
```json
// package.json
{
  "scripts": {
    "test:coverage": "vitest run --coverage"
  }
}
```

**Futtatás:**
```bash
npm run test:coverage
```

## Testing Best Practices

### 1. Arrange-Act-Assert Pattern

```typescript
it('should create ticket', async () => {
  // Arrange
  const dto = { title: 'Test', description: 'Test' };
  const onSuccess = vi.fn();
  
  // Act
  await handleCreateTicket(dto, onSuccess);
  
  // Assert
  expect(onSuccess).toHaveBeenCalled();
});
```

### 2. Test Nevek

**Konvenció:** `should <expected behavior> when <condition>`

**Példák:**
```typescript
it('should display ticket title when ticket is provided', () => { });
it('should show error message when API call fails', () => { });
it('should redirect to login when user is not authenticated', () => { });
```

### 3. Mocking

**API mocking:**
```typescript
import { vi } from 'vitest';
import { ticketService } from '../services/ticketService';

vi.mock('../services/ticketService');

it('should load tickets', async () => {
  const mockTickets = [{ id: '1', title: 'Test' }];
  vi.mocked(ticketService.getTickets).mockResolvedValue(mockTickets);
  
  // Test
});
```

### 4. Async Testing

```typescript
it('should handle async operations', async () => {
  const promise = loadTickets();
  
  expect(screen.getByText('Loading...')).toBeInTheDocument();
  
  await waitFor(() => {
    expect(screen.getByText('Ticket 1')).toBeInTheDocument();
  });
});
```

## Test Futtatás

### Összes Test

```bash
npm test
```

### Watch Mode

```bash
npm test -- --watch
```

### UI Mode

```bash
npm run test:ui
```

### E2E Tests

```bash
# Playwright
npx playwright test

# Cypress
npx cypress open
```

## További Információk

- [Frontend Setup](./setup.md)
- [Frontend Architektúra](./architecture.md)
- [Coding Standards](./coding-standards.md)
- [Vitest dokumentáció](https://vitest.dev/)
- [React Testing Library](https://testing-library.com/react)

