# InTicky - Kezdési Útmutató

## Áttekintés

Ez a dokumentum a projekt kezdési lépéseit és a fejlesztés indítását írja le.

## Projekt Fázisok

### Fázis 1: Projekt Inicializálás (1-2 hét)

**Cél:** Alapvető projekt struktúra és környezet létrehozása.

#### 1.1 Repository Létrehozása

**Lépések:**
1. **Git repository létrehozása**
   - GitHub/GitLab repository
   - Monorepo struktúra
   - README.md inicializálása
   - .gitignore beállítása

2. **Monorepo mappák létrehozása**
   ```
   inticky/
   ├── frontend/
   ├── backend/
   ├── db/
   ├── docs/
   └── deployment/  # Egyedi deployment modul (később)
   ```

3. **Branching stratégia beállítása**
   - `main` - Production
   - `develop` - Development
   - `feature/*` - Új funkciók
   - `fix/*` - Bug javítások

#### 1.2 Development Environment Setup

**Lokális fejlesztés:**
1. **Docker Compose létrehozása**
   - PostgreSQL container
   - Redis container
   - (Opcionális) Backend service-ek

2. **Backend setup**
   - Java projekt inicializálás (Quarkus)
   - Maven/Gradle konfiguráció
   - Alapvető service struktúra (api-gateway, auth-service, stb.)
   - Quarkus 3.x konfiguráció

3. **Frontend setup**
   - React projekt inicializálás (Vite vagy CRA)
   - TypeScript konfiguráció
   - Alapvető struktúra

4. **Adatbázis setup**
   - Flyway vagy Liquibase inicializálás
   - Migrációs mappák létrehozása
   - Seed adatok mappák

#### 1.3 Dokumentáció Kezdete

**Szükséges dokumentációk:**

**DB-seknek:**
- [ ] Adatbázis séma dokumentáció
- [ ] Migrációk dokumentációja
- [ ] Seed adatok dokumentációja
- [ ] Adatbázis connection string dokumentáció
- [ ] Backup/restore procedúrák

**Backend fejlesztőknek:**
- [ ] Backend architektúra dokumentáció
- [ ] Service struktúra dokumentáció
- [ ] API dokumentáció (OpenAPI/Swagger)
- [ ] Development environment setup útmutató
- [ ] Coding standards és best practices
- [ ] Testing útmutató

**Frontend fejlesztőknek:**
- [ ] Frontend architektúra dokumentáció
- [ ] Komponens struktúra dokumentáció
- [ ] State management dokumentáció
- [ ] Development environment setup útmutató
- [ ] Coding standards és best practices
- [ ] Testing útmutató

**DevOps/Deployment:**
- [ ] CI/CD pipeline dokumentáció
- [ ] Azure deployment útmutató
- [ ] Environment konfigurációk
- [ ] Monitoring és logging setup

### Fázis 2: CI/CD Kialakítása (1 hét)

**Cél:** Automatizált build, test és deployment pipeline.

#### 2.1 Jenkins Setup

**Lépések:**
1. **Jenkins szerver konfigurálása**
   - Jenkins telepítés (ha nincs)
   - Plugins telepítése (Docker, Azure, Git, stb.)
   - Credentials beállítása (Azure, GitHub, stb.)

2. **Jenkinsfile létrehozása**
   - Root Jenkinsfile (monorepo)
   - Pipeline stages:
     - Checkout
     - Build (Frontend + Backend)
     - Test (Unit + Integration)
     - Docker Build
     - Push to Registry
     - Deploy (Staging/Production)

3. **Azure Container Registry setup**
   - ACR létrehozása
   - Jenkins integráció

#### 2.2 CI/CD Pipeline Stages

**Pipeline struktúra:**
```groovy
pipeline {
    agent any
    
    stages {
        stage('Checkout') { ... }
        stage('Build Frontend') { ... }
        stage('Build Backend') { ... }
        stage('Run Tests') { ... }
        stage('Docker Build') { ... }
        stage('Push to ACR') { ... }
        stage('Deploy to Staging') { ... }
        stage('Deploy to Production') { ... } // Manual approval
    }
}
```

#### 2.3 Environment Konfigurációk

