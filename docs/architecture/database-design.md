# InTicky - Adatbázis Tervezés

## Célközönség

Ez a dokumentum az InTicky adatbázis struktúráját és tervezési döntéseit mutatja be. Adatbázis fejlesztők és backend fejlesztők számára készült.

## Adatbázis Választás

**Választott:** PostgreSQL 14+

**Indoklás:**
- Robusztus, enterprise szintű
- Kiváló multi-tenant támogatás
- JSON/JSONB támogatás (rugalmas adatokhoz)
- Teljes ACID compliance
- Széles körben használt, jó dokumentáció
- Ingyenes és open source

## Multi-Tenant Modell

**Választott stratégia:** Shared Database, Shared Schema

**Jellemzők:**
- Minden tenant ugyanazt az adatbázist és sémát használja
- `tenant_id` mező minden táblában (kötelező)
- Row Level Security (RLS) adatbázis szintű védelem
- Application szintű validáció

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

A rendszer bérlői (tenant-ek), akik használják a rendszert.

**Tábla:** `tenants`

**Főbb mezők:**
- `id` - UUID, PRIMARY KEY
- `name` - Tenant neve
- `slug` - URL-barát azonosító (pl. "acme-corp")
- `domain` - Egyedi domain (opcionális)
- `subscription_tier` - free, basic, premium, enterprise
- `is_active` - Aktív-e a tenant

### 2. Tenant Modules (Tenant Modulok)

Tenant-onként aktiválható modulok.

**Tábla:** `tenant_modules`

**Főbb mezők:**
- `tenant_id` - Tenant azonosító
- `module_name` - Modul neve (ticket, project, report, file, stb.)
- `is_active` - Aktív-e a modul
- `activated_at` - Aktiválás dátuma

**Modul nevek:**
- `ticket` - Support ticketek modul
- `project` - Fejlesztési projektek modul
- `user` - Felhasználó kezelés modul
- `auth` - Autentikáció modul
- `notification` - Értesítések modul
- `file` - Fájl kezelés modul
- `report` - Jelentések modul (opcionális)

### 3. Users (Felhasználók)

Rendszer felhasználói.

**Tábla:** `users`

**Főbb mezők:**
- `id` - UUID, PRIMARY KEY
- `tenant_id` - Tenant azonosító (kötelező)
- `email` - Email cím (unique)
- `password_hash` - Hashelt jelszó
- `first_name`, `last_name` - Név
- `role` - admin, support_manager, agent, customer, developer
- `is_active` - Aktív-e a felhasználó

### 4. Tickets (Support Feladatok)

Support ticketek (csak support feladatokhoz).

**Tábla:** `tickets`

**Főbb mezők:**
- `id` - UUID, PRIMARY KEY
- `tenant_id` - Tenant azonosító (kötelező)
- `ticket_number` - Ticket szám (TNT-001 formátum)
- `title` - Ticket címe
- `description` - Leírás
- `type` - support, bug, question, feature_request
- `status` - new, in_progress, resolved, closed, cancelled
- `priority` - low, medium, high, critical
- `assigned_to_id` - Hozzárendelt felhasználó
- `created_by_id` - Létrehozó felhasználó

**Kapcsolódó táblák:**
- `ticket_categories` - Kategóriák
- `ticket_statuses` - Státuszok
- `comments` - Kommentek
- `attachments` - Csatolmányok
- `tags`, `ticket_tags` - Címkék

### 5. Projects (Fejlesztési Projektek)

Fejlesztési projektek (külön entitás, nem kapcsolódik ticketekhez).

**Tábla:** `projects`

**Főbb mezők:**
- `id` - UUID, PRIMARY KEY
- `tenant_id` - Tenant azonosító (kötelező)
- `project_number` - Projekt szám (PRJ-001 formátum)
- `name` - Projekt neve
- `description` - Leírás
- `status` - planning, in_progress, on_hold, completed, cancelled
- `customer_id` - Ügyfél
- `manager_id` - Projekt menedzser
- `budget` - Költségvetés
- `estimated_hours`, `actual_hours` - Időkövetés

**Kapcsolódó táblák:**
- `project_tasks` - Projekt feladatok
- `project_task_comments` - Feladat kommentek
- `project_task_attachments` - Feladat csatolmányok
- `quotes` - Árajánlatok
- `orders` - Rendelések
- `time_entries` - Időkövetés

### 6. Comments (Kommentek)

Kommentek ticketekhez és projekt feladatokhoz.

**Táblák:**
- `comments` - Ticket kommentek
- `project_task_comments` - Projekt feladat kommentek

