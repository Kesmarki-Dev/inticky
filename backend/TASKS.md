# Backend Agent - Feladatlista

## Projekt Kontextus

Az **InTicky** egy modern, multi-tenant ticketing rendszer, amely egy régi Delphi 2009-es asztali alkalmazás modernizált, webes változata. A rendszer támogatja a support ticketek kezelését és a fejlesztési projektek menedzsmentjét, mikroszolgáltatások architektúrával.

**Technológiai Stack:**
- **Java 17+** (LTS)
- **Quarkus 3.x** - Framework
- **Maven** - Build tool
- **Hibernate ORM with Panache** - Database access
- **PostgreSQL 14+** - Database (másik agent kezeli)
- **Redis** - Caching
- **AgentInSec AI Library v3.5.0** - AI chat és agent

**Architektúra:**
- Mikroszolgáltatások (minden modul külön service)
- Multi-tenant izoláció (tenant_id minden adatbázis műveletnél)
- Moduláris felépítés (tenant-onként aktiválható modulok)

## Függőségek Más Agentekkel

### Database Agent
- **Függőség:** Alapvető migrációk (tenants, users, tenant_modules) készítése
- **Időpont:** Backend fejlesztés előtt
- **Kommunikáció:** Database agent létrehozza a migrációkat, backend agent használja az adatbázis sémát

### Frontend Agent
- **Függőség:** Backend API-k készítése
- **Időpont:** Frontend fejlesztés során
- **Kommunikáció:** Backend agent létrehozza az API-kat, frontend agent használja őket

### CI/CD Agent
- **Függőség:** Backend build és deployment
- **Időpont:** CI/CD pipeline beállítása után
- **Kommunikáció:** CI/CD agent buildeli és deployolja a backend service-eket

## Fázisok és Feladatok

### Fázis 1: Projekt Inicializálás (1. hét)

**Cél:** Alapvető projekt struktúra és környezet létrehozása.

#### 1.1 Monorepo Struktúra
- [ ] `backend/` mappa létrehozása (már létezik)
- [ ] Service mappák létrehozása:
  - [ ] `api-gateway/`
  - [ ] `auth-service/`
  - [ ] `user-service/`
  - [ ] `ticket-service/`
  - [ ] `project-service/`
  - [ ] `notification-service/`
  - [ ] `file-service/`
  - [ ] `ai-agent-service/`

#### 1.2 Quarkus Projekt Inicializálás
- [ ] API Gateway Quarkus projekt létrehozása
  - [ ] `mvn io.quarkus.platform:quarkus-maven-plugin:3.x:create`
  - [ ] Maven pom.xml konfiguráció
  - [ ] application.yml létrehozása
- [ ] Auth Service Quarkus projekt létrehozása
- [ ] User Service Quarkus projekt létrehozása
- [ ] Ticket Service Quarkus projekt létrehozása
- [ ] Project Service Quarkus projekt létrehozása
- [ ] Notification Service Quarkus projekt létrehozása
- [ ] File Service Quarkus projekt létrehozása
- [ ] AI Agent Service Quarkus projekt létrehozása

#### 1.3 Maven Konfiguráció
- [ ] Parent POM létrehozása (ha szükséges)
- [ ] Common dependencies definiálása
- [ ] Quarkus BOM konfiguráció
- [ ] Maven profiles (dev, staging, prod)

#### 1.4 Alapvető Konfiguráció
- [ ] `application.yml` template minden service-hez
- [ ] Database connection string konfiguráció
- [ ] Redis connection konfiguráció
- [ ] Logging konfiguráció
- [ ] Multi-tenant kontextus konfiguráció

**Dokumentáció:**
- [../docs/development/backend/setup.md](../docs/development/backend/setup.md)
- [../plan/02_technikai_kovetelmenyek.md](../plan/02_technikai_kovetelmenyek.md)

### Fázis 2: Core Service-ek (2-3. hét)

**Cél:** Alapvető funkcionalitás implementálása (Auth, User, API Gateway).

#### 2.1 Auth Service
- [ ] JWT token generálás és validáció
  - [ ] JWT library integráció (SmallRye JWT)
  - [ ] Token generálás service
  - [ ] Token validáció interceptor
- [ ] Bejelentkezés API
  - [ ] POST `/api/v1/auth/login` endpoint
  - [ ] Email/jelszó validáció
  - [ ] Password hash ellenőrzés (BCrypt)
  - [ ] JWT token visszaadása
- [ ] Jelszó visszaállítás
  - [ ] POST `/api/v1/auth/reset-password` endpoint
  - [ ] Email küldés (Notification Service integráció)
- [ ] Token refresh
  - [ ] POST `/api/v1/auth/refresh` endpoint