**Környezetek:**
- **Development** - Lokális Docker Compose
- **Staging** - Azure App Service
- **Production** - Azure App Service

**Konfigurációk:**
- Environment változók
- Connection string-ek
- Feature flags

### Fázis 3: Alapvető Infrastruktúra (1 hét)

**Cél:** Azure erőforrások létrehozása és konfigurálása.

#### 3.1 Azure Resource Group

**Létrehozás:**
- Resource group: `inticky-rg`
- Location: választás (pl. West Europe)

#### 3.2 Azure Szolgáltatások

**Szükséges erőforrások:**
- [ ] Azure Database for PostgreSQL
- [ ] Azure Cache for Redis
- [ ] Azure Blob Storage
- [ ] Azure Container Registry
- [ ] Azure App Service (Backend service-ekhez)
- [ ] Azure Static Web Apps (Frontend-hez)
- [ ] Azure API Management (API Gateway)
- [ ] Azure Application Insights (Monitoring)

#### 3.3 Networking és Security

- [ ] Virtual Network (ha szükséges)
- [ ] Firewall rules
- [ ] SSL/TLS tanúsítványok
- [ ] Azure AD integráció (ha szükséges)

### Fázis 4: Core Funkciók Fejlesztése (4-6 hét)

**Cél:** Alapvető funkcionalitás implementálása.

#### 4.1 Backend Core

**Prioritás szerint:**
1. **Auth Service**
   - JWT token generálás
   - Bejelentkezés API
   - Jelszó hash
   - Multi-tenant support

2. **User Service**
   - Felhasználó CRUD
   - Tenant kezelés
   - Szerepkör kezelés

3. **API Gateway**
   - Routing konfiguráció
   - Authentication middleware
   - Tenant resolution
   - Modul aktiválás ellenőrzés

4. **Ticket Service** (ha ticket modul aktív)
   - Ticket CRUD
   - Kommentek
   - Csatolmányok

5. **Project Service** (ha project modul aktív)
   - Projekt CRUD
   - Projekt feladatok
   - Időkövetés

#### 4.2 Frontend Core

**Prioritás szerint:**
1. **Alapvető struktúra**
   - Routing
   - Layout komponensek
   - Navigation

2. **Autentikáció**
   - Bejelentkezés oldal
   - JWT token kezelés
   - Protected routes

3. **Tenant kezelés**
   - Tenant kiválasztás
   - Tenant context

4. **Dashboard**
   - Alapvető dashboard
   - Statisztikák

5. **Ticket/Projekt modulok** (ha aktívak)
   - Lista nézetek
   - Részletes nézetek
   - CRUD műveletek

#### 4.3 Adatbázis

**Prioritás szerint:**
1. **Alapvető migrációk**
   - Tenants tábla
   - Users tábla
   - Tenant_modules tábla

2. **Modul specifikus migrációk**
   - Tickets (ha ticket modul)
   - Projects (ha project modul)

3. **Seed adatok**
   - Alapértelmezett tenant
   - Admin felhasználó
   - Alapértelmezett modulok

### Fázis 5: Egyedi Deployment Modul (Később)

**Megjegyzés:** Ez a modul később lesz specifikálva a régi Delphi rendszer működéséhez.

**Tervezett funkciók:**
- [ ] Régi rendszer integráció
- [ ] Adat migráció eszközök
- [ ] Deployment automatizálás
- [ ] Rollback mechanizmus

## Dokumentáció Struktúra

### docs/ Mappa Struktúra

```
docs/
├── architecture/
│   ├── system-overview.md
│   ├── microservices.md
│   ├── database-design.md
│   └── api-design.md
├── development/
│   ├── getting-started.md
│   ├── backend/
│   │   ├── setup.md
│   │   ├── architecture.md
│   │   ├── coding-standards.md
│   │   └── testing.md
│   ├── frontend/
│   │   ├── setup.md
│   │   ├── architecture.md
│   │   ├── coding-standards.md
│   │   └── testing.md
│   └── database/
│       ├── setup.md
│       ├── migrations.md
│       ├── seed-data.md
│       └── backup-restore.md
├── deployment/
│   ├── ci-cd.md
│   ├── azure-setup.md
│   ├── environments.md
│   └── monitoring.md
└── api/
    ├── openapi.yaml
    └── swagger-ui.md
```

