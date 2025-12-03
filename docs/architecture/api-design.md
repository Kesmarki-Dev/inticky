# InTicky - API Design

## Célközönség

Ez a dokumentum az InTicky REST API tervezési elveit és konvencióit mutatja be. Backend fejlesztők és frontend fejlesztők számára készült.

## API Elvek

### RESTful API

**Választott:** RESTful API elvek

**Alapelvek:**
- Resource-alapú URL-ek
- HTTP metódusok használata (GET, POST, PUT, DELETE, PATCH)
- Konzisztens válasz formátum (JSON)
- Stateless kommunikáció
- Cacheable responses (ahol lehetséges)

## API Struktúra

### Verziózás

**Formátum:** `/api/v1/`

**Példa:**
```
GET /api/v1/tickets
POST /api/v1/tickets
GET /api/v1/tickets/:id
```

**Verzióváltás:**
- Új verzió: `/api/v2/`
- Régi verzió továbbra is elérhető
- Deprecation policy

### Resource Naming

**Konvenciók:**
- Kicsi betűk
- Többszörös szó esetén kötőjel (`-`) vagy camelCase
- Főnév használata (nem ige)
- Többes szám használata listákhoz

**Példák:**
```
✅ GET /api/v1/tickets
✅ GET /api/v1/projects
✅ GET /api/v1/projects/:id/tasks
❌ GET /api/v1/getTickets
❌ GET /api/v1/ticket
```

### HTTP Metódusok

**Használat:**
- `GET` - Adatok lekérdezése
- `POST` - Új erőforrás létrehozása
- `PUT` - Teljes erőforrás frissítése
- `PATCH` - Részleges erőforrás frissítése
- `DELETE` - Erőforrás törlése

**Példák:**
```
GET    /api/v1/tickets          # Lista
POST   /api/v1/tickets          # Létrehozás
GET    /api/v1/tickets/:id      # Részletek
PUT    /api/v1/tickets/:id      # Teljes frissítés
PATCH  /api/v1/tickets/:id      # Részleges frissítés
DELETE /api/v1/tickets/:id      # Törlés
```

## Request/Response Formátum

### Request Headers

**Kötelező:**
```
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
X-Tenant-ID: <tenant_id> (ha header alapú tenant azonosítás)
```

**Opcionális:**
```
Accept: application/json
X-Request-ID: <unique_request_id>
```

### Response Formátum

**Sikeres válasz:**
```json
{
  "data": { ... },
  "meta": {
    "pagination": { ... }
  }
}
```

