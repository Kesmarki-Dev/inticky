# Documentation Agent - Feladatlista

## Projekt Kontextus

Az **InTicky** egy modern, multi-tenant ticketing rendszer, amely egy régi Delphi 2009-es asztali alkalmazás modernizált, webes változata. A rendszer támogatja a support ticketek kezelését és a fejlesztési projektek menedzsmentjét, mikroszolgáltatások architektúrával.

**Dokumentáció Célja:**
- Fejlesztők számára útmutatók
- Architektúra dokumentáció
- API dokumentáció
- Deployment útmutatók
- Best practices és coding standards

**Dokumentáció Formátum:**
- Markdown fájlok
- Kód példákkal
- Step-by-step útmutatók
- Troubleshooting részek

## Függőségek Más Agentekkel

### Backend Agent
- **Függőség:** Backend implementáció dokumentálása
- **Időpont:** Backend fejlesztés során és után
- **Kommunikáció:** Backend agent implementál, documentation agent dokumentálja

### Frontend Agent
- **Függőség:** Frontend implementáció dokumentálása
- **Időpont:** Frontend fejlesztés során és után
- **Kommunikáció:** Frontend agent implementál, documentation agent dokumentálja

### Database Agent
- **Függőség:** Adatbázis séma dokumentálása
- **Időpont:** Database migrációk során és után
- **Kommunikáció:** Database agent létrehozza a migrációkat, documentation agent dokumentálja

### CI/CD Agent
- **Függőség:** CI/CD pipeline dokumentálása
- **Időpont:** CI/CD setup során és után
- **Kommunikáció:** CI/CD agent beállítja a pipeline-t, documentation agent dokumentálja

## Fázisok és Feladatok

### Fázis 1: Alapvető Dokumentációk (1. hét)

**Cél:** Getting started és setup útmutatók létrehozása.

#### 1.1 Getting Started Dokumentáció
- [ ] `docs/development/getting-started.md` frissítése
- [ ] Projekt áttekintés
- [ ] Előfeltételek (Java, Node.js, Docker, stb.)
- [ ] Monorepo struktúra
- [ ] Gyors kezdés lépések:
  - [ ] Repository klónozása
  - [ ] Docker Compose indítása
  - [ ] Backend indítása
  - [ ] Frontend indítása
  - [ ] Adatbázis migrációk futtatása
- [ ] Troubleshooting rész

**Dokumentáció:**
- [../plan/09_kezdesi_utmutato.md](../plan/09_kezdesi_utmutato.md)

#### 1.2 Backend Setup Dokumentáció
- [ ] `docs/development/backend/setup.md` frissítése
- [ ] Quarkus projekt inicializálás
- [ ] Maven konfiguráció
- [ ] Docker Compose setup
- [ ] Lokális fejlesztés indítása:
  - [ ] PostgreSQL connection
  - [ ] Redis connection
  - [ ] Qdrant connection (AI Agent)
- [ ] IDE beállítások (IntelliJ IDEA, VS Code)
- [ ] Environment változók
- [ ] Troubleshooting

**Dokumentáció:**
- [../plan/02_technikai_kovetelmenyek.md](../plan/02_technikai_kovetelmenyek.md)
- [../plan/09_kezdesi_utmutato.md](../plan/09_kezdesi_utmutato.md)

#### 1.3 Frontend Setup Dokumentáció
- [ ] `docs/development/frontend/setup.md` frissítése
- [ ] React projekt inicializálás (Vite)
- [ ] TypeScript konfiguráció
- [ ] NPM/Yarn package management
- [ ] Lokális fejlesztés indítása
- [ ] IDE beállítások
- [ ] Environment változók
- [ ] Troubleshooting

**Dokumentáció:**
- [../plan/02_technikai_kovetelmenyek.md](../plan/02_technikai_kovetelmenyek.md)

#### 1.4 Database Setup Dokumentáció
- [ ] `docs/development/database/setup.md` frissítése
- [ ] PostgreSQL lokális telepítés
- [ ] Docker Compose használata
- [ ] Connection string konfiguráció
- [ ] Adatbázis létrehozása
- [ ] Qdrant Vector Store setup (AI Agent)
- [ ] Troubleshooting

**Dokumentáció:**
- [../plan/02_technikai_kovetelmenyek.md](../plan/02_technikai_kovetelmenyek.md)
- [../plan/03_adatbazis_tervezes.md](../plan/03_adatbazis_tervezes.md)

### Fázis 2: Architektúra Dokumentációk (2. hét)

**Cél:** Rendszer architektúra részletes dokumentálása.

#### 2.1 System Overview
- [ ] `docs/architecture/system-overview.md` frissítése
- [ ] Rendszer áttekintés
- [ ] Főbb komponensek (Frontend, Backend, Database)
- [ ] Multi-tenant architektúra
- [ ] Moduláris felépítés
- [ ] Diagramok (opcionális)

**Dokumentáció:**
- [../plan/00_projekt_attekintes.md](../plan/00_projekt_attekintes.md)
- [../plan/08_modularis_felepites.md](../plan/08_modularis_felepites.md)

