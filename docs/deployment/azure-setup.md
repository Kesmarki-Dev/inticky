# InTicky - Azure Setup

## Célközönség

Ez a dokumentum az Azure erőforrások létrehozását és konfigurálását mutatja be. DevOps mérnökök számára készült.

## Előfeltételek

- Azure account
- Azure CLI telepítve
- Azure subscription
- Szükséges jogosultságok (Owner vagy Contributor)

## Azure CLI Beállítás

### Bejelentkezés

```bash
az login
```

### Subscription Kiválasztása

```bash
# Subscription lista
az account list

# Subscription beállítása
az account set --subscription "Your Subscription Name"
```

## Resource Group Létrehozása

### Resource Group

**Létrehozás:**
```bash
az group create \
  --name inticky-rg \
  --location westeurope
```

**Ellenőrzés:**
```bash
az group show --name inticky-rg
```

## Azure Szolgáltatások Létrehozása

### 1. Azure Database for PostgreSQL

**Flexible Server létrehozása:**
```bash
az postgres flexible-server create \
  --resource-group inticky-rg \
  --name inticky-db \
  --location westeurope \
  --admin-user intickyadmin \
  --admin-password <secure-password> \
  --sku-name Standard_B2s \
  --tier Burstable \
  --version 14 \
  --storage-size 32 \
  --public-access 0.0.0.0
```

**Firewall rule (fejlesztéshez):**
```bash
az postgres flexible-server firewall-rule create \
  --resource-group inticky-rg \
  --name inticky-db \
  --rule-name AllowLocalDev \
  --start-ip-address <your-ip> \
  --end-ip-address <your-ip>
```

**Connection string:**
```
jdbc:postgresql://inticky-db.postgres.database.azure.com:5432/inticky?sslmode=require
```

### 2. Azure Cache for Redis

**Redis cache létrehozása:**
```bash
az redis create \
  --resource-group inticky-rg \
  --name inticky-redis \
  --location westeurope \
  --sku Basic \
  --vm-size c0
```

**Connection string lekérdezése:**
```bash
az redis list-keys \
  --resource-group inticky-rg \
  --name inticky-redis
```

### 3. Azure Blob Storage

**Storage account létrehozása:**
```bash
az storage account create \
  --resource-group inticky-rg \
  --name intickystorage \
  --location westeurope \
  --sku Standard_LRS
```

**Container létrehozása:**
```bash
az storage container create \
  --account-name intickystorage \
  --name files \
  --public-access off
```

**Connection string:**
```bash
az storage account show-connection-string \
  --resource-group inticky-rg \
  --name intickystorage
```

### 4. Azure Container Registry

**ACR létrehozása:**
```bash
az acr create \
  --resource-group inticky-rg \
  --name inticky \
  --sku Basic \
  --admin-enabled true
```

**Login:**
```bash
az acr login --name inticky
```

**Credentials:**
```bash
az acr credential show --name inticky
```

### 5. Azure App Service (Backend Services)

**App Service Plan létrehozása:**
```bash
# Staging
az appservice plan create \
  --resource-group inticky-rg \
  --name inticky-staging-plan \
  --location westeurope \
  --sku S1 \
  --is-linux

# Production
az appservice plan create \
  --resource-group inticky-rg \
  --name inticky-prod-plan \
  --location westeurope \
  --sku S2 \
  --is-linux
```

**App Service létrehozása (példa: ticket-service):**
```bash
az webapp create \
  --resource-group inticky-rg \
  --plan inticky-staging-plan \
  --name inticky-ticket-service-staging \
  --deployment-container-image-name inticky.azurecr.io/ticket-service:latest
```

**Container settings:**
```bash
az webapp config container set \
  --resource-group inticky-rg \
  --name inticky-ticket-service-staging \
  --docker-custom-image-name inticky.azurecr.io/ticket-service:latest \
  --docker-registry-server-url https://inticky.azurecr.io \
  --docker-registry-server-user <acr-username> \
  --docker-registry-server-password <acr-password>
```

### 6. Azure Static Web Apps (Frontend)

**Static Web App létrehozása:**
```bash
az staticwebapp create \
  --resource-group inticky-rg \
  --name inticky-frontend-staging \
  --location westeurope \
  --sku Free
```

**Deployment token:**
```bash
az staticwebapp secrets list \
  --resource-group inticky-rg \
  --name inticky-frontend-staging
```

### 7. Azure API Management

