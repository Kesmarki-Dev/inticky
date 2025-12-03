# InTicky - Backend Coding Standards

## Célközönség

Ez a dokumentum a backend kódolási szabályokat és best practice-eket mutatja be. Backend fejlesztők számára készült.

## Java Coding Conventions

### Naming Conventions

**Osztályok:**
- PascalCase
- Főnév vagy főnév + melléknév
- Controller: `TicketController`
- Service: `TicketService`
- Repository: `TicketRepository`
- Entity: `Ticket`
- DTO: `TicketDTO`, `CreateTicketDTO`

**Metódusok:**
- camelCase
- Ige + főnév
- `getTicket()`, `createTicket()`, `updateTicket()`

**Változók:**
- camelCase
- `ticketId`, `userName`, `isActive`

**Konstansok:**
- UPPER_SNAKE_CASE
- `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT`

### Package Naming

```
com.inticky.service.ticket
├── controller
├── service
├── repository
├── model
│   ├── entity
│   └── dto
├── config
└── exception
```

## Quarkus Best Practices

### Dependency Injection

**CDI használata:**
```java
// ✅ Jó
@ApplicationScoped
public class TicketService {
    // ...
}

// ❌ Rossz
public class TicketService {
    // ...
}
```

**Field injection (egyszerű esetek):**
```java
@Inject
TicketRepository ticketRepository;
```

**Constructor injection (komplex esetek):**
```java
@ApplicationScoped
public class TicketService {
    
    private final TicketRepository ticketRepository;
    
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }
}
```

### REST Controllers

**JAX-RS használata:**
```java
@Path("/api/v1/tickets")
@ApplicationScoped
public class TicketController {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TicketDTO> getTickets() {
        // ...
    }
}
```

**Response típusok:**
```java
// ✅ Jó - DTO használata
@GET
public List<TicketDTO> getTickets() {
    return ticketService.getAllTickets();
}

// ❌ Rossz - Entity közvetlen visszaadása
@GET
public List<Ticket> getTickets() {
    return ticketRepository.findAll();
}
```

### Panache Repository Pattern

**Repository implementáció:**
```java
@ApplicationScoped
public class TicketRepository implements PanacheRepository<Ticket> {
    
    public List<Ticket> findByTenantId(UUID tenantId) {
        return find("tenant_id", tenantId).list();
    }
}
```

**Custom query-k:**
```java
public List<Ticket> findByStatusAndPriority(String status, String priority) {
    return find("status = ?1 AND priority = ?2", status, priority).list();
}
```

## Code Style

### Formázás

**Indentáció:**
- 4 spaces (nem tab)
- Konzisztens formázás

**Sor hossz:**
- Maximum 120 karakter
- Hosszabb sorok tördelése

**Példa:**
```java
// ✅ Jó
public List<TicketDTO> getTickets(
    UUID tenantId, 
    String status, 
    String priority
) {
    // ...
}

// ❌ Rossz
public List<TicketDTO> getTickets(UUID tenantId, String status, String priority) {
    // ...
}
```

### Kommentelés

**JavaDoc használata:**
```java
/**
 * Létrehoz egy új ticketet a megadott adatokkal.
 * 
 * @param dto A ticket létrehozási adatok
 * @return A létrehozott ticket DTO
 * @throws ValidationException Ha az adatok érvénytelenek
 */
public TicketDTO createTicket(CreateTicketDTO dto) {
    // ...
}
```

**Inline kommentek:**
```java
// Csak akkor használj, ha a kód nem önmagát dokumentálja
// Komplex business logika magyarázata
```

### Exception Handling

**Specifikus exception-ök:**
```java
// ✅ Jó
throw new TicketNotFoundException(ticketId);

// ❌ Rossz
throw new RuntimeException("Ticket not found");
```

**Exception mapper használata:**
```java
@Provider
public class TicketNotFoundExceptionMapper 
    implements ExceptionMapper<TicketNotFoundException> {
    
    @Override
    public Response toResponse(TicketNotFoundException exception) {
        // Error response
    }
}
```

## Tenant Izoláció

### Mindig Tenant ID Szűrés

```java
// ✅ Jó
public List<Ticket> getTickets() {
    UUID tenantId = tenantContext.getTenantId();
    return ticketRepository.find("tenant_id", tenantId).list();
}

// ❌ Rossz - Tenant ID hiányzik
public List<Ticket> getTickets() {
    return ticketRepository.findAll(); // VESZÉLYES!
}
```

### Tenant ID Validáció

```java
public Ticket createTicket(CreateTicketDTO dto) {
    UUID tenantId = tenantContext.getTenantId();
    
    // Tenant ID beállítása
    Ticket ticket = new Ticket();
    ticket.setTenantId(tenantId);
    
    // Validáció: DTO-ban ne legyen tenant_id
    if (dto.getTenantId() != null) {
        throw new ValidationException("Tenant ID cannot be set manually");
    }
    
    return ticket;
}
```

## Logging

### Structured Logging

```java
@ApplicationScoped
public class TicketService {
    
    private static final Logger LOG = Logger.getLogger(TicketService.class);
    
    public TicketDTO createTicket(CreateTicketDTO dto) {
        LOG.infof("Creating ticket for tenant: %s, title: %s", 
            tenantContext.getTenantId(), dto.getTitle());
        
        try {
            // ...
            LOG.infof("Ticket created successfully: %s", ticket.getId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to create ticket: %s", e.getMessage());
            throw e;
        }
    }
}
```

### Log Szintek

- `DEBUG` - Részletes fejlesztési információk
- `INFO` - Általános információk (request-ek, műveletek)
- `WARN` - Figyelmeztetések (nem kritikus hibák)
- `ERROR` - Hibák (exception-ök)

## Testing

### Unit Testing

```java
@QuarkusTest
class TicketServiceTest {
    
    @Inject
    TicketService ticketService;
    
    @Test
    void testCreateTicket() {
        CreateTicketDTO dto = new CreateTicketDTO();
        dto.setTitle("Test Ticket");
        
        TicketDTO result = ticketService.createTicket(dto);
        
        assertNotNull(result);
        assertEquals("Test Ticket", result.getTitle());
    }
}
```

### Integration Testing

```java
@QuarkusTest
class TicketResourceTest {
    
    @Test
    void testGetTickets() {
        given()
          .header("Authorization", "Bearer " + getToken())
          .when().get("/api/v1/tickets")
          .then()
             .statusCode(200)
             .body("data", not(empty()));
    }
}
```

## További Információk

- [Backend Setup](./setup.md)
- [Backend Architektúra](./architecture.md)
- [Testing](./testing.md)
- [Quarkus Coding Guidelines](https://quarkus.io/guides/coding-guidelines)