#### 2.2 Mikroszolgáltatások Dokumentáció
- [ ] `docs/architecture/microservices.md` frissítése
- [ ] Service-ek listája és leírása
- [ ] Service kommunikáció (REST, Message Queue)
- [ ] API Gateway szerepe
- [ ] Modul aktiválás mechanizmus
- [ ] Deployment stratégia

**Dokumentáció:**
- [../plan/08_modularis_felepites.md](../plan/08_modularis_felepites.md)

#### 2.3 Database Design Dokumentáció
- [ ] `docs/architecture/database-design.md` frissítése
- [ ] Adatbázis struktúra összefoglaló
- [ ] Főbb entitások (Tenants, Users, Tickets, Projects, stb.)
- [ ] Multi-tenant modell (Shared Database, Shared Schema)
- [ ] Indexelési stratégia
- [ ] RLS policy-k áttekintése

**Dokumentáció:**
- [../plan/03_adatbazis_tervezes.md](../plan/03_adatbazis_tervezes.md)
- [../plan/04_multi_tenant_architektura.md](../plan/04_multi_tenant_architektura.md)

#### 2.4 API Design Dokumentáció
- [ ] `docs/architecture/api-design.md` frissítése
- [ ] RESTful API elvek
- [ ] API Gateway konfiguráció
- [ ] Verziózás (/api/v1/)
- [ ] Pagináció, filtering, sorting
- [ ] Error handling
- [ ] Authentication és authorization

**Dokumentáció:**
- [../plan/02_technikai_kovetelmenyek.md](../plan/02_technikai_kovetelmenyek.md)

### Fázis 3: Fejlesztői Dokumentációk (2-3. hét)

**Cél:** Backend és frontend fejlesztők számára részletes útmutatók.

#### 3.1 Backend Architektúra Dokumentáció
- [ ] `docs/development/backend/architecture.md` frissítése
- [ ] Service struktúra
- [ ] Moduláris felépítés (minden modul külön service)
- [ ] Package struktúra (controller, service, repository, model, config)
- [ ] Modul aktiválás implementáció
- [ ] Multi-tenant izoláció implementáció
- [ ] Service-to-service kommunikáció

**Dokumentáció:**
- [../plan/08_modularis_felepites.md](../plan/08_modularis_felepites.md)
- [../plan/07_monorepo_struktura.md](../plan/07_monorepo_struktura.md)

#### 3.2 Backend Coding Standards
- [ ] `docs/development/backend/coding-standards.md` frissítése
- [ ] Java coding conventions
- [ ] Quarkus best practices
- [ ] Code style guide
- [ ] Naming conventions
- [ ] Kommentelési szabályok
- [ ] Error handling patterns

#### 3.3 Backend Testing Dokumentáció
- [ ] `docs/development/backend/testing.md` frissítése
- [ ] Unit testing (JUnit 5)
- [ ] Integration testing
- [ ] REST API testing (REST Assured)
- [ ] Test coverage követelmények (minimum 70%)
- [ ] Testing best practices
- [ ] Mock-olás és test data

#### 3.4 Frontend Architektúra Dokumentáció
- [ ] `docs/development/frontend/architecture.md` frissítése
- [ ] Komponens struktúra
- [ ] State management (Redux Toolkit vagy Zustand)
- [ ] Routing (React Router)
- [ ] API integráció
- [ ] Modul check implementáció

**Dokumentáció:**
- [../plan/08_modularis_felepites.md](../plan/08_modularis_felepites.md)

#### 3.5 Frontend Coding Standards
- [ ] `docs/development/frontend/coding-standards.md` frissítése
- [ ] React/TypeScript coding conventions
- [ ] Komponens best practices
- [ ] Hooks használata
- [ ] Code style guide
- [ ] Naming conventions

#### 3.6 Frontend Testing Dokumentáció
- [ ] `docs/development/frontend/testing.md` frissítése
- [ ] Unit testing (Vitest, React Testing Library)
- [ ] Component testing
- [ ] E2E testing (Playwright vagy Cypress)
- [ ] Test coverage követelmények
- [ ] Testing best practices

#### 3.7 AI Agent Integráció Dokumentáció
- [ ] `docs/development/backend/ai-agent-integration.md` frissítése
- [ ] AgentInSec library overview
- [ ] Maven dependency
- [ ] Service struktúra
- [ ] Konfiguráció (application.yml)
- [ ] Service implementáció példák
- [ ] Tool regisztráció példák
- [ ] Info blocks kezelés
- [ ] Tenant izoláció
- [ ] Qdrant Vector Store integráció

**Dokumentáció:**
- [../plan/01_funkcio_lista.md](../plan/01_funkcio_lista.md) - AI Agent funkciók
- [../plan/02_technikai_kovetelmenyek.md](../plan/02_technikai_kovetelmenyek.md) - Qdrant

#### 3.8 Database Migrations Dokumentáció
- [ ] `docs/development/database/migrations.md` frissítése
- [ ] Flyway vagy Liquibase használata
- [ ] Migrációk létrehozása
- [ ] Migrációk futtatása
- [ ] Rollback procedúrák
- [ ] Naming konvenciók

