# InTicky - Feladat Végrehajtási Sorrend

## Áttekintés

Ez a dokumentum bemutatja, hogy milyen sorrendben kell dolgozni az agenteknek, és mi lehet párhuzamosan.

## Kritikus Függőségek

### 1. Database Agent → Backend Agent
**Függőség:** Backend agent nem tud dolgozni adatbázis nélkül
- Database agent: Alapvető migrációk (tenants, users, tenant_modules) **ELŐSZÖR**
- Backend agent: Service-ek fejlesztése **UTÁNA**

### 2. Backend Agent → Frontend Agent
**Függőség:** Frontend agent API-kat hív
- Backend agent: API végpontok létrehozása **ELŐSZÖR**
- Frontend agent: UI komponensek és API integráció **UTÁNA**

### 3. Backend/Frontend Agent → CI/CD Agent
**Függőség:** CI/CD agent buildeli és deployolja a kódot
- Backend/Frontend agent: Kód implementálása **ELŐSZÖR**
- CI/CD agent: Build és deployment pipeline **UTÁNA**

## Ajánlott Végrehajtási Sorrend

### Fázis 1: Alapvető Infrastruktúra (1. hét) - PÁRHUZAMOSAN

**Database Agent:**
- ✅ Migráció tool setup (Flyway/Liquibase)
- ✅ Alapvető migrációk (tenants, users, tenant_modules)
- ✅ Seed adatok (alapértelmezett tenant, admin user)

**Documentation Agent:**
- ✅ Getting Started dokumentáció
- ✅ Setup útmutatók (Backend, Frontend, Database)
- ✅ Alapvető architektúra dokumentáció

**Backend Agent:**
- ✅ Projekt struktúra létrehozása
- ✅ Quarkus projektek inicializálása
- ✅ Alapvető konfiguráció

**Frontend Agent:**
- ✅ React projekt inicializálása
- ✅ TypeScript konfiguráció
- ✅ Dependencies telepítése
- ✅ Alapvető struktúra

**CI/CD Agent:**
- ✅ Jenkins setup (ha van idő)
- ✅ Alapvető pipeline struktúra (build nélkül)

### Fázis 2: Core Funkciók (2-3. hét) - SZEKVENCIÁLISAN

**1. Database Agent (1. hét):**
- ✅ Ticket modul migrációk (ha ticket modul)
- ✅ Project modul migrációk (ha project modul)
- ✅ AI Agent migrációk (ha ai_agent modul)
- ✅ Indexek és RLS policy-k

**2. Backend Agent (2. hét):**
- ✅ Auth Service implementálása
- ✅ User Service implementálása
- ✅ API Gateway implementálása
- ✅ Modul aktiválás ellenőrzés

**3. Frontend Agent (2-3. hét):**
- ✅ Routing és layout
- ✅ Autentikáció UI
- ✅ Tenant kezelés UI
- ✅ Dashboard

**4. Documentation Agent (párhuzamosan):**
- ✅ Backend architektúra dokumentáció
- ✅ Frontend architektúra dokumentáció
- ✅ API dokumentáció (ahogy készülnek az API-k)

### Fázis 3: Modul Funkciók (4-6. hét) - PÁRHUZAMOSAN (modulonként)

**Ticket Modul:**
1. Database Agent: Ticket táblák migrációi
2. Backend Agent: Ticket Service implementálása
3. Frontend Agent: Ticket UI implementálása
4. Documentation Agent: Ticket modul dokumentáció

**Project Modul:**
1. Database Agent: Project táblák migrációi
2. Backend Agent: Project Service implementálása
3. Frontend Agent: Project UI implementálása
4. Documentation Agent: Project modul dokumentáció

**AI Agent Modul:**
1. Database Agent: AI Agent táblák migrációi
2. Backend Agent: AI Agent Service implementálása
3. Frontend Agent: AI Chat UI implementálása
4. Documentation Agent: AI Agent dokumentáció

**Megjegyzés:** A modulok között nincs függőség, párhuzamosan dolgozhatsz rajtuk!

### Fázis 4: Integráció és Deployment (7-8. hét) - SZEKVENCIÁLISAN

**1. Backend Agent:**
- ✅ Service-to-service kommunikáció
- ✅ Testing
- ✅ API dokumentáció (OpenAPI)

**2. Frontend Agent:**
- ✅ Modul check implementáció
- ✅ Testing
- ✅ UI finomhangolás

**3. CI/CD Agent:**
- ✅ Docker build
- ✅ Azure infrastruktúra
- ✅ Deployment pipeline
- ✅ Environment konfigurációk

**4. Documentation Agent:**
- ✅ Deployment dokumentáció
- ✅ Monitoring dokumentáció
- ✅ Troubleshooting útmutatók

## Részletes Végrehajtási Táblázat

