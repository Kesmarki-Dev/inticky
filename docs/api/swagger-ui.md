# InTicky - Swagger UI Használata

## Célközönség

Ez a dokumentum a Swagger UI használatát mutatja be az API dokumentáció megtekintéséhez és teszteléséhez. Fejlesztők számára készült.

## Swagger UI Elérése

### Lokális Fejlesztés

**URL:**
```
http://localhost:8080/q/swagger-ui
```

**Vagy:**
```
http://localhost:8080/swagger-ui
```

### Staging

**URL:**
```
https://api-staging.inticky.com/swagger-ui
```

### Production

**URL:**
```
https://api.inticky.com/swagger-ui
```

## Quarkus OpenAPI Integráció

### Extension Telepítése

**pom.xml:**
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-openapi</artifactId>
</dependency>
```

### Konfiguráció

**application.yml:**
```yaml
quarkus:
  swagger-ui:
    always-include: true
    path: /swagger-ui
  smallrye-openapi:
    path: /openapi
```

## Swagger UI Funkciók

### API Végpontok Böngészése

**Funkciók:**
- Végpontok listázása
- Végpont részletek
- Request/Response példák
- Schema definíciók

### API Tesztelés

**Try it out:**
1. Válassz egy végpontot
2. Kattints a "Try it out" gombra
3. Töltsd ki a paramétereket
4. Kattints az "Execute" gombra
5. Nézd meg a választ

### Autentikáció

**Bearer Token beállítása:**
1. Kattints a "Authorize" gombra (felső jobb sarok)
2. Add meg a JWT token-t
3. Kattints az "Authorize" gombra
4. Most már minden request tartalmazza a token-t

**Példa token:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## OpenAPI Spec Elérése

### OpenAPI JSON

**URL:**
```
http://localhost:8080/openapi
```

**Vagy:**
```
http://localhost:8080/q/openapi
```

### OpenAPI YAML

**URL:**
```
http://localhost:8080/openapi.yaml
```

## API Dokumentáció Generálás

### Automatikus Generálás

Quarkus automatikusan generálja az OpenAPI spec-et a kódból:
- REST endpoint-ok
- Request/Response típusok
- Validáció szabályok
- Dokumentáció kommentek

### Dokumentáció Kommentek

**JavaDoc használata:**
```java
/**
 * Létrehoz egy új ticketet.
 * 
 * @param dto A ticket létrehozási adatok
 * @return A létrehozott ticket
 */
@POST
@Path("/tickets")
@Operation(summary = "Új ticket létrehozása", description = "Létrehoz egy új support ticketet")
@APIResponse(responseCode = "201", description = "Ticket létrehozva")
@APIResponse(responseCode = "400", description = "Hibás adatok")
public TicketDTO createTicket(CreateTicketDTO dto) {
    // ...
}
```

## További Információk

- [API Design](../architecture/api-design.md)
- [OpenAPI Spec](./openapi.yaml)
- [Quarkus OpenAPI dokumentáció](https://quarkus.io/guides/openapi-swaggerui)