**Dokumentáció:**
- [../plan/03_adatbazis_tervezes.md](../plan/03_adatbazis_tervezes.md)

#### 3.9 Database Seed Data Dokumentáció
- [ ] `docs/development/database/seed-data.md` frissítése
- [ ] Seed adatok célja
- [ ] Seed scriptek létrehozása
- [ ] Seed adatok futtatása
- [ ] Dev és test seed adatok

#### 3.10 Database Backup/Restore Dokumentáció
- [ ] `docs/development/database/backup-restore.md` frissítése
- [ ] Backup stratégia
- [ ] Backup scriptek
- [ ] Restore procedúrák
- [ ] Lokális és Azure backup

### Fázis 4: Deployment Dokumentációk (2. hét)

**Cél:** CI/CD és Azure deployment részletes dokumentálása.

#### 4.1 CI/CD Pipeline Dokumentáció
- [ ] `docs/deployment/ci-cd.md` frissítése
- [ ] Jenkins pipeline konfiguráció
- [ ] Pipeline stages (Build, Test, Docker Build, Deploy)
- [ ] Jenkinsfile példa
- [ ] Azure Container Registry integráció
- [ ] Environment konfigurációk

**Dokumentáció:**
- [../plan/02_technikai_kovetelmenyek.md](../plan/02_technikai_kovetelmenyek.md)
- [../plan/09_kezdesi_utmutato.md](../plan/09_kezdesi_utmutato.md)

#### 4.2 Azure Setup Dokumentáció
- [ ] `docs/deployment/azure-setup.md` frissítése
- [ ] Azure Resource Group létrehozása
- [ ] Azure szolgáltatások (App Service, PostgreSQL, Redis, Blob Storage, stb.)
- [ ] Azure Container Registry
- [ ] Networking és Security
- [ ] Qdrant deployment opciók (Azure Container Instances, AKS, Qdrant Cloud)

**Dokumentáció:**
- [../plan/02_technikai_kovetelmenyek.md](../plan/02_technikai_kovetelmenyek.md)
- [../plan/09_kezdesi_utmutato.md](../plan/09_kezdesi_utmutato.md)

#### 4.3 Environments Dokumentáció
- [ ] `docs/deployment/environments.md` frissítése
- [ ] Development, Staging, Production környezetek
- [ ] Environment változók
- [ ] Connection string-ek
- [ ] Feature flags
- [ ] Qdrant konfiguráció minden környezethez

#### 4.4 Monitoring Dokumentáció
- [ ] `docs/deployment/monitoring.md` frissítése
- [ ] Azure Application Insights setup
- [ ] Logging konfiguráció
- [ ] Monitoring metrikák
- [ ] Error tracking
- [ ] Performance monitoring

### Fázis 5: API Dokumentáció (1-2. hét)

**Cél:** REST API részletes dokumentálása.

#### 5.1 OpenAPI Spec
- [ ] `docs/api/openapi.yaml` frissítése
- [ ] OpenAPI 3.0 spec skeleton
- [ ] Alapvető struktúra
- [ ] Példa végpontok (Auth, Tickets, Projects)
- [ ] Request/Response példák
- [ ] Error response formátumok

**Dokumentáció:**
- [../plan/02_technikai_kovetelmenyek.md](../plan/02_technikai_kovetelmenyek.md)
- [../plan/01_funkcio_lista.md](../plan/01_funkcio_lista.md)

#### 5.2 Swagger UI Dokumentáció
- [ ] `docs/api/swagger-ui.md` frissítése
- [ ] Swagger UI használata
- [ ] API dokumentáció elérése
- [ ] Swagger UI konfiguráció
- [ ] Példa kérések

### Fázis 6: Cursor Workspace Setup Dokumentáció (Kész)

**Cél:** Cursor agent workspace setup útmutató (már kész).

- [x] `docs/development/cursor-workspace-setup.md` létrehozva
- [x] Különböző megoldási opciók
- [x] Lépésről lépésre setup útmutató
- [x] Best practices és troubleshooting

## Dokumentáció Karbantartás (Folyamatos)

**Cél:** Dokumentáció naprakészen tartása.

- [ ] Dokumentáció frissítése változtatások esetén
- [ ] Linkek ellenőrzése
- [ ] Példák frissítése
- [ ] Troubleshooting bővítése új problémákkal
- [ ] Best practices bővítése

## Fontos Emlékeztetők

1. **Világos és érthető:** Olvasóbarát nyelv használata
2. **Példák:** Mindig tartalmaz kód példákat
3. **Troubleshooting:** Gyakori problémák és megoldások
4. **Linkek:** Kapcsolódó dokumentumok linkelése
5. **Frissítés:** Tartsd naprakészen a dokumentációt változtatások esetén
6. **Markdown formátum:** Konzisztens formázás

## Dokumentáció Linkek

- [Tervezési Dokumentumok](../plan/)
- [Architektúra Dokumentációk](./architecture/)
- [Fejlesztői Dokumentációk](./development/)
- [Deployment Dokumentációk](./deployment/)
- [API Dokumentációk](./api/)

