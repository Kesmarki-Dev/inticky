# InTicky - Backend Setup

## Célközönség

Ez a dokumentum a backend fejlesztési környezet beállítását mutatja be. Backend fejlesztők számára készült.

## Előfeltételek

- Java 17+ (LTS)
- Maven 3.8+ vagy Gradle 7+
- Docker és Docker Compose
- IDE (IntelliJ IDEA, Eclipse, vagy VS Code)

## Quarkus Projekt Inicializálás

### Új Service Létrehozása

**Quarkus CLI használatával:**
```bash
quarkus create app com.inticky.service:ticket-service \
  --extension=resteasy-reactive-jackson,hibernate-orm-panache,postgresql,redis-client
```

**Vagy Maven archetype:**
```bash
mvn io.quarkus.platform:quarkus-maven-plugin:3.6.0:create \
  -DprojectGroupId=com.inticky \
  -DprojectArtifactId=ticket-service \
  -Dextensions="resteasy-reactive-jackson,hibernate-orm-panache,postgresql,redis-client"
```

### Projekt Struktúra

**Alapvető struktúra:**
```
ticket-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/inticky/ticket/
│   │   │       ├── controller/    # REST controllers
│   │   │       ├── service/       # Business logika
│   │   │       ├── repository/    # Data access
│   │   │       ├── model/         # Entity-k, DTO-k
│   │   │       └── config/        # Konfiguráció
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/
├── Dockerfile
└── pom.xml
```

## Maven Konfiguráció

### pom.xml Példa

```xml
<?xml version="1.0"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
    http://maven.apache.org/xsd/maven-4.0.0.xsd" 
    xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  
  <groupId>com.inticky</groupId>
  <artifactId>ticket-service</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  
  <properties>
    <quarkus.platform.version>3.6.0</quarkus.platform.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>
  
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>io.quarkus.platform</groupId>
        <artifactId>quarkus-bom</artifactId>
        <version>${quarkus.platform.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
  
  <dependencies>
    <!-- Quarkus Core -->
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-arc</artifactId>
    </dependency>
    
    <!-- REST API -->
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-hibernate-orm-panache</artifactId>
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-jdbc-postgresql</artifactId>
    </dependency>
    
    <!-- Redis -->
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-redis-client</artifactId>
    </dependency>
    
    <!-- Security -->
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-security</artifactId>
    </dependency>
    
    <!-- Testing -->
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-junit5</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>io.rest-assured</groupId>
      <artifactId>rest-assured</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
  
  <build>
    <plugins>
      <plugin>
        <groupId>io.quarkus.platform</groupId>
        <artifactId>quarkus-maven-plugin</artifactId>
        <version>${quarkus.platform.version}</version>
        <extensions>true</extensions>
        <executions>
          <execution>
            <goals>
              <goal>build</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

## Docker Compose Setup

### docker-compose.yml

A root `docker-compose.yml` tartalmazza az infrastruktúrát:

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

volumes:
  postgres_data:
  redis_data:
  qdrant_data:
```

**Indítás:**
```bash
docker-compose up -d postgres redis qdrant
```

**Megjegyzés:** Qdrant opcionális, csak akkor szükséges, ha az AI agent service-t használod Qdrant vector store-ral (production-hez ajánlott).

## Application Konfiguráció

### application.yml

```yaml
quarkus:
  application:
    name: ticket-service
    version: 1.0.0
  
  http:
    port: 8080
    cors:
      origins: "http://localhost:5173"
      headers: "accept,authorization,content-type,x-requested-with"
      methods: "GET,POST,PUT,DELETE,OPTIONS"
  
  datasource:
    db-kind: postgresql
    jdbc:
      url: jdbc:postgresql://localhost:5432/inticky
    username: inticky
    password: inticky
  
  hibernate-orm:
    database:
      generation: none  # Migrációk Flyway/Liquibase-vel
    log:
      sql: false
  
  redis:
    hosts: localhost:6379
  
  log:
    level: INFO
    console:
      json: false
```

### application-dev.yml

```yaml
quarkus:
  log:
    level: DEBUG
    category:
      "com.inticky": DEBUG
      "org.hibernate.SQL": DEBUG
  
  hibernate-orm:
    log:
      sql: true
```

## Lokális Fejlesztés Indítása

### Dev Mode

**Quarkus Dev Mode (ajánlott):**
```bash
cd backend/ticket-service
mvn quarkus:dev
```

**Előnyök:**
- Hot reload (automatikus újraindítás változásoknál)
- Dev Services (automatikus PostgreSQL, Redis indítás)
- Dev UI: `http://localhost:8080/q/dev`

### Build és Run

**Build:**
```bash
mvn clean package
```

**Run:**
```bash
java -jar target/ticket-service-1.0.0-SNAPSHOT-runner.jar
```

## IDE Beállítások

### IntelliJ IDEA

1. **Projekt megnyitása:**
   - `File > Open > backend/ticket-service`

2. **Maven projekt importálása:**
   - Automatikusan felismeri a `pom.xml`-t

3. **Java SDK beállítása:**
   - `File > Project Structure > Project`
   - SDK: Java 17

4. **Quarkus Dev Mode indítása:**
   - Run Configuration: `Quarkus`
   - Command: `quarkus:dev`

### VS Code

1. **Extensions telepítése:**
   - Java Extension Pack
   - Quarkus Tools

2. **Workspace megnyitása:**
   - `File > Open Folder > backend/ticket-service`

3. **Quarkus Dev Mode:**
   - Command Palette: `Quarkus: Start Dev Mode`

## Testing

### Unit Testing

```java
@QuarkusTest
class TicketServiceTest {
    
    @Test
    void testCreateTicket() {
        // Test implementation
    }
}
```

**Futtatás:**
```bash
mvn test
```

### Integration Testing

```java
@QuarkusTest
class TicketResourceTest {
    
    @Test
    void testGetTickets() {
        given()
          .when().get("/api/v1/tickets")
          .then()
             .statusCode(200);
    }
}
```

## További Információk

- [Backend Architektúra](./architecture.md)
- [AI Agent Integráció](./ai-agent-integration.md)
- [Coding Standards](./coding-standards.md)
- [Testing](./testing.md)
- [Quarkus dokumentáció](https://quarkus.io/guides/)

## Troubleshooting

### Port már használatban

**Hiba:** `Port 8080 is already in use`

**Megoldás:**
```yaml
# application.yml
quarkus:
  http:
    port: 8081  # Vagy más port
```

### Adatbázis kapcsolat hiba

**Hiba:** `Connection refused`

**Megoldás:**
1. Ellenőrizd, hogy fut-e a PostgreSQL: `docker ps`
2. Ellenőrizd a connection string-et: `application.yml`
3. Indítsd újra a Docker Compose-t: `docker-compose restart postgres`

### Maven dependency hiba

**Hiba:** `Could not resolve dependencies`

**Megoldás:**
```bash
mvn clean install -U
```

