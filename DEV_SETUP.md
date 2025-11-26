# 🚀 Inticky Development Environment Setup

Ez a dokumentum leírja, hogyan indítsd el a teljes Inticky ticketing rendszert fejlesztői környezetben.

## 📋 Előfeltételek

### Szükséges szoftverek
- **Docker** (v20.10+)
- **Docker Compose** (v2.0+)
- **Java 17+** (ha lokálisan futtatod a service-eket)
- **Gradle 8.4+** (ha lokálisan buildelni szeretnél)

### Opcionális
- **Git** (verziókezeléshez)
- **curl** vagy **httpie** (API teszteléshez)
- **Postman** vagy **Insomnia** (API fejlesztéshez)

## 🔧 Gyors Indítás

### 1. Környezeti változók beállítása

```bash
# Másold le a példa konfigurációt
cp env.example .env

# Szerkeszd a .env fájlt a saját beállításaiddal
nano .env
```

**Fontos környezeti változók:**
- `OPENAI_API_KEY` - AgentInSec-AI működéshez (opcionális dev-ben)
- `MAIL_USERNAME` és `MAIL_PASSWORD` - Email értesítésekhez
- `JWT_SECRET` - JWT token titkosításhoz

### 2. Infrastruktúra indítása

```bash
# Csak az infrastruktúra szolgáltatások (PostgreSQL, Redis, Kafka, Monitoring)
./dev-start.sh --infrastructure-only
```

### 3. Alkalmazás szolgáltatások indítása

```bash
# Alkalmazás service-ek indítása
./dev-start.sh --services-only
```

### 4. Teljes stack indítása (egy lépésben)

```bash
# Minden egyszerre
./dev-start.sh --full-stack
```

## 🌐 Elérhető Szolgáltatások

### Infrastruktúra
| Szolgáltatás | URL | Leírás |
|-------------|-----|--------|
| PostgreSQL | `localhost:5432` | Adatbázis (user: inticky, pass: inticky123) |
| Redis | `localhost:6379` | Cache és session store |
| Kafka | `localhost:9092` | Event streaming |
| Kafka UI | http://localhost:8090 | Kafka management interface |
| Jaeger | http://localhost:16686 | Distributed tracing |
| Prometheus | http://localhost:9090 | Metrics collection |
| Grafana | http://localhost:3000 | Monitoring dashboards (admin/admin123) |

### Alkalmazás Service-ek
| Service | URL | Swagger UI |
|---------|-----|------------|
| API Gateway | http://localhost:8080 | http://localhost:8080/swagger-ui.html |
| Tenant Service | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
| User Service | http://localhost:8082 | http://localhost:8082/swagger-ui.html |
| Ticket Service | http://localhost:8083 | http://localhost:8083/swagger-ui.html |
| AI Service | http://localhost:8084 | http://localhost:8084/swagger-ui.html |
| Notification Service | http://localhost:8085 | http://localhost:8085/swagger-ui.html |

## 🔍 Fejlesztői Eszközök

### Logok megtekintése
```bash
# Összes service log
docker-compose logs -f

# Konkrét service log
docker-compose logs -f api-gateway
docker-compose logs -f ticket-service
```

### Service újraindítása
```bash
# Egy service újraindítása
docker-compose restart ticket-service

# Több service újraindítása
docker-compose restart api-gateway user-service
```

### Állapot ellenőrzése
```bash
# Összes container állapota
docker-compose ps

# Service health check
curl http://localhost:8080/actuator/health
curl http://localhost:8083/actuator/health
```

## 🧪 API Tesztelés

### 1. Tenant létrehozása
```bash
curl -X POST http://localhost:8081/api/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Company",
    "domain": "test.com",
    "plan": "PREMIUM"
  }'
```

### 2. Felhasználó regisztráció
```bash
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: your-tenant-id" \
  -d '{
    "email": "admin@test.com",
    "password": "password123",
    "firstName": "Admin",
    "lastName": "User"
  }'
```

### 3. Bejelentkezés
```bash
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: your-tenant-id" \
  -d '{
    "email": "admin@test.com",
    "password": "password123"
  }'
```

### 4. Ticket létrehozása
```bash
curl -X POST http://localhost:8083/api/tickets \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-jwt-token" \
  -H "X-Tenant-ID: your-tenant-id" \
  -d '{
    "title": "Test ticket",
    "description": "This is a test ticket",
    "category": "TECHNICAL",
    "priority": "MEDIUM"
  }'
```

### 5. AI Chat tesztelése
```bash
curl -X POST http://localhost:8084/api/ai/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-jwt-token" \
  -H "X-Tenant-ID: your-tenant-id" \
  -d '{
    "message": "Hello, can you help me with my ticket?",
    "createSession": true
  }'
```

## 🛠️ Fejlesztői Workflow

### 1. Kód módosítás után
```bash
# Service újrabuildelése és indítása
docker-compose build ticket-service
docker-compose up -d ticket-service

# Vagy Gradle-lel lokálisan
./gradlew :services:ticket-service:bootRun
```

### 2. Adatbázis séma változás
```bash
# Flyway migration futtatása
docker-compose exec ticket-service java -jar app.jar --spring.flyway.migrate

# Vagy adatbázis reset
docker-compose down -v
docker-compose up -d postgres
```

### 3. Kafka topic kezelés
```bash
# Kafka UI-n keresztül: http://localhost:8090
# Vagy CLI-vel:
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

## 🔧 Hibaelhárítás

### Service nem indul el
```bash
# Részletes logok
docker-compose logs service-name

# Container állapot ellenőrzése
docker-compose ps

# Health check
curl http://localhost:port/actuator/health
```

### Port ütközés
```bash
# Futó processek ellenőrzése
lsof -i :8080
netstat -tulpn | grep :8080

# Port felszabadítása
kill -9 PID
```

### Adatbázis kapcsolat hiba
```bash
# PostgreSQL elérhetőség
docker-compose exec postgres pg_isready -U inticky

# Adatbázis kapcsolat tesztelése
docker-compose exec postgres psql -U inticky -d inticky -c "SELECT 1;"
```

### Memory/Performance problémák
```bash
# Container resource használat
docker stats

# JVM heap beállítása
export JAVA_OPTS="-Xmx2g -Xms1g"
```

## 🛑 Leállítás

### Graceful shutdown
```bash
# Service-ek leállítása
docker-compose stop

# Minden leállítása és cleanup
docker-compose down

# Volumes törlése is (adatvesztés!)
docker-compose down -v
```

### Teljes cleanup
```bash
# Minden container, network, volume törlése
docker-compose down -v --remove-orphans
docker system prune -a
```

## 📚 További Dokumentáció

- [API Documentation](http://localhost:8080/swagger-ui.html) - Swagger UI
- [AgentInSec-AI Guide](AGENTINSEC.md) - AI integráció
- [Architecture Overview](TICKETING_SYSTEM_REQUIREMENTS.md) - Rendszer áttekintés
- [Cursor Rules](.cursorrules) - Fejlesztői szabályok

## 🆘 Segítség

Ha problémába ütközöl:

1. Ellenőrizd a logokat: `docker-compose logs -f`
2. Nézd meg a service health-et: `curl http://localhost:port/actuator/health`
3. Ellenőrizd a környezeti változókat: `cat .env`
4. Restart-eld a problémás service-t: `docker-compose restart service-name`

---

**Happy coding! 🚀**
