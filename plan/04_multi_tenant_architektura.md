# InTicky - Multi-Tenant Architektúra

## Áttekintés

Az InTicky rendszer multi-tenant architektúrával készül, hogy több független ügyfelet (tenant-et) tudjon kiszolgálni egyetlen alkalmazás példányban. Ez a dokumentum a multi-tenant modell tervezését és implementációs stratégiáját írja le.

## Multi-Tenant Modell Választása

### Opciók Összehasonlítása

#### 1. Shared Database, Shared Schema
**Leírás:** Minden tenant ugyanazt az adatbázist és sémát használja, `tenant_id` mezővel izolálva.

**Előnyök:**
- ✅ Alacsonyabb költségek (egy adatbázis)
- ✅ Egyszerűbb adminisztráció és karbantartás
- ✅ Könnyebb skálázás
- ✅ Egyszerűbb backup és restore
- ✅ Könnyebb sémaváltoztatások (egy helyen)

**Hátrányok:**
- ❌ Adatbázis szintű izoláció hiánya
- ❌ Komplexebb biztonsági követelmények
- ❌ Nehezebb tenant specifikus optimalizálás
- ❌ Nagyobb kockázat adatszivárgás esetén

**Ajánlás:** ✅ **Ez a választott modell**

#### 2. Shared Database, Separate Schema
**Leírás:** Minden tenant ugyanazt az adatbázist használja, de külön sémával.

**Előnyök:**
- ✅ Jobb adatizoláció
- ✅ Tenant specifikus sémaváltoztatások lehetségesek

**Hátrányok:**
- ❌ Komplexebb adminisztráció
- ❌ Nehezebb sémaváltoztatások (minden tenant-nél)
- ❌ Adatbázis kapacitás korlátok

#### 3. Separate Database
**Leírás:** Minden tenant-nek saját adatbázisa van.

**Előnyök:**
- ✅ Legjobb adatizoláció
- ✅ Teljes kontroll tenant-onként
- ✅ Könnyebb tenant specifikus backup

**Hátrányok:**
- ❌ Magas költségek
- ❌ Komplex adminisztráció
- ❌ Nehezebb skálázás
- ❌ Nehezebb sémaváltoztatások

## Választott Modell: Shared Database, Shared Schema

### Alapelvek

1. **Tenant ID minden táblában** - Kötelező mező
2. **Adatbázis szintű izoláció** - Row Level Security (RLS)
3. **Application szintű validáció** - Dupla védelem
4. **Automatikus tenant szűrés** - Minden query-ben

## Tenant Azonosítás

### Lehetséges Módszerek

#### 1. Subdomain alapú (Ajánlott)
**Példa:** `acme.inticky.com`, `company.inticky.com`

**Előnyök:**
- ✅ Könnyen konfigurálható
- ✅ SSL tanúsítvány kezelés egyszerűbb
- ✅ Jó SEO és branding

**Implementáció:**
- DNS wildcard beállítás
- Application middleware tenant azonosításhoz
- Cookie/session tenant ID tárolása

#### 2. Domain alapú
**Példa:** `tickets.acme.com`, `support.company.com`

**Előnyök:**
- ✅ Teljes branding kontroll
- ✅ Jobb ügyfélélmény

**Hátrányok:**
- ❌ Komplexebb DNS konfiguráció
- ❌ SSL tanúsítvány kezelés per domain

#### 3. Path alapú
**Példa:** `inticky.com/acme`, `inticky.com/company`

**Előnyök:**
- ✅ Egyszerűbb DNS
- ✅ Könnyebb SSL

**Hátrányok:**
- ❌ Kevésbé professzionális
- ❌ Nehezebb branding

#### 4. Header alapú
**Példa:** `X-Tenant-ID` header

**Előnyök:**
- ✅ API-khoz jó
- ✅ Rugalmas

**Hátrányok:**
- ❌ Böngészős használathoz nem ideális

### Választott Módszer: Subdomain + Header kombináció

- **Webes felület:** Subdomain alapú (`{tenant-slug}.inticky.com`)
- **API hozzáférés:** Header alapú (`X-Tenant-ID` vagy `X-Tenant-Slug`)

## Tenant Konfiguráció

### Tenant Beállítások Struktúrája

```json
{
  "tenant_id": "uuid",
  "name": "Acme Corporation",
  "slug": "acme",
  "domain": "tickets.acme.com", // Opcionális egyedi domain
  "logo_url": "https://...",
  "primary_color": "#007bff",
  "secondary_color": "#6c757d",
  "features": {
    "custom_fields": true,
    "sla_tracking": false,
    "knowledge_base": true,
    "time_tracking": false
  },
  "limits": {
    "max_users": 50,
    "max_tickets_per_month": 1000,
    "storage_gb": 10
  },
  "subscription": {
    "tier": "premium", // free, basic, premium, enterprise
    "expires_at": "2024-12-31",
    "is_active": true
  },
  "settings": {
    "email_from_name": "Acme Support",
    "email_from_address": "support@acme.com",
    "ticket_auto_assign": true,
    "default_priority": "medium"
  }
}
```

## Biztonsági Stratégia

### 1. Adatbázis Szintű Védelem

#### Row Level Security (RLS) - PostgreSQL

```sql
-- Minden táblához RLS engedélyezése
ALTER TABLE tickets ENABLE ROW LEVEL SECURITY;

-- Policy létrehozása
CREATE POLICY tenant_isolation_policy ON tickets
    FOR ALL
    USING (tenant_id = current_setting('app.current_tenant_id')::UUID);

-- Application szinten tenant ID beállítása
SET app.current_tenant_id = 'tenant-uuid';
```

#### Trigger-based Védelem

