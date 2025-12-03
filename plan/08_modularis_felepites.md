# InTicky - Moduláris Felépítés Elemzése

## Áttekintés

Ez a dokumentum elemzi, hogy a moduláris felépítés hogyan illeszkedik a jelenlegi mikroszolgáltatásos architektúrához, és mennyire bonyolítja meg a rendszert.

## Mit Jelent Moduláris Felépítés?

### Definíció

**Moduláris felépítés** = A rendszer funkcionalitásait logikai modulokra bontjuk, amelyek:
- ✅ **Függetlenül fejleszthetők** - Külön csapatok dolgozhatnak rajtuk
- ✅ **Opcionálisan aktiválhatók** - Tenant-onként vagy subscription tier alapján
- ✅ **Függetlenül telepíthetők** - Nem kell minden modult telepíteni
- ✅ **Jól definiált interfészek** - Modulok közötti kommunikáció

### Moduláris vs. Mikroszolgáltatások

**Különbség:**
- **Mikroszolgáltatások** = Technikai szétválasztás (külön process-ek, deployment-ek)
- **Moduláris felépítés** = Logikai/funkcionális szétválasztás (lehet ugyanabban a service-ben)

**Kombináció lehetséges:**
- ✅ Moduláris mikroszolgáltatások (ajánlott)
- ✅ Moduláris monolit (ha nem mikroszolgáltatások)

## Moduláris Felépítés Lehetőségei

### Opció 1: Moduláris Mikroszolgáltatások (Ajánlott)

**Leírás:** Minden modul külön mikroszolgáltatás.

**Modulok:**
- **Ticket Modul** → Ticket Service
- **Projekt Modul** → Project Service
- **User Modul** → User Service
- **Notification Modul** → Notification Service
- **File Modul** → File Service
- **Report Modul** → Report Service
- **Auth Modul** → Auth Service

**Előnyök:**
- ✅ Teljes függetlenség (deployment, skálázás)
- ✅ Különböző technológiák használhatók (ha szükséges)
- ✅ Külön csapatok dolgozhatnak
- ✅ Modul specifikus optimalizálás
- ✅ Modul specifikus monitoring

**Hátrányok:**
- ❌ Több deployment overhead
- ❌ Több infrastruktúra
- ❌ Kommunikáció komplexebb (service-to-service)
- ❌ Distributed transaction kezelés

**Bonyolultság:** ⭐⭐⭐ (Közepes-Magas)

### Opció 2: Moduláris Monolit (Egyszerűbb)

**Leírás:** Egy alkalmazás, de modulokra bontva (package/module szinten).

**Strukturálás:**
```
backend/
├── core/              # Core funkcionalitás
├── ticket-module/     # Ticket modul
├── project-module/    # Projekt modul
├── user-module/       # User modul
└── ...
```

**Előnyök:**
- ✅ Egyszerűbb deployment (egy alkalmazás)
- ✅ Egyszerűbb kommunikáció (in-process)
- ✅ Egyszerűbb transaction kezelés
- ✅ Kevesebb infrastruktúra

**Hátrányok:**
- ❌ Nehezebb független skálázás
- ❌ Nehezebb különböző technológiák használata
- ❌ Nagyobb alkalmazás méret

**Bonyolultság:** ⭐⭐ (Alacsony-Közepes)

### Opció 3: Hibrid Megközelítés (Kiegyensúlyozott)

**Leírás:** Kritikus modulok külön service-ek, kisebb modulok együtt.

**Strukturálás:**
```
backend/
├── core-service/      # Core + User + Auth (együtt)
├── ticket-service/    # Ticket modul (külön)
├── project-service/   # Projekt modul (külön)
├── notification-service/  # Notification modul (külön)
└── file-service/     # File modul (külön)
```

**Előnyök:**
- ✅ Kiegyensúlyozott komplexitás
- ✅ Kritikus modulok függetlenül skálázhatók
- ✅ Kevesebb service, mint az Opció 1

**Hátrányok:**
- ❌ Döntés szükséges: melyik modulok együtt
- ❌ Valamennyire komplexebb, mint az Opció 2

**Bonyolultság:** ⭐⭐⭐ (Közepes)

## Modul Aktiválás és Feature Flags

### Modul Aktiválás Stratégia

**1. Tenant szintű aktiválás:**
```sql
tenant_modules
├── tenant_id
├── module_name (ticket, project, report, stb.)
├── is_active
└── activated_at
```

**2. Subscription tier alapján:**
```sql
subscription_tiers
├── tier_name (free, basic, premium, enterprise)
├── modules (JSON array: ["ticket", "project"])
└── ...
```

**3. Feature flags (runtime):**
- **Azure App Configuration** - Feature flags kezelés
- **LaunchDarkly** - Feature flag service (opcionális)
- **Saját implementáció** - Redis + config

### Modul Aktiválás Implementáció