- [ ] Multi-tenant support
  - [ ] Tenant kiválasztás bejelentkezéskor
  - [ ] Tenant context beállítása
- [ ] Session kezelés
  - [ ] Session entitás létrehozása
  - [ ] Session repository
  - [ ] Session service

**Dokumentáció:**
- [../docs/architecture/microservices.md](../docs/architecture/microservices.md) - Auth Service részletek
- [../docs/development/backend/architecture.md](../docs/development/backend/architecture.md)

#### 2.2 User Service
- [ ] Felhasználó CRUD műveletek
  - [ ] User entitás létrehozása (Panache Entity)
  - [ ] User repository (PanacheRepository)
  - [ ] User service
  - [ ] GET `/api/v1/users` - Listázás (tenant szűréssel)
  - [ ] POST `/api/v1/users` - Létrehozás
  - [ ] GET `/api/v1/users/:id` - Részletek
  - [ ] PUT `/api/v1/users/:id` - Frissítés
  - [ ] DELETE `/api/v1/users/:id` - Törlés (soft delete)
- [ ] Multi-tenant izoláció
  - [ ] Tenant ID minden query-ben
  - [ ] Tenant context interceptor
- [ ] Szerepkör kezelés
  - [ ] Role enum vagy entitás
  - [ ] Role-based access control (RBAC)
- [ ] Felhasználó profil kezelés
  - [ ] GET `/api/v1/users/me` - Saját profil
  - [ ] PUT `/api/v1/users/me` - Profil frissítés

**Dokumentáció:**
- [../docs/architecture/microservices.md](../docs/architecture/microservices.md) - User Service részletek
- [../plan/03_adatbazis_tervezes.md](../plan/03_adatbazis_tervezes.md) - Users tábla séma

#### 2.3 API Gateway
- [ ] Routing konfiguráció
  - [ ] Service URL-ek konfigurálása
  - [ ] Route definíciók
  - [ ] Load balancing (ha szükséges)
- [ ] Authentication middleware
  - [ ] JWT token validáció
  - [ ] Unauthenticated request kezelés
  - [ ] Token refresh kezelés
- [ ] Tenant resolution
  - [ ] Subdomain alapú tenant azonosítás
  - [ ] Header alapú tenant azonosítás (X-Tenant-ID)
  - [ ] Tenant context beállítása
- [ ] Modul aktiválás ellenőrzés
  - [ ] `@ModuleRequired` annotation létrehozása
  - [ ] Modul check interceptor
  - [ ] `tenant_modules` tábla lekérdezés
  - [ ] 403 Forbidden, ha modul nem aktív
- [ ] Request/Response logging
  - [ ] Logging filter
  - [ ] Request ID generálás
- [ ] Rate limiting
  - [ ] Rate limit konfiguráció
  - [ ] Rate limit middleware

**Dokumentáció:**
- [../docs/architecture/microservices.md](../docs/architecture/microservices.md) - API Gateway részletek
- [../plan/08_modularis_felepites.md](../plan/08_modularis_felepites.md) - Modul aktiválás

### Fázis 3: Modul Service-ek (4-6. hét)

**Cél:** Modul specifikus service-ek implementálása.

#### 3.1 Ticket Service
- [ ] Ticket entitás és repository
  - [ ] Ticket Panache Entity
  - [ ] Ticket repository (tenant szűréssel)
- [ ] Ticket CRUD műveletek
  - [ ] GET `/api/v1/tickets` - Listázás (szűrés, pagináció)
  - [ ] POST `/api/v1/tickets` - Létrehozás
  - [ ] GET `/api/v1/tickets/:id` - Részletek
  - [ ] PUT `/api/v1/tickets/:id` - Frissítés
  - [ ] DELETE `/api/v1/tickets/:id` - Törlés (soft delete)
- [ ] Ticket státusz kezelés
  - [ ] Státusz változtatás
  - [ ] Státusz workflow validáció
- [ ] Kommentek kezelése
  - [ ] Comment entitás
  - [ ] GET `/api/v1/tickets/:id/comments` - Kommentek listázása
  - [ ] POST `/api/v1/tickets/:id/comments` - Komment hozzáadása
- [ ] Csatolmányok kezelése
  - [ ] Attachment entitás
  - [ ] File Service integráció
- [ ] Ticket hozzárendelés
  - [ ] Assigned to kezelés
  - [ ] Hozzárendelés történet
- [ ] Modul aktiválás ellenőrzés
  - [ ] `@ModuleRequired("ticket")` annotation használata

