# AgentInSec-AI Library Integráció

## Áttekintés

Az **AgentInSec-AI** egy modern Java library AI chat funkcionalitáshoz Router Pattern architektúrával. A library optimalizálja az AI válaszokat releváns információk és funkciók intelligens kiválasztásával. Kétfázisú AI pipeline-t használ: Router AI + Agent AI.

## Főbb Funkciók

### 🤖 Kétfázisú AI Pipeline
- **Router AI**: Info blokkok és funkciók intelligens kiválasztása
- **Agent AI**: Válasz generálás és funkció végrehajtás
- **Kontextus optimalizálás**: Csak releváns információk használata

### 📚 Info Blokkok (Vector Store)
- **Ticket workflow**: Folyamatok és szabályok
- **SLA policies**: Válaszidők és eszkalációs szabályok
- **User permissions**: Jogosultságok és szerepkörök
- **Ticket categories**: Kategóriák és típusok

### ⚡ AI Funkciók
- **create_ticket**: Új ticket létrehozás
- **search_tickets**: Ticket keresés és szűrés
- **update_ticket_status**: Státusz módosítás
- **assign_ticket**: Ticket hozzárendelés
- **add_comment**: Kommentek hozzáadása
- **escalate_ticket**: Ticket eszkaláció

## Technikai Implementáció

### Maven Dependency

```xml
<dependency>
    <groupId>com.agentinsec</groupId>
    <artifactId>agentinsec-ai</artifactId>
    <version>1.8.0</version>
</dependency>
```

### Konfigurációs Beállítások

```yaml
agentinsec:
  api-key: ${AGENTINSEC_API_KEY:demo-key-12345}  # OpenAI/Azure API kulcs
  provider: openai                                # vagy azure
  azure:
    endpoint: ${AZURE_OPENAI_ENDPOINT:}          # Azure OpenAI endpoint
    router-deployment: gpt-router                # Router AI deployment
    agent-deployment: gpt-agent                  # Agent AI deployment
    embedding-deployment: gpt-embedding          # Embedding deployment
  vector-store:
    type: memory                                 # vagy qdrant
    qdrant:
      url: http://localhost:6333                 # Qdrant URL
      collection: ticketing_vectors              # Collection név
  debug-mode: true                              # Debug mód fejlesztéshez
```

### Környezeti Változók

```bash
# Éles környezetben állítsd be az API kulcsot
export AGENTINSEC_API_KEY="your-production-api-key"

# Opcionális: Custom endpoint
export AGENTINSEC_ENDPOINT="https://your-custom-endpoint.com/api"
```

## API Végpontok

### Biztonsági Állapot Lekérdezése
```http
GET /api/security/status
```

**Válasz:**
```json
{
  "monitoring_active": true,
  "last_check": "2024-01-15T10:30:00",
  "service_name": "AgentInsec Security Monitor",
  "version": "1.0.0"
}
```

### Biztonsági Riport
```http
GET /api/security/report
```

**Válasz:**
```json
{
  "report_generated": "2024-01-15T10:30:00",
  "monitoring_status": "ACTIVE",
  "events_logged_today": "N/A - Demo mode",
  "threats_detected": 0,
  "recommendations": "System operating normally"
}
```

### Fenyegetés-detektálás Tesztelése
```http
POST /api/security/threat-detection
Content-Type: application/json

{
  "data": "SELECT * FROM users WHERE id = 1; DROP TABLE users;",
  "user": "test_user"
}
```

**Válasz:**
```json
{
  "threat_detected": true,
  "analyzed_data": "SELECT * FROM users WHERE id = 1; DROP TABLE users;",
  "user_context": "test_user",
  "recommendation": "Potential security threat detected. Review and sanitize input."
}
```

### Monitoring Vezérlése
```http
POST /api/security/monitoring/start
POST /api/security/monitoring/stop
```

### Gyanús Aktivitás Jelentése
```http
POST /api/security/report-suspicious
Content-Type: application/json

{
  "activity_type": "UNUSUAL_ACCESS_PATTERN",
  "details": "Multiple failed login attempts from same IP",
  "user": "suspicious_user"
}
```

