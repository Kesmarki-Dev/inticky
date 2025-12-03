# InTicky - Database Backup/Restore

## Célközönség

Ez a dokumentum az adatbázis backup és restore procedúráit mutatja be. DevOps mérnökök és adatbázis adminisztrátorok számára készült.

## Backup Stratégia

### Backup Típusok

**1. Teljes Backup (Full Backup)**
- Teljes adatbázis másolása
- Napi backup (éles környezet)
- Heti backup (hosszú távú tárolás)

**2. Incremental Backup**
- Csak változások
- Transaction log backup
- Point-in-time recovery

**3. Tenant Specifikus Backup**
- Egy tenant adatainak exportálása
- JSON vagy SQL formátum

## Lokális Backup

### pg_dump Használata

**Teljes adatbázis backup:**
```bash
pg_dump -h localhost -U inticky -d inticky -F c -f backup_$(date +%Y%m%d_%H%M%S).dump
```

**Csak séma (struktúra):**
```bash
pg_dump -h localhost -U inticky -d inticky --schema-only -f schema_backup.sql
```

**Csak adatok:**
```bash
pg_dump -h localhost -U inticky -d inticky --data-only -f data_backup.sql
```

**Tenant specifikus backup:**
```bash
pg_dump -h localhost -U inticky -d inticky \
  --table=tenants \
  --table=users \
  --table=tickets \
  --where="tenant_id='00000000-0000-0000-0000-000000000001'" \
  -f tenant_backup.sql
```

### Backup Script

**backup.sh:**
```bash
#!/bin/bash

BACKUP_DIR="./backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/inticky_backup_$DATE.dump"

mkdir -p $BACKUP_DIR

pg_dump -h localhost -U inticky -d inticky -F c -f $BACKUP_FILE

echo "Backup created: $BACKUP_FILE"
```

**Futtatás:**
```bash
chmod +x backup.sh
./backup.sh
```

## Restore Procedúrák

### pg_restore Használata

**Teljes adatbázis restore:**
```bash
pg_restore -h localhost -U inticky -d inticky -c backup_file.dump
```

**Csak adatok (séma megtartása):**
```bash
pg_restore -h localhost -U inticky -d inticky --data-only backup_file.dump
```

**Csak séma:**
```bash
pg_restore -h localhost -U inticky -d inticky --schema-only backup_file.dump
```

### SQL File Restore

**SQL fájlból:**
```bash
psql -h localhost -U inticky -d inticky -f backup.sql
```

## Azure Backup

### Azure Database for PostgreSQL

**Automatikus backup:**
- Azure automatikusan készít backup-okat
- Konfigurálható retention period
- Point-in-time restore

**Manuális backup:**
```bash
# Azure CLI
az postgres flexible-server backup create \
  --resource-group inticky-rg \
  --server-name inticky-db \
  --backup-name manual-backup-$(date +%Y%m%d)
```

### Backup Exportálás

**Azure Storage-ba:**
```bash
az postgres flexible-server backup export \
  --resource-group inticky-rg \
  --server-name inticky-db \
  --backup-name backup-name \
  --storage-account intickystorage \
  --storage-container backups
```

## Restore Stratégia

### Teljes Restore

**Lépések:**
1. Új adatbázis létrehozása (ha szükséges)
2. Backup fájl restore
3. Adatok validálása
4. Alkalmazás újraindítása

**Script:**
```bash
#!/bin/bash

# 1. Új adatbázis létrehozása
psql -h localhost -U postgres -c "DROP DATABASE IF EXISTS inticky_restore;"
psql -h localhost -U postgres -c "CREATE DATABASE inticky_restore;"

# 2. Restore
pg_restore -h localhost -U inticky -d inticky_restore backup_file.dump

# 3. Validáció
psql -h localhost -U inticky -d inticky_restore -c "SELECT COUNT(*) FROM tenants;"
```

### Point-in-Time Restore

**Azure Database for PostgreSQL:**
```bash
az postgres flexible-server restore \
  --resource-group inticky-rg \
  --name inticky-db-restored \
  --source-server inticky-db \
  --restore-time "2024-01-15T10:30:00Z"
```

## Backup Retention

### Retention Policy

**Lokális backup-ok:**
- Napi backup: 7 nap
- Heti backup: 4 hét
- Havi backup: 12 hónap

**Azure backup-ok:**
- Konfigurálható retention period
- Ajánlott: 30 nap

### Backup Tisztítás

**Script:**
```bash
#!/bin/bash

BACKUP_DIR="./backups"
RETENTION_DAYS=7

# Régi backup-ok törlése
find $BACKUP_DIR -name "*.dump" -mtime +$RETENTION_DAYS -delete

echo "Old backups cleaned"
```

## Tenant Specifikus Backup

### Exportálás

**JSON formátum:**
```sql
-- Export tenant adatai JSON-ba
COPY (
    SELECT json_agg(row_to_json(t))
    FROM (
        SELECT * FROM tenants WHERE id = '...'
    ) t
) TO '/tmp/tenant_export.json';
```

**SQL formátum:**
```bash
pg_dump -h localhost -U inticky -d inticky \
  --table=tenants \
  --table=users \
  --where="tenant_id='...'" \
  -f tenant_export.sql
```

### Importálás

**JSON formátum:**
```sql
-- Import tenant adatai JSON-ból
-- (Manuális SQL script szükséges)
```

**SQL formátum:**
```bash
psql -h localhost -U inticky -d inticky -f tenant_export.sql
```

## Backup Monitoring

### Backup Státusz Ellenőrzés

**Lokális:**
```bash
# Backup fájlok listázása
ls -lh backups/

# Legutóbbi backup
ls -t backups/ | head -1
```

**Azure:**
```bash
az postgres flexible-server backup list \
  --resource-group inticky-rg \
  --server-name inticky-db
```

## További Információk

- [Database Setup](./setup.md)
- [Migrációk](./migrations.md)
- [Seed Data](./seed-data.md)
- [PostgreSQL Backup dokumentáció](https://www.postgresql.org/docs/current/backup.html)

