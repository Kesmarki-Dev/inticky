# InTicky - Adatbázis Tervezés

## Áttekintés

Ez a dokumentum az InTicky ticketing rendszer adatbázis struktúrájának tervezését tartalmazza. A tervezés multi-tenant architektúrát vesz figyelembe.

## Tervezési Döntés: Opció 2 - Külön Projektek és Ticketek

**Választott modell:** Support feladatok = Ticketek, Fejlesztési projektek = Külön Projekt entitás

**Következmények:**
- Ticketek csak support feladatokhoz (project_id mező nincs)
- Projektek külön entitások, project_tasks-okkal
- Tiszta elkülönítés support és fejlesztés között
- Projekt menedzsment funkciók külön implementálva

## Adatbázis Választás

**Ajánlás: PostgreSQL**

**Indoklás:**
- Robusztus, enterprise szintű
- Kiváló multi-tenant támogatás
- JSON/JSONB támogatás (rugalmas adatokhoz)
- Teljes ACID compliance
- Széles körben használt, jó dokumentáció
- Ingyenes és open source

## Multi-Tenant Modell

**Választott stratégia: Shared Database, Shared Schema**

**Előnyök:**
- Egyszerűbb adminisztráció
- Könnyebb skálázás
- Alacsonyabb költségek
- Könnyebb backup és maintenance

**Biztonsági megfontolások:**
- Minden táblában `tenant_id` mező kötelező
- Adatbázis szintű constraint-ek
- Row Level Security (RLS) használata PostgreSQL-ben
- Application szintű validáció

## Főbb Entitások

### 1. Tenants (Bérlők)

```sql
tenants
├── id (UUID, PRIMARY KEY)
├── name (VARCHAR, NOT NULL)
├── slug (VARCHAR, UNIQUE, NOT NULL) -- URL-barát azonosító
├── domain (VARCHAR, NULLABLE) -- Egyedi domain (opcionális)
├── logo_url (VARCHAR, NULLABLE)
├── settings (JSONB) -- Tenant specifikus beállítások
├── subscription_tier (VARCHAR) -- free, basic, premium
├── is_active (BOOLEAN, DEFAULT true)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)
```

### 2. Tenant Modules (Tenant Modulok)

**Megjegyzés:** Opció 1 (Moduláris Mikroszolgáltatások) alapján minden modul külön service, és tenant-onként aktiválható.

```sql
tenant_modules
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── module_name (VARCHAR, NOT NULL) -- 'ticket', 'project', 'report', 'file', stb.
├── is_active (BOOLEAN, DEFAULT false)
├── activated_at (TIMESTAMP, NULLABLE)
├── subscription_tier (VARCHAR, NULLABLE) -- free, basic, premium, enterprise
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

PRIMARY KEY: (tenant_id, module_name)
INDEX: (tenant_id, is_active)
INDEX: module_name
```

**Modul nevek:**
- `ticket` - Support ticketek modul
- `project` - Fejlesztési projektek modul
- `user` - Felhasználó kezelés modul
- `auth` - Autentikáció modul
- `notification` - Értesítések modul
- `file` - Fájl kezelés modul
- `report` - Jelentések modul (opcionális)
- `ai_agent` - AI chat és agent modul (AgentInSec)

### 3. Users (Felhasználók)

```sql
users
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── email (VARCHAR, UNIQUE, NOT NULL)
├── password_hash (VARCHAR, NOT NULL)
├── first_name (VARCHAR)
├── last_name (VARCHAR)
├── avatar_url (VARCHAR, NULLABLE)
├── role (VARCHAR, NOT NULL) -- admin, support_manager, agent, customer, developer
├── is_active (BOOLEAN, DEFAULT true)
├── email_verified (BOOLEAN, DEFAULT false)
├── last_login_at (TIMESTAMP, NULLABLE)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, email)
INDEX: email
```

**Megjegyzés:** Email egyediség tenant-onként vagy globálisan? Döntés szükséges.

### 4. Tickets (Jegyek - Support feladatok)

**Megjegyzés:** 
- Opció 2 alapján a ticketek csak support feladatokhoz használatosak. Fejlesztési projektek külön entitás (Projects).
- Opció 1 (Moduláris Mikroszolgáltatások) alapján a ticket modul külön service (ticket-service).

