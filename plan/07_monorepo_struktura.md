# InTicky - Monorepo Struktúra

## Áttekintés

Az InTicky projekt monorepo struktúrában lesz kialakítva, ahol minden komponens egy repository-ban található.

## Repository Struktúra

```
inticky/
├── frontend/          # React alkalmazás
├── backend/           # Java mikroszolgáltatások
├── db/                # Adatbázis migrációk és seed adatok
├── docs/              # Dokumentáció
├── .gitignore
├── README.md
└── docker-compose.yml  # Lokális fejlesztéshez
```

## Mappa Részletek

### frontend/

**Tartalom:**
- React alkalmazás
- TypeScript konfiguráció
- Build konfiguráció (Vite vagy CRA)
- UI komponensek
- State management
- API integráció

**Strukturálás (tervezett):**
```
frontend/
├── src/
│   ├── components/     # React komponensek
│   ├── pages/         # Oldalak
│   ├── services/      # API hívások
│   ├── store/         # State management
│   ├── utils/         # Segédfunkciók
│   └── types/         # TypeScript típusok
├── public/
├── package.json
├── tsconfig.json
└── vite.config.ts (vagy cra config)
```

### backend/

**Tartalom:**
- Java mikroszolgáltatások
- Maven/Gradle projektek
- API implementációk
- Business logika

**Strukturálás (tervezett - Opció 1: Moduláris Mikroszolgáltatások):**
```
backend/
├── api-gateway/       # API Gateway szolgáltatás
├── auth-service/      # Auth modul - Autentikáció szolgáltatás
├── ticket-service/    # Ticket modul - Support ticketek szolgáltatás
├── project-service/   # Projekt modul - Fejlesztési projektek szolgáltatás
├── user-service/      # User modul - Felhasználó kezelés szolgáltatás
├── notification-service/  # Notification modul - Értesítések szolgáltatás
├── file-service/      # File modul - Fájl kezelés szolgáltatás
├── report-service/    # Report modul - Jelentések szolgáltatás (opcionális)
├── ai-agent-service/  # AI Agent modul - AI chat és agent funkcionalitás (AgentInSec)
├── shared/            # Közös kód (DTO-k, utilities, modul registry)
└── pom.xml (vagy settings.gradle)  # Parent POM vagy Gradle settings
```

**Megjegyzés:** Opció 1 alapján minden modul külön mikroszolgáltatás. Modul aktiválás per tenant (tenant_modules tábla).

**Minden szolgáltatás struktúrája:**
```
service-name/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/inticky/service/
│   │   │       ├── controller/    # REST controllers
│   │   │       ├── service/       # Business logika
│   │   │       ├── repository/    # Data access
│   │   │       ├── model/        # Entity-k, DTO-k
│   │   │       └── config/       # Konfiguráció
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/
├── Dockerfile
└── pom.xml (vagy build.gradle)
```

### db/

**Tartalom:**
- Adatbázis migrációk (Flyway, Liquibase)
- Seed adatok
- Adatbázis scriptek

**Strukturálás (tervezett):**
```
db/
├── migrations/        # Migrációs scriptek
│   ├── V1__initial_schema.sql
│   ├── V2__add_projects.sql
│   └── ...
├── seeds/            # Seed adatok
│   ├── dev/
│   └── test/
├── scripts/          # Utility scriptek
└── README.md
```

### docs/

**Tartalom:**
- API dokumentáció
- Architektúra dokumentáció
- Fejlesztői útmutatók
- Deployment útmutatók

**Strukturálás (tervezett):**
```
docs/
├── api/              # API dokumentáció (OpenAPI/Swagger)
├── architecture/    # Architektúra diagramok
├── development/      # Fejlesztői útmutatók
├── deployment/       # Deployment útmutatók
└── adr/             # Architecture Decision Records
```

## Docker Compose (Lokális Fejlesztés)

**docker-compose.yml** a root-ban:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: inticky
      POSTGRES_USER: inticky
      POSTGRES_PASSWORD: inticky
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  qdrant:
    image: qdrant/qdrant:latest
    ports:
      - "6333:6333"  # REST API
      - "6334:6334"  # gRPC
    volumes:
      - qdrant_data:/qdrant/storage
    environment:
      - QDRANT__SERVICE__GRPC_PORT=6334

  # Backend services (ha szükséges lokálisan)
  # auth-service:
  #   build: ./backend/auth-service
  #   ...

volumes:
  postgres_data:
  redis_data:
  qdrant_data:
```

## Build és Deployment

### Lokális Fejlesztés

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

**Backend:**
```bash
cd backend/service-name
mvn quarkus:dev
```

**Adatbázis:**
```bash
docker-compose up -d postgres redis
```

### CI/CD (Jenkins)

**Jenkinsfile** a root-ban vagy service-ekenként:

```groovy
pipeline {
    agent any
    
    stages {
        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }
        
        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean package'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                // Docker image-ek build-elése
            }
        }
        
        stage('Deploy') {
            steps {
                // Azure-ba deployment
            }
        }
    }
}
```

## Git Workflow

**Branching:**
- `main` - Production kód
- `develop` - Development kód
- `feature/*` - Új funkciók
- `fix/*` - Bug javítások
- `release/*` - Release előkészítés

**Commit konvenció:**
```
type(scope): subject

feat(frontend): add ticket list component
fix(backend): resolve authentication issue
docs: update API documentation
```

## Közös Kód Kezelés

### Backend Shared Modul

**backend/shared/** mappa:
- Közös DTO-k
- Közös utilities
- Közös konfigurációk
- Közös exception handling

**Használat:**
- Maven/Gradle dependency-ként
- Minden service importálja

### Frontend Shared

**frontend/src/shared/** mappa:
- Közös komponensek
- Közös utilities
- Közös típusok
- Közös API client

## Előnyök

✅ **Egyszerűbb koordináció** - Minden kód egy helyen
✅ **Egyszerűbb refaktorálás** - Kereszt-módosítások könnyebbek
✅ **Közös verziózás** - Minden komponens ugyanazt a verziót használja
✅ **Egyszerűbb CI/CD** - Egy pipeline mindent kezel
✅ **Jobb áttekinthetőség** - Minden kód egy repository-ban

## Hátrányok és Megoldások

❌ **Nagy repository** - Megoldás: Git LFS nagy fájlokhoz
❌ **Lassabb clone** - Megoldás: Shallow clone, sparse checkout
❌ **Komplex build** - Megoldás: Incremental build, caching

## Következő Lépések

1. **Repository létrehozása** - GitHub/GitLab
2. **Mappák létrehozása** - frontend/, backend/, db/, docs/
3. **Docker Compose setup** - Lokális fejlesztéshez
4. **Jenkins pipeline** - CI/CD konfiguráció
5. **Shared modulok** - Közös kód szervezése