**Főbb mezők:**
- `id` - UUID, PRIMARY KEY
- `tenant_id` - Tenant azonosító (kötelező)
- `ticket_id` vagy `project_task_id` - Kapcsolódó entitás
- `user_id` - Komment író
- `content` - Komment tartalma
- `is_internal` - Csak ügynökök láthatják

### 7. Attachments (Csatolmányok)

Fájl csatolmányok.

**Táblák:**
- `attachments` - Ticket csatolmányok
- `project_task_attachments` - Projekt feladat csatolmányok

**Főbb mezők:**
- `id` - UUID, PRIMARY KEY
- `tenant_id` - Tenant azonosító (kötelező)
- `file_name` - Fájl neve
- `file_path` - Fájl elérési út (Azure Blob Storage vagy lokális)
- `file_size` - Fájl méret (bytes)
- `file_type` - MIME type

### 8. Notifications (Értesítések)

Rendszer értesítések.

**Tábla:** `notifications`

**Főbb mezők:**
- `id` - UUID, PRIMARY KEY
- `tenant_id` - Tenant azonosító (kötelező)
- `user_id` - Címzett felhasználó
- `type` - Értesítés típusa
- `title`, `message` - Értesítés tartalma
- `is_read` - Olvasva van-e

### 9. Time Entries (Időkövetés)

Időkövetés projektekhez és projekt feladatokhoz.

**Tábla:** `time_entries`

**Főbb mezők:**
- `id` - UUID, PRIMARY KEY
- `tenant_id` - Tenant azonosító (kötelező)
- `project_id` - Projekt
- `project_task_id` - Projekt feladat (opcionális)
- `user_id` - Felhasználó
- `hours` - Rögzített órák
- `date` - Dátum
- `billable` - Számlázható-e

### 10. AI Chat Sessions (AI Chat Munkamenetek)

AI chat munkamenetek (AgentInSec integráció).

**Tábla:** `chat_sessions`

**Főbb mezők:**
- `id` - UUID, PRIMARY KEY
- `tenant_id` - Tenant azonosító (kötelező)
- `user_id` - Felhasználó
- `session_id` - AgentInSec session ID
- `title` - Chat session címe

### 11. AI Chat Messages (AI Chat Üzenetek)

AI chat üzenetek.

**Tábla:** `chat_messages`

**Főbb mezők:**
- `id` - UUID, PRIMARY KEY
- `tenant_id` - Tenant azonosító (kötelező)
- `session_id` - Chat session
- `user_id` - Felhasználó
- `message_type` - 'user' vagy 'assistant'
- `content` - Üzenet tartalma

### 12. AI Agent Memory (Agent Memória)

AgentInSec episodic, procedural, knowledge, learning és reflection memória táblák.

**Táblák:**
- `agent_episodes` - Események (episodic memory)
- `agent_procedures` - Eljárások (procedural memory)
- `agent_knowledge` - Tudásbázis (semantic knowledge)
- `agent_learning_history` - Tanulási előzmények
- `agent_tool_usage` - Tool használat követése
- `agent_reflection_sessions` - Önértékelési munkamenetek
- `agent_execution_plans` - Végrehajtási tervek (confirmation)

**Főbb jellemzők:**
- Minden tábla tartalmaz `tenant_id` mezőt
- JSONB mezők komplex adatok tárolásához
- Timestamp mezők időkövetéshez

## Indexelési Stratégia

### Kritikus Indexek

1. **Tenant ID** - Minden táblában, mert minden query tenant-alapú
2. **Foreign Keys** - Automatikusan indexelődnek
3. **Keresési mezők** - Email, ticket_number, project_number
4. **Státusz és dátum mezők** - Gyakori szűrési kritériumok

### Kompozit Indexek

- `(tenant_id, status)` - Tickets szűréséhez
- `(tenant_id, user_id, is_read)` - Notifications lekérdezéséhez
- `(tenant_id, created_at)` - Időrendi listázáshoz
- `(tenant_id, module_name, is_active)` - Modul aktiválás ellenőrzéshez

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

**Ajánlott:** Flyway vagy Liquibase

**Verziózás:**
- Minden migráció verziózott
- Visszavonható migrációk
- Seed adatok külön fájlokban

**Strukturálás:**
```
db/
├── migrations/
│   ├── V1__initial_schema.sql
│   ├── V2__add_projects.sql
│   └── ...
└── seeds/
    ├── dev/
    └── test/
```

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

## További Információk

- [Rendszer áttekintés](./system-overview.md)
- [Mikroszolgáltatások](./microservices.md)
- [Adatbázis setup](../development/database/setup.md)
- [Migrációk](../development/database/migrations.md)
- [Részletes adatbázis tervezés](../../plan/03_adatbazis_tervezes.md)

