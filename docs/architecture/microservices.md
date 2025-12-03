# InTicky - Mikroszolgáltatások Architektúra

## Célközönség

Ez a dokumentum az InTicky mikroszolgáltatások architektúráját részletezi. Backend fejlesztők és DevOps mérnökök számára készült.

## Áttekintés

Az InTicky rendszer mikroszolgáltatások architektúrával készül, ahol minden modul külön, függetlenül deployolható szolgáltatás.

## Szolgáltatások Listája

### 1. API Gateway

**Cél:** Egyetlen belépési pont az összes backend szolgáltatáshoz.

**Funkciók:**
- Request routing a megfelelő service-hez
- Authentication middleware
- Tenant resolution (subdomain vagy header alapján)
- Modul aktiválás ellenőrzés
- Rate limiting
- Request/Response logging

**Technológia:**
- Quarkus REST
- Azure API Management (éles környezetben)

**Port:** 8080 (lokális fejlesztés)

### 2. Auth Service

**Cél:** Autentikáció és autorizáció kezelése.

**Funkciók:**
- JWT token generálás és validáció
- Bejelentkezés (email/jelszó)
- Jelszó visszaállítás
- OAuth 2.0 támogatás
- Session kezelés
- Role-Based Access Control (RBAC)

**API végpontok:**
```
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
POST   /api/v1/auth/refresh
POST   /api/v1/auth/reset-password
GET    /api/v1/auth/me
```

**Adatbázis táblák:**
- users
- user_sessions

### 3. User Service

**Cél:** Felhasználó kezelés.

**Funkciók:**
- Felhasználó CRUD műveletek
- Felhasználó profil kezelés
- Szerepkör kezelés
- Felhasználó lista és keresés

**API végpontok:**
```
GET    /api/v1/users
POST   /api/v1/users
GET    /api/v1/users/:id
PUT    /api/v1/users/:id
DELETE /api/v1/users/:id
```

**Adatbázis táblák:**
- users

### 4. Ticket Service

**Cél:** Support ticketek kezelése.

**Funkciók:**
- Ticket CRUD műveletek
- Kommentek kezelése
- Csatolmányok kezelése
- Ticket státusz követés
- Ticket hozzárendelés

**API végpontok:**
```
GET    /api/v1/tickets
POST   /api/v1/tickets
GET    /api/v1/tickets/:id
PUT    /api/v1/tickets/:id
DELETE /api/v1/tickets/:id
GET    /api/v1/tickets/:id/comments
POST   /api/v1/tickets/:id/comments
```

**Adatbázis táblák:**
- tickets
- ticket_categories
- ticket_statuses
- comments
- attachments
- tags
- ticket_tags

**Modul aktiválás:** `ticket` modul kell, hogy aktív legyen a tenant-nál.

### 5. Project Service

**Cél:** Fejlesztési projektek kezelése.

**Funkciók:**
- Projekt CRUD műveletek
- Projekt feladatok (project_tasks) kezelése
- Időkövetés (time_entries)
- Árajánlat és rendelés kezelés
- Projekt statisztikák

**API végpontok:**
```
GET    /api/v1/projects
POST   /api/v1/projects
GET    /api/v1/projects/:id
PUT    /api/v1/projects/:id
GET    /api/v1/projects/:id/tasks
POST   /api/v1/projects/:id/tasks
GET    /api/v1/projects/:id/time-entries
POST   /api/v1/projects/:id/time-entries
```

**Adatbázis táblák:**
- projects
- project_tasks
- project_task_comments
- project_task_attachments
- quotes
- orders
- time_entries

**Modul aktiválás:** `project` modul kell, hogy aktív legyen a tenant-nál.

### 6. Notification Service

**Cél:** Értesítések küldése.

**Funkciók:**
- Email értesítések küldése
- In-app értesítések
- Értesítési beállítások kezelése
- Email sablonok kezelése

**API végpontok:**
```
GET    /api/v1/notifications
POST   /api/v1/notifications
PUT    /api/v1/notifications/:id/read
```

**Adatbázis táblák:**
- notifications

**Kommunikáció:**
- Asynchronous (Azure Service Bus)
- Event-driven (ticket létrehozva, státusz változás, stb.)

**Modul aktiválás:** `notification` modul kell, hogy aktív legyen a tenant-nál.

### 7. File Service

**Cél:** Fájl feltöltés és kezelés.

**Funkciók:**
- Fájl feltöltés
- Fájl letöltés
- Fájl törlés
- Fájl preview (képek, PDF)

