# InTicky - AI Agent Integráció (AgentInSec)

## Célközönség

Ez a dokumentum az AgentInSec AI Library integrációját mutatja be az InTicky backend-be. Backend fejlesztők számára készült.

## AgentInSec Library

**Repository:** https://github.com/Kesmarki-Dev/agentinsec

**Verzió:** 3.5.0

**Főbb funkciók:**
- Router Pattern architektúra (Router AI + Agent AI)
- Function calling (dinamikus funkciókiválasztás)
- Info blocks (kontextus információk)
- Autonomous execution
- Self-learning
- Tool discovery
- Persistent memory
- Reflection

## Maven Dependency

**pom.xml:**
```xml
<dependency>
    <groupId>com.agentinsec</groupId>
    <artifactId>agentinsec-ai</artifactId>
    <version>3.5.0</version>
</dependency>
```

## Service Struktúra

### AI Agent Service

**Package struktúra:**
```
ai-agent-service/
├── controller/
│   └── AIChatController.java
├── service/
│   ├── AIChatService.java
│   ├── ToolRegistryService.java
│   └── InfoBlockService.java
├── repository/
│   ├── ChatSessionRepository.java
│   ├── ChatMessageRepository.java
│   └── AgentMemoryRepository.java
├── model/
│   ├── ChatSession.java
│   ├── ChatMessage.java
│   └── AgentMemory.java
└── config/
    └── AgentInSecConfig.java
```

## Konfiguráció

### application.yml

```yaml
agentinsec:
  ai:
    # Azure OpenAI Configuration
    azure:
      endpoint: ${AZURE_OPENAI_ENDPOINT}
      api-key: ${AZURE_OPENAI_API_KEY}
      router-deployment: ${AZURE_ROUTER_DEPLOYMENT:gpt-router}
      agent-deployment: ${AZURE_AGENT_DEPLOYMENT:gpt-agent}
      embedding-deployment: ${AZURE_EMBEDDING_DEPLOYMENT:gpt-embedding}
    
    # Model Configuration
    router-model: gpt-4o
    agent-model: gpt-4o
    embedding-model: text-embedding-ada-002
    
    # Performance Settings
    max-info-blocks: 10
    max-functions: 5
    temperature: 0.7
    
    # Azure SDK használata (ajánlott)
    use-azure-sdk: true
    
    # Conversation History
    history:
      type: database  # database vagy memory
    
    # Qdrant Vector Store (opcionális, production-hez ajánlott)
    qdrant:
      enabled: ${QDRANT_ENABLED:false}
      url: ${QDRANT_URL:http://localhost:6333}
      collection-name: ${QDRANT_COLLECTION:inticky_vectors}
```

## Service Implementáció

### AIChatService

```java
@ApplicationScoped
public class AIChatService {
    
    @Inject
    EnhancedSmartAIChatSimple aiChat;
    
    @Inject
    TenantContext tenantContext;
    
    @Inject
    ChatSessionRepository sessionRepository;
    
    public ChatResponse chat(String userId, String message, String sessionId) {
        UUID tenantId = tenantContext.getTenantId();
        
        // Session kezelés
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            createSession(tenantId, userId, sessionId);
        }
        
        // Chat végrehajtás
        String response = aiChat.chatInSession(sessionId, userId, message);
        
        // Üzenet mentése
        saveMessage(tenantId, sessionId, userId, message, response);
        
        return new ChatResponse(response, sessionId);
    }
    
    private void createSession(UUID tenantId, String userId, String sessionId) {
        ChatSession session = new ChatSession();
        session.setTenantId(tenantId);
        session.setUserId(UUID.fromString(userId));
        session.setSessionId(sessionId);
        sessionRepository.persist(session);
    }
    
    private void saveMessage(UUID tenantId, String sessionId, String userId, 
                            String userMessage, String aiResponse) {
        // User message
        ChatMessage userMsg = new ChatMessage();
        userMsg.setTenantId(tenantId);
        userMsg.setSessionId(sessionId);
        userMsg.setUserId(UUID.fromString(userId));
        userMsg.setMessageType("user");
        userMsg.setContent(userMessage);
        // ... persist
        
        // AI response
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setTenantId(tenantId);
        aiMsg.setSessionId(sessionId);
        aiMsg.setUserId(UUID.fromString(userId));
        aiMsg.setMessageType("assistant");
        aiMsg.setContent(aiResponse);
        // ... persist
    }
}
```