**Hibás válasz:**
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Error message",
    "details": { ... }
  }
}
```

### HTTP Status Codes

**Sikeres válaszok:**
- `200 OK` - Sikeres GET, PUT, PATCH
- `201 Created` - Sikeres POST (új erőforrás)
- `204 No Content` - Sikeres DELETE

**Ügyfél hibák:**
- `400 Bad Request` - Hibás request
- `401 Unauthorized` - Nincs autentikáció
- `403 Forbidden` - Nincs jogosultság / Modul nem aktív
- `404 Not Found` - Erőforrás nem található
- `409 Conflict` - Konfliktus (pl. duplikáció)

**Szerver hibák:**
- `500 Internal Server Error` - Szerver hiba
- `503 Service Unavailable` - Szolgáltatás nem elérhető

## Pagináció

**Query paraméterek:**
```
GET /api/v1/tickets?page=1&limit=20
```

**Response:**
```json
{
  "data": [ ... ],
  "meta": {
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 150,
      "totalPages": 8
    }
  }
}
```

## Filtering, Sorting, Searching

### Filtering

**Query paraméterek:**
```
GET /api/v1/tickets?status=open&priority=high
GET /api/v1/tickets?assigned_to_id=123
```

### Sorting

**Query paraméterek:**
```
GET /api/v1/tickets?sort=created_at&order=desc
GET /api/v1/tickets?sort=priority,created_at&order=asc,desc
```

### Searching

**Query paraméterek:**
```
GET /api/v1/tickets?search=login+issue
GET /api/v1/tickets?q=bug
```

## API Gateway

### Azure API Management

**Funkciók:**
- Request routing
- Authentication/Authorization
- Rate limiting
- Request/Response transformation
- Monitoring és logging

### Routing

**Szabályok:**
- `/api/v1/auth/*` → Auth Service
- `/api/v1/users/*` → User Service
- `/api/v1/tickets/*` → Ticket Service
- `/api/v1/projects/*` → Project Service
- `/api/v1/notifications/*` → Notification Service
- `/api/v1/files/*` → File Service

### Modul Aktiválás Ellenőrzés

**Flow:**
1. Request érkezik API Gateway-hez
2. Tenant azonosítás
3. Modul aktiválás ellenőrzés (`tenant_modules` tábla)
4. Ha modul nem aktív → 403 Forbidden
5. Ha aktív → Request továbbítása a service-hez

## Példa Végpontok

### Auth Service

```
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
POST   /api/v1/auth/refresh
POST   /api/v1/auth/reset-password
GET    /api/v1/auth/me
```

### Ticket Service

```
GET    /api/v1/tickets
POST   /api/v1/tickets
GET    /api/v1/tickets/:id
PUT    /api/v1/tickets/:id
PATCH  /api/v1/tickets/:id
DELETE /api/v1/tickets/:id
GET    /api/v1/tickets/:id/comments
POST   /api/v1/tickets/:id/comments
GET    /api/v1/tickets/:id/attachments
POST   /api/v1/tickets/:id/attachments
```

### Project Service

```
GET    /api/v1/projects
POST   /api/v1/projects
GET    /api/v1/projects/:id
PUT    /api/v1/projects/:id
GET    /api/v1/projects/:id/tasks
POST   /api/v1/projects/:id/tasks
GET    /api/v1/projects/:id/tasks/:taskId
PUT    /api/v1/projects/:id/tasks/:taskId
GET    /api/v1/projects/:id/time-entries
POST   /api/v1/projects/:id/time-entries
```

### User Service

```
GET    /api/v1/users
POST   /api/v1/users
GET    /api/v1/users/:id
PUT    /api/v1/users/:id
DELETE /api/v1/users/:id
```

### AI Agent Service

```
POST   /api/v1/ai/chat
POST   /api/v1/ai/chat/:sessionId
GET    /api/v1/ai/sessions
GET    /api/v1/ai/sessions/:sessionId
GET    /api/v1/ai/sessions/:sessionId/messages
DELETE /api/v1/ai/sessions/:sessionId
POST   /api/v1/ai/tools/register
GET    /api/v1/ai/tools
POST   /api/v1/ai/info-blocks
GET    /api/v1/ai/info-blocks
DELETE /api/v1/ai/info-blocks/:id
POST   /api/v1/ai/execution-plans/:id/confirm
GET    /api/v1/ai/memory/episodes
GET    /api/v1/ai/memory/knowledge
```

## Rate Limiting

**Szabályok:**
- Per tenant rate limiting
- Per user rate limiting
- Per endpoint rate limiting

**Példa:**
- 1000 requests / minute per tenant
- 100 requests / minute per user
- 10 requests / minute per endpoint (POST, PUT, DELETE)

## API Dokumentáció

### OpenAPI/Swagger

**Specifikáció:** OpenAPI 3.0

**Elérés:**
- Swagger UI: `https://api.inticky.com/swagger-ui`
- OpenAPI spec: `https://api.inticky.com/openapi.yaml`

**Generálás:**
- Quarkus OpenAPI extension
- Automatikus generálás a kódból

## Hiba Kezelés

### Hiba Formátum

```json
{
  "error": {
    "code": "TICKET_NOT_FOUND",
    "message": "Ticket not found",
    "details": {
      "ticketId": "123e4567-e89b-12d3-a456-426614174000"
    },
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

### Hiba Kódok

**Kategóriák:**
- `VALIDATION_ERROR` - Validációs hiba
- `NOT_FOUND` - Erőforrás nem található
- `UNAUTHORIZED` - Nincs autentikáció
- `FORBIDDEN` - Nincs jogosultság
- `MODULE_NOT_ACTIVE` - Modul nem aktív
- `CONFLICT` - Konfliktus
- `INTERNAL_ERROR` - Szerver hiba

## További Információk

- [Rendszer áttekintés](./system-overview.md)
- [Mikroszolgáltatások](./microservices.md)
- [OpenAPI spec](../api/openapi.yaml)
- [Swagger UI](../api/swagger-ui.md)
- [Backend architektúra](../development/backend/architecture.md)