**API Management létrehozása:**
```bash
az apim create \
  --resource-group inticky-rg \
  --name inticky-apim \
  --location westeurope \
  --sku-name Consumption \
  --publisher-email admin@inticky.com \
  --publisher-name "InTicky"
```

**Backend service hozzáadása:**
```bash
az apim backend create \
  --resource-group inticky-rg \
  --service-name inticky-apim \
  --backend-id ticket-service \
  --url https://inticky-ticket-service-staging.azurewebsites.net
```

### 8. Azure Application Insights

**Application Insights létrehozása:**
```bash
az monitor app-insights component create \
  --resource-group inticky-rg \
  --app inticky-insights \
  --location westeurope \
  --application-type web
```

**Instrumentation key:**
```bash
az monitor app-insights component show \
  --resource-group inticky-rg \
  --app inticky-insights \
  --query instrumentationKey
```

### 9. Qdrant Vector Store (AI Agent)

**Qdrant deployment opciók:**

**Opció 1: Azure Container Instances (ajánlott)**
```bash
az container create \
  --resource-group inticky-rg \
  --name inticky-qdrant \
  --image qdrant/qdrant:latest \
  --cpu 2 \
  --memory 4 \
  --ports 6333 6334 \
  --environment-variables QDRANT__SERVICE__GRPC_PORT=6334 \
  --dns-name-label inticky-qdrant
```

**Opció 2: Azure Kubernetes Service (ha AKS-t használunk)**
```yaml
# qdrant-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: qdrant
spec:
  replicas: 1
  selector:
    matchLabels:
      app: qdrant
  template:
    metadata:
      labels:
        app: qdrant
    spec:
      containers:
      - name: qdrant
        image: qdrant/qdrant:latest
        ports:
        - containerPort: 6333
        - containerPort: 6334
        env:
        - name: QDRANT__SERVICE__GRPC_PORT
          value: "6334"
        volumeMounts:
        - name: qdrant-storage
          mountPath: /qdrant/storage
      volumes:
      - name: qdrant-storage
        persistentVolumeClaim:
          claimName: qdrant-pvc
```

**Opció 3: Qdrant Cloud (managed service)**
- Regisztráció: https://cloud.qdrant.io/
- Managed Qdrant cluster létrehozása
- Connection string használata

**Connection string:**
```
http://inticky-qdrant.westeurope.azurecontainer.io:6333
```

**Environment változók:**
```bash
QDRANT_URL=http://inticky-qdrant.westeurope.azurecontainer.io:6333
QDRANT_COLLECTION=inticky_vectors
QDRANT_ENABLED=true
```

## Networking és Security

### Virtual Network (Opcionális)

**VNet létrehozása:**
```bash
az network vnet create \
  --resource-group inticky-rg \
  --name inticky-vnet \
  --address-prefix 10.0.0.0/16
```

### SSL/TLS Tanúsítványok

**App Service Managed Certificate:**
```bash
az webapp config ssl bind \
  --resource-group inticky-rg \
  --name inticky-api-gateway-staging \
  --certificate-thumbprint <thumbprint> \
  --ssl-type SNI
```

## Environment Változók Beállítása

### App Service Environment Variables

**Példa (általános service):**
```bash
az webapp config appsettings set \
  --resource-group inticky-rg \
  --name inticky-ticket-service-staging \
  --settings \
    DATASOURCE_URL="jdbc:postgresql://..." \
    DATASOURCE_USERNAME="inticky" \
    DATASOURCE_PASSWORD="..." \
    REDIS_HOST="inticky-redis.redis.cache.windows.net" \
    REDIS_PASSWORD="..." \
    BLOB_STORAGE_CONNECTION_STRING="..."
```

**Példa (AI Agent Service):**
```bash
az webapp config appsettings set \
  --resource-group inticky-rg \
  --name inticky-ai-agent-service-staging \
  --settings \
    DATASOURCE_URL="jdbc:postgresql://..." \
    DATASOURCE_USERNAME="inticky" \
    DATASOURCE_PASSWORD="..." \
    AZURE_OPENAI_ENDPOINT="https://..." \
    AZURE_OPENAI_API_KEY="..." \
    QDRANT_URL="http://inticky-qdrant.westeurope.azurecontainer.io:6333" \
    QDRANT_COLLECTION="inticky_vectors" \
    QDRANT_ENABLED="true"
```

## További Információk

- [CI/CD Pipeline](./ci-cd.md)
- [Environments](./environments.md)
- [Monitoring](./monitoring.md)
- [Azure dokumentáció](https://docs.microsoft.com/azure/)

