# InTicky - Database Migrations

## Célközönség

Ez a dokumentum az adatbázis migrációk kezelését mutatja be. Adatbázis fejlesztők és backend fejlesztők számára készült.

## Migrációs Eszközök

### Flyway (Ajánlott)

**Előnyök:**
- Verziózott migrációk
- Visszavonható migrációk
- Java integráció
- Quarkus támogatás

### Liquibase (Alternatíva)

**Előnyök:**
- XML, YAML, SQL támogatás
- ChangeSet alapú
- Rollback támogatás

## Flyway Használata

### Projekt Setup

**pom.xml:**
```xml
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <configuration>
        <url>jdbc:postgresql://localhost:5432/inticky</url>
        <user>inticky</user>
        <password>inticky</password>
        <locations>
            <location>filesystem:db/migrations</location>
        </locations>
    </configuration>
</plugin>
```

### Migrációk Struktúrája

```
db/
├── migrations/
│   ├── V1__initial_schema.sql
│   ├── V2__add_projects.sql
│   ├── V3__add_tenant_modules.sql
│   └── ...
└── seeds/
    ├── dev/
    └── test/
```

### Migráció Naming Convention

**Formátum:** `V<version>__<description>.sql`

**Példák:**
- `V1__initial_schema.sql`
- `V2__add_projects.sql`
- `V3__add_indexes.sql`

**Szabályok:**
- Verzió szám növekvő
- Két aláhúzás (`__`) a verzió és leírás között
- Leírás: snake_case

### Migráció Létrehozása

**1. Új migráció fájl:**
```bash
touch db/migrations/V4__add_notifications.sql
```

**2. SQL tartalom:**
```sql
-- V4__add_notifications.sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_notifications_tenant_user 
    ON notifications(tenant_id, user_id, is_read);
```

### Migrációk Futtatása

**Maven:**
```bash
cd db
mvn flyway:migrate
```

**Flyway CLI:**
```bash
flyway migrate
```

**Quarkus automatikus:**
```yaml
# application.yml
quarkus:
  flyway:
    migrate-at-start: true
    locations: db/migrations
```

### Migráció Státusz

**Ellenőrzés:**
```bash
mvn flyway:info
```

**Kimenet:**
```
+-----------+-------------------+---------------------+---------+
| Category  | Version           | Description         | State   |
+-----------+-------------------+---------------------+---------+
|           | 1                 | initial_schema      | Success |
|           | 2                 | add_projects        | Success |
|           | 3                 | add_tenant_modules  | Pending |
+-----------+-------------------+---------------------+---------+
```

## Liquibase Használata

### Projekt Setup

**pom.xml:**
```xml
<plugin>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-maven-plugin</artifactId>
    <configuration>
        <changeLogFile>db/changelog/db.changelog-master.xml</changeLogFile>
        <url>jdbc:postgresql://localhost:5432/inticky</url>
        <username>inticky</username>
        <password>inticky</password>
    </configuration>
</plugin>
```

### Changelog Struktúra

```
db/
├── changelog/
│   ├── db.changelog-master.xml
│   ├── changes/
│   │   ├── 001-initial-schema.xml
│   │   ├── 002-add-projects.xml
│   │   └── ...
```

### Changelog Példa

```xml
<!-- db/changelog/changes/001-initial-schema.xml -->
<databaseChangeLog>
    <changeSet id="1" author="developer">
        <createTable tableName="tenants">
            <column name="id" type="UUID">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="name" type="VARCHAR(255)">
                <constraints nullable="false"/>
            </column>
            <!-- ... -->
        </createTable>
    </changeSet>
</databaseChangeLog>
```

## Rollback Procedúrák

### Flyway Rollback

**Manuális rollback:**
```sql
-- Visszavonás SQL script
DROP TABLE IF EXISTS notifications;
```

**Vagy új migráció:**
```sql
-- V5__rollback_notifications.sql
DROP TABLE IF EXISTS notifications;
```

### Liquibase Rollback

**Automatikus rollback:**
```xml
<rollback>
    <dropTable tableName="notifications"/>
</rollback>
```

## Best Practices

### 1. Idempotens Migrációk

**✅ Jó:**
```sql
CREATE TABLE IF NOT EXISTS notifications (
    -- ...
);
```

**❌ Rossz:**
```sql
CREATE TABLE notifications (
    -- ...
);
```

### 2. Migrációk Kisebb Részekre Bontása

**✅ Jó:**
- `V1__initial_schema.sql` - Alapvető táblák
- `V2__add_indexes.sql` - Indexek
- `V3__add_constraints.sql` - Constraint-ek

**❌ Rossz:**
- `V1__everything.sql` - Minden egy fájlban

### 3. DDL és DML Különválasztása

**DDL (Data Definition Language):**
- Táblák, indexek, constraint-ek
- Strukturális változások

**DML (Data Manipulation Language):**
- Seed adatok
- Data migration
- Külön fájlokban vagy seed mappában

### 4. Migrációk Tesztelése

**Lépések:**
1. Lokális adatbázison tesztelés
2. Test adatbázison tesztelés
3. Staging környezeten tesztelés
4. Production környezet

## További Információk

- [Database Setup](./setup.md)
- [Seed Data](./seed-data.md)
- [Backup/Restore](./backup-restore.md)
- [Flyway dokumentáció](https://flywaydb.org/documentation/)
- [Liquibase dokumentáció](https://docs.liquibase.com/)