**Dokumentáció:**
- [../docs/architecture/microservices.md](../docs/architecture/microservices.md) - Ticket Service részletek
- [../plan/01_funkcio_lista.md](../plan/01_funkcio_lista.md) - Ticket funkciók
- [../plan/03_adatbazis_tervezes.md](../plan/03_adatbazis_tervezes.md) - Tickets tábla séma

#### 3.2 Project Service
- [ ] Projekt entitás és repository
  - [ ] Project Panache Entity
  - [ ] Project repository (tenant szűréssel)
- [ ] Projekt CRUD műveletek
  - [ ] GET `/api/v1/projects` - Listázás
  - [ ] POST `/api/v1/projects` - Létrehozás
  - [ ] GET `/api/v1/projects/:id` - Részletek
  - [ ] PUT `/api/v1/projects/:id` - Frissítés
  - [ ] DELETE `/api/v1/projects/:id` - Törlés (soft delete)
- [ ] Projekt feladatok (Project Tasks)
  - [ ] ProjectTask entitás
  - [ ] GET `/api/v1/projects/:id/tasks` - Feladatok listázása
  - [ ] POST `/api/v1/projects/:id/tasks` - Feladat létrehozása
  - [ ] PUT `/api/v1/projects/:id/tasks/:taskId` - Feladat frissítése
  - [ ] DELETE `/api/v1/projects/:id/tasks/:taskId` - Feladat törlése
- [ ] Időkövetés (Time Entries)
  - [ ] TimeEntry entitás
  - [ ] GET `/api/v1/projects/:id/time-entries` - Időbejegyzések
  - [ ] POST `/api/v1/projects/:id/time-entries` - Idő rögzítése
- [ ] Árajánlat és rendelés kezelés
  - [ ] Quote entitás
  - [ ] Order entitás
  - [ ] CRUD műveletek
- [ ] Modul aktiválás ellenőrzés
  - [ ] `@ModuleRequired("project")` annotation használata

**Dokumentáció:**
- [../docs/architecture/microservices.md](../docs/architecture/microservices.md) - Project Service részletek
- [../plan/01_funkcio_lista.md](../plan/01_funkcio_lista.md) - Projekt funkciók
- [../plan/03_adatbazis_tervezes.md](../plan/03_adatbazis_tervezes.md) - Projects tábla séma

#### 3.3 Notification Service
- [ ] Notification entitás és repository
- [ ] Email küldés
  - [ ] Azure Email Services integráció
  - [ ] Email sablonok kezelése
  - [ ] Email queue kezelés
- [ ] In-app értesítések
  - [ ] GET `/api/v1/notifications` - Értesítések listázása
  - [ ] PUT `/api/v1/notifications/:id/read` - Olvasott jelölés
- [ ] Event-driven kommunikáció
  - [ ] Azure Service Bus integráció
  - [ ] Event listener-ek (ticket.created, ticket.status.changed, stb.)
- [ ] Modul aktiválás ellenőrzés
  - [ ] `@ModuleRequired("notification")` annotation használata

**Dokumentáció:**
- [../docs/architecture/microservices.md](../docs/architecture/microservices.md) - Notification Service részletek

#### 3.4 File Service
- [ ] Fájl feltöltés
  - [ ] POST `/api/v1/files/upload` - Fájl feltöltés
  - [ ] Multipart file kezelés
  - [ ] Fájl validáció (méret, típus)
- [ ] Fájl tárolás
  - [ ] Azure Blob Storage integráció (éles)
  - [ ] Local filesystem (fejlesztés)
- [ ] Fájl letöltés
  - [ ] GET `/api/v1/files/:id/download` - Fájl letöltés
- [ ] Fájl törlés
  - [ ] DELETE `/api/v1/files/:id` - Fájl törlés
- [ ] Fájl preview
  - [ ] Képek preview
  - [ ] PDF preview
- [ ] Attachment entitás kezelés
- [ ] Modul aktiválás ellenőrzés
  - [ ] `@ModuleRequired("file")` annotation használata

**Dokumentáció:**
- [../docs/architecture/microservices.md](../docs/architecture/microservices.md) - File Service részletek

#### 3.5 AI Agent Service
- [ ] AgentInSec AI Library integráció
  - [ ] Maven dependency hozzáadása
  - [ ] AgentInSec konfiguráció
  - [ ] Azure OpenAI integráció
- [ ] AI Chat funkciók
  - [ ] POST `/api/v1/ai/chat` - Chat üzenet küldése
  - [ ] POST `/api/v1/ai/chat/:sessionId` - Session folytatása
  - [ ] GET `/api/v1/ai/sessions` - Session-ök listázása
  - [ ] GET `/api/v1/ai/sessions/:sessionId` - Session részletek
  - [ ] GET `/api/v1/ai/sessions/:sessionId/messages` - Üzenetek
