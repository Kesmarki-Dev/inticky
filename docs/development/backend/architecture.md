# InTicky - Backend Architektúra

## Célközönség

Ez a dokumentum a backend architektúráját és service struktúráját mutatja be. Backend fejlesztők számára készült.

## Service Struktúra

### Moduláris Mikroszolgáltatások

**Választott modell:** Opció 1 - Moduláris Mikroszolgáltatások

**Jellemzők:**
- Minden modul külön mikroszolgáltatás
- Modul aktiválás per tenant
- Független deployment és skálázás
- Service-to-service kommunikáció

**Modul → Service Mapping:**
```
Ticket Modul      → ticket-service
Projekt Modul     → project-service
User Modul        → user-service
Auth Modul        → auth-service
Notification Modul → notification-service
File Modul        → file-service
Report Modul      → report-service (opcionális)
AI Agent Modul    → ai-agent-service
```

## Package Struktúra

### Alapvető Struktúra

Minden service követi ezt a struktúrát:

```
service-name/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/inticky/service/
│   │   │       ├── controller/    # REST controllers
│   │   │       ├── service/       # Business logika
│   │   │       ├── repository/    # Data access
│   │   │       ├── model/         # Entity-k, DTO-k
│   │   │       ├── config/        # Konfiguráció
│   │   │       └── exception/     # Exception handling
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/
└── pom.xml
```

### Package Részletek

#### controller/

REST API végpontok.

**Példa:**
```java
@Path("/api/v1/tickets")
@ApplicationScoped
public class TicketController {
    
    @Inject
    TicketService ticketService;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TicketDTO> getTickets() {
        return ticketService.getAllTickets();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TicketDTO createTicket(CreateTicketDTO dto) {
        return ticketService.createTicket(dto);
    }
}
```

#### service/

Business logika.

**Példa:**
```java
@ApplicationScoped
public class TicketService {
    
    @Inject
    TicketRepository ticketRepository;
    
    @Inject
    TenantContext tenantContext;
    
    public TicketDTO createTicket(CreateTicketDTO dto) {
        Ticket ticket = new Ticket();
        ticket.setTenantId(tenantContext.getTenantId());
        ticket.setTitle(dto.getTitle());
        // ... business logic
        ticketRepository.persist(ticket);
        return mapToDTO(ticket);
    }
}
```

#### repository/

Data access layer (Panache).

**Példa:**
```java
@ApplicationScoped
public class TicketRepository implements PanacheRepository<Ticket> {
    
    public List<Ticket> findByTenantIdAndStatus(UUID tenantId, String status) {
        return find("tenant_id = ?1 AND status = ?2", tenantId, status).list();
    }
}
```

#### model/

Entity-k és DTO-k.

**Entity példa:**
```java
@Entity
@Table(name = "tickets")
public class Ticket extends PanacheEntityBase {
    
    @Id
    @GeneratedValue
    public UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    public UUID tenantId;
    
    @Column(nullable = false)
    public String title;
    
    // ... további mezők
}
```

**DTO példa:**
```java
public class TicketDTO {
    public UUID id;
    public String ticketNumber;
    public String title;
    public String status;
    // ... további mezők
}
```

#### config/

Konfiguráció osztályok.

**Példa:**
```java
@ApplicationScoped
public class TenantConfig {
    
    @ConfigProperty(name = "app.tenant.header")
    String tenantHeader;
    
    // ... konfiguráció
}
```

#### exception/

Exception handling.

**Példa:**
```java
@ApplicationScoped
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {
    
    @Override
    public Response toResponse(Exception exception) {
        // Error response handling
    }
}
```

## Modul Aktiválás Implementáció

### Modul Check Interceptor