```sql
tickets
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── ticket_number (VARCHAR, UNIQUE, NOT NULL) -- TNT-001, TNT-002 formátum
├── title (VARCHAR, NOT NULL)
├── description (TEXT)
├── category_id (UUID, FOREIGN KEY -> ticket_categories.id)
├── priority (VARCHAR, NOT NULL) -- low, medium, high, critical
├── status (VARCHAR, NOT NULL) -- new, in_progress, resolved, closed, cancelled
├── assigned_to_id (UUID, FOREIGN KEY -> users.id, NULLABLE)
├── created_by_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── type (VARCHAR, DEFAULT 'support') -- support, bug, question, feature_request
├── due_date (TIMESTAMP, NULLABLE)
├── resolved_at (TIMESTAMP, NULLABLE)
├── closed_at (TIMESTAMP, NULLABLE)
├── metadata (JSONB) -- További egyedi mezők
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, status)
INDEX: (tenant_id, assigned_to_id)
INDEX: (tenant_id, created_by_id)
INDEX: (tenant_id, category_id)
INDEX: (tenant_id, type)
INDEX: ticket_number
INDEX: created_at
```

### 5. Ticket Categories (Kategóriák)

```sql
ticket_categories
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── name (VARCHAR, NOT NULL)
├── description (TEXT, NULLABLE)
├── color (VARCHAR) -- Hex színkód
├── icon (VARCHAR, NULLABLE)
├── is_active (BOOLEAN, DEFAULT true)
├── sort_order (INTEGER, DEFAULT 0)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

UNIQUE: (tenant_id, name)
INDEX: (tenant_id, is_active)
```

### 6. Ticket Statuses (Státuszok)

```sql
ticket_statuses
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── name (VARCHAR, NOT NULL)
├── slug (VARCHAR, NOT NULL) -- new, in_progress, resolved, stb.
├── color (VARCHAR)
├── is_default (BOOLEAN, DEFAULT false)
├── is_closed (BOOLEAN, DEFAULT false) -- Lezárt státusz jelzője
├── sort_order (INTEGER, DEFAULT 0)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

UNIQUE: (tenant_id, slug)
INDEX: (tenant_id, is_default)
```

### 7. Comments (Kommentek - Ticketekhez)

**Megjegyzés:** Opció 2 alapján a kommentek csak support ticketekhez kapcsolódnak.

```sql
comments
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── ticket_id (UUID, FOREIGN KEY -> tickets.id, NOT NULL)
├── user_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── content (TEXT, NOT NULL)
├── is_internal (BOOLEAN, DEFAULT false) -- Csak ügynökök láthatják
├── is_edited (BOOLEAN, DEFAULT false)
├── edited_at (TIMESTAMP, NULLABLE)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, ticket_id)
INDEX: (tenant_id, user_id)
INDEX: created_at
```

### 7.1. Project Task Comments (Kommentek - Projekt Feladatokhoz)

```sql
project_task_comments
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── project_task_id (UUID, FOREIGN KEY -> project_tasks.id, NOT NULL)
├── user_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── content (TEXT, NOT NULL)
├── is_internal (BOOLEAN, DEFAULT false)
├── is_edited (BOOLEAN, DEFAULT false)
├── edited_at (TIMESTAMP, NULLABLE)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, project_task_id)
INDEX: (tenant_id, user_id)
INDEX: created_at
```

### 8. Attachments (Csatolmányok - Ticketekhez)

**Megjegyzés:** Opció 2 alapján a csatolmányok lehetnek ticketekhez és projekt feladatokhoz is.

```sql
attachments
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── ticket_id (UUID, FOREIGN KEY -> tickets.id, NULLABLE)
├── comment_id (UUID, FOREIGN KEY -> comments.id, NULLABLE)
├── file_name (VARCHAR, NOT NULL)
├── file_path (VARCHAR, NOT NULL) -- S3 path vagy lokális path
├── file_size (BIGINT, NOT NULL) -- bytes
├── file_type (VARCHAR) -- MIME type
├── uploaded_by_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, ticket_id)
INDEX: (tenant_id, comment_id)
```

### 8.1. Project Task Attachments (Csatolmányok - Projekt Feladatokhoz)

