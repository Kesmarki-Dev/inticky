# InTicky - Modern Multi-Tenant Ticketing System

[![License](https://img.shields.io/badge/license-Proprietary-red.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.oracle.com/java/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.x-4695EB.svg)](https://quarkus.io/)
[![React](https://img.shields.io/badge/React-18+-61DAFB.svg)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-336791.svg)](https://www.postgresql.org/)

## Áttekintés

Az **InTicky** egy modern, multi-tenant ticketing rendszer, amely egy régi Delphi 2009 alapú asztali alkalmazás modernizált, webes változata. A rendszer támogatja a support ticketek kezelését és a fejlesztési projektek menedzsmentjét, mikroszolgáltatások architektúrával.

## Főbb Jellemzők

- 🏢 **Multi-tenant architektúra** - Shared Database, Shared Schema modell
- 🎫 **Support Ticket kezelés** - Teljes életciklus kezelés
- 📦 **Projekt menedzsment** - Fejlesztési projektek és feladatok
- 🤖 **AI Chat és Agent** - AgentInSec integrációval
- 🔐 **Moduláris felépítés** - Tenant-onként aktiválható modulok
- ☁️ **Azure Cloud** - Teljes Azure infrastruktúra
- 🚀 **Mikroszolgáltatások** - Quarkus alapú backend

## Technológiai Stack

### Frontend
- **React 18+** + TypeScript
- **Vite** - Build tool
- **React Router** - Routing
- **Redux Toolkit** vagy **Zustand** - State management

### Backend
- **Java 17+** (LTS)
- **Quarkus 3.x** - Framework
- **Hibernate ORM with Panache**
- **AgentInSec AI Library v3.5.0** - AI chat és agent

### Database
- **PostgreSQL 14+** - Fő adatbázis
- **Redis** - Caching
- **Qdrant** - Vector store (AI Agent)

### Cloud & Infrastructure
- **Azure App Service** - Backend hosting
- **Azure Static Web Apps** - Frontend hosting
- **Azure Database for PostgreSQL** - Managed database
- **Azure Cache for Redis** - Managed cache
- **Azure Blob Storage** - File storage
- **Jenkins** - CI/CD

## Projekt Struktúra

```
inticky/
├── backend/          # Java Quarkus mikroszolgáltatások
├── frontend/         # React alkalmazás
├── db/               # Adatbázis migrációk
├── docs/             # Dokumentáció
├── plan/             # Tervezési dokumentumok
└── deployment/       # Deployment konfigurációk
```

## Gyors Kezdés

### Előfeltételek

- Java 17+ (LTS)
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 14+ (vagy Docker)
- Redis (vagy Docker)
- Qdrant (vagy Docker)

### Lokális Fejlesztés Indítása

1. **Repository klónozása:**
```bash
git clone https://github.com/Kesmarki-Dev/inticky.git
cd inticky
```

2. **Docker Compose indítása:**
```bash
docker-compose up -d
```

3. **Backend indítása:**
```bash
cd backend
mvn quarkus:dev
```

4. **Frontend indítása:**
```bash
cd frontend
npm install
npm run dev
```

Részletes útmutató: [Getting Started](docs/development/getting-started.md)

## Dokumentáció

### Tervezési Dokumentumok
- [Projekt Áttekintés](plan/00_projekt_attekintes.md)
- [Funkció Lista](plan/01_funkcio_lista.md)
- [Technikai Követelmények](plan/02_technikai_kovetelmenyek.md)
- [Adatbázis Tervezés](plan/03_adatbazis_tervezes.md)
- [Multi-Tenant Architektúra](plan/04_multi_tenant_architektura.md)
- [Migrációs Terv](plan/05_migracios_terv.md)
- [Monorepo Struktúra](plan/07_monorepo_struktura.md)
- [Moduláris Felépítés](plan/08_modularis_felepites.md)
- [Kezdési Útmutató](plan/09_kezdesi_utmutato.md)

### Fejlesztői Dokumentáció
- [Architektúra Áttekintés](docs/architecture/system-overview.md)
- [Mikroszolgáltatások](docs/architecture/microservices.md)
- [Adatbázis Tervezés](docs/architecture/database-design.md)
- [API Tervezés](docs/architecture/api-design.md)
- [Backend Setup](docs/development/backend/setup.md)
- [Frontend Setup](docs/development/frontend/setup.md)
- [Database Setup](docs/development/database/setup.md)
- [AI Agent Integráció](docs/development/backend/ai-agent-integration.md)
- [Cursor Workspace Setup](docs/development/cursor-workspace-setup.md)

### Deployment Dokumentáció
- [Azure Setup](docs/deployment/azure-setup.md)
- [CI/CD Pipeline](docs/deployment/ci-cd.md)
- [Environment Konfiguráció](docs/deployment/environments.md)
- [Monitoring](docs/deployment/monitoring.md)

## Mikroszolgáltatások

1. **API Gateway** - Központi belépési pont
2. **Auth Service** - Autentikáció és autorizáció
3. **Ticket Service** - Support ticketek kezelése
4. **Project Service** - Fejlesztési projektek
5. **User Service** - Felhasználó kezelés
6. **Notification Service** - Értesítések
7. **File Service** - Fájl kezelés
8. **AI Agent Service** - AI chat és agent funkciók

## Moduláris Felépítés

A rendszer modulárisan épül fel, ahol minden modul egy mikroszolgáltatáshoz kapcsolódik. A modulok tenant-onként aktiválhatók a `tenant_modules` táblán keresztül.

### Alapértelmezett Modulok
- `ticket` - Support ticketek
- `project` - Fejlesztési projektek
- `user` - Felhasználó kezelés
- `notification` - Értesítések
- `file` - Fájl kezelés
- `ai_agent` - AI chat és agent

## Multi-Tenant Architektúra

A rendszer **Shared Database, Shared Schema** modellt használ `tenant_id` alapú izolációval. Minden adatbázis műveletnél kötelező a `tenant_id` használata.

## AI Agent Integráció

Az InTicky integrálja az **AgentInSec AI Library v3.5.0**-t, amely lehetővé teszi:
- AI chat funkciókat
- Autonomous agent működést
- Function calling
- Persistent memory
- Self-learning
- Tool discovery

## Fejlesztés

### Coding Standards

- [Backend Coding Standards](docs/development/backend/coding-standards.md)
- [Frontend Coding Standards](docs/development/frontend/coding-standards.md)

### Testing

- [Backend Testing](docs/development/backend/testing.md)
- [Frontend Testing](docs/development/frontend/testing.md)

## CI/CD

A projekt **Jenkins** pipeline-t használ a CI/CD-hez. Részletek: [CI/CD Pipeline](docs/deployment/ci-cd.md)

## Licenc

Proprietary - Kesmarki-Dev © 2024

## Kapcsolat

- **Repository:** https://github.com/Kesmarki-Dev/inticky
- **Szervezet:** [Kesmarki-Dev](https://github.com/Kesmarki-Dev)

## Következő Lépések

1. ✅ Projekt tervezés és dokumentáció
2. ⏳ Adatbázis migrációk létrehozása
3. ⏳ Backend mikroszolgáltatások implementálása
4. ⏳ Frontend alkalmazás fejlesztése
5. ⏳ CI/CD pipeline beállítása
6. ⏳ Azure deployment

Részletes útmutató: [Kezdési Útmutató](plan/09_kezdesi_utmutato.md)

