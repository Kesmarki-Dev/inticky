# Frontend Agent - Feladatlista

## Projekt Kontextus

Az **InTicky** egy modern, multi-tenant ticketing rendszer, amely egy régi Delphi 2009-es asztali alkalmazás modernizált, webes változata. A rendszer támogatja a support ticketek kezelését és a fejlesztési projektek menedzsmentjét.

**Technológiai Stack:**
- **React 18+** - UI library
- **TypeScript** - Type safety
- **Vite** - Build tool
- **React Router** - Routing
- **Redux Toolkit** vagy **Zustand** - State management
- **Axios** - HTTP client
- **Vitest** - Testing

**Architektúra:**
- Single Page Application (SPA)
- Component-based architecture
- API-driven (REST API)
- Multi-tenant support (tenant kiválasztás)

## Függőségek Más Agentekkel

### Backend Agent
- **Függőség:** REST API végpontok
- **Időpont:** Frontend fejlesztés során
- **Kommunikáció:** Frontend agent API-kat hív, backend agent biztosítja őket

### Database Agent
- **Függőség:** Nincs közvetlen függőség
- **Időpont:** N/A
- **Kommunikáció:** Frontend agent API-kon keresztül éri el az adatokat

## Fázisok és Feladatok

### Fázis 1: Projekt Inicializálás (1. hét)

**Cél:** Alapvető React projekt struktúra létrehozása.

#### 1.1 React Projekt Létrehozása
- [ ] Vite projekt inicializálás
  - [ ] `npm create vite@latest . -- --template react-ts`
  - [ ] Vagy `yarn create vite . --template react-ts`
- [ ] Projekt struktúra létrehozása:
  ```
  frontend/
  ├── src/
  │   ├── components/     # Újrafelhasználható komponensek
  │   ├── pages/          # Oldalak/routes
  │   ├── services/       # API hívások
  │   ├── store/          # State management
  │   ├── hooks/          # Custom hooks
  │   ├── utils/          # Segédfunkciók
  │   ├── types/          # TypeScript típusok
  │   └── App.tsx
  └── package.json
  ```

#### 1.2 TypeScript Konfiguráció
- [ ] `tsconfig.json` beállítása
- [ ] Strict mode engedélyezése
- [ ] Path aliases konfigurálása (`@/components`, `@/services`, stb.)

#### 1.3 Dependencies Telepítése
- [ ] React Router: `npm install react-router-dom`
- [ ] State management:
  - [ ] Redux Toolkit: `npm install @reduxjs/toolkit react-redux`
  - [ ] Vagy Zustand: `npm install zustand`
- [ ] HTTP client: `npm install axios`
- [ ] Form handling: `npm install react-hook-form` (opcionális)
- [ ] UI library (opcionális):
  - [ ] Material-UI: `npm install @mui/material @emotion/react @emotion/styled`
  - [ ] Vagy Tailwind CSS: `npm install -D tailwindcss`
- [ ] Testing: `npm install -D vitest @testing-library/react @testing-library/jest-dom`

#### 1.4 Build és Dev Server Konfiguráció
- [ ] `vite.config.ts` beállítása
- [ ] Environment változók (`.env`, `.env.local`)
- [ ] Proxy konfiguráció (API hívásokhoz)

**Dokumentáció:**
- [../docs/development/frontend/setup.md](../docs/development/frontend/setup.md)
- [../plan/02_technikai_kovetelmenyek.md](../plan/02_technikai_kovetelmenyek.md)

### Fázis 2: Alapvető Struktúra (1-2. hét)

**Cél:** Routing, layout és navigation implementálása.

#### 2.1 Routing Setup
- [ ] React Router konfiguráció
- [ ] Route definíciók:
  - [ ] `/login` - Bejelentkezés
  - [ ] `/dashboard` - Dashboard
  - [ ] `/tickets` - Ticket lista (ha ticket modul aktív)
  - [ ] `/tickets/:id` - Ticket részletek
  - [ ] `/projects` - Projekt lista (ha project modul aktív)
  - [ ] `/projects/:id` - Projekt részletek
  - [ ] `/users` - Felhasználók (admin)
  - [ ] `/settings` - Beállítások
- [ ] Protected routes implementálása
- [ ] 404 oldal

#### 2.2 Layout Komponensek
- [ ] Main Layout komponens
  - [ ] Header komponens
  - [ ] Sidebar/Navigation komponens
  - [ ] Footer komponens (opcionális)
- [ ] Navigation menu
  - [ ] Modul alapú menüpontok (csak aktív modulok)
  - [ ] Tenant váltás (ha több tenant)
- [ ] Responsive design
  - [ ] Mobile menu
  - [ ] Sidebar collapse

#### 2.3 State Management Setup
- [ ] Store konfiguráció (Redux Toolkit vagy Zustand)
- [ ] Auth store:
  - [ ] User state
  - [ ] Token state
  - [ ] Login/logout actions
- [ ] Tenant store:
  - [ ] Current tenant state
  - [ ] Tenant list (ha több tenant)
  - [ ] Tenant switch action