**Backend (Java):**
```java
@ModuleRequired("ticket")
@RestController
public class TicketController {
    // Csak akkor érhető el, ha a ticket modul aktív
}

// Vagy interceptor-ben:
@Component
public class ModuleCheckInterceptor implements HandlerInterceptor {
    public boolean preHandle(...) {
        if (!moduleService.isModuleActive("ticket")) {
            throw new ModuleNotActiveException("ticket");
        }
    }
}
```

**Frontend (React):**
```typescript
// Feature flag hook
const { isModuleActive } = useModules();

if (!isModuleActive('project')) {
    return <ModuleNotAvailable module="project" />;
}
```

## Modul Kommunikáció

### Modulok Közötti Kommunikáció

**1. REST API (Synchronous):**
- Service-to-service HTTP hívások
- API Gateway routing
- Circuit breaker pattern (Resilience4j)

**2. Message Queue (Asynchronous):**
- Azure Service Bus
- Event-driven kommunikáció
- Pub/Sub pattern

**3. Shared Database (ha ugyanabban a service-ben):**
- In-process kommunikáció
- Transaction support

### Modul Függőségek

**Minimális függőségek:**
- ✅ Minden modul függ a **Core modultól** (Auth, User)
- ✅ Modulok nem függnek egymástól (ha lehetséges)

**Függőség kezelés:**
```java
// Modul interface
public interface Module {
    String getName();
    boolean isActive(TenantId tenantId);
    void initialize();
}

// Modul registry
@Component
public class ModuleRegistry {
    private Map<String, Module> modules;
    
    public boolean isModuleActive(String moduleName, TenantId tenantId) {
        return modules.get(moduleName).isActive(tenantId);
    }
}
```

## Moduláris Felépítés Bonyolultság Elemzése

### Fejlesztési Bonyolultság

**Opció 1 (Mikroszolgáltatások):**
- ⭐⭐⭐ **Architektúra tervezés** - Service boundaries, API design
- ⭐⭐⭐ **Kommunikáció** - Service-to-service, error handling
- ⭐⭐ **Deployment** - Több service deployment
- ⭐⭐⭐ **Testing** - Integration tesztek, contract testing
- ⭐⭐⭐ **Monitoring** - Több service monitoring

**Opció 2 (Monolit):**
- ⭐⭐ **Architektúra tervezés** - Package/module struktúra
- ⭐ **Kommunikáció** - In-process, egyszerűbb
- ⭐ **Deployment** - Egy alkalmazás
- ⭐⭐ **Testing** - Unit és integration tesztek
- ⭐⭐ **Monitoring** - Egy alkalmazás monitoring

**Opció 3 (Hibrid):**
- ⭐⭐⭐ **Architektúra tervezés** - Döntések szükségesek
- ⭐⭐ **Kommunikáció** - Vegyes (in-process + HTTP)
- ⭐⭐ **Deployment** - Több service, de kevesebb
- ⭐⭐ **Testing** - Vegyes komplexitás
- ⭐⭐ **Monitoring** - Több service, de kevesebb

### Infrastruktúra Bonyolultság

**Opció 1:**
- ⭐⭐⭐ Több Azure App Service vagy AKS pod
- ⭐⭐⭐ Service discovery
- ⭐⭐⭐ Load balancing
- ⭐⭐ API Gateway konfiguráció

**Opció 2:**
- ⭐ Egy Azure App Service
- ⭐ Egyszerűbb infrastruktúra
- ⭐ Egyszerűbb monitoring

**Opció 3:**
- ⭐⭐ Több service, de kevesebb
- ⭐⭐ Közepes komplexitás

### Működtetési Bonyolultság

**Opció 1:**
- ⭐⭐⭐ Több service monitoring
- ⭐⭐⭐ Distributed logging
- ⭐⭐⭐ Error tracking (több service)
- ⭐⭐ Deployment koordináció

**Opció 2:**
- ⭐⭐ Egyszerűbb monitoring
- ⭐⭐ Centralizált logging
- ⭐⭐ Egyszerűbb error tracking

**Opció 3:**
- ⭐⭐ Közepes komplexitás

## Moduláris Felépítés + Multi-Tenant

### Modul Aktiválás per Tenant

**Adatbázis:**
```sql
tenant_modules
├── tenant_id (UUID, FOREIGN KEY -> tenants.id)
├── module_name (VARCHAR) -- 'ticket', 'project', 'report', stb.
├── is_active (BOOLEAN, DEFAULT false)
├── activated_at (TIMESTAMP, NULLABLE)
├── subscription_tier (VARCHAR, NULLABLE)
└── UNIQUE (tenant_id, module_name)
```

**Aktiválás logika:**
- Tenant regisztrációkor alapértelmezett modulok aktiválása
- Subscription tier alapján modulok aktiválása
- Adminisztrátor manuális aktiválás