```sql
project_task_attachments
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── project_task_id (UUID, FOREIGN KEY -> project_tasks.id, NULLABLE)
├── project_task_comment_id (UUID, FOREIGN KEY -> project_task_comments.id, NULLABLE)
├── file_name (VARCHAR, NOT NULL)
├── file_path (VARCHAR, NOT NULL)
├── file_size (BIGINT, NOT NULL)
├── file_type (VARCHAR)
├── uploaded_by_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, project_task_id)
INDEX: (tenant_id, project_task_comment_id)
```

### 9. Tags (Címkék)

```sql
tags
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── name (VARCHAR, NOT NULL)
├── color (VARCHAR, NULLABLE)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

UNIQUE: (tenant_id, name)
```

### 9.1. Ticket Tags (Jegy-Címke kapcsolás)

```sql
ticket_tags
├── ticket_id (UUID, FOREIGN KEY -> tickets.id, NOT NULL)
├── tag_id (UUID, FOREIGN KEY -> tags.id, NOT NULL)
└── PRIMARY KEY (ticket_id, tag_id)

INDEX: ticket_id
INDEX: tag_id
```

### 10. Projects (Projektek - Fejlesztési projektek)

**Megjegyzés:** Opció 1 (Moduláris Mikroszolgáltatások) alapján a projekt modul külön service (project-service).

**Megjegyzés:** Opció 2 alapján a projektek külön entitások, nem kapcsolódnak ticketekhez. A projektekhez project_tasks kapcsolódnak.

```sql
projects
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── project_number (VARCHAR, UNIQUE, NOT NULL) -- PRJ-001, PRJ-002 formátum
├── name (VARCHAR, NOT NULL)
├── description (TEXT, NULLABLE)
├── status (VARCHAR, NOT NULL) -- planning, in_progress, on_hold, completed, cancelled
├── customer_id (UUID, FOREIGN KEY -> users.id, NULLABLE)
├── manager_id (UUID, FOREIGN KEY -> users.id, NULLABLE)
├── budget (DECIMAL, NULLABLE)
├── estimated_hours (DECIMAL, NULLABLE)
├── actual_hours (DECIMAL, NULLABLE) -- Számított a project_tasks alapján
├── start_date (DATE, NULLABLE)
├── end_date (DATE, NULLABLE)
├── due_date (DATE, NULLABLE)
├── completed_at (TIMESTAMP, NULLABLE)
├── quote_id (UUID, FOREIGN KEY -> quotes.id, NULLABLE)
├── order_id (UUID, FOREIGN KEY -> orders.id, NULLABLE)
├── metadata (JSONB) -- További egyedi mezők
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, status)
INDEX: (tenant_id, customer_id)
INDEX: (tenant_id, manager_id)
INDEX: project_number
INDEX: created_at
```

### 10.1. Project Tasks (Projekt feladatok)

```sql
project_tasks
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── project_id (UUID, FOREIGN KEY -> projects.id, NOT NULL)
├── task_number (VARCHAR, NOT NULL) -- PRJ-001-001, PRJ-001-002 formátum
├── title (VARCHAR, NOT NULL)
├── description (TEXT, NULLABLE)
├── status (VARCHAR, NOT NULL) -- todo, in_progress, review, done, blocked
├── priority (VARCHAR, NOT NULL) -- low, medium, high, critical
├── assigned_to_id (UUID, FOREIGN KEY -> users.id, NULLABLE)
├── created_by_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── estimated_hours (DECIMAL, NULLABLE)
├── actual_hours (DECIMAL, NULLABLE) -- Time tracking alapján
├── due_date (DATE, NULLABLE)
├── completed_at (TIMESTAMP, NULLABLE)
├── sort_order (INTEGER, DEFAULT 0) -- Feladat sorrendje
├── metadata (JSONB)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, project_id)
INDEX: (tenant_id, project_id, status)
INDEX: (tenant_id, assigned_to_id)
INDEX: task_number
```

### 10.2. Quotes (Árajánlatok)