**API végpontok:**
```
POST   /api/v1/files/upload
GET    /api/v1/files/:id
DELETE /api/v1/files/:id
GET    /api/v1/files/:id/download
```

**Tárolás:**
- Azure Blob Storage (éles)
- Local filesystem (fejlesztés)

**Adatbázis táblák:**
- attachments
- project_task_attachments

**Modul aktiválás:** `file` modul kell, hogy aktív legyen a tenant-nál.

### 8. Report Service (Opcionális)

**Cél:** Jelentések generálása.

**Funkciók:**
- Ticket jelentések
- Projekt jelentések
- Statisztikák generálása
- Exportálás (CSV, PDF)

**API végpontok:**
```
GET    /api/v1/reports/tickets
GET    /api/v1/reports/projects
GET    /api/v1/reports/dashboard
```

**Modul aktiválás:** `report` modul kell, hogy aktív legyen a tenant-nál.

### 9. AI Agent Service

**Cél:** AI chat és agent funkcionalitás kezelése (AgentInSec library integráció).

**Funkciók:**
- AI chat üzenetek kezelése
- Conversation history kezelés
- AI agent képességek (autonomous, learning, tool discovery, memory, reflection)
- Tool regisztráció és végrehajtás
- Info blocks kezelés (kontextus információk)
- Agent memory kezelés (episodes, procedures, knowledge, learning history)
- Execution plan confirmation

**API végpontok:**
```
POST   /api/v1/ai/chat
POST   /api/v1/ai/chat/:sessionId
GET    /api/v1/ai/sessions
GET    /api/v1/ai/sessions/:sessionId
GET    /api/v1/ai/sessions/:sessionId/messages
POST   /api/v1/ai/tools/register
GET    /api/v1/ai/tools
POST   /api/v1/ai/info-blocks
GET    /api/v1/ai/info-blocks
POST   /api/v1/ai/execution-plans/:id/confirm
```

**Adatbázis táblák:**
- chat_sessions
- chat_messages
- agent_episodes
- agent_procedures
- agent_knowledge
- agent_learning_history
- agent_tool_usage
- agent_reflection_sessions
- agent_execution_plans

**Technológia:**
- AgentInSec AI Library v3.5.0
- Azure OpenAI integráció
- Qdrant Vector Store (opcionális, perzisztens tárolás)

**Modul aktiválás:** `ai_agent` modul kell, hogy aktív legyen a tenant-nál.

## Service Kommunikáció

### Synchronous Kommunikáció

**REST API (HTTP):**
- Service-to-service HTTP hívások
- API Gateway routing
- Circuit breaker pattern (Resilience4j)

**Példa:**
```java
// Ticket Service hívja a User Service-t
@RestClient
public interface UserServiceClient {
    @GET
    @Path("/users/{id}")
    User getUser(@PathParam("id") UUID id);
}
```

### Asynchronous Kommunikáció

**Azure Service Bus:**
- Event-driven kommunikáció
- Pub/Sub pattern
- Message queue

**Példa események:**
- `ticket.created`
- `ticket.status.changed`
- `project.created`
- `notification.send`

## Service Discovery

**Azure környezetben:**
- Azure API Management routing
- Service URL-ek konfigurációban
- Environment változók

**Lokális fejlesztés:**
- Docker Compose service nevek
- Localhost portok

## Modul Aktiválás

Minden service (kivéve Auth és User) modul aktiválást igényel.

**Implementáció:**
1. API Gateway ellenőrzi a modul aktiválást
2. `tenant_modules` tábla lekérdezése
3. Ha modul nem aktív → 403 Forbidden
4. Ha aktív → request továbbítása a service-hez

**Példa:**
```java
@ModuleRequired("ticket")
@GET
@Path("/tickets")
public List<Ticket> getTickets() {
    // Csak akkor érhető el, ha ticket modul aktív
}
```

## Deployment

### Lokális Fejlesztés

- Docker Compose
- Minden service külön container
- Port mapping

### Azure Deployment

- Azure App Service (minden service-hez)
- Azure Container Registry
- CI/CD pipeline (Jenkins)

## További Információk

- [Rendszer áttekintés](./system-overview.md)
- [Adatbázis tervezés](./database-design.md)
- [API design](./api-design.md)
- [Backend architektúra](../development/backend/architecture.md)
- [Moduláris felépítés](../../plan/08_modularis_felepites.md)

