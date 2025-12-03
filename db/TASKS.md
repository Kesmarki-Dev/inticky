# Database Agent - Feladatlista

## Projekt Kontextus

Az **InTicky** egy modern, multi-tenant ticketing rendszer, amely egy régi Delphi 2009-es asztali alkalmazás modernizált, webes változata. A rendszer támogatja a support ticketek kezelését és a fejlesztési projektek menedzsmentjét.

**Adatbázis Stack:**
- **PostgreSQL 14+** - Fő adatbázis
- **Flyway** vagy **Liquibase** - Migrációk kezelése
- **Row Level Security (RLS)** - Adatizoláció
- **JSONB** - Rugalmas adatok tárolása

**Multi-Tenant Modell:**
- **Shared Database, Shared Schema** stratégia
- Minden táblában `tenant_id` mező kötelező
- Row Level Security (RLS) policy-k minden táblához
- Tenant-onként adatizoláció

## Függőségek Más Agentekkel

### Backend Agent
- **Függőség:** Adatbázis séma használata
- **Időpont:** Backend fejlesztés során
- **Kommunikáció:** Database agent létrehozza a migrációkat, backend agent használja az adatbázis sémát

### Frontend Agent
- **Függőség:** Nincs közvetlen függőség
- **Időpont:** Frontend fejlesztés során
- **Kommunikáció:** Frontend agent API-kon keresztül éri el az adatokat

## Fázisok és Feladatok

### Fázis 1: Alapvető Migrációk (1. hét)

**Cél:** Core táblák létrehozása (tenants, users, tenant_modules).

#### 1.1 Migráció Tool Setup
- [ ] Flyway vagy Liquibase választása
- [ ] Migráció tool inicializálás
- [ ] Migrációs mappák létrehozása
  - [ ] `db/migrations/` mappa
  - [ ] `db/seed/` mappa (seed adatokhoz)

#### 1.2 Tenants Tábla
- [ ] Migráció létrehozása: `V1__Create_tenants_table.sql`
- [ ] Tábla struktúra:
  - [ ] `id` (UUID, PRIMARY KEY)
  - [ ] `name` (VARCHAR, NOT NULL)
  - [ ] `slug` (VARCHAR, UNIQUE, NOT NULL)
  - [ ] `domain` (VARCHAR, NULLABLE)
  - [ ] `logo_url` (VARCHAR, NULLABLE)
  - [ ] `settings` (JSONB)
  - [ ] `subscription_tier` (VARCHAR)
  - [ ] `is_active` (BOOLEAN, DEFAULT true)
  - [ ] `created_at` (TIMESTAMP)
  - [ ] `updated_at` (TIMESTAMP)
- [ ] Indexek:
  - [ ] `slug` UNIQUE index
  - [ ] `is_active` index
- [ ] RLS policy:
  - [ ] RLS engedélyezése
  - [ ] Policy létrehozása (ha szükséges)

**Dokumentáció:**
- [../plan/03_adatbazis_tervezes.md](../plan/03_adatbazis_tervezes.md) - Tenants tábla séma