### Bejelentkezés Szimulációja
```http
POST /api/security/simulate-login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

## Automatikus Funkciók

### 🚀 Alkalmazás Indítás
- AgentInsec automatikusan elindul az alkalmazással
- Biztonsági monitoring aktiválódik
- Rendszer indítási esemény naplózása

### 🔄 Valós idejű Monitoring
- Minden API hívás automatikus naplózása
- Bejelentkezési kísérletek nyomon követése
- Gyanús minták automatikus detektálása

### 📝 Esemény-naplózás Típusok

| Esemény Típus | Leírás | Példa |
|---------------|--------|-------|
| `SYSTEM_START` | Alkalmazás indítás | Application started with AgentInsec monitoring |
| `SYSTEM_STOP` | Alkalmazás leállítás | Application shutting down |
| `AUTH_SUCCESS` | Sikeres bejelentkezés | User: admin |
| `AUTH_FAILURE` | Sikertelen bejelentkezés | User: invalid_user |
| `API_CALL` | API végpont hívás | Endpoint: GET /api/users, IP: 192.168.1.1 |
| `SUSPICIOUS_ACTIVITY` | Gyanús aktivitás | Activity: POTENTIAL_INJECTION, User: test_user |
| `DB_OPERATION` | Adatbázis művelet | Operation: SELECT, Table: users, User: admin |

## Integráció Más Komponensekkel

### Spring Security Integráció
```java
@Component
public class SecurityEventListener {
    
    @Autowired
    private AgentInsecService agentInsecService;
    
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        agentInsecService.logLoginAttempt(username, getClientIP(), true);
    }
}
```

### AOP Integráció Példa
```java
@Aspect
@Component
public class SecurityAspect {
    
    @Autowired
    private AgentInsecService agentInsecService;
    
    @Around("@annotation(Secured)")
    public Object logSecureMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        // Biztonsági művelet naplózása
        agentInsecService.logSecurityEvent("SECURE_METHOD_CALL", 
                                          joinPoint.getSignature().getName());
        return joinPoint.proceed();
    }
}
```

## Fejlesztési Útmutató

### 1. Dependency Hozzáadása
```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.kesmarki</groupId>
    <artifactId>agentinsec</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Konfiguráció Beállítása
```yaml
# application.yml
agentinsec:
  enabled: true
  api-key: ${AGENTINSEC_API_KEY}
  debug-mode: false  # Éles környezetben
```

### 3. Service Injektálása
```java
@Service
public class YourService {
    
    @Autowired
    private AgentInsecService agentInsecService;
    
    public void yourMethod() {
        // Biztonsági esemény naplózása
        agentInsecService.logSecurityEvent("CUSTOM_EVENT", "Your details");
    }
}
```

## Éles Környezeti Beállítások

### Teljesítmény Optimalizálás
```yaml
agentinsec:
  enabled: true
  debug-mode: false          # Éles környezetben kikapcsolni
  timeout-ms: 3000          # Rövidebb timeout
  max-retries: 2            # Kevesebb újrapróbálkozás
```

### Biztonság
- **API kulcs**: Soha ne commitold a kódba, használj környezeti változót
- **HTTPS**: Mindig HTTPS-t használj az AgentInsec endpoint-hoz
- **Naplózás**: Érzékeny adatok ne kerüljenek a naplókba

### Monitoring és Riasztások
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,security
```

## Hibaelhárítás

### Gyakori Problémák

1. **AgentInsec nem indul el**
   - Ellenőrizd az `agentinsec.enabled` beállítást
   - Nézd meg a Spring Boot naplókat

2. **API kulcs hibák**
   - Ellenőrizd a `AGENTINSEC_API_KEY` környezeti változót
   - Teszteld a demo kulccsal: `demo-key-12345`

3. **Timeout hibák**
   - Növeld a `timeout-ms` értékét
   - Ellenőrizd a hálózati kapcsolatot

### Debug Mód
```yaml
agentinsec:
  debug-mode: true
```

Debug módban részletes naplózás történik minden AgentInsec műveletről.

## Roadmap és Jövőbeli Funkciók

- 🔄 **Real-time Dashboard**: Valós idejű biztonsági dashboard
- 🤖 **AI-powered Threat Detection**: Mesterséges intelligencia alapú fenyegetés-detektálás
- 📊 **Advanced Analytics**: Fejlett biztonsági analitika és riportok
- 🔗 **Third-party Integrations**: SIEM rendszerekkel való integráció
- 📱 **Mobile Alerts**: Mobil push értesítések kritikus eseményekről

## Támogatás és Dokumentáció

- **GitHub Repository**: https://github.com/Kesmarki-Dev/agentinsec
- **Dokumentáció**: https://docs.kesmarki.dev/agentinsec
- **Support Email**: support@kesmarki.dev
- **Slack Channel**: #agentinsec-support

---

*Ez a dokumentáció az AgentInsec library 1.0.0 verziójához készült. A legfrissebb információkért látogasd meg a hivatalos dokumentációt.*