| Fázis | Database | Backend | Frontend | CI/CD | Documentation | Megjegyzés |
|-------|----------|---------|----------|-------|---------------|------------|
| **1. hét** | ✅ Alap migrációk | ✅ Projekt setup | ✅ Projekt setup | ⏸️ Jenkins setup | ✅ Setup docs | Párhuzamosan |
| **2. hét** | ✅ Modul migrációk | ✅ Core services | ✅ Routing/Auth | ⏸️ Várakozás | ✅ Architektúra | Database → Backend |
| **3. hét** | ✅ Indexek/RLS | ✅ Core services | ✅ Dashboard | ⏸️ Várakozás | ✅ API docs | Backend → Frontend |
| **4. hét** | ✅ Ticket migrációk | ✅ Ticket Service | ✅ Ticket UI | ⏸️ Várakozás | ✅ Ticket docs | Modul párhuzamosan |
| **5. hét** | ✅ Project migrációk | ✅ Project Service | ✅ Project UI | ⏸️ Várakozás | ✅ Project docs | Modul párhuzamosan |
| **6. hét** | ✅ AI Agent migrációk | ✅ AI Agent Service | ✅ AI Chat UI | ⏸️ Várakozás | ✅ AI Agent docs | Modul párhuzamosan |
| **7. hét** | ✅ Backup/Restore | ✅ Testing/Integráció | ✅ Testing | ✅ Docker/Azure | ✅ Deployment docs | Integráció |
| **8. hét** | ✅ Karbantartás | ✅ Finomhangolás | ✅ Finomhangolás | ✅ Deployment | ✅ Karbantartás | Deployment |

## Párhuzamosan Dolgozható Feladatok

### Mindig Párhuzamosan:
- **Documentation Agent** - Bármikor dolgozhat, amikor más agentek implementálnak
- **Database Agent** - Modul migrációk között nincs függőség
- **Backend Agent** - Modul service-ek között nincs függőség (kivéve Auth/User)
- **Frontend Agent** - Modul UI-k között nincs függőség

### SZEKVENCIÁLISAN (Függőségek):
1. **Database Agent (alap migrációk)** → **Backend Agent (core services)**
2. **Backend Agent (API-k)** → **Frontend Agent (UI)**
3. **Backend/Frontend Agent (kód)** → **CI/CD Agent (deployment)**

## Gyors Kezdés Ajánlás

### 1. Nap: Mindenki indulhat
- **Database Agent:** Migráció tool setup, tenants/users táblák
- **Backend Agent:** Projekt struktúra, Quarkus projektek
- **Frontend Agent:** React projekt, TypeScript setup
- **Documentation Agent:** Getting Started dokumentáció
- **CI/CD Agent:** Jenkins alapok (ha van idő)

### 2-3. Nap: Database Agent prioritás
- **Database Agent:** Alapvető migrációk befejezése
- **Backend Agent:** Várakozás vagy konfigurációk
- **Frontend Agent:** Alapvető struktúra, routing
- **Documentation Agent:** Setup dokumentációk

### 4-5. Nap: Backend Agent prioritás
- **Database Agent:** Modul migrációk (párhuzamosan)
- **Backend Agent:** Auth és User Service
- **Frontend Agent:** Várakozás vagy UI mock-olás
- **Documentation Agent:** Backend dokumentáció

### 6-7. Nap: Frontend Agent prioritás
- **Database Agent:** További migrációk
- **Backend Agent:** API Gateway, további service-ek
- **Frontend Agent:** Autentikáció UI, Dashboard
- **Documentation Agent:** Frontend dokumentáció

### 8+ Nap: Modulok párhuzamosan
- **Database Agent:** Modul migrációk (ticket, project, ai_agent)
- **Backend Agent:** Modul service-ek (párhuzamosan)
- **Frontend Agent:** Modul UI-k (párhuzamosan)
- **Documentation Agent:** Modul dokumentációk

## Fontos Megjegyzések

1. **Database Agent:** Alapvető migrációk (tenants, users, tenant_modules) **MINDIG ELŐSZÖR**
2. **Backend Agent:** Auth és User Service **ELŐBB**, mint a többi service
3. **Frontend Agent:** Routing és Auth UI **ELŐBB**, mint a modul UI-k
4. **CI/CD Agent:** Deployment csak akkor, amikor van kód
5. **Documentation Agent:** Bármikor dolgozhat, de legjobb, ha párhuzamosan a fejlesztéssel

## Kommunikáció Szabályok

- **Database Agent:** Jelentse, amikor alap migrációk készen vannak
- **Backend Agent:** Jelentse, amikor új API végpontok készen vannak
- **Frontend Agent:** Kérdezze meg, ha API specifikáció hiányzik
- **CI/CD Agent:** Kérdezze meg, mielőtt deployment pipeline-t készít
- **Documentation Agent:** Frissítse a dokumentációt változtatások esetén

## Összefoglalás

**Kritikus sorrend:**
1. Database Agent (alap migrációk) → Backend Agent (core services)
2. Backend Agent (API-k) → Frontend Agent (UI)
3. Backend/Frontend (kód) → CI/CD Agent (deployment)

**Párhuzamosan dolgozható:**
- Modulok között (ticket, project, ai_agent)
- Documentation Agent bármikor
- Database Agent modul migrációk között

**Ajánlás:** Kezdjétek a Database Agent alap migrációival, aztán Backend Agent core service-ekkel, majd Frontend Agent UI-val. A modulok párhuzamosan dolgozhatók!

