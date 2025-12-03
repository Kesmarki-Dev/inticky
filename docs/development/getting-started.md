# InTicky - Getting Started

## Célközönség

Ez a dokumentum az InTicky projekt gyors kezdését mutatja be. Új fejlesztők számára készült.

## Projekt Áttekintés

Az InTicky egy modern, webes ticketing rendszer, amely:
- Multi-tenant architektúrával működik
- Mikroszolgáltatások alapú
- Moduláris felépítésű
- Azure-ban fut

**Technológiai stack:**
- **Frontend:** React + TypeScript
- **Backend:** Java 17+ + Quarkus 3.x
- **Adatbázis:** PostgreSQL 14+
- **Cache:** Redis
- **Cloud:** Azure
- **AI Agent:** AgentInSec AI Library v3.5.0

## Előfeltételek

### Szükséges Szoftverek

**Backend fejlesztéshez:**
- Java 17 vagy újabb (LTS)
- Maven 3.8+ vagy Gradle 7+
- Docker és Docker Compose
- IDE (IntelliJ IDEA, Eclipse, vagy VS Code)

**Frontend fejlesztéshez:**
- Node.js 18+ (LTS)
- npm vagy yarn
- IDE (VS Code ajánlott)

**Adatbázis fejlesztéshez:**
- PostgreSQL 14+ (vagy Docker)
- Flyway vagy Liquibase CLI (opcionális)

**Általános:**
- Git
- Azure CLI (ha Azure-ba deployolunk)

### Telepítés

**macOS (Homebrew):**
```bash
# Java
brew install openjdk@17

# Node.js
brew install node

# Docker
brew install --cask docker

# Git (általában már telepítve van)
```

**Windows:**
- Java: [Adoptium](https://adoptium.net/)
- Node.js: [Node.js hivatalos oldal](https://nodejs.org/)
- Docker: [Docker Desktop](https://www.docker.com/products/docker-desktop)

**Linux (Ubuntu/Debian):**
```bash
# Java
sudo apt update
sudo apt install openjdk-17-jdk

# Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# Docker
sudo apt install docker.io docker-compose
```

## Monorepo Struktúra

A projekt monorepo struktúrában van, minden komponens egy repository-ban:

```
inticky/
├── frontend/          # React alkalmazás
├── backend/           # Java mikroszolgáltatások
├── db/                # Adatbázis migrációk és seed adatok
├── docs/              # Dokumentáció
├── plan/              # Tervezési dokumentumok
├── .gitignore
├── README.md
└── docker-compose.yml # Lokális fejlesztéshez
```

## Gyors Kezdés

### 1. Repository Klónozása

```bash
git clone <repository-url>
cd inticky
```

### 2. Docker Compose Indítása

**Adatbázis, Redis és Qdrant:**
```bash
docker-compose up -d postgres redis qdrant
```

Ez elindítja:
- PostgreSQL (port 5432)
- Redis (port 6379)
- Qdrant (port 6333, 6334) - AI agent vector store (opcionális)

### 3. Backend Indítása

**Első alkalommal:**
```bash
cd backend
# Maven dependency-k telepítése
mvn clean install
```

**Service indítása (pl. auth-service):**
```bash
cd backend/auth-service
mvn quarkus:dev
```

A service elérhető lesz: `http://localhost:8080`

### 4. Frontend Indítása

**Első alkalommal:**
```bash
cd frontend
npm install
```

**Fejlesztési szerver indítása:**
```bash
npm run dev
```

A frontend elérhető lesz: `http://localhost:5173` (Vite default port)

### 5. Adatbázis Migrációk Futtatása

**Flyway használatával:**
```bash
cd db
mvn flyway:migrate
```

**Vagy Liquibase:**
```bash
cd db
mvn liquibase:update
```

## Fejlesztési Környezet Beállítása

### IDE Beállítások

**IntelliJ IDEA:**
1. Projekt megnyitása: `File > Open > inticky`
2. Maven projekt importálása
3. Java SDK beállítása (Java 17)
4. Code style importálása (ha van)

**VS Code:**
1. Workspace megnyitása: `File > Open Folder > inticky`
2. Extensions telepítése:
   - Java Extension Pack
   - ESLint
   - Prettier
   - Docker

### Environment Változók

**Backend (.env vagy application-dev.yml):**
```yaml
quarkus:
  datasource:
    jdbc:
      url: jdbc:postgresql://localhost:5432/inticky
    username: inticky
    password: inticky
  redis:
    hosts: localhost:6379
```

**Frontend (.env):**
```
VITE_API_URL=http://localhost:8080/api/v1
```

## Következő Lépések

### Backend Fejlesztőknek

1. Olvasd el a [Backend Setup](../development/backend/setup.md) dokumentációt
2. Nézd meg a [Backend Architektúra](../development/backend/architecture.md) dokumentációt
3. Ismerkedj meg a [Coding Standards](../development/backend/coding-standards.md) dokumentációval
4. Olvasd el az [AI Agent Integráció](../development/backend/ai-agent-integration.md) dokumentációt

### Frontend Fejlesztőknek

1. Olvasd el a [Frontend Setup](../development/frontend/setup.md) dokumentációt
2. Nézd meg a [Frontend Architektúra](../development/frontend/architecture.md) dokumentációt
3. Ismerkedj meg a [Coding Standards](../development/frontend/coding-standards.md) dokumentációval

### Adatbázis Fejlesztőknek

1. Olvasd el a [Database Setup](../development/database/setup.md) dokumentációt
2. Nézd meg a [Migrációk](../development/database/migrations.md) dokumentációt
3. Ismerkedj meg a [Seed Data](../development/database/seed-data.md) dokumentációval

## Gyakori Problémák

### Port már használatban

**Hiba:** `Port 5432 is already in use`

**Megoldás:**
```bash
# PostgreSQL port változtatása docker-compose.yml-ben
# Vagy leállítani a meglévő PostgreSQL-t
```

### Java verzió hiba

**Hiba:** `Unsupported class file major version`

**Megoldás:**
- Ellenőrizd a Java verziót: `java -version`
- Telepítsd a Java 17-et vagy újabbat

### Node modules hiba

**Hiba:** `Cannot find module`

**Megoldás:**
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

## Cursor Workspace Setup

Ha különböző területeken szeretnél külön Cursor agent-ekkel dolgozni (backend, database, frontend, docs, CI/CD), lásd:

- [Cursor Workspace Setup Útmutató](./cursor-workspace-setup.md)

## További Információk

- [Rendszer áttekintés](../architecture/system-overview.md)
- [Mikroszolgáltatások](../architecture/microservices.md)
- [Monorepo struktúra](../../plan/07_monorepo_struktura.md)
- [Kezdési útmutató](../../plan/09_kezdesi_utmutato.md)

## Segítség

Ha problémáid vannak:
1. Nézd meg a troubleshooting részt a specifikus dokumentációkban
2. Kérdezz a csapatban
3. Hozz létre egy issue-t a repository-ban

