# InTicky - Seed Data

## Célközönség

Ez a dokumentum a seed adatok kezelését mutatja be. Adatbázis fejlesztők és backend fejlesztők számára készült.

## Seed Adatok Célja

**Használati területek:**
- Fejlesztési környezet inicializálása
- Tesztelési adatok
- Demo adatok
- Alapértelmezett konfigurációk

## Seed Adatok Struktúra

```
db/
├── seeds/
│   ├── dev/          # Fejlesztési seed adatok
│   │   ├── 01_tenants.sql
│   │   ├── 02_users.sql
│   │   ├── 03_ticket_categories.sql
│   │   └── ...
│   └── test/         # Tesztelési seed adatok
│       ├── 01_tenants.sql
│       └── ...
```

## Seed Scriptek Létrehozása

### Dev Seed Adatok

**01_tenants.sql:**
```sql
-- Dev seed: Tenants
INSERT INTO tenants (id, name, slug, subscription_tier, is_active, created_at)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'Dev Tenant', 'dev', 'premium', true, NOW()),
    ('00000000-0000-0000-0000-000000000002', 'Test Tenant', 'test', 'basic', true, NOW())
ON CONFLICT (id) DO NOTHING;
```

**02_users.sql:**
```sql
-- Dev seed: Users
INSERT INTO users (id, tenant_id, email, password_hash, first_name, last_name, role, is_active, created_at)
VALUES 
    (
        '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001',
        'admin@dev.local',
        '$2a$10$...', -- bcrypt hash for 'password'
        'Admin',
        'User',
        'admin',
        true,
        NOW()
    ),
    (
        '00000000-0000-0000-0000-000000000002',
        '00000000-0000-0000-0000-000000000001',
        'agent@dev.local',
        '$2a$10$...',
        'Agent',
        'User',
        'agent',
        true,
        NOW()
    )
ON CONFLICT (id) DO NOTHING;
```

**03_ticket_categories.sql:**
```sql
-- Dev seed: Ticket Categories
INSERT INTO ticket_categories (id, tenant_id, name, color, is_active, created_at)
SELECT 
    gen_random_uuid(),
    '00000000-0000-0000-0000-000000000001',
    name,
    color,
    true,
    NOW()
FROM (VALUES
    ('Support', '#007bff'),
    ('Bug Report', '#dc3545'),
    ('Feature Request', '#28a745'),
    ('Question', '#ffc107')
) AS categories(name, color)
ON CONFLICT DO NOTHING;
```

**04_tenant_modules.sql:**
```sql
-- Dev seed: Tenant Modules
INSERT INTO tenant_modules (tenant_id, module_name, is_active, activated_at, created_at)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'ticket', true, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000001', 'project', true, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000001', 'user', true, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000001', 'auth', true, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000001', 'notification', true, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000001', 'file', true, NOW(), NOW())
ON CONFLICT (tenant_id, module_name) DO NOTHING;
```

## Seed Adatok Futtatása

### Manuális Futtatás

**psql:**
```bash
psql -h localhost -U inticky -d inticky -f db/seeds/dev/01_tenants.sql
psql -h localhost -U inticky -d inticky -f db/seeds/dev/02_users.sql
# ... stb.
```

**Vagy összes seed egyszerre:**
```bash
for file in db/seeds/dev/*.sql; do
    psql -h localhost -U inticky -d inticky -f "$file"
done
```

### Maven Plugin

**pom.xml:**
```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>seed-dev</id>
            <phase>initialize</phase>
            <goals>
                <goal>exec</goal>
            </goals>
            <configuration>
                <executable>psql</executable>
                <arguments>
                    <argument>-h</argument>
                    <argument>localhost</argument>
                    <argument>-U</argument>
                    <argument>inticky</argument>
                    <argument>-d</argument>
                    <argument>inticky</argument>
                    <argument>-f</argument>
                    <argument>${project.basedir}/db/seeds/dev/01_tenants.sql</argument>
                </arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

**Futtatás:**
```bash
mvn exec:exec@seed-dev
```

### Quarkus Dev Services

**Automatikus seed futtatás:**
```yaml
# application-dev.yml
quarkus:
  flyway:
    locations: 
      - db/migrations
      - db/seeds/dev
```

## Test Seed Adatok

### Test Környezet Seed-ek

**Minimális adatok teszteléshez:**
```sql
-- db/seeds/test/01_tenants.sql
INSERT INTO tenants (id, name, slug, subscription_tier, is_active, created_at)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'Test Tenant', 'test', 'free', true, NOW())
ON CONFLICT (id) DO NOTHING;
```

## Seed Adatok Best Practices

### 1. Idempotens Scriptek

**✅ Jó:**
```sql
INSERT INTO tenants (id, name, slug)
VALUES ('...', 'Dev Tenant', 'dev')
ON CONFLICT (id) DO NOTHING;
```

**❌ Rossz:**
```sql
INSERT INTO tenants (id, name, slug)
VALUES ('...', 'Dev Tenant', 'dev');
-- Hiba, ha már létezik
```

### 2. Fix UUID-k Használata

**✅ Jó:**
```sql
-- Fix UUID seed adatokhoz
'00000000-0000-0000-0000-000000000001'
```

**Előnyök:**
- Könnyebb tesztelés
- Konzisztens adatok
- Referenciák könnyen kezelhetők

### 3. Seed Adatok Tisztítása

**Script törléshez:**
```sql
-- db/seeds/dev/cleanup.sql
DELETE FROM ticket_tags;
DELETE FROM ticket_tags;
DELETE FROM comments;
DELETE FROM tickets;
DELETE FROM project_tasks;
DELETE FROM projects;
DELETE FROM tenant_modules;
DELETE FROM users;
DELETE FROM tenants;
```

### 4. Seed Adatok Dokumentálása

**README a seeds mappában:**
```markdown
# Seed Data

## Dev Seeds
- 01_tenants.sql - Alapértelmezett tenant-ek
- 02_users.sql - Teszt felhasználók
- 03_ticket_categories.sql - Alapértelmezett kategóriák
- 04_tenant_modules.sql - Modul aktiválások

## Test Seeds
- Minimális adatok teszteléshez
```

## További Információk

- [Database Setup](./setup.md)
- [Migrációk](./migrations.md)
- [Backup/Restore](./backup-restore.md)

