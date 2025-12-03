# InTicky - Technikai Követelmények

## Áttekintés

Ez a dokumentum az InTicky ticketing rendszer technikai követelményeit és architektúráját írja le.

## Technológiai Stack - Döntések

### Backend

✅ **Választott: Java + Quarkus**

**Framework:**
- ✅ **Quarkus** - Választott framework
- **Verzió:** Quarkus 3.x (Java 17+)
- **Indoklás:** Modern, cloud-native, gyors indítás, alacsony memóriaigény, Azure költségoptimalizált

**Java verzió:** Java 17+ (LTS)

**Ajánlott kiegészítők:**
- **Hibernate ORM** - JPA implementáció
- **Quarkus Security** - Autentikáció és autorizáció
- **Quarkus REST** - RESTful API (JAX-RS)
- **Maven/Gradle** - Build tool
- **JUnit 5 + REST Assured** - Testing
- **Lombok** - Boilerplate csökkentés
- **Quarkus Dev Services** - Fejlesztési eszközök
- **SmallRye Health** - Health checks
- **Micrometer** - Metrics
- **AgentInSec AI Library v3.5.0** - AI chat és agent funkcionalitás (https://github.com/Kesmarki-Dev/agentinsec)

### Frontend

✅ **Választott: React**

**Ajánlott kiegészítők:**
- **TypeScript** - Típusbiztonság
- **State management** (Redux Toolkit, Zustand)
- **UI Framework** (Material-UI, Ant Design, Tailwind CSS)
- **Form kezelés** (React Hook Form)
- **Routing** (React Router)
- **HTTP Client** (Axios, Fetch API)
- **Build tool** (Vite vagy Create React App)

### Adatbázis

✅ **Választott: PostgreSQL**

**Verzió:** PostgreSQL 14+

**Multi-tenant stratégia:**
- ✅ **Shared Database, Shared Schema** - Alapértelmezett (tenant_id alapú izoláció)
- ⚠️ **Separate Database** - Enterprise megrendelések esetén (fel kell készülni)

**Azure szolgáltatás:**
- **Azure Database for PostgreSQL** - Managed service
- **Flexible Server** vagy **Single Server** deployment

**Fejlesztéshez:**
- **Docker PostgreSQL** - Lokális fejlesztés
- **PostgreSQL lokális telepítés**

### Fájl Tárolás

✅ **Választott: Azure Blob Storage**

**Környezetek:**
- **Production/Staging:** Azure Blob Storage
- **Development:** Local filesystem

**Azure Blob Storage konfiguráció:**
- **Storage Account** - Standard vagy Premium tier
- **Container-ek** - Tenant-onként vagy típus szerint (tickets, projects, avatars)
- **CDN integráció** - Azure CDN (opcionális)
- **Access tiers** - Hot, Cool, Archive

**Fejlesztéshez:**
- **Local filesystem** - `./storage` vagy `./uploads` mappa
- **Azure Storage Emulator** (Azurite) - Lokális teszteléshez

### Autentikáció és Biztonság

✅ **Minden szükséges autentikációs módszer:**

**Alapvető:**
- **JWT (JSON Web Tokens)** - Session kezeléshez
- **OAuth 2.0** - Külső bejelentkezéshez (Google, Microsoft, GitHub, stb.)
- **OpenID Connect (OIDC)** - Identity provider integráció
- **SAML 2.0** - Enterprise SSO (opcionális)

**Jelszó kezelés:**
- **bcrypt/argon2** - Jelszó hashelés
- **Password reset flow** - Email alapú
- **Password strength validation**

**Biztonság:**
- **HTTPS** - Kötelező (TLS 1.2+)
- **CORS** - Konfigurálható
- **Rate limiting** - API védelme
- **Input validation** - XSS, SQL injection védelem
- **CSRF protection**
- **Security headers** (CSP, HSTS, stb.)

**Azure integráció:**
- **Azure Active Directory (Azure AD)** - SSO
- **Azure AD B2C** - Customer identity management (opcionális)

### Email

✅ **Választott: Azure Email Services**

**Azure szolgáltatások:**
- **Azure Communication Services Email** - Modern, API-alapú
- **SendGrid (Azure)** - Transactional email (ha szükséges)
- **Azure Logic Apps** - Email workflow automatizálás (opcionális)

**Funkciók:**
- **Email sablonok** - Dinamikus tartalom (Thymeleaf, Freemarker)
- **Email queue** - Aszinkron küldés (Azure Service Bus vagy Redis)
- **Email tracking** - Delivery, open, click tracking (opcionális)
- **Bounce handling** - Hibás email címek kezelése

**Fejlesztéshez:**
- **MailHog** - Lokális email tesztelés
- **SMTP mock** - Unit teszteléshez

### Caching

✅ **Választott: Redis**

**Azure szolgáltatás:**
- **Azure Cache for Redis** - Managed Redis service
- **Tier:** Basic, Standard, Premium (döntés szükséges)

**Használati területek:**
- **Session storage** - User session-ök
- **Application cache** - Gyakran használt adatok
- **Query result cache** - Lassú lekérdezések eredményei
- **Rate limiting** - API rate limiting
- **Distributed locks** - Concurrency control

**Fejlesztéshez:**
- **Docker Redis** - Lokális fejlesztés
- **Redis lokális telepítés**

### Vector Store (AI Agent)

✅ **Választott: Qdrant** (AgentInSec AI Library)

**Cél:** AI agent embedding-ek és semantic search tárolása.

**Qdrant Vector Database:**
- **Perzisztens tárolás** - Embedding-ek és vector search
- **Semantic search** - Info blocks keresése relevancia alapján
- **Production ready** - Skálázható, thread-safe

**Használati területek:**
- **Info blocks storage** - Kontextus információk embedding-jei
- **Semantic search** - Releváns információk kiválasztása
- **Agent memory** - Perzisztens agent memória (opcionális)

**Deployment opciók:**
- **Docker Qdrant** - Lokális fejlesztés (ajánlott)
- **Qdrant Cloud** - Managed service (production)
- **Self-hosted Qdrant** - Saját szerveren (Azure VM vagy Container Instances)

**Fejlesztéshez:**
- **Docker Qdrant** - Lokális fejlesztés
- **InMemoryVectorStore** - Alternatíva fejlesztéshez (AgentInSec library, adatok elvesznek restart után)

**Azure integráció:**
- **Azure Container Instances** - Qdrant container deployment (ajánlott)
- **Azure Kubernetes Service** - Qdrant pod deployment (ha AKS-t használunk)
- **Azure VM** - Self-hosted Qdrant (opcionális)
- **Qdrant Cloud** - Managed service (production, opcionális)

### Message Queue

**Azure szolgáltatások:**
- **Azure Service Bus** - Enterprise message queue
- **Azure Queue Storage** - Egyszerűbb queue megoldás
- **Azure Event Grid** - Event-driven architektúra (opcionális)

**Használati területek:**
- **Email küldés** - Aszinkron email queue
- **Notification küldés** - Aszinkron értesítések
- **Background jobs** - Hosszú futású feladatok
- **Event processing** - Rendszer események feldolgozása

**Alternatíva (ha szükséges):**
- **Redis + Java queue library** - Egyszerűbb megoldás

## Architektúra

✅ **Választott: Mikroszolgáltatások**

**Szolgáltatások (tervezett):**
- **API Gateway** - Egyetlen belépési pont
- **Auth Service** - Autentikáció és autorizáció
- **Ticket Service** - Support ticketek kezelése
- **Project Service** - Fejlesztési projektek kezelése
- **User Service** - Felhasználó kezelés
- **Notification Service** - Értesítések küldése
- **File Service** - Fájl feltöltés és kezelés
- **Report Service** - Jelentések generálása (opcionális)

**Kommunikáció:**
- **Synchronous:** REST API (HTTP)
- **Asynchronous:** Message Queue (Azure Service Bus)

**Service Discovery:**
- **Azure Service Fabric** (ha szükséges)
- **Kubernetes Service Discovery** (ha Azure Kubernetes Service-t használunk)
- **API Gateway routing** - Egyszerűbb megoldás

### API Design

✅ **RESTful API elvek elfogadva**

**API elvek:**
- Resource-alapú URL-ek
- HTTP metódusok használata (GET, POST, PUT, DELETE, PATCH)
- Konzisztens válasz formátum (JSON)
- Verziózás (/api/v1/)
- Pagináció
- Filtering, sorting, searching
- HATEOAS (opcionális)

**API Gateway:**
- **Azure API Management** - API gateway, rate limiting, monitoring
- **Kong** - Open source alternatíva (ha szükséges)

**API Dokumentáció:**
- **OpenAPI/Swagger** - API specifikáció
- **Swagger UI** - Interaktív dokumentáció

**Példa végpontok:**
```
GET    /api/v1/tickets
POST   /api/v1/tickets
GET    /api/v1/tickets/:id
PUT    /api/v1/tickets/:id
DELETE /api/v1/tickets/:id
GET    /api/v1/tickets/:id/comments
POST   /api/v1/tickets/:id/comments

GET    /api/v1/projects
POST   /api/v1/projects
GET    /api/v1/projects/:id
GET    /api/v1/projects/:id/tasks
POST   /api/v1/projects/:id/tasks
```

## Deployment

### Környezetek

- **Development** - Lokális fejlesztés (Docker Compose)
- **Staging** - Azure környezet (tesztelés)
- **Production** - Azure környezet (éles)

### Containerizáció

✅ **Docker használata**

**Container-ek:**
- Backend service-ek (Java)
- Frontend (Nginx vagy Node.js serve)
- PostgreSQL (fejlesztéshez)
- Redis (fejlesztéshez)

**Docker Compose** - Lokális fejlesztéshez

**Dockerfile-ok:**
- Multi-stage builds - Optimalizált image méret
- Layer caching - Gyorsabb build

### CI/CD

✅ **Választott: Jenkins**

**Jenkins konfiguráció:**
- **Jenkinsfile** - Pipeline as Code
- **Jenkins Agents** - Build és deployment
- **Azure integráció** - Azure plugins

**Pipeline fázisok:**
1. **Build** - Java build (Maven/Gradle), Frontend build
2. **Test** - Unit tesztek, Integration tesztek
3. **Docker Build** - Container image-ek készítése
4. **Push to Registry** - Azure Container Registry
5. **Deploy** - Azure-ba deployment (App Service, AKS, stb.)

**Alternatíva/komplementer:**
- **GitHub Actions** - CI (ha szükséges)
- **Azure DevOps Pipelines** - Azure integráció (ha szükséges)

### Hosting - Azure

✅ **Minden Azure-ban**

**Backend szolgáltatások:**
- **Azure App Service** - Java alkalmazások (Quarkus)
- **Azure Container Instances (ACI)** - Egyszerű container deployment
- **Azure Kubernetes Service (AKS)** - Mikroszolgáltatások orchestration (ha szükséges)
- **Azure Functions** - Serverless (opcionális, specifikus funkciókhoz)

**Frontend:**
- **Azure Static Web Apps** - React alkalmazás hosting
- **Azure Blob Storage + CDN** - Static file hosting alternatíva
- **Azure App Service** - Ha SSR szükséges

**Adatbázis:**
- **Azure Database for PostgreSQL** - Managed PostgreSQL
- **Azure Database for PostgreSQL Flexible Server** - Ajánlott

**Cache:**
- **Azure Cache for Redis** - Managed Redis

**Fájl tárolás:**
- **Azure Blob Storage** - Fájlok tárolása

**Monitoring és Logging:**
- **Azure Application Insights** - APM, error tracking
- **Azure Monitor** - Metrikák, naplók
- **Azure Log Analytics** - Log aggregation

## Teljesítmény Követelmények

### Válaszidők

- **API válaszidő**: < 200ms (átlag)
- **Oldal betöltés**: < 2s (első render)
- **Keresés**: < 500ms

### Skálázhatóság

- **Egyidejű felhasználók**: 100+ (kezdetben)
- **Ticketek száma**: 10,000+ per tenant
- **API kérések**: 1000+ per másodperc

### Optimalizálás

- **Database indexing** - Kritikus mezők indexelése
- **Query optimization** - N+1 probléma elkerülése
- **Caching** - Gyakran használt adatok cache-elése
- **Lazy loading** - Nagy listák esetén
- **Image optimization** - Képek tömörítése
- **CDN** - Statikus fájlok terjesztése

## Biztonsági Követelmények

### Adatvédelem

- **GDPR compliance** - EU adatvédelmi követelmények
- **Adat titkosítás** - Rest és transit alatt
- **Backup** - Rendszeres biztonsági mentés
- **Adatmegőrzés** - Törlési szabályzat

### Hozzáférés Kontroll

- **Role-Based Access Control (RBAC)**
- **Tenant izoláció** - Adatok szigorú elkülönítése
- **API rate limiting**
- **IP whitelisting** (opcionális)

### Audit és Naplózás

- **Audit log** - Minden kritikus művelet naplózása
- **Hibanaplózás** - Error tracking (Sentry, Rollbar)
- **Hozzáférési naplók**

## Monitoring és Logging

### Application Monitoring

- **APM** (Application Performance Monitoring)
- **Error tracking** (Sentry)
- **Uptime monitoring** (UptimeRobot, Pingdom)

### Logging

- **Structured logging** (JSON formátum)
- **Log aggregation** (ELK Stack, Loki)
- **Log retention** - Megfelelő időtartam

### Metrikák

- **Response times**
- **Error rates**
- **Database query times**
- **API usage**
- **Active users**

## Fejlesztési Eszközök

### Version Control

✅ **Választott: Git**

**Repository struktúra:**
✅ **Monorepo** - Egy repository, több mappa

**Mappák:**
- `frontend/` - React alkalmazás
- `backend/` - Java mikroszolgáltatások
- `db/` - Adatbázis migrációk, seed adatok
- `docs/` - Dokumentáció

**Repository hosting:**
- **GitHub** - Ajánlott
- **GitLab** - Alternatíva
- **Azure DevOps Repos** - Ha Azure DevOps-t használunk

**Branching stratégia:**
- **Git Flow** vagy **GitHub Flow**
- **Main/Master** - Production
- **Develop** - Development
- **Feature branches** - Új funkciók
- **Release branches** - Release előkészítés

### Code Quality

- **Linting** (ESLint, Pylint, stb.)
- **Formatting** (Prettier, Black)
- **Type checking** (TypeScript, mypy)
- **Code review** - Pull request folyamat

### Testing

- **Unit tests** - Funkciók tesztelése
- **Integration tests** - API tesztelés
- **E2E tests** - Teljes folyamatok (Playwright, Cypress)
- **Test coverage** - Minimum 70%

### Dokumentáció

- **API dokumentáció** (OpenAPI/Swagger)
- **Code comments** - Komplex logika dokumentálása
- **README** - Projekt beállítás
- **Architecture Decision Records (ADR)** - Döntések dokumentálása

## Következő Lépések

1. ✅ **Technológiai stack kiválasztása** - Döntések meghozva
2. ✅ **Backend framework döntés** - Quarkus
3. **Monorepo struktúra létrehozása** - frontend/, backend/, db/, docs/ mappák
4. **Development environment setup** - Docker Compose, lokális fejlesztés
5. **Azure erőforrások tervezése** - Resource groups, services
6. **Jenkins pipeline setup** - CI/CD konfiguráció
7. **API Gateway tervezés** - Mikroszolgáltatások routing
8. **Proof of Concept** - Alapvető funkciók prototípusa

## Döntések Összefoglalása

✅ **Elfogadott:**
- Frontend: React
- Backend: Java + Quarkus
- Adatbázis: PostgreSQL
- Multi-tenant: Shared Database, Shared Schema (készen állni külön adatbázisra)
- Cloud: Azure (minden)
- Fájl tárolás: Azure Blob Storage (éles), Local (dev)
- Auth: Minden módszer
- Email: Azure Email Services
- Caching: Redis (Azure Cache for Redis)
- Architektúra: Mikroszolgáltatások
- API: RESTful
- CI/CD: Jenkins
- Repository: Monorepo (frontend, backend, db, docs)

✅ **Minden döntés meghozva:**
- Backend framework: ✅ Quarkus