## Tool Regisztráció

### Ticket Tool Példa

```java
@ApplicationScoped
public class TicketToolRegistry {
    
    @Inject
    EnhancedSmartAIChatSimple aiChat;
    
    @Inject
    TicketService ticketService;
    
    @PostConstruct
    public void registerTools() {
        // Ticket létrehozás tool
        FunctionDescriptor createTicketTool = ToolBuilder.create("create_ticket")
            .description("Létrehoz egy új support ticketet")
            .category("ticket")
            .execute(params -> {
                String title = (String) params.get("title");
                String description = (String) params.get("description");
                String priority = (String) params.getOrDefault("priority", "medium");
                
                CreateTicketDTO dto = new CreateTicketDTO();
                dto.setTitle(title);
                dto.setDescription(description);
                dto.setPriority(priority);
                
                TicketDTO ticket = ticketService.createTicket(dto);
                
                return Map.of(
                    "success", true,
                    "ticketId", ticket.getId().toString(),
                    "ticketNumber", ticket.getTicketNumber()
                );
            })
            .build();
        
        aiChat.registerFunction(createTicketTool);
        
        // Ticket státusz változtatás tool
        FunctionDescriptor updateTicketStatusTool = ToolBuilder.create("update_ticket_status")
            .description("Frissíti egy ticket státuszát")
            .category("ticket")
            .execute(params -> {
                String ticketId = (String) params.get("ticketId");
                String status = (String) params.get("status");
                
                ticketService.updateTicketStatus(UUID.fromString(ticketId), status);
                
                return Map.of("success", true, "ticketId", ticketId, "status", status);
            })
            .build();
        
        aiChat.registerFunction(updateTicketStatusTool);
    }
}
```

## Info Blocks Kezelés

### Kontextus Információk Hozzáadása

```java
@ApplicationScoped
public class InfoBlockService {
    
    @Inject
    EnhancedSmartAIChatSimple aiChat;
    
    @Inject
    TenantContext tenantContext;
    
    public void addTenantInfoBlocks() {
        UUID tenantId = tenantContext.getTenantId();
        
        // Tenant információk
        InfoBlock tenantInfo = InfoBlock.builder()
            .id("tenant_" + tenantId)
            .title("Tenant Information")
            .content("Tenant ID: " + tenantId + ", Name: ...")
            .category("tenant")
            .build();
        
        aiChat.addInfoBlock(tenantInfo);
    }
    
    public void addTicketInfoBlocks(List<Ticket> tickets) {
        for (Ticket ticket : tickets) {
            InfoBlock ticketInfo = InfoBlock.builder()
                .id("ticket_" + ticket.getId())
                .title("Ticket: " + ticket.getTitle())
                .content("Status: " + ticket.getStatus() + ", Priority: " + ticket.getPriority())
                .category("ticket")
                .build();
            
            aiChat.addInfoBlock(ticketInfo);
        }
    }
}
```

## Tenant Izoláció

### Multi-Tenant AI Agent