- [ ] UI store (opcionális):
  - [ ] Sidebar state
  - [ ] Modal state
  - [ ] Notification state

**Dokumentáció:**
- [../docs/development/frontend/architecture.md](../docs/development/frontend/architecture.md)

### Fázis 3: Autentikáció és Tenant Kezelés (1. hét)

**Cél:** Bejelentkezés és tenant kiválasztás implementálása.

#### 3.1 Bejelentkezés Oldal
- [ ] Login komponens létrehozása
- [ ] Form validáció (email, password)
- [ ] API integráció:
  - [ ] POST `/api/v1/auth/login` hívás
  - [ ] JWT token tárolása (localStorage vagy cookie)
  - [ ] Error handling
- [ ] Loading state
- [ ] Success redirect (dashboard)

#### 3.2 JWT Token Kezelés
- [ ] Token storage (localStorage vagy httpOnly cookie)
- [ ] Token refresh mechanizmus
- [ ] Axios interceptor:
  - [ ] Token automatikus hozzáadása header-hez
  - [ ] Token refresh 401 esetén
  - [ ] Logout 403 esetén

#### 3.3 Protected Routes
- [ ] ProtectedRoute komponens
- [ ] Authentication check
- [ ] Redirect login-re, ha nincs token
- [ ] Tenant check (ha szükséges)

#### 3.4 Tenant Kiválasztás
- [ ] Tenant selector komponens
- [ ] Tenant list API hívás
- [ ] Tenant váltás funkció
- [ ] Tenant context beállítása
- [ ] X-Tenant-ID header automatikus hozzáadása

**Dokumentáció:**
- [../docs/development/frontend/architecture.md](../docs/development/frontend/architecture.md) - State management

### Fázis 4: Dashboard (1. hét)

**Cél:** Főoldal és statisztikák implementálása.

#### 4.1 Dashboard Oldal
- [ ] Dashboard komponens létrehozása
- [ ] Statisztika kártyák:
  - [ ] Nyitott ticketek száma (ha ticket modul aktív)
  - [ ] Aktív projektek száma (ha project modul aktív)
  - [ ] Felhasználók száma
- [ ] Chart komponensek (opcionális):
  - [ ] Ticket státusz szerinti eloszlás
  - [ ] Projekt státusz szerinti eloszlás
  - [ ] Időszak alapú statisztikák
- [ ] Recent activity lista

#### 4.2 API Integráció
- [ ] Dashboard API hívások
- [ ] Data fetching (React Query vagy SWR - opcionális)
- [ ] Loading és error state kezelés

### Fázis 5: Ticket Modul UI (2-3. hét)

**Cél:** Support ticketek kezelésének UI-ja (ha ticket modul aktív).

#### 5.1 Ticket Lista Oldal
- [ ] TicketList komponens
- [ ] Ticket lista táblázat vagy kártya nézet
- [ ] Szűrés:
  - [ ] Státusz szerint
  - [ ] Prioritás szerint
  - [ ] Kategória szerint
  - [ ] Hozzárendelt szerint
  - [ ] Dátum szerint
- [ ] Keresés:
  - [ ] Cím szerint
  - [ ] Leírás szerint
  - [ ] Ticket szám szerint
- [ ] Rendezés (dátum, prioritás, státusz)
- [ ] Pagináció
- [ ] Modul check: csak akkor jelenik meg, ha ticket modul aktív

#### 5.2 Ticket Részletek Oldal
- [ ] TicketDetail komponens
- [ ] Ticket információk megjelenítése
- [ ] Kommentek lista
- [ ] Komment hozzáadása
- [ ] Csatolmányok lista
- [ ] Csatolmány feltöltés
- [ ] Ticket státusz változtatás
- [ ] Ticket hozzárendelés
- [ ] Ticket prioritás módosítása

#### 5.3 Új Ticket Létrehozása
- [ ] TicketCreate komponens
- [ ] Form mezők:
  - [ ] Cím
  - [ ] Leírás (rich text editor - opcionális)
  - [ ] Kategória választás
  - [ ] Prioritás választás
  - [ ] Típus választás
  - [ ] Csatolmányok feltöltése
- [ ] Form validáció
- [ ] API integráció: POST `/api/v1/tickets`

#### 5.4 Ticket API Service
- [ ] `services/tickets.ts` létrehozása
- [ ] API metódusok:
  - [ ] `getTickets(filters, pagination)`
  - [ ] `getTicket(id)`
  - [ ] `createTicket(data)`
  - [ ] `updateTicket(id, data)`
  - [ ] `deleteTicket(id)`
  - [ ] `addComment(ticketId, comment)`
  - [ ] `updateTicketStatus(ticketId, status)`

**Dokumentáció:**
- [../plan/01_funkcio_lista.md](../plan/01_funkcio_lista.md) - Ticket funkciók
- [../docs/architecture/api-design.md](../docs/architecture/api-design.md) - API végpontok

### Fázis 6: Project Modul UI (2-3. hét)

