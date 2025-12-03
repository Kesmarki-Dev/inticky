# CI/CD Agent - Feladatlista

## Projekt Kontextus

Az **InTicky** egy modern, multi-tenant ticketing rendszer, amely egy régi Delphi 2009-es asztali alkalmazás modernizált, webes változata. A rendszer támogatja a support ticketek kezelését és a fejlesztési projektek menedzsmentjét.

**CI/CD Stack:**
- **Jenkins** - CI/CD szerver
- **Docker** - Containerizáció
- **Azure** - Cloud platform
- **Maven** - Backend build
- **NPM/Yarn** - Frontend build
- **Azure Container Registry** - Container registry
- **Azure App Service** - Backend deployment
- **Azure Static Web Apps** - Frontend deployment

## Függőségek Más Agentekkel

### Backend Agent
- **Függőség:** Backend service-ek build és deployment
- **Időpont:** Backend fejlesztés után
- **Kommunikáció:** CI/CD agent buildeli és deployolja a backend service-eket

### Frontend Agent
- **Függőség:** Frontend build és deployment
- **Időpont:** Frontend fejlesztés után
- **Kommunikáció:** CI/CD agent buildeli és deployolja a frontend-et

### Database Agent
- **Függőség:** Adatbázis migrációk futtatása deployment során
- **Időpont:** Deployment során
- **Kommunikáció:** CI/CD agent futtatja a migrációkat deployment előtt

## Fázisok és Feladatok

### Fázis 1: Jenkins Setup (1. hét)

**Cél:** Jenkins szerver konfigurálása és alapvető pipeline létrehozása.

#### 1.1 Jenkins Szerver Konfiguráció
- [ ] Jenkins telepítés (ha nincs)
- [ ] Plugins telepítése:
  - [ ] Docker plugin
  - [ ] Azure plugin
  - [ ] Git plugin
  - [ ] Pipeline plugin
- [ ] Credentials beállítása:
  - [ ] Azure credentials
  - [ ] GitHub credentials
  - [ ] Docker registry credentials