```java
@ApplicationScoped
public class TenantAwareAIChatService {
    
    private final Map<UUID, EnhancedSmartAIChatSimple> tenantAgents = new ConcurrentHashMap<>();
    
    @Inject
    TenantContext tenantContext;
    
    public EnhancedSmartAIChatSimple getAgentForTenant(UUID tenantId) {
        return tenantAgents.computeIfAbsent(tenantId, this::createAgentForTenant);
    }
    
    private EnhancedSmartAIChatSimple createAgentForTenant(UUID tenantId) {
        AIConfig config = AIConfig.builder()
            .useAzure(true)
            .azureEndpoint(System.getenv("AZURE_OPENAI_ENDPOINT"))
            .azureApiKey(System.getenv("AZURE_OPENAI_API_KEY"))
            .build();
        
        EnhancedSmartAIChatSimple agent = EnhancedSmartAIChatBuilderSimple.create()
            .withConfig(config)
            .build();
        
        // Tenant specifikus info blocks
        addTenantInfoBlocks(agent, tenantId);
        
        // Tenant specifikus tools
        registerTenantTools(agent, tenantId);
        
        return agent;
    }
}
```

## Qdrant Vector Store Integráció

### Qdrant Setup

**Docker Compose (lokális fejlesztés):**
```yaml
# docker-compose.yml
services:
  qdrant:
    image: qdrant/qdrant:latest
    ports:
      - "6333:6333"
      - "6334:6334"
    volumes:
      - qdrant_data:/qdrant/storage
    environment:
      - QDRANT__SERVICE__GRPC_PORT=6334

volumes:
  qdrant_data:
```

**Indítás:**
```bash
docker-compose up -d qdrant
```

**Ellenőrzés:**
```bash
curl http://localhost:6333/health
```

### Qdrant Használata AgentInSec-ben

```java
@ApplicationScoped
public class QdrantVectorStoreConfig {
    
    @Inject
    AIConfig aiConfig;
    
    public VectorStore createQdrantVectorStore() {
        // Embedding provider
        EmbeddingProvider embeddingProvider = new AzureEmbeddingProvider(
            aiConfig.getAzureEndpoint(),
            aiConfig.getAzureApiKey(),
            aiConfig.getAzureEmbeddingDeploymentName(),
            "2024-08-01-preview"
        );
        
        // Qdrant vector store
        VectorStore qdrantStore = new QdrantVectorStore(
            embeddingProvider,
            System.getenv("QDRANT_URL"),  // http://localhost:6333
            System.getenv("QDRANT_COLLECTION")  // inticky_vectors
        );
        
        return qdrantStore;
    }
}
```

### AIChat Qdrant-tal

```java
@ApplicationScoped
public class AIChatService {
    
    @Inject
    AIConfig aiConfig;
    
    @Inject
    QdrantVectorStoreConfig qdrantConfig;
    
    public EnhancedSmartAIChatSimple createAIChatWithQdrant() {
        VectorStore qdrantStore = qdrantConfig.createQdrantVectorStore();
        
        EnhancedSmartAIChatSimple agent = EnhancedSmartAIChatBuilderSimple.create()
            .withConfig(aiConfig)
            .withVectorStore(qdrantStore)  // Qdrant használata
            .build();
        
        return agent;
    }
}
```

### Qdrant vs InMemoryVectorStore

**InMemoryVectorStore (fejlesztés):**
- ✅ Gyors setup
- ✅ Nincs külső függőség
- ❌ Adatok elvesznek restart után
- ❌ Nem skálázható

**QdrantVectorStore (production):**
- ✅ Perzisztens tárolás
- ✅ Skálázható
- ✅ Thread-safe
- ✅ Production ready
- ❌ Külső szolgáltatás szükséges

## További Információk

- [AgentInSec README](https://github.com/Kesmarki-Dev/agentinsec/blob/main/README.md)
- [AgentInSec Integration Guide](https://github.com/Kesmarki-Dev/agentinsec/blob/main/INTEGRATION_GUIDE.md)
- [AgentInSec Usage Guide](https://github.com/Kesmarki-Dev/agentinsec/blob/main/USAGE_GUIDE.md)
- [Qdrant dokumentáció](https://qdrant.tech/documentation/)
- [Backend Architektúra](./architecture.md)
- [Mikroszolgáltatások](../../architecture/microservices.md)