```sql
quotes
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── quote_number (VARCHAR, UNIQUE, NOT NULL) -- QTE-001 formátum
├── project_id (UUID, FOREIGN KEY -> projects.id, NULLABLE)
├── customer_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── title (VARCHAR, NOT NULL)
├── description (TEXT, NULLABLE)
├── amount (DECIMAL, NOT NULL)
├── currency (VARCHAR, DEFAULT 'HUF')
├── status (VARCHAR, NOT NULL) -- draft, sent, accepted, rejected, expired
├── valid_until (DATE, NULLABLE)
├── accepted_at (TIMESTAMP, NULLABLE)
├── metadata (JSONB)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, status)
INDEX: (tenant_id, customer_id)
INDEX: (tenant_id, project_id)
INDEX: quote_number
```

### 10.3. Orders (Rendelések)

```sql
orders
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── order_number (VARCHAR, UNIQUE, NOT NULL) -- ORD-001 formátum
├── project_id (UUID, FOREIGN KEY -> projects.id, NULLABLE)
├── quote_id (UUID, FOREIGN KEY -> quotes.id, NULLABLE)
├── customer_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── title (VARCHAR, NOT NULL)
├── description (TEXT, NULLABLE)
├── amount (DECIMAL, NOT NULL)
├── currency (VARCHAR, DEFAULT 'HUF')
├── status (VARCHAR, NOT NULL) -- pending, confirmed, in_progress, completed, cancelled
├── payment_status (VARCHAR, NOT NULL) -- unpaid, partial, paid
├── paid_amount (DECIMAL, DEFAULT 0)
├── metadata (JSONB)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, status)
INDEX: (tenant_id, payment_status)
INDEX: (tenant_id, customer_id)
INDEX: (tenant_id, project_id)
INDEX: order_number
```

### 10.4. Time Tracking (Időkövetés)

```sql
time_entries
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── project_id (UUID, FOREIGN KEY -> projects.id, NULLABLE)
├── project_task_id (UUID, FOREIGN KEY -> project_tasks.id, NULLABLE)
├── user_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── description (TEXT, NULLABLE)
├── hours (DECIMAL, NOT NULL)
├── date (DATE, NOT NULL)
├── billable (BOOLEAN, DEFAULT true)
├── hourly_rate (DECIMAL, NULLABLE)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, project_id)
INDEX: (tenant_id, project_task_id)
INDEX: (tenant_id, user_id)
INDEX: (tenant_id, date)
```

### 11. Notifications (Értesítések)

**Megjegyzés:** Opció 1 (Moduláris Mikroszolgáltatások) alapján a notification modul külön service (notification-service).

```sql
notifications
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── user_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── type (VARCHAR, NOT NULL) -- ticket_assigned, comment_added, status_changed
├── title (VARCHAR, NOT NULL)
├── message (TEXT)
├── related_ticket_id (UUID, FOREIGN KEY -> tickets.id, NULLABLE)
├── is_read (BOOLEAN, DEFAULT false)
├── read_at (TIMESTAMP, NULLABLE)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, user_id, is_read)
INDEX: created_at
```

### 12. Audit Logs (Audit Napló)

```sql
audit_logs
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── user_id (UUID, FOREIGN KEY -> users.id, NULLABLE)
├── action (VARCHAR, NOT NULL) -- created, updated, deleted
├── entity_type (VARCHAR, NOT NULL) -- ticket, comment, user
├── entity_id (UUID, NOT NULL)
├── changes (JSONB) -- Előző és új értékek
├── ip_address (VARCHAR, NULLABLE)
├── user_agent (VARCHAR, NULLABLE)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, entity_type, entity_id)
INDEX: (tenant_id, user_id)
INDEX: created_at
```

### 13. User Sessions (Felhasználói Munkamenetek)

```sql
user_sessions
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── user_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── token (VARCHAR, UNIQUE, NOT NULL) -- JWT token hash
├── ip_address (VARCHAR, NULLABLE)
├── user_agent (VARCHAR, NULLABLE)
├── expires_at (TIMESTAMP, NOT NULL)
├── last_activity_at (TIMESTAMP)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, user_id)
INDEX: token
INDEX: expires_at
```

### 14. AI Chat Sessions (AI Chat Munkamenetek)

**Megjegyzés:** AgentInSec AI Library conversation history támogatásához.