**Cél:** Fejlesztési projektek kezelésének UI-ja (ha project modul aktív).

#### 6.1 Projekt Lista Oldal
- [ ] ProjectList komponens
- [ ] Projekt lista táblázat vagy kártya nézet
- [ ] Szűrés (státusz, customer, manager, dátum)
- [ ] Keresés
- [ ] Rendezés
- [ ] Pagináció
- [ ] Modul check: csak akkor jelenik meg, ha project modul aktív

#### 6.2 Projekt Részletek Oldal
- [ ] ProjectDetail komponens
- [ ] Projekt információk
- [ ] Projekt feladatok lista
- [ ] Projekt feladat létrehozása
- [ ] Projekt feladat szerkesztése
- [ ] Időkövetés lista
- [ ] Idő rögzítése
- [ ] Projekt statisztikák

#### 6.3 Új Projekt Létrehozása
- [ ] ProjectCreate komponens
- [ ] Form mezők
- [ ] Form validáció
- [ ] API integráció

#### 6.4 Projekt API Service
- [ ] `services/projects.ts` létrehozása
- [ ] API metódusok implementálása

**Dokumentáció:**
- [../plan/01_funkcio_lista.md](../plan/01_funkcio_lista.md) - Projekt funkciók

### Fázis 7: AI Chat UI (1-2. hét)

**Cél:** AI chat és agent funkciók UI-ja (ha ai_agent modul aktív).

#### 7.1 AI Chat Komponens
- [ ] ChatUI komponens létrehozása
- [ ] Chat üzenetek lista
- [ ] Üzenet küldés input
- [ ] Streaming válaszok (opcionális)
- [ ] Chat session kezelés
- [ ] Session lista (oldal vagy sidebar)

#### 7.2 AI Chat API Service
- [ ] `services/ai-chat.ts` létrehozása
- [ ] API metódusok:
  - [ ] `sendMessage(sessionId, message)`
  - [ ] `getSessions()`
  - [ ] `getSession(sessionId)`
  - [ ] `getMessages(sessionId)`
  - [ ] `createSession()`

#### 7.3 Execution Plan Confirmation
- [ ] Execution plan modal
- [ ] Plan részletek megjelenítése
- [ ] Confirmation gomb
- [ ] API integráció: POST `/api/v1/ai/execution-plans/:id/confirm`

**Dokumentáció:**
- [../plan/01_funkcio_lista.md](../plan/01_funkcio_lista.md) - AI Agent funkciók
- [../docs/development/backend/ai-agent-integration.md](../docs/development/backend/ai-agent-integration.md)

### Fázis 8: Modul Check Implementáció (Folyamatos)

**Cél:** Modul aktiválás ellenőrzése frontend-en.

#### 8.1 Modul Check Hook
- [ ] `hooks/useModule.ts` létrehozása
- [ ] Modul aktiválás ellenőrzése
- [ ] Loading state
- [ ] Error handling

#### 8.2 Modul Check Komponens
- [ ] `components/ModuleNotAvailable.tsx` létrehozása
- [ ] Modul nem elérhető üzenet
- [ ] Upgrade gomb (ha szükséges)

#### 8.3 Modul Check Használata
- [ ] Minden modul specifikus oldalon modul check
- [ ] Menüpontok csak aktív modulok esetén
- [ ] Route protection modul alapján

**Dokumentáció:**
- [../plan/08_modularis_felepites.md](../plan/08_modularis_felepites.md) - Modul aktiválás

### Fázis 9: Testing (Folyamatos)

**Cél:** Frontend komponensek és funkciók tesztelése.

#### 9.1 Unit Tesztek
- [ ] Komponens tesztek (React Testing Library)
- [ ] Hook tesztek
- [ ] Utility function tesztek
- [ ] Service tesztek (API mock-olással)

#### 9.2 Integration Tesztek
- [ ] User flow tesztek
- [ ] API integráció tesztek

#### 9.3 E2E Tesztek (Opcionális)
- [ ] Playwright vagy Cypress setup
- [ ] Kritikus user flow-ok tesztelése

**Dokumentáció:**
- [../docs/development/frontend/testing.md](../docs/development/frontend/testing.md)

## Fontos Emlékeztetők

1. **TypeScript:** Mindig típusozott kód, `any` használata kerülendő
2. **Modul check:** Minden modul specifikus funkciónál ellenőrizd a modul aktiválást
3. **Tenant ID:** API hívásoknál automatikus tenant ID hozzáadása header-hez
4. **Error handling:** Minden API hívásnál error handling
5. **Loading state:** Asynchronous műveleteknél loading state kezelése
6. **Responsive design:** Mobile-first megközelítés

## Dokumentáció Linkek

- [Frontend Setup](../docs/development/frontend/setup.md)
- [Frontend Architektúra](../docs/development/frontend/architecture.md)
- [Coding Standards](../docs/development/frontend/coding-standards.md)
- [Testing](../docs/development/frontend/testing.md)
- [API Design](../docs/architecture/api-design.md)
- [Tervezési Dokumentumok](../plan/)