#### 1.2 Jenkinsfile Létrehozása
- [ ] Root Jenkinsfile létrehozása
- [ ] Pipeline struktúra:
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
          stage('Deploy') { ... }
      }
  }
  ```

**Dokumentáció:**
- [../docs/deployment/ci-cd.md](../docs/deployment/ci-cd.md)
- [../plan/09_kezdesi_utmutato.md](../plan/09_kezdesi_utmutato.md)

### Fázis 2: Build Stages (1. hét)

**Cél:** Frontend és backend build automatizálása.

#### 2.1 Frontend Build
- [ ] Frontend build stage
- [ ] NPM/Yarn install
- [ ] TypeScript compilation
- [ ] Production build
- [ ] Build artifact storage

#### 2.2 Backend Build
- [ ] Backend build stage (minden service-hez)
- [ ] Maven build
- [ ] Unit tesztek futtatása
- [ ] Build artifact storage

#### 2.3 Test Stage
- [ ] Unit tesztek futtatása (frontend + backend)
- [ ] Integration tesztek futtatása
- [ ] Test coverage reporting
- [ ] Test failure blocking deployment

### Fázis 3: Docker Build és Registry (1. hét)

**Cél:** Container image-ek létrehozása és tárolása.

#### 3.1 Docker Build
- [ ] Dockerfile-ek létrehozása:
  - [ ] Backend service-ek Dockerfile-jei
  - [ ] Frontend Dockerfile (ha szükséges)
- [ ] Multi-stage build optimalizálás
- [ ] Docker build stage pipeline-ben

#### 3.2 Azure Container Registry
- [ ] ACR létrehozása
- [ ] ACR credentials konfiguráció
- [ ] Image push stage
- [ ] Image tagging (version, latest)

### Fázis 4: Azure Infrastruktúra (2. hét)

**Cél:** Azure erőforrások létrehozása és konfigurálása.

#### 4.1 Azure Resource Group
- [ ] Resource group létrehozása: `inticky-rg`
- [ ] Location választása (pl. West Europe)

#### 4.2 Azure Szolgáltatások
- [ ] Azure Database for PostgreSQL
  - [ ] Server létrehozása
  - [ ] Firewall rules
  - [ ] Connection string konfiguráció
- [ ] Azure Cache for Redis
  - [ ] Redis cache létrehozása
  - [ ] Connection string konfiguráció
- [ ] Azure Blob Storage
  - [ ] Storage account létrehozása
  - [ ] Container-ek létrehozása
- [ ] Azure Container Registry
  - [ ] ACR létrehozása (ha még nincs)
- [ ] Azure App Service (Backend service-ekhez)
  - [ ] App Service plan létrehozása
  - [ ] App Service-ek létrehozása (minden service-hez)
  - [ ] Environment változók konfigurálása
- [ ] Azure Static Web Apps (Frontend-hez)
  - [ ] Static Web App létrehozása
  - [ ] Build konfiguráció
- [ ] Azure API Management (API Gateway)
  - [ ] API Management instance létrehozása
  - [ ] Backend service-ek regisztrálása
- [ ] Azure Application Insights (Monitoring)
  - [ ] Application Insights létrehozása
  - [ ] Instrumentation key konfiguráció
- [ ] Qdrant Deployment (AI Agent)
  - [ ] Azure Container Instances (opció 1)
  - [ ] Azure Kubernetes Service (opció 2)
  - [ ] Qdrant Cloud (opció 3)

**Dokumentáció:**
- [../docs/deployment/azure-setup.md](../docs/deployment/azure-setup.md)
- [../plan/09_kezdesi_utmutato.md](../plan/09_kezdesi_utmutato.md)

#### 4.3 Networking és Security
- [ ] Virtual Network (ha szükséges)
- [ ] Firewall rules
- [ ] SSL/TLS tanúsítványok
- [ ] Azure AD integráció (ha szükséges)

### Fázis 5: Deployment Stages (1. hét)

**Cél:** Automatizált deployment pipeline.

#### 5.1 Staging Deployment
- [ ] Staging environment konfiguráció
- [ ] Deployment stage (staging)
- [ ] Health check
- [ ] Smoke tests

#### 5.2 Production Deployment
- [ ] Production environment konfiguráció
- [ ] Manual approval gate
- [ ] Deployment stage (production)
- [ ] Health check
- [ ] Rollback mechanizmus

#### 5.3 Database Migrations
- [ ] Migrációk futtatása deployment előtt
- [ ] Flyway/Liquibase integráció
- [ ] Rollback script (ha szükséges)

### Fázis 6: Environment Konfigurációk (1. hét)

**Cél:** Környezet-specifikus konfigurációk beállítása.

#### 6.1 Environment Változók
- [ ] Development environment változók
- [ ] Staging environment változók
- [ ] Production environment változók
- [ ] Connection string-ek
- [ ] Feature flags

#### 6.2 Qdrant Konfiguráció
- [ ] Development Qdrant connection
- [ ] Staging Qdrant connection
- [ ] Production Qdrant connection

**Dokumentáció:**
- [../docs/deployment/environments.md](../docs/deployment/environments.md)

### Fázis 7: Monitoring és Logging (1. hét)

**Cél:** Monitoring és logging beállítása.

#### 7.1 Application Insights
- [ ] Application Insights setup
- [ ] Instrumentation minden service-ben
- [ ] Custom metrics
- [ ] Alert rules

#### 7.2 Logging
- [ ] Centralized logging konfiguráció
- [ ] Log aggregation
- [ ] Log retention policy

**Dokumentáció:**
- [../docs/deployment/monitoring.md](../docs/deployment/monitoring.md)

## Fontos Emlékeztetők

1. **Test coverage:** Minimum 70% követelmény
2. **Pipeline reliability:** Error handling minden stage-ben
3. **Security:** Secrets management, security scanning
4. **Performance:** Build idő optimalizálás
5. **Documentation:** Pipeline változások dokumentálása

## Dokumentáció Linkek

- [CI/CD Pipeline](../docs/deployment/ci-cd.md)
- [Azure Setup](../docs/deployment/azure-setup.md)
- [Environments](../docs/deployment/environments.md)
- [Monitoring](../docs/deployment/monitoring.md)
- [Tervezési Dokumentumok](../plan/)

