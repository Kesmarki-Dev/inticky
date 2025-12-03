# InTicky - Environment Konfigurációk

## Célközönség

Ez a dokumentum a különböző környezetek (Development, Staging, Production) konfigurációját mutatja be. DevOps mérnökök és fejlesztők számára készült.

## Környezetek

### 1. Development (Lokális Fejlesztés)

**Cél:** Lokális fejlesztés és tesztelés.

**Jellemzők:**
- Lokális Docker Compose
- Lokális PostgreSQL és Redis
- Hot reload
- Debug mód

**Konfiguráció:**
- Backend: `application-dev.yml`
- Frontend: `.env.development`

### 2. Staging

**Cél:** Tesztelés és validáció production előtt.

**Jellemzők:**
- Azure App Service
- Production-like infrastruktúra
- Teszt adatok
- Monitoring és logging

**Konfiguráció:**
- Backend: `application-staging.yml`
- Frontend: `.env.staging`

### 3. Production

**Cél:** Éles környezet, valódi felhasználók.

**Jellemzők:**
- Azure App Service (nagyobb tier)
- Production adatbázis
- SSL/TLS kötelező
- Monitoring és alerting
- Backup stratégia

**Konfiguráció:**
- Backend: `application-prod.yml`
- Frontend: `.env.production`

## Environment Változók

### Backend Environment Változók

**Development (application-dev.yml):**
```yaml
quarkus:
  application:
    name: ticket-service
    version: 1.0.0-SNAPSHOT
  
  http:
    port: 8080
    cors:
      origins: "http://localhost:5173"
  
  datasource:
    jdbc:
      url: jdbc:postgresql://localhost:5432/inticky
    username: inticky
    password: inticky
  
  redis:
    hosts: localhost:6379
  
  log:
    level: DEBUG
```

**Staging (application-staging.yml):**
```yaml
quarkus:
  application:
    name: ticket-service
    version: 1.0.0
  
  http:
    port: 8080
    cors:
      origins: "https://staging.inticky.com"
  
  datasource:
    jdbc:
      url: ${DATASOURCE_URL}
    username: ${DATASOURCE_USERNAME}
    password: ${DATASOURCE_PASSWORD}
  
  redis:
    hosts: ${REDIS_HOST}
    password: ${REDIS_PASSWORD}
  
  log:
    level: INFO
    console:
      json: true
```

**Production (application-prod.yml):**
```yaml
quarkus:
  application:
    name: ticket-service
    version: 1.0.0
  
  http:
    port: 8080
    cors:
      origins: "https://inticky.com"
  
  datasource:
    jdbc:
      url: ${DATASOURCE_URL}
    username: ${DATASOURCE_USERNAME}
    password: ${DATASOURCE_PASSWORD}
  
  redis:
    hosts: ${REDIS_HOST}
    password: ${REDIS_PASSWORD}
  
  log:
    level: WARN
    console:
      json: true
```

### Frontend Environment Változók

**.env.development:**
```
VITE_API_URL=http://localhost:8080/api/v1
VITE_APP_NAME=InTicky (Dev)
VITE_ENABLE_DEV_TOOLS=true
```

**.env.staging:**
```
VITE_API_URL=https://api-staging.inticky.com/api/v1
VITE_APP_NAME=InTicky (Staging)
VITE_ENABLE_DEV_TOOLS=false
```

**.env.production:**
```
VITE_API_URL=https://api.inticky.com/api/v1
VITE_APP_NAME=InTicky
VITE_ENABLE_DEV_TOOLS=false
```

## Connection String-ek

### Development

**PostgreSQL:**
```
jdbc:postgresql://localhost:5432/inticky
```

**Redis:**
```
localhost:6379
```

**Qdrant:**
```
http://localhost:6333
```

**Blob Storage:**
```
UseDefaultCredential=true;AccountName=devstorage;EndpointSuffix=core.windows.net
```

### Staging

**PostgreSQL:**
```
jdbc:postgresql://inticky-db-staging.postgres.database.azure.com:5432/inticky?sslmode=require
```

**Redis:**
```
inticky-redis-staging.redis.cache.windows.net:6380,ssl=true,password=...
```

**Qdrant:**
```
http://inticky-qdrant-staging.westeurope.azurecontainer.io:6333
```

**Blob Storage:**
```
DefaultEndpointsProtocol=https;AccountName=intickystaging;AccountKey=...;EndpointSuffix=core.windows.net
```

### Production

**PostgreSQL:**
```
jdbc:postgresql://inticky-db-prod.postgres.database.azure.com:5432/inticky?sslmode=require
```

**Redis:**
```
inticky-redis-prod.redis.cache.windows.net:6380,ssl=true,password=...
```

**Qdrant:**
```
http://inticky-qdrant-prod.westeurope.azurecontainer.io:6333
```

**Blob Storage:**
```
DefaultEndpointsProtocol=https;AccountName=intickyprod;AccountKey=...;EndpointSuffix=core.windows.net
```

## Feature Flags

### Feature Flag Konfiguráció

**Development:**
```yaml
features:
  enable-module-activation: true
  enable-time-tracking: true
  enable-reports: false
  enable-advanced-search: true
```

**Staging:**
```yaml
features:
  enable-module-activation: true
  enable-time-tracking: true
  enable-reports: true
  enable-advanced-search: true
```

**Production:**
```yaml
features:
  enable-module-activation: true
  enable-time-tracking: true
  enable-reports: true
  enable-advanced-search: true
```

## Environment Specifikus Beállítások

### Development

- **Debug mode:** Engedélyezve
- **Hot reload:** Engedélyezve
- **CORS:** Minden origin engedélyezve
- **Logging:** DEBUG szint
- **Error details:** Teljes stack trace

### Staging

- **Debug mode:** Letiltva
- **Hot reload:** Letiltva
- **CORS:** Csak staging domain
- **Logging:** INFO szint
- **Error details:** Csak error message

### Production

- **Debug mode:** Letiltva
- **Hot reload:** Letiltva
- **CORS:** Csak production domain
- **Logging:** WARN szint
- **Error details:** Csak error code
- **Rate limiting:** Aktív
- **SSL/TLS:** Kötelező

## Environment Változók Kezelése

### Azure App Service

**Environment változók beállítása:**
```bash
az webapp config appsettings set \
  --resource-group inticky-rg \
  --name inticky-ticket-service-staging \
  --settings \
    ENVIRONMENT=staging \
    DATASOURCE_URL="..." \
    REDIS_HOST="..."
```

**Environment változók listázása:**
```bash
az webapp config appsettings list \
  --resource-group inticky-rg \
  --name inticky-ticket-service-staging
```

### Secrets Kezelése

**Azure Key Vault (ajánlott):**
```bash
# Key Vault létrehozása
az keyvault create \
  --resource-group inticky-rg \
  --name inticky-keyvault \
  --location westeurope

# Secret hozzáadása
az keyvault secret set \
  --vault-name inticky-keyvault \
  --name datasource-password \
  --value <password>
```

**App Service Key Vault integráció:**
```bash
az webapp config appsettings set \
  --resource-group inticky-rg \
  --name inticky-ticket-service-staging \
  --settings \
    @Microsoft.KeyVault(SecretUri=https://inticky-keyvault.vault.azure.net/secrets/datasource-password/)
```

## További Információk

- [CI/CD Pipeline](./ci-cd.md)
- [Azure Setup](./azure-setup.md)
- [Monitoring](./monitoring.md)