**API:**
```
GET    /api/v1/tenants/:id/modules
POST   /api/v1/tenants/:id/modules/:module/activate
DELETE /api/v1/tenants/:id/modules/:module/deactivate
```

## Választott Megközelítés

✅ **Opció 1 - Moduláris Mikroszolgáltatások** lett kiválasztva.

### Döntés Indoklása
1. ✅ **Már mikroszolgáltatásokkal tervezünk** - Nem kell változtatni
2. ✅ **Modul = Service** - Egyszerű mapping
3. ✅ **Független fejlesztés** - Külön csapatok
4. ✅ **Független skálázás** - Modul-onként
5. ✅ **Modul specifikus optimalizálás** - Adatbázis, cache, stb.

### Implementációs Következmények
- Minden modul külön mikroszolgáltatás
- Modul aktiválás per tenant
- Service-to-service kommunikáció
- Modul specifikus deployment és skálázás

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

### Modul Aktiválás Implementáció

**1. Modul Registry Service:**
```java
@Service
public class ModuleRegistryService {
    public boolean isModuleActive(String moduleName, UUID tenantId) {
        // Check tenant_modules table
        // Check subscription tier
        // Check feature flags
    }
}
```

**2. API Gateway Routing:**
- Modul aktiválás ellenőrzése
- Ha modul nem aktív → 403 Forbidden
- Routing csak aktív modulokhoz

**3. Frontend Modul Check:**
```typescript
// API call előtt
if (!await checkModuleActive('project', tenantId)) {
    showModuleNotAvailable();
    return;
}
```

## Bonyolultság Összefoglalás

### Mennyire Bonyolítja Meg?

**Rövid válasz:** ⭐⭐ **Közepes komplexitás növekedés**

**Részletesen:**

**Nem bonyolítja jelentősen, mert:**
- ✅ Már mikroszolgáltatásokkal tervezünk → Modul = Service mapping egyszerű
- ✅ Modul aktiválás = egyszerű database check
- ✅ Feature flags = standard pattern
- ✅ Modul kommunikáció = már tervezett (service-to-service)

**Bonyolítja, mert:**
- ⚠️ Modul aktiválás logika implementálása
- ⚠️ Modul függőségek kezelése
- ⚠️ Modul specifikus monitoring
- ⚠️ Modul specifikus deployment

**Komplexitás növekedés:**
- **Fejlesztés:** +15-20% (modul aktiválás, feature flags)
- **Infrastruktúra:** +0% (már mikroszolgáltatások)
- **Működtetés:** +10% (modul monitoring)

## Implementációs Lépések

### 1. Modul Definíciók

**Modulok listája:**
- `ticket` - Support ticketek
- `project` - Fejlesztési projektek
- `user` - Felhasználó kezelés
- `auth` - Autentikáció
- `notification` - Értesítések
- `file` - Fájl kezelés
- `report` - Jelentések (opcionális)

### 2. Modul Aktiválás Adatbázis

```sql
CREATE TABLE tenant_modules (
    tenant_id UUID NOT NULL,
    module_name VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT false,
    activated_at TIMESTAMP,
    subscription_tier VARCHAR(50),
    PRIMARY KEY (tenant_id, module_name),
    FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);
```

### 3. Modul Check Middleware/Interceptor

**Java (Quarkus):**
```java
@Interceptor
@Priority(1000)
public class ModuleCheckInterceptor {
    @AroundInvoke
    public Object checkModule(InvocationContext context) {
        ModuleRequired annotation = // get annotation
        if (annotation != null) {
            if (!moduleService.isActive(annotation.value(), tenantId)) {
                throw new ModuleNotActiveException();
            }
        }
        return context.proceed();
    }
}
```

### 4. Frontend Modul Check

**React Hook:**
```typescript
export function useModule(moduleName: string) {
    const { tenant } = useTenant();
    const [isActive, setIsActive] = useState(false);
    
    useEffect(() => {
        checkModuleActive(moduleName, tenant.id)
            .then(setIsActive);
    }, [moduleName, tenant.id]);
    
    return { isActive };
}
```

## Következő Lépések

1. **Modul lista véglegesítése** - Melyik modulok lesznek
2. **Modul aktiválás stratégia** - Tenant-onként vagy tier alapján
3. **Modul check implementáció** - Backend és frontend
4. **Modul dokumentáció** - Minden modul API dokumentációja
5. **Modul tesztelés** - Modul aktiválás/deaktiválás tesztek

## Összefoglalás

**Moduláris felépítés:**
- ✅ **Jó illeszkedés** - Már mikroszolgáltatásokkal tervezünk
- ✅ **Közepes komplexitás** - Nem jelentősen bonyolítja
- ✅ **Rugalmas** - Modulok opcionálisan aktiválhatók
- ✅ **Skálázható** - Modul-onként skálázható

**Választott megoldás:** ✅ **Opció 1 - Moduláris mikroszolgáltatások** - Modul = Service mapping