```sql
chat_sessions
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── user_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── session_id (VARCHAR, UNIQUE, NOT NULL) -- AgentInSec session ID
├── title (VARCHAR, NULLABLE) -- Chat session címe
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, user_id)
INDEX: session_id
INDEX: created_at
```

### 15. AI Chat Messages (AI Chat Üzenetek)

```sql
chat_messages
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── session_id (VARCHAR, FOREIGN KEY -> chat_sessions.session_id, NOT NULL)
├── user_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── message_type (VARCHAR, NOT NULL) -- 'user' vagy 'assistant'
├── content (TEXT, NOT NULL)
├── metadata (JSONB) -- További információk
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, session_id)
INDEX: (session_id, created_at)
INDEX: user_id
```

### 16. AI Agent Memory - Episodes (Események)

**Megjegyzés:** AgentInSec episodic memory támogatásához.

```sql
agent_episodes
├── id (VARCHAR, PRIMARY KEY) -- AgentInSec episode ID
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── user_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── timestamp (BIGINT, NOT NULL)
├── user_query (TEXT, NOT NULL)
├── goal_id (VARCHAR, NULLABLE)
├── execution_results (JSONB)
├── success_rate (DOUBLE PRECISION)
├── execution_time_ms (BIGINT)
├── context (JSONB)
├── tags (TEXT[])
├── created_at (TIMESTAMP)

INDEX: (tenant_id, user_id)
INDEX: timestamp
INDEX: goal_id
INDEX: created_at
```

### 17. AI Agent Memory - Procedures (Eljárások)

```sql
agent_procedures
├── id (VARCHAR, PRIMARY KEY) -- AgentInSec procedure ID
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── procedure_name (VARCHAR, NOT NULL)
├── description (TEXT)
├── steps (JSONB, NOT NULL)
├── success_count (INT, DEFAULT 0)
├── failure_count (INT, DEFAULT 0)
├── avg_execution_time_ms (BIGINT)
├── last_executed (BIGINT)
├── effectiveness_score (DOUBLE PRECISION)
├── tags (TEXT[])
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, procedure_name)
INDEX: effectiveness_score
INDEX: last_executed
```

### 18. AI Agent Memory - Knowledge (Tudásbázis)

```sql
agent_knowledge
├── id (VARCHAR, PRIMARY KEY) -- AgentInSec knowledge ID
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── title (VARCHAR, NOT NULL)
├── content (TEXT, NOT NULL)
├── category (VARCHAR)
├── source (VARCHAR)
├── confidence_score (DOUBLE PRECISION)
├── usage_count (INT, DEFAULT 0)
├── last_accessed (BIGINT)
├── metadata (JSONB)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, category)
INDEX: confidence_score
INDEX: usage_count
```

### 19. AI Agent Memory - Learning History (Tanulási Előzmények)

```sql
agent_learning_history
├── id (VARCHAR, PRIMARY KEY) -- AgentInSec learning ID
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── user_id (UUID, FOREIGN KEY -> users.id, NULLABLE)
├── timestamp (BIGINT, NOT NULL)
├── learning_type (VARCHAR)
├── domain (VARCHAR)
├── pattern_name (VARCHAR)
├── performance_delta (DOUBLE PRECISION)
├── confidence (DOUBLE PRECISION)
├── evidence (JSONB)
├── created_at (TIMESTAMP)

INDEX: (tenant_id, user_id)
INDEX: timestamp
INDEX: learning_type
INDEX: domain
```

### 20. AI Agent Memory - Tool Usage (Tool Használat)

```sql
agent_tool_usage
├── id (VARCHAR, PRIMARY KEY) -- AgentInSec tool usage ID
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── user_id (UUID, FOREIGN KEY -> users.id, NULLABLE)
├── tool_name (VARCHAR, NOT NULL)
├── timestamp (BIGINT, NOT NULL)
├── goal_id (VARCHAR, NULLABLE)
├── success (BOOLEAN)
├── execution_time_ms (BIGINT)
├── parameters (JSONB)
├── result (JSONB)
├── created_at (TIMESTAMP)

INDEX: (tenant_id, tool_name)
INDEX: timestamp
INDEX: success
INDEX: user_id
```

### 21. AI Agent Memory - Reflection Sessions (Önértékelési Munkamenetek)