#### 1.3 Tenant Modules Tábla
- [ ] Migráció létrehozása: `V2__Create_tenant_modules_table.sql`
- [ ] Tábla struktúra:
  - [ ] `tenant_id` (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
  - [ ] `module_name` (VARCHAR, NOT NULL)
  - [ ] `is_active` (BOOLEAN, DEFAULT false)
  - [ ] `activated_at` (TIMESTAMP, NULLABLE)
  - [ ] `subscription_tier` (VARCHAR, NULLABLE)
  - [ ] `created_at` (TIMESTAMP)
  - [ ] `updated_at` (TIMESTAMP)
- [ ] Constraint-ek:
  - [ ] PRIMARY KEY: (tenant_id, module_name)
  - [ ] FOREIGN KEY: tenant_id -> tenants.id
- [ ] Indexek:
  - [ ] (tenant_id, is_active) index
  - [ ] module_name index

**Dokumentáció:**
- [../plan/03_adatbazis_tervezes.md](../plan/03_adatbazis_tervezes.md) - Tenant Modules tábla séma
- [../plan/08_modularis_felepites.md](../plan/08_modularis_felepites.md) - Modul nevek

#### 1.4 Users Tábla
- [ ] Migráció létrehozása: `V3__Create_users_table.sql`
- [ ] Tábla struktúra:
  - [ ] `id` (UUID, PRIMARY KEY)
  - [ ] `tenant_id` (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
  - [ ] `email` (VARCHAR, UNIQUE, NOT NULL)
  - [ ] `password_hash` (VARCHAR, NOT NULL)
  - [ ] `first_name` (VARCHAR)
  - [ ] `last_name` (VARCHAR)
  - [ ] `avatar_url` (VARCHAR, NULLABLE)
  - [ ] `role` (VARCHAR, NOT NULL)
  - [ ] `is_active` (BOOLEAN, DEFAULT true)
  - [ ] `email_verified` (BOOLEAN, DEFAULT false)
  - [ ] `last_login_at` (TIMESTAMP, NULLABLE)
  - [ ] `created_at` (TIMESTAMP)
  - [ ] `updated_at` (TIMESTAMP)
- [ ] Constraint-ek:
  - [ ] FOREIGN KEY: tenant_id -> tenants.id
  - [ ] Email egyediség (tenant-onként vagy globálisan - döntés szükséges)
- [ ] Indexek:
  - [ ] (tenant_id, email) index
  - [ ] email index
  - [ ] (tenant_id, role) index
- [ ] RLS policy:
  - [ ] RLS engedélyezése
  - [ ] Policy: tenant_id = current_setting('app.current_tenant_id')::UUID

**Dokumentáció:**
- [../plan/03_adatbazis_tervezes.md](../plan/03_adatbazis_tervezes.md) - Users tábla séma

### Fázis 2: Ticket Modul Migrációk (2. hét)

**Cél:** Support ticketek tábláinak létrehozása.

#### 2.1 Tickets Tábla
- [ ] Migráció létrehozása: `V4__Create_tickets_table.sql`
- [ ] Tábla struktúra:
  - [ ] `id` (UUID, PRIMARY KEY)
  - [ ] `tenant_id` (UUID, FOREIGN KEY -> tenants.id, NOT NULL)
  - [ ] `ticket_number` (VARCHAR, UNIQUE, NOT NULL)
  - [ ] `title` (VARCHAR, NOT NULL)
  - [ ] `description` (TEXT)
  - [ ] `category_id` (UUID, FOREIGN KEY -> ticket_categories.id)
  - [ ] `priority` (VARCHAR, NOT NULL)
  - [ ] `status` (VARCHAR, NOT NULL)
  - [ ] `assigned_to_id` (UUID, FOREIGN KEY -> users.id, NULLABLE)
  - [ ] `created_by_id` (UUID, FOREIGN KEY -> users.id, NOT NULL)
  - [ ] `type` (VARCHAR, DEFAULT 'support')
  - [ ] `due_date` (TIMESTAMP, NULLABLE)
  - [ ] `resolved_at` (TIMESTAMP, NULLABLE)
  - [ ] `closed_at` (TIMESTAMP, NULLABLE)
  - [ ] `metadata` (JSONB)
  - [ ] `created_at` (TIMESTAMP)
  - [ ] `updated_at` (TIMESTAMP)
- [ ] Indexek:
  - [ ] (tenant_id, status) index
  - [ ] (tenant_id, assigned_to_id) index
  - [ ] (tenant_id, created_by_id) index
  - [ ] (tenant_id, category_id) index
  - [ ] (tenant_id, type) index
  - [ ] ticket_number UNIQUE index
  - [ ] created_at index
- [ ] RLS policy:
  - [ ] RLS engedélyezése
  - [ ] Policy: tenant_id = current_setting('app.current_tenant_id')::UUID

**Dokumentáció:**
- [../plan/03_adatbazis_tervezes.md](../plan/03_adatbazis_tervezes.md) - Tickets tábla séma

#### 2.2 Ticket Categories Tábla
- [ ] Migráció létrehozása: `V5__Create_ticket_categories_table.sql`
- [ ] Tábla struktúra és constraint-ek
- [ ] Indexek: (tenant_id, is_active)
- [ ] RLS policy

#### 2.3 Ticket Statuses Tábla
- [ ] Migráció létrehozása: `V6__Create_ticket_statuses_table.sql`
- [ ] Tábla struktúra és constraint-ek
- [ ] Indexek: (tenant_id, is_default)
- [ ] RLS policy

#### 2.4 Comments Tábla
- [ ] Migráció létrehozása: `V7__Create_comments_table.sql`
- [ ] Tábla struktúra:
  - [ ] `id` (UUID, PRIMARY KEY)
  - [ ] `tenant_id` (UUID, NOT NULL)
  - [ ] `ticket_id` (UUID, FOREIGN KEY -> tickets.id, NOT NULL)
  - [ ] `user_id` (UUID, FOREIGN KEY -> users.id, NOT NULL)
  - [ ] `content` (TEXT, NOT NULL)
  - [ ] `is_internal` (BOOLEAN, DEFAULT false)
  - [ ] `created_at` (TIMESTAMP)
  - [ ] `updated_at` (TIMESTAMP)
- [ ] Indexek és RLS policy

#### 2.5 Attachments Tábla
- [ ] Migráció létrehozása: `V8__Create_attachments_table.sql`
- [ ] Tábla struktúra és constraint-ek
- [ ] Indexek és RLS policy

#### 2.6 Tags és Ticket Tags Táblák
- [ ] Migráció létrehozása: `V9__Create_tags_and_ticket_tags_tables.sql`
- [ ] Tags tábla
- [ ] Ticket_tags junction tábla
- [ ] Indexek és RLS policy-k

**Dokumentáció:**
- [../plan/03_adatbazis_tervezes.md](../plan/03_adatbazis_tervezes.md) - Ticket táblák séma

### Fázis 3: Project Modul Migrációk (2. hét)

**Cél:** Fejlesztési projektek tábláinak létrehozása.

#### 3.1 Projects Tábla
- [ ] Migráció létrehozása: `V10__Create_projects_table.sql`
- [ ] Tábla struktúra:
  - [ ] `id` (UUID, PRIMARY KEY)
  - [ ] `tenant_id` (UUID, NOT NULL)
  - [ ] `name` (VARCHAR, NOT NULL)
  - [ ] `description` (TEXT)
  - [ ] `status` (VARCHAR, NOT NULL)
  - [ ] `customer_id` (UUID, FOREIGN KEY -> users.id, NULLABLE)
  - [ ] `manager_id` (UUID, FOREIGN KEY -> users.id, NULLABLE)
  - [ ] `start_date` (DATE, NULLABLE)
  - [ ] `end_date` (DATE, NULLABLE)
  - [ ] `due_date` (DATE, NULLABLE)
  - [ ] `budget` (DECIMAL, NULLABLE)
  - [ ] `actual_cost` (DECIMAL, NULLABLE)
  - [ ] `estimated_hours` (DECIMAL, NULLABLE)
  - [ ] `actual_hours` (DECIMAL, NULLABLE)
  - [ ] `created_at` (TIMESTAMP)
  - [ ] `updated_at` (TIMESTAMP)
- [ ] Indexek és RLS policy

#### 3.2 Project Tasks Tábla
- [ ] Migráció létrehozása: `V11__Create_project_tasks_table.sql`
- [ ] Tábla struktúra és constraint-ek
- [ ] Indexek és RLS policy

#### 3.3 Project Task Comments és Attachments
- [ ] Migráció létrehozása: `V12__Create_project_task_comments_and_attachments.sql`
- [ ] Project_task_comments tábla
- [ ] Project_task_attachments tábla
- [ ] Indexek és RLS policy-k

#### 3.4 Quotes és Orders Táblák
- [ ] Migráció létrehozása: `V13__Create_quotes_and_orders_tables.sql`
- [ ] Quotes tábla
- [ ] Orders tábla
- [ ] Indexek és RLS policy-k

#### 3.5 Time Entries Tábla
- [ ] Migráció létrehozása: `V14__Create_time_entries_table.sql`
- [ ] Tábla struktúra:
  - [ ] `id` (UUID, PRIMARY KEY)
  - [ ] `tenant_id` (UUID, NOT NULL)
  - [ ] `project_id` (UUID, FOREIGN KEY -> projects.id, NULLABLE)
  - [ ] `project_task_id` (UUID, FOREIGN KEY -> project_tasks.id, NULLABLE)
  - [ ] `user_id` (UUID, FOREIGN KEY -> users.id, NOT NULL)
  - [ ] `date` (DATE, NOT NULL)
  - [ ] `hours` (DECIMAL, NOT NULL)
  - [ ] `description` (TEXT)
  - [ ] `is_billable` (BOOLEAN, DEFAULT true)
  - [ ] `hourly_rate` (DECIMAL, NULLABLE)
  - [ ] `created_at` (TIMESTAMP)
  - [ ] `updated_at` (TIMESTAMP)
- [ ] Indexek és RLS policy

**Dokumentáció:**
- [../plan/03_adatbazis_tervezes.md](../plan/03_adatbazis_tervezes.md) - Project táblák séma

### Fázis 4: AI Agent Migrációk (1. hét)

**Cél:** AI Agent memória és chat tábláinak létrehozása.

#### 4.1 Chat Sessions és Messages
- [ ] Migráció létrehozása: `V15__Create_chat_sessions_and_messages_tables.sql`
- [ ] Chat_sessions tábla:
  - [ ] `id` (UUID, PRIMARY KEY)
  - [ ] `tenant_id` (UUID, NOT NULL)
  - [ ] `user_id` (UUID, FOREIGN KEY -> users.id, NOT NULL)
  - [ ] `title` (VARCHAR, NULLABLE)
  - [ ] `created_at` (TIMESTAMP)
  - [ ] `updated_at` (TIMESTAMP)
- [ ] Chat_messages tábla:
  - [ ] `id` (UUID, PRIMARY KEY)
  - [ ] `tenant_id` (UUID, NOT NULL)
  - [ ] `session_id` (UUID, FOREIGN KEY -> chat_sessions.id, NOT NULL)
  - [ ] `role` (VARCHAR, NOT NULL) -- user, assistant, system
  - [ ] `content` (TEXT, NOT NULL)
  - [ ] `created_at` (TIMESTAMP)
- [ ] Indexek és RLS policy-k

#### 4.2 Agent Memory Táblák
- [ ] Migráció létrehozása: `V16__Create_agent_memory_tables.sql`
- [ ] Agent_episodes tábla (episodic memory)
- [ ] Agent_procedures tábla (procedural memory)
- [ ] Agent_knowledge tábla (semantic knowledge)
- [ ] Agent_learning_history tábla
- [ ] Agent_tool_usage tábla
- [ ] Agent_reflection_sessions tábla
- [ ] Agent_execution_plans tábla
- [ ] Indexek és RLS policy-k (tenant izoláció)

**Dokumentáció:**
- [../plan/03_adatbazis_tervezes.md](../plan/03_adatbazis_tervezes.md) - AI Agent táblák séma
- [../docs/development/backend/ai-agent-integration.md](../docs/development/backend/ai-agent-integration.md)

### Fázis 5: Indexek és Optimalizáció (1. hét)

**Cél:** Teljesítmény optimalizálás indexekkel és RLS policy-kkel.

#### 5.1 További Indexek
- [ ] Gyakori query-k azonosítása
- [ ] Kompozit indexek létrehozása:
  - [ ] (tenant_id, status, created_at) - Ticket listázás
  - [ ] (tenant_id, user_id, created_at) - User specifikus lekérdezések
  - [ ] (tenant_id, project_id, status) - Projekt feladatok
- [ ] Full-text search indexek (ha szükséges)

#### 5.2 RLS Policy-k Ellenőrzése
- [ ] Minden táblához RLS engedélyezve
- [ ] Policy-k helyes működése
- [ ] Tenant izoláció tesztelése

#### 5.3 Constraint-ek Ellenőrzése
- [ ] FOREIGN KEY constraint-ek
- [ ] UNIQUE constraint-ek
- [ ] CHECK constraint-ek (ha szükséges)
- [ ] NOT NULL constraint-ek

### Fázis 6: Seed Adatok (1. hét)

**Cél:** Fejlesztéshez és teszteléshez szükséges alapadatok.

#### 6.1 Alapértelmezett Tenant
- [ ] Seed script: `seed/01_default_tenant.sql`
- [ ] Alapértelmezett tenant létrehozása
- [ ] Tenant slug: `default` vagy `demo`

#### 6.2 Admin Felhasználó
- [ ] Seed script: `seed/02_admin_user.sql`
- [ ] Admin felhasználó létrehozása
- [ ] Jelszó hash generálása (BCrypt)
- [ ] Email: `admin@example.com` (vagy konfigurálható)

#### 6.3 Alapértelmezett Modulok
- [ ] Seed script: `seed/03_default_modules.sql`
- [ ] Alapértelmezett modulok aktiválása:
  - [ ] `auth` modul
  - [ ] `user` modul
  - [ ] `ticket` modul (opcionális)
  - [ ] `project` modul (opcionális)

#### 6.4 Teszt Adatok (Opcionális)
- [ ] Seed script: `seed/04_test_data.sql`
- [ ] Teszt tenant-ok
- [ ] Teszt felhasználók
- [ ] Teszt ticketek (ha ticket modul aktív)
- [ ] Teszt projektek (ha project modul aktív)

**Dokumentáció:**
- [../docs/development/database/seed-data.md](../docs/development/database/seed-data.md)

### Fázis 7: Backup és Restore (Folyamatos)

**Cél:** Adatbázis backup és restore mechanizmusok.

#### 7.1 Backup Scriptek
- [ ] Backup script létrehozása
- [ ] Automatikus backup konfiguráció
- [ ] Azure Database for PostgreSQL backup (éles környezetben)

#### 7.2 Restore Procedúrák
- [ ] Restore script létrehozása
- [ ] Restore tesztelése
- [ ] Dokumentáció

**Dokumentáció:**
- [../docs/development/database/backup-restore.md](../docs/development/database/backup-restore.md)

## Fontos Emlékeztetők

1. **Tenant ID kötelező:** MINDEN táblában, MINDEN query-ben
2. **RLS policy-k:** Minden táblához RLS engedélyezése és policy létrehozása
3. **Indexek:** Tenant ID + gyakori szűrési mezők kombinációja
4. **Migrációk idempotensek:** `IF NOT EXISTS`, `ON CONFLICT DO NOTHING` használata
5. **Constraints:** Adatintegritás biztosítása (FOREIGN KEY, UNIQUE, CHECK)
6. **Naming konvenció:** Flyway: `V{version}__{description}.sql`

## Dokumentáció Linkek

- [Database Setup](../docs/development/database/setup.md)
- [Migrations](../docs/development/database/migrations.md)
- [Seed Data](../docs/development/database/seed-data.md)
- [Backup/Restore](../docs/development/database/backup-restore.md)
- [Database Design](../docs/architecture/database-design.md)
- [Részletes Tervezés](../plan/03_adatbazis_tervezes.md)
- [Multi-Tenant Architektúra](../plan/04_multi_tenant_architektura.md)