```sql
-- Trigger, ami biztosítja, hogy tenant_id mindig be legyen állítva
CREATE OR REPLACE FUNCTION enforce_tenant_id()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.tenant_id IS NULL THEN
        NEW.tenant_id := current_setting('app.current_tenant_id')::UUID;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tickets_tenant_id_trigger
    BEFORE INSERT ON tickets
    FOR EACH ROW
    EXECUTE FUNCTION enforce_tenant_id();
```

### 2. Application Szintű Védelem

#### Middleware Pattern

```javascript
// Példa (Node.js/Express)
async function tenantMiddleware(req, res, next) {
    // Tenant azonosítás subdomain-ből vagy header-ből
    const tenantSlug = req.subdomain || req.headers['x-tenant-id'];
    
    if (!tenantSlug) {
        return res.status(400).json({ error: 'Tenant not specified' });
    }
    
    // Tenant betöltése adatbázisból
    const tenant = await getTenantBySlug(tenantSlug);
    
    if (!tenant || !tenant.is_active) {
        return res.status(404).json({ error: 'Tenant not found' });
    }
    
    // Tenant ID hozzáadása request-hez
    req.tenant = tenant;
    req.tenantId = tenant.id;
    
    // Adatbázis session-be beállítás
    await db.query('SET app.current_tenant_id = $1', [tenant.id]);
    
    next();
}
```

#### Query Builder Wrapper

```javascript
// Minden query automatikusan tenant szűrve
class TenantAwareQueryBuilder {
    constructor(tenantId) {
        this.tenantId = tenantId;
    }
    
    select(table) {
        return db(table).where('tenant_id', this.tenantId);
    }
    
    insert(table, data) {
        return db(table).insert({
            ...data,
            tenant_id: this.tenantId
        });
    }
}
```

### 3. API Szintű Védelem

- **Tenant validáció** - Minden request-ben
- **Authorization check** - Felhasználó a megfelelő tenant-hoz tartozik-e
- **Rate limiting** - Tenant-onként
- **Input validation** - Tenant ID nem módosítható

## Tenant Izoláció Ellenőrzési Lista

### Adatbázis

- [ ] `tenant_id` mező minden táblában
- [ ] RLS policy-k minden táblához
- [ ] Foreign key constraint-ek tenant_id-vel
- [ ] Unique constraint-ek tenant_id-vel kombinálva
- [ ] Trigger-ek tenant_id automatikus beállításához

### Backend

- [ ] Tenant middleware minden route-on
- [ ] Query builder automatikus tenant szűrése
- [ ] Tenant validáció minden műveletnél
- [ ] Authorization check (user -> tenant mapping)
- [ ] Error handling (tenant not found, access denied)

### Frontend

- [ ] Tenant kontextus (React Context, Vue Store)
- [ ] API kérések tenant ID-val
- [ ] Subdomain kezelés
- [ ] Tenant specifikus branding

## Tenant Onboarding Folyamat

### 1. Regisztráció

1. Új tenant regisztráció
2. Email megerősítés
3. Alapértelmezett beállítások létrehozása
4. Admin felhasználó létrehozása
5. Welcome email küldése

### 2. Kezdeti Beállítás

1. Tenant információk kitöltése (név, logo, stb.)
2. Alapértelmezett kategóriák létrehozása
3. Alapértelmezett státuszok létrehozása
4. Felhasználók meghívása
5. Integrációk beállítása (opcionális)

### 3. Aktiválás

1. Subscription aktiválás
2. Domain konfiguráció (ha van)
3. SSL tanúsítvány beállítása (ha van)
4. Tenant aktívvá tétele

## Tenant Limitációk és Quota

### Limit Típusok

1. **Felhasználók száma** - Max users per tenant
2. **Ticketek száma** - Max tickets per month/year
3. **Tárhely** - Max storage per tenant
4. **API hívások** - Rate limiting per tenant
5. **Funkciók** - Feature flags per subscription tier

### Implementáció

```sql
-- Quota tracking tábla
tenant_quotas
├── tenant_id (UUID, PRIMARY KEY)
├── users_count (INTEGER, DEFAULT 0)
├── tickets_count_this_month (INTEGER, DEFAULT 0)
├── storage_used_bytes (BIGINT, DEFAULT 0)
├── api_calls_today (INTEGER, DEFAULT 0)
└── updated_at (TIMESTAMP)
```

### Quota Ellenőrzés

- **Pre-flight check** - Művelet előtt
- **Post-action update** - Művelet után
- **Background job** - Napi/havi reset
- **Alerting** - Quota közelítésekor

## Tenant Migráció és Backup

### Backup Stratégia

1. **Teljes adatbázis backup** - Minden tenant-t tartalmaz
2. **Tenant specifikus export** - JSON/SQL formátum
3. **Incremental backup** - Csak változások

### Migráció

- **Tenant export** - Adatok exportálása
- **Tenant import** - Adatok importálása új rendszerbe
- **Adatvalidáció** - Integritás ellenőrzés

## Monitoring és Analytics

### Tenant Szintű Metrikák

- Aktív felhasználók száma
- Ticketek száma (naponta/havonta)
- Átlagos válaszidő
- Storage használat
- API hívások száma

### Rendszer Szintű Metrikák

- Összes tenant száma
- Aktív tenant-ek száma
- Teljes storage használat
- Teljes API hívások
- Teljesítmény metrikák per tenant

## Következő Lépések

1. **Tenant azonosítás implementálása** - Subdomain/header kezelés
2. **RLS policy-k létrehozása** - PostgreSQL-ben
3. **Middleware fejlesztése** - Backend-ben
4. **Query builder wrapper** - Automatikus tenant szűrés
5. **Tenant onboarding flow** - Regisztráció és beállítás
6. **Quota rendszer** - Limitációk implementálása
7. **Monitoring** - Tenant szintű metrikák