- [ ] Chat session kezelés
  - [ ] ChatSession entitás
  - [ ] ChatMessage entitás
  - [ ] Conversation history tárolás
- [ ] Tool regisztráció
  - [ ] POST `/api/v1/ai/tools/register` - Tool regisztrálása
  - [ ] GET `/api/v1/ai/tools` - Tool-ok listázása
  - [ ] Ticket kezelés tool-ok
  - [ ] Projekt kezelés tool-ok
  - [ ] Felhasználó keresés tool-ok
- [ ] Info blocks kezelés
  - [ ] POST `/api/v1/ai/info-blocks` - Info block hozzáadása
  - [ ] GET `/api/v1/ai/info-blocks` - Info block-ok listázása
- [ ] Agent memory kezelés
  - [ ] Episodic memory (agent_episodes tábla)
  - [ ] Procedural memory (agent_procedures tábla)
  - [ ] Semantic knowledge (agent_knowledge tábla)
  - [ ] Learning history (agent_learning_history tábla)
- [ ] Execution plan confirmation
  - [ ] POST `/api/v1/ai/execution-plans/:id/confirm` - Terv megerősítése
- [ ] Tenant izoláció
  - [ ] AI agent memória tenant-onként izolálva
  - [ ] User-specifikus AI tanulás
- [ ] Qdrant Vector Store integráció (opcionális)
  - [ ] Qdrant client konfiguráció
  - [ ] Embedding provider
  - [ ] Vector store inicializálás
- [ ] Modul aktiválás ellenőrzés
  - [ ] `@ModuleRequired("ai_agent")` annotation használata

**Dokumentáció:**
- [../docs/development/backend/ai-agent-integration.md](../docs/development/backend/ai-agent-integration.md)
- [../docs/architecture/microservices.md](../docs/architecture/microservices.md) - AI Agent Service részletek
- [../plan/01_funkcio_lista.md](../plan/01_funkcio_lista.md) - AI Agent funkciók
- [../plan/03_adatbazis_tervezes.md](../plan/03_adatbazis_tervezes.md) - AI Agent táblák séma

### Fázis 4: Integrációk és Tesztelés (7-8. hét)

**Cél:** Service-ek közötti kommunikáció és tesztelés.

#### 4.1 Service-to-Service Kommunikáció
- [ ] REST Client konfiguráció
  - [ ] Quarkus REST Client használata
  - [ ] Service URL-ek konfigurálása
  - [ ] Circuit breaker (Resilience4j)
- [ ] Asynchronous kommunikáció
  - [ ] Azure Service Bus integráció
  - [ ] Event publisher-ek
  - [ ] Event listener-ek
- [ ] Error handling
  - [ ] Global exception handler
  - [ ] Error response formátum
  - [ ] Error logging

#### 4.2 Testing
- [ ] Unit tesztek
  - [ ] Service tesztek
  - [ ] Repository tesztek
  - [ ] Controller tesztek
- [ ] Integration tesztek
  - [ ] REST API tesztek (REST Assured)
  - [ ] Database tesztek
  - [ ] Service-to-service tesztek
- [ ] Test coverage
  - [ ] Minimum 70% coverage
  - [ ] JaCoCo konfiguráció

**Dokumentáció:**
- [../docs/development/backend/testing.md](../docs/development/backend/testing.md)

#### 4.3 Dokumentáció
- [ ] API dokumentáció (OpenAPI/Swagger)
  - [ ] OpenAPI spec generálás
  - [ ] Swagger UI beállítás
- [ ] Code dokumentáció
  - [ ] JavaDoc kommentek
  - [ ] README minden service-hez

## Fontos Emlékeztetők

1. **Multi-tenant izoláció:** MINDEN adatbázis műveletnél kötelező a `tenant_id` használata
2. **Modul aktiválás:** Service-ek ellenőrzik a modul aktiválást a `tenant_modules` táblából
3. **DTO-k használata:** Ne adj vissza entity-ket közvetlenül, használj DTO-kat
4. **Error handling:** Specifikus exception-ök és exception mapper-ek
5. **Logging:** Structured logging használata
6. **Testing:** Minimum 70% test coverage

## Dokumentáció Linkek

- [Backend Setup](../docs/development/backend/setup.md)
- [Backend Architektúra](../docs/development/backend/architecture.md)
- [Coding Standards](../docs/development/backend/coding-standards.md)
- [Testing](../docs/development/backend/testing.md)
- [AI Agent Integráció](../docs/development/backend/ai-agent-integration.md)
- [Mikroszolgáltatások](../docs/architecture/microservices.md)
- [API Design](../docs/architecture/api-design.md)
- [Tervezési Dokumentumok](../plan/)

