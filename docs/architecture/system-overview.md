# InTicky - Rendszer Áttekintés

## Célközönség

Ez a dokumentum az InTicky ticketing rendszer magas szintű architektúráját mutatja be. Fejlesztők, architektusok és projektmenedzserek számára készült.

## Rendszer Áttekintés

Az InTicky egy modern, webes ticketing rendszer, amely egy régi Delphi 2009-es asztali alkalmazás modernizált változata. A rendszer célja, hogy egyrészt kiszolgálja a jelenlegi működést (átállás), másrészt multi-tenant képességgel bárki számára elérhető legyen.

## Főbb Komponensek

### 1. Frontend (React)

**Technológia:**
- React + TypeScript
- Vite build tool
- Modern UI framework (Material-UI, Ant Design, vagy Tailwind CSS)

**Funkciók:**
- Felhasználói felület
- Ticket és projekt kezelés
- Dashboard és statisztikák
- Multi-tenant támogatás

### 2. Backend (Java + Quarkus)

**Technológia:**
- Java 17+ (LTS)
- Quarkus 3.x framework
- Mikroszolgáltatások architektúra

**Szolgáltatások:**
- API Gateway
- Auth Service
- Ticket Service
- Project Service
- User Service
- Notification Service
- File Service
- Report Service (opcionális)
- AI Agent Service (AgentInSec integráció)

### 3. Adatbázis (PostgreSQL)

**Technológia:**
- PostgreSQL 14+
- Shared Database, Shared Schema multi-tenant modell

**Funkciók:**
- Multi-tenant adatizoláció
- Row Level Security (RLS)
- Migrációk (Flyway vagy Liquibase)

### 4. Infrastruktúra (Azure)

**Szolgáltatások:**
- Azure App Service (Backend)
- Azure Static Web Apps (Frontend)
- Azure Database for PostgreSQL
- Azure Cache for Redis
- Azure Blob Storage
- Azure API Management
- Azure Application Insights
- Qdrant Vector Store (AI Agent - Azure Container Instances vagy AKS)

## Architektúra Modell

### Multi-Tenant Architektúra

**Választott modell:** Shared Database, Shared Schema

**Jellemzők:**
- Minden tenant ugyanazt az adatbázist és sémát használja
- `tenant_id` mezővel izolálva
- Row Level Security (RLS) adatbázis szintű védelem
- Application szintű validáció

**Előnyök:**
- Alacsonyabb költségek (egy adatbázis)
- Egyszerűbb adminisztráció
- Könnyebb skálázás
- Egyszerűbb backup és restore

### Moduláris Felépítés

**Választott modell:** Moduláris Mikroszolgáltatások (Opció 1)

**Jellemzők:**
- Minden modul külön mikroszolgáltatás
- Modul aktiválás per tenant
- Független deployment és skálázás
- Service-to-service kommunikáció

**Modul → Service Mapping:**
```
Ticket Modul      → ticket-service
Projekt Modul     → project-service
User Modul        → user-service
Auth Modul        → auth-service
Notification Modul → notification-service
File Modul        → file-service
Report Modul      → report-service (opcionális)
AI Agent Modul    → ai-agent-service
```

## Kommunikáció

### Service-to-Service Kommunikáció

**Synchronous:**
- REST API (HTTP)
- API Gateway routing
- Circuit breaker pattern

**Asynchronous:**
- Azure Service Bus (Message Queue)
- Event-driven architektúra
- Pub/Sub pattern

### Frontend-Backend Kommunikáció

- RESTful API hívások
- JWT token autentikáció
- API Gateway-en keresztül

## Adatfolyam

### Ticket Létrehozás Példa

1. **Frontend:** Felhasználó létrehoz egy ticketet
2. **API Gateway:** Request fogadása, tenant azonosítás
3. **Auth Service:** Token validáció
4. **Ticket Service:** Ticket létrehozása adatbázisban
5. **Notification Service:** Értesítés küldése (asynchronous)
6. **Response:** Ticket adatok visszaadása

### Projekt Létrehozás Példa

1. **Frontend:** Felhasználó létrehoz egy projektet
2. **API Gateway:** Request fogadása, modul aktiválás ellenőrzés
3. **Project Service:** Projekt létrehozása
4. **File Service:** Fájlok feltöltése (ha van)
5. **Response:** Projekt adatok visszaadása

## Biztonság

### Autentikáció és Autorizáció

- JWT token alapú autentikáció
- OAuth 2.0 támogatás
- Role-Based Access Control (RBAC)
- Multi-tenant izoláció

### Adatvédelem

- Tenant adatok izolálása (tenant_id alapú)
- Row Level Security (RLS)
- HTTPS kötelező
- Input validation
- SQL injection védelem

## Skálázhatóság

### Horizontális Skálázás

- Mikroszolgáltatások független skálázása
- Load balancing (Azure App Service)
- Stateless service-ek
- Redis cache a session kezeléshez

### Vertikális Skálázás

- Azure App Service tier-ek (S1, S2, S3)
- Adatbázis skálázás (Azure Database for PostgreSQL)
- Redis cache skálázás

## Monitoring és Logging

### Application Monitoring

- Azure Application Insights
- Health checks (SmallRye Health)
- Metrics (Micrometer)
- Error tracking

### Logging

- Structured logging (JSON formátum)
- Azure Log Analytics
- Centralizált log aggregation
- Log retention policy

## További Információk

- [Mikroszolgáltatások részletek](./microservices.md)
- [Adatbázis tervezés](./database-design.md)
- [API design](./api-design.md)
- [Multi-tenant architektúra](../../plan/04_multi_tenant_architektura.md)
- [Moduláris felépítés](../../plan/08_modularis_felepites.md)

