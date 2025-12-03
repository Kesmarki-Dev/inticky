# InTicky - Database Setup

## Célközönség

Ez a dokumentum az adatbázis fejlesztési környezet beállítását mutatja be. Adatbázis fejlesztők és backend fejlesztők számára készült.

## Előfeltételek

- Docker és Docker Compose (ajánlott)
- Vagy PostgreSQL 14+ lokális telepítés
- psql CLI (opcionális)

## PostgreSQL Lokális Telepítés

### Docker Használata (Ajánlott)

**Docker Compose:**
```yaml
# docker-compose.yml (root)
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
      - ./db/init:/docker-entrypoint-initdb.d

volumes:
  postgres_data:
```

**Indítás:**
```bash
docker-compose up -d postgres
```

**Leállítás:**
```bash
docker-compose down
```

### Lokális Telepítés

**macOS (Homebrew):**
```bash
brew install postgresql@14
brew services start postgresql@14
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install postgresql-14
sudo systemctl start postgresql
```

**Windows:**
- [PostgreSQL hivatalos installer](https://www.postgresql.org/download/windows/)

## Adatbázis Létrehozása

### Manuális Létrehozás

**psql használatával:**
```bash
psql -U postgres

CREATE DATABASE inticky;
CREATE USER inticky WITH PASSWORD 'inticky';
GRANT ALL PRIVILEGES ON DATABASE inticky TO inticky;
\q
```

**Vagy SQL script:**
```sql
-- db/init/01_create_database.sql
CREATE DATABASE inticky;
CREATE USER inticky WITH PASSWORD 'inticky';
GRANT ALL PRIVILEGES ON DATABASE inticky TO inticky;
```

### Docker Automatikus Létrehozás

A `docker-compose.yml` automatikusan létrehozza az adatbázist a `POSTGRES_DB` environment változó alapján.

## Connection String Konfiguráció

### Backend Konfiguráció

**application.yml:**
```yaml
quarkus:
  datasource:
    db-kind: postgresql
    jdbc:
      url: jdbc:postgresql://localhost:5432/inticky
    username: inticky
    password: inticky
```

**Environment változók:**
```bash
export DB_URL=jdbc:postgresql://localhost:5432/inticky
export DB_USERNAME=inticky
export DB_PASSWORD=inticky
```

### Connection String Formátum

```
jdbc:postgresql://[host]:[port]/[database]?[parameters]
```

**Példák:**
```
# Lokális
jdbc:postgresql://localhost:5432/inticky

# Azure
jdbc:postgresql://inticky-db.postgres.database.azure.com:5432/inticky?sslmode=require

# Custom port
jdbc:postgresql://localhost:5433/inticky
```

## Adatbázis Kapcsolat Tesztelése

### psql Használata

```bash
psql -h localhost -U inticky -d inticky
```

**Kapcsolat ellenőrzése:**
```sql
SELECT version();
\conninfo
```

### Backend-ből Tesztelés

**Quarkus Dev Services:**
- Automatikus adatbázis kapcsolat tesztelés
- Health check: `http://localhost:8080/q/health`

## Row Level Security (RLS) Beállítás

### RLS Engedélyezése

```sql
-- Minden táblához
ALTER TABLE tickets ENABLE ROW LEVEL SECURITY;
ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
-- ... stb.
```

### RLS Policy Létrehozása

```sql
-- Példa: tickets táblához
CREATE POLICY tenant_isolation_policy ON tickets
    FOR ALL
    USING (tenant_id = current_setting('app.current_tenant_id')::UUID);
```

## Qdrant Vector Store Setup (AI Agent)

**Megjegyzés:** Qdrant opcionális, de ajánlott production-hez az AI agent perzisztens memóriájához.

### Docker Használata (Ajánlott)

**Docker Compose:**
```yaml
qdrant:
  image: qdrant/qdrant:latest
  ports:
    - "6333:6333"  # REST API
    - "6334:6334"  # gRPC
  volumes:
    - qdrant_data:/qdrant/storage
  environment:
    - QDRANT__SERVICE__GRPC_PORT=6334
```

**Indítás:**
```bash
docker-compose up -d qdrant
```

**Ellenőrzés:**
```bash
curl http://localhost:6333/health
```

### Qdrant Konfiguráció

**Backend konfiguráció:**
```yaml
agentinsec:
  ai:
    qdrant:
      enabled: true
      url: http://localhost:6333
      collection-name: inticky_vectors
```

**Environment változók:**
```bash
export QDRANT_URL=http://localhost:6333
export QDRANT_COLLECTION=inticky_vectors
export QDRANT_ENABLED=true
```

## További Információk

- [Migrációk](./migrations.md)
- [Seed Data](./seed-data.md)
- [Backup/Restore](./backup-restore.md)
- [AI Agent Integráció](../backend/ai-agent-integration.md)
- [Adatbázis tervezés](../../architecture/database-design.md)
- [Részletes adatbázis tervezés](../../../plan/03_adatbazis_tervezes.md)

## Troubleshooting

### Connection Refused

**Hiba:** `Connection refused`

**Megoldás:**
1. Ellenőrizd, hogy fut-e a PostgreSQL: `docker ps` vagy `ps aux | grep postgres`
2. Ellenőrizd a port-ot: `netstat -an | grep 5432`
3. Indítsd újra a PostgreSQL-t

### Authentication Failed

**Hiba:** `password authentication failed`

**Megoldás:**
1. Ellenőrizd a jelszót: `application.yml` vagy environment változók
2. Ellenőrizd a felhasználónevet
3. PostgreSQL pg_hba.conf beállítások

### Database Does Not Exist

**Hiba:** `database "inticky" does not exist`

**Megoldás:**
```bash
# Adatbázis létrehozása
psql -U postgres -c "CREATE DATABASE inticky;"
```