```sql
agent_reflection_sessions
├── id (VARCHAR, PRIMARY KEY) -- AgentInSec reflection ID
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── user_id (UUID, FOREIGN KEY -> users.id, NULLABLE)
├── timestamp (BIGINT, NOT NULL)
├── assessment (JSONB, NOT NULL)
├── improvements (JSONB)
├── application_result (JSONB)
├── overall_score (DOUBLE PRECISION)
├── success_rate (DOUBLE PRECISION)
├── created_at (TIMESTAMP)

INDEX: (tenant_id, user_id)
INDEX: timestamp
INDEX: overall_score
```

### 22. AI Agent Execution Plans (Végrehajtási Tervek)

**Megjegyzés:** AgentInSec execution plan confirmation támogatásához.

```sql
agent_execution_plans
├── id (UUID, PRIMARY KEY)
├── tenant_id (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
├── chat_message_id (VARCHAR, NOT NULL) -- Chat message azonosító
├── user_id (UUID, FOREIGN KEY -> users.id, NOT NULL)
├── function_name (VARCHAR, NOT NULL)
├── parameters (JSONB)
├── execution_plan (JSONB)
├── status (VARCHAR, NOT NULL) -- 'pending', 'confirmed', 'rejected'
├── confirmed_at (TIMESTAMP, NULLABLE)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (tenant_id, chat_message_id)
INDEX: (tenant_id, user_id)
INDEX: status
INDEX: created_at
```

## Indexelési Stratégia

### Kritikus Indexek

1. **Tenant ID** - Minden táblában, mert minden query tenant-alapú
2. **Foreign Keys** - Automatikusan indexelődnek, de explicit is lehet
3. **Keresési mezők** - Email, ticket_number, stb.
4. **Státusz és dátum mezők** - Gyakori szűrési kritériumok

### Kompozit Indexek

- `(tenant_id, status)` - Tickets szűréséhez
- `(tenant_id, user_id, is_read)` - Notifications lekérdezéséhez
- `(tenant_id, created_at)` - Időrendi listázáshoz

## Adatbázis Szintű Biztonság

### Row Level Security (PostgreSQL)

Minden táblához RLS policy:

```sql
-- Példa: tickets táblához
ALTER TABLE tickets ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_policy ON tickets
    FOR ALL
    USING (tenant_id = current_setting('app.current_tenant_id')::UUID);
```

### Constraints

- **NOT NULL** - Kötelező mezők
- **UNIQUE** - Egyedi értékek (tenant-onként)
- **FOREIGN KEY** - Referenciális integritás
- **CHECK** - Érték validáció (pl. priority értékek)

## Migrációs Stratégia

### Migrációs Eszközök

- **Knex.js** (Node.js)
- **Alembic** (Python)
- **Flyway** (Java)
- **Liquibase** (Java)
- **Prisma Migrate** (Node.js)

### Verziózás

- Minden migráció verziózott
- Visszavonható migrációk
- Seed adatok külön fájlokban

## Backup Stratégia

- **Napi backup** - Teljes adatbázis
- **Heti backup** - Hosszú távú tárolás
- **Transaction log backup** - Point-in-time recovery
- **Tenant specifikus backup** - Opcionális

## Teljesítmény Optimalizálás

### Query Optimalizálás

- **N+1 probléma elkerülése** - Eager loading
- **Pagination** - Limit/offset vagy cursor-based
- **Selective fields** - Csak szükséges mezők lekérdezése

### Caching Stratégia

- **Redis** - Gyakran használt adatok
- **Query result cache** - Lassú lekérdezések eredményei
- **User session cache**

## Következő Lépések

1. ✅ **Döntés meghozatala** - Opció 2: Külön Projektek és Ticketek
2. **Döntés meghozatala** - Email egyediség (globális vagy tenant-onként)
3. **Séma finomhangolása** - További mezők hozzáadása
   - Project tasks kapcsolatok ellenőrzése
   - Time tracking integráció
   - Quote és Order kapcsolatok
4. **Migrációs scriptek** - Első verzió létrehozása
5. **Seed adatok** - Teszteléshez (ticketek és projektek)
6. **RLS policy-k** - Biztonsági szabályok implementálása
   - Tickets táblához
   - Projects táblához
   - Project_tasks táblához