**Implementáció:**
```java
@Interceptor
@Priority(1000)
public class ModuleCheckInterceptor {
    
    @Inject
    ModuleRegistryService moduleRegistry;
    
    @Inject
    TenantContext tenantContext;
    
    @AroundInvoke
    public Object checkModule(InvocationContext context) throws Exception {
        ModuleRequired annotation = getModuleRequiredAnnotation(context);
        if (annotation != null) {
            String moduleName = annotation.value();
            UUID tenantId = tenantContext.getTenantId();
            
            if (!moduleRegistry.isModuleActive(moduleName, tenantId)) {
                throw new ModuleNotActiveException(moduleName);
            }
        }
        return context.proceed();
    }
}
```

### Modul Required Annotation

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleRequired {
    String value();
}
```

### Használat

```java
@ModuleRequired("ticket")
@Path("/api/v1/tickets")
public class TicketController {
    // Csak akkor érhető el, ha ticket modul aktív
}
```

## Tenant Context

### Tenant Azonosítás

**Subdomain alapú:**
```java
@ApplicationScoped
public class TenantResolver {
    
    public UUID resolveTenant(HttpServletRequest request) {
        String subdomain = extractSubdomain(request.getServerName());
        return tenantService.findBySlug(subdomain).getId();
    }
}
```

**Header alapú:**
```java
public UUID resolveTenant(HttpHeaders headers) {
    String tenantId = headers.getHeaderString("X-Tenant-ID");
    return UUID.fromString(tenantId);
}
```

### Tenant Context Service

```java
@ApplicationScoped
public class TenantContext {
    
    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();
    
    public void setTenantId(UUID tenantId) {
        TENANT_ID.set(tenantId);
    }
    
    public UUID getTenantId() {
        return TENANT_ID.get();
    }
    
    public void clear() {
        TENANT_ID.remove();
    }
}
```

## Service-to-Service Kommunikáció

### REST Client (Synchronous)

**Quarkus REST Client:**
```java
@RegisterRestClient
@Path("/api/v1/users")
public interface UserServiceClient {
    
    @GET
    @Path("/{id}")
    UserDTO getUser(@PathParam("id") UUID id);
}
```

**Használat:**
```java
@Inject
@RestClient
UserServiceClient userServiceClient;

public UserDTO getUser(UUID userId) {
    return userServiceClient.getUser(userId);
}
```

### Message Queue (Asynchronous)

**Azure Service Bus integráció:**
```java
@ApplicationScoped
public class NotificationPublisher {
    
    @Inject
    ServiceBusSenderClient senderClient;
    
    public void publishTicketCreated(TicketCreatedEvent event) {
        senderClient.sendMessage(new ServiceBusMessage(event.toJson()));
    }
}
```

## Adatbázis Hozzáférés

### Panache Repository Pattern

```java
@ApplicationScoped
public class TicketRepository implements PanacheRepository<Ticket> {
    
    public List<Ticket> findByTenant(UUID tenantId) {
        return find("tenant_id", tenantId).list();
    }
    
    public Optional<Ticket> findByTicketNumber(String ticketNumber) {
        return find("ticket_number", ticketNumber).firstResultOptional();
    }
}
```

### Tenant Szűrés Automatikus

```java
@ApplicationScoped
public class TenantAwareRepository<T extends PanacheEntityBase> {
    
    @Inject
    TenantContext tenantContext;
    
    public List<T> findAll() {
        UUID tenantId = tenantContext.getTenantId();
        return find("tenant_id", tenantId).list();
    }
}
```

## Error Handling

### Exception Mapper

```java
@Provider
public class ModuleNotActiveExceptionMapper 
    implements ExceptionMapper<ModuleNotActiveException> {
    
    @Override
    public Response toResponse(ModuleNotActiveException exception) {
        ErrorResponse error = new ErrorResponse(
            "MODULE_NOT_ACTIVE",
            "Module " + exception.getModuleName() + " is not active"
        );
        return Response.status(403).entity(error).build();
    }
}
```

## További Információk

- [Backend Setup](./setup.md)
- [Coding Standards](./coding-standards.md)
- [Testing](./testing.md)
- [Mikroszolgáltatások](../../architecture/microservices.md)
- [Moduláris felépítés](../../../plan/08_modularis_felepites.md)

