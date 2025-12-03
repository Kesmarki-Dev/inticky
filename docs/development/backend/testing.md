# InTicky - Backend Testing

## Célközönség

Ez a dokumentum a backend tesztelési stratégiáját mutatja be. Backend fejlesztők számára készült.

## Testing Stack

**Választott eszközök:**
- **JUnit 5** - Unit és integration tesztek
- **REST Assured** - REST API tesztek
- **Mockito** - Mocking
- **Quarkus Test Framework** - Integration tesztek

## Test Típusok

### 1. Unit Tests

**Cél:** Egyedi osztályok/metódusok tesztelése izolációban.

**Példa:**
```java
@QuarkusTest
class TicketServiceTest {
    
    @Inject
    TicketService ticketService;
    
    @Mock
    TicketRepository ticketRepository;
    
    @Test
    void testCreateTicket() {
        // Given
        CreateTicketDTO dto = new CreateTicketDTO();
        dto.setTitle("Test Ticket");
        dto.setDescription("Test Description");
        
        // When
        TicketDTO result = ticketService.createTicket(dto);
        
        // Then
        assertNotNull(result);
        assertEquals("Test Ticket", result.getTitle());
        verify(ticketRepository).persist(any(Ticket.class));
    }
}
```

### 2. Integration Tests

**Cél:** Service-ek és adatbázis integráció tesztelése.

**Példa:**
```java
@QuarkusTest
@Transactional
class TicketRepositoryTest {
    
    @Inject
    TicketRepository ticketRepository;
    
    @Inject
    TenantContext tenantContext;
    
    @Test
    void testFindByTenantId() {
        // Given
        UUID tenantId = UUID.randomUUID();
        tenantContext.setTenantId(tenantId);
        
        Ticket ticket = new Ticket();
        ticket.setTenantId(tenantId);
        ticket.setTitle("Test");
        ticketRepository.persist(ticket);
        
        // When
        List<Ticket> result = ticketRepository.findByTenantId(tenantId);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getTitle());
    }
}
```

### 3. REST API Tests

**Cél:** REST végpontok teljes funkcionalitás tesztelése.

**Példa:**
```java
@QuarkusTest
class TicketResourceTest {
    
    @Test
    void testGetTickets() {
        given()
          .header("Authorization", "Bearer " + getAuthToken())
          .header("X-Tenant-ID", getTenantId().toString())
          .when()
          .get("/api/v1/tickets")
          .then()
             .statusCode(200)
             .body("data", not(empty()))
             .body("meta.pagination", notNullValue());
    }
    
    @Test
    void testCreateTicket() {
        CreateTicketDTO dto = new CreateTicketDTO();
        dto.setTitle("New Ticket");
        dto.setDescription("Description");
        
        given()
          .header("Authorization", "Bearer " + getAuthToken())
          .header("X-Tenant-ID", getTenantId().toString())
          .contentType(ContentType.JSON)
          .body(dto)
          .when()
          .post("/api/v1/tickets")
          .then()
             .statusCode(201)
             .body("data.title", equalTo("New Ticket"));
    }
}
```

## Test Coverage

### Követelmények

**Minimum coverage:** 70%

**Kritikus részek:** 90%+
- Business logika
- Security funkciók
- Tenant izoláció

### Coverage Mérés

**JaCoCo használata:**
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

**Futtatás:**
```bash
mvn clean test jacoco:report
```

## Test Adatok

### Test Fixtures

**Példa:**
```java
public class TestFixtures {
    
    public static Ticket createTestTicket(UUID tenantId) {
        Ticket ticket = new Ticket();
        ticket.setTenantId(tenantId);
        ticket.setTitle("Test Ticket");
        ticket.setStatus("new");
        return ticket;
    }
    
    public static User createTestUser(UUID tenantId) {
        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail("test@example.com");
        return user;
    }
}
```

### Test Database

**Quarkus Dev Services:**
- Automatikus test database indítás
- Minden test után rollback

**Konfiguráció:**
```yaml
quarkus:
  datasource:
    devservices:
      enabled: true
```

## Testing Best Practices

### 1. Arrange-Act-Assert Pattern

```java
@Test
void testCreateTicket() {
    // Arrange (Given)
    CreateTicketDTO dto = new CreateTicketDTO();
    dto.setTitle("Test");
    
    // Act (When)
    TicketDTO result = ticketService.createTicket(dto);
    
    // Assert (Then)
    assertNotNull(result);
    assertEquals("Test", result.getTitle());
}
```

### 2. Test Nevek

**Konvenció:** `test<MethodName>_<Condition>_<ExpectedResult>`

**Példák:**
```java
@Test
void testCreateTicket_WithValidData_ReturnsTicket() { }

@Test
void testCreateTicket_WithInvalidData_ThrowsException() { }

@Test
void testGetTickets_WithActiveTenant_ReturnsTickets() { }
```

### 3. Mocking

**Mock használata:**
```java
@QuarkusTest
class TicketServiceTest {
    
    @Inject
    TicketService ticketService;
    
    @Mock
    TicketRepository ticketRepository;
    
    @Mock
    NotificationService notificationService;
    
    @BeforeEach
    void setUp() {
        when(ticketRepository.persist(any())).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(UUID.randomUUID());
            return ticket;
        });
    }
}
```

### 4. Tenant Context Mocking

```java
@QuarkusTest
class TicketServiceTest {
    
    @Inject
    TicketService ticketService;
    
    @Mock
    TenantContext tenantContext;
    
    @BeforeEach
    void setUp() {
        UUID tenantId = UUID.randomUUID();
        when(tenantContext.getTenantId()).thenReturn(tenantId);
    }
}
```

## Test Futtatás

### Összes Test

```bash
mvn test
```

### Egyedi Test

```bash
mvn test -Dtest=TicketServiceTest
```

### Integration Tests

```bash
mvn verify
```

## További Információk

- [Backend Setup](./setup.md)
- [Backend Architektúra](./architecture.md)
- [Coding Standards](./coding-standards.md)
- [Quarkus Testing Guide](https://quarkus.io/guides/getting-started-testing)