## Kezdési Checklist

### Előkészítés (1. hét)

- [ ] **Repository létrehozása**
  - [ ] GitHub/GitLab repository
  - [ ] Monorepo mappák létrehozása
  - [ ] README.md
  - [ ] .gitignore

- [ ] **Development Environment**
  - [ ] Docker Compose létrehozása
  - [ ] Backend projekt inicializálás
  - [ ] Frontend projekt inicializálás
  - [ ] Adatbázis migráció tool setup

- [ ] **Dokumentáció kezdete**
  - [ ] docs/ mappa struktúra
  - [ ] Getting started útmutató
  - [ ] Backend setup útmutató
  - [ ] Frontend setup útmutató
  - [ ] Database setup útmutató

### CI/CD (2. hét)

- [ ] **Jenkins Setup**
  - [ ] Jenkins szerver konfiguráció
  - [ ] Plugins telepítése
  - [ ] Credentials beállítása

- [ ] **Pipeline Létrehozása**
  - [ ] Jenkinsfile létrehozása
  - [ ] Build stage
  - [ ] Test stage
  - [ ] Docker build stage
  - [ ] Deploy stage

- [ ] **Azure Setup**
  - [ ] Resource group létrehozása
  - [ ] Container Registry
  - [ ] App Service (staging)

### Fejlesztés (3. hét+)

- [ ] **Backend Core**
  - [ ] Auth Service
  - [ ] User Service
  - [ ] API Gateway

- [ ] **Frontend Core**
  - [ ] Routing
  - [ ] Authentication
  - [ ] Layout

- [ ] **Adatbázis**
  - [ ] Alapvető migrációk
  - [ ] Seed adatok

## Ajánlott Munkafolyamat

### 1. Repository → CI/CD → Fejlesztés

**Szekvencia:**
1. ✅ **Repository létrehozása** - Első lépés
2. ✅ **Alapvető struktúra** - Mappák, Docker Compose
3. ✅ **CI/CD alapok** - Jenkins pipeline (build, test)
4. ✅ **Dokumentáció** - Setup útmutatók
5. ✅ **Fejlesztés** - Core funkciók

### 2. Párhuzamos Munka

**Csapatok:**
- **Backend csapat:** Service-ek fejlesztése
- **Frontend csapat:** UI komponensek
- **DB csapat:** Migrációk, séma finomhangolás
- **DevOps:** CI/CD, Azure setup

## Dokumentáció Prioritások

### Sürgős (1. hét)

1. **Getting Started** - Hogyan kezdjük el
2. **Backend Setup** - Hogyan indítsuk a backend-et
3. **Frontend Setup** - Hogyan indítsuk a frontend-et
4. **Database Setup** - Hogyan állítsuk be az adatbázist

### Fontos (2-3. hét)

1. **Backend Architektúra** - Service struktúra
2. **Frontend Architektúra** - Komponens struktúra
3. **API Dokumentáció** - OpenAPI spec
4. **Coding Standards** - Code style guide

### Később (4+ hét)

1. **Deployment Útmutató** - Részletes deployment
2. **Monitoring** - Monitoring setup
3. **Troubleshooting** - Gyakori problémák
4. **Best Practices** - További best practices

## Következő Lépések

1. **Repository létrehozása** - GitHub/GitLab
2. **Monorepo struktúra** - Mappák létrehozása
3. **Docker Compose** - Lokális fejlesztés
4. **Backend projekt** - Java projekt inicializálás
5. **Frontend projekt** - React projekt inicializálás
6. **Jenkins pipeline** - CI/CD alapok
7. **Dokumentáció** - Getting started útmutatók

## Egyedi Deployment Modul

**Státusz:** Később specifikáljuk

**Tervezett helye:**
```
inticky/
├── deployment/
│   ├── delphi-integration/  # Régi rendszer integráció
│   ├── migration-tools/     # Adat migráció eszközök
│   └── deployment-scripts/  # Deployment automatizálás
```

**Megjegyzés:** Ez a modul a régi Delphi rendszer működéséhez készül, részletek később.

