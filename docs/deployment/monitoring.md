# InTicky - Monitoring és Logging

## Célközönség

Ez a dokumentum a monitoring és logging beállítását mutatja be. DevOps mérnökök számára készült.

## Monitoring Stack

**Választott eszközök:**
- **Azure Application Insights** - Application Performance Monitoring (APM)
- **Azure Monitor** - Metrikák és naplók
- **Azure Log Analytics** - Log aggregation
- **SmallRye Health** - Health checks (Quarkus)

## Azure Application Insights

### Setup

**Application Insights létrehozása:**
```bash
az monitor app-insights component create \
  --resource-group inticky-rg \
  --app inticky-insights \
  --location westeurope \
  --application-type web
```

**Instrumentation Key lekérdezése:**
```bash
az monitor app-insights component show \
  --resource-group inticky-rg \
  --app inticky-insights \
  --query instrumentationKey
```

### Backend Integráció

**Quarkus Application Insights extension:**
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-micrometer-registry-azure-monitor</artifactId>
</dependency>
```

**Konfiguráció:**
```yaml
quarkus:
  micrometer:
    export:
      azure-monitor:
        enabled: true
        instrumentation-key: ${APPLICATIONINSIGHTS_INSTRUMENTATION_KEY}
```

### Metrikák

**Automatikus metrikák:**
- Request count
- Response time
- Error rate
- HTTP status codes
- Database query times

**Custom metrikák:**
```java
@Inject
MeterRegistry meterRegistry;

public void createTicket() {
    Counter.builder("tickets.created")
        .tag("tenant", tenantId.toString())
        .register(meterRegistry)
        .increment();
}
```

## Health Checks

### Quarkus Health Checks

**SmallRye Health extension:**
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-health</artifactId>
</dependency>
```

**Health check endpoint:**
```
GET /q/health
GET /q/health/live
GET /q/health/ready
```

**Custom health check:**
```java
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {
    
    @Inject
    DataSource dataSource;
    
    @Override
    public HealthCheckResponse call() {
        try {
            dataSource.getConnection().close();
            return HealthCheckResponse.up("database");
        } catch (Exception e) {
            return HealthCheckResponse.down("database")
                .withData("error", e.getMessage());
        }
    }
}
```

## Logging

### Structured Logging

**Quarkus logging konfiguráció:**
```yaml
quarkus:
  log:
    level: INFO
    console:
      json: true
      format: "%d{yyyy-MM-dd HH:mm:ss} %-5p [%c{2.}] (%t) %s%e%n"
    category:
      "com.inticky": DEBUG
      "org.hibernate.SQL": DEBUG
```

**JSON formátum (production):**
```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "level": "INFO",
  "logger": "com.inticky.ticket.TicketService",
  "message": "Ticket created",
  "tenant_id": "123e4567-...",
  "ticket_id": "456e7890-..."
}
```

### Azure Log Analytics

**Log Analytics Workspace létrehozása:**
```bash
az monitor log-analytics workspace create \
  --resource-group inticky-rg \
  --workspace-name inticky-logs \
  --location westeurope
```

**App Service integráció:**
```bash
az monitor diagnostic-settings create \
  --resource-group inticky-rg \
  --resource /subscriptions/.../resourceGroups/inticky-rg/providers/Microsoft.Web/sites/inticky-ticket-service-staging \
  --name app-logs \
  --workspace inticky-logs \
  --logs '[{"category": "AppServiceAppLogs", "enabled": true}]'
```

## Error Tracking

### Application Insights Error Tracking

**Automatikus error tracking:**
- Exception-ök automatikusan naplózva
- Stack trace-ek
- Request context

**Custom error tracking:**
```java
@Inject
TelemetryClient telemetryClient;

public void handleError(Exception e) {
    telemetryClient.trackException(e);
}
```

## Alerting

### Azure Monitor Alerts

**Alert rule létrehozása:**
```bash
az monitor metrics alert create \
  --resource-group inticky-rg \
  --name high-error-rate \
  --scopes /subscriptions/.../resourceGroups/inticky-rg/providers/Microsoft.Web/sites/inticky-ticket-service-staging \
  --condition "avg requests/failed > 10" \
  --window-size 5m \
  --evaluation-frequency 1m \
  --action-group <action-group-id>
```

**Alert típusok:**
- High error rate
- High response time
- Low availability
- High CPU/Memory usage

## Dashboard

### Azure Dashboard

**Metrikák:**
- Request count (per service)
- Response time (p50, p95, p99)
- Error rate
- Active users
- Database query performance

**Log queries:**
```kusto
// Application Insights KQL
requests
| where timestamp > ago(1h)
| summarize count() by bin(timestamp, 5m), name
| render timechart
```

## További Információk

- [CI/CD Pipeline](./ci-cd.md)
- [Azure Setup](./azure-setup.md)
- [Environments](./environments.md)
- [Azure Application Insights dokumentáció](https://docs.microsoft.com/azure/azure-monitor/app/app-insights-overview)

