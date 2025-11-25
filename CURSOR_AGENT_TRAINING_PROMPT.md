# AgentInSec AI Library - Cursor Agent Oktatási Prompt

## Bevezetés
Az AgentInSec egy modern Java library AI chat funkcionalitáshoz Router Pattern architektúrával. A library optimalizálja az AI válaszokat releváns információk és funkciók intelligens kiválasztásával.

## GitHub Repository Információk
- **Repository URL**: https://github.com/Kesmarki-Dev/agentinsec
- **Branch**: main (production), dev (fejlesztés)
- **Package**: com.agentinsec:agentinsec-ai
- **Verzió**: 1.8.0
- **Java verzió**: 17+

## Architektúra - Kétfázisú AI Pipeline

```
Felhasználói kérés
       ↓
┌─────────────────┐
│   Router AI     │ ◄─── Info Blocks (Vector Store)
│  - Info Select  │ ◄─── Functions Registry
│  - Func Select  │
└──────┬──────────┘
       │ SelectedContext
       ▼
┌─────────────────┐
│   Agent AI      │ ◄─── Selected Info Blocks
│  - Válasz gen.  │ ◄─── Selected Functions
│  - Func exec    │
└──────┬──────────┘
       │
       ▼
   Válasz
```

## Főbb Komponensek és Package Struktúra

### Core AI Komponensek (`com.agentinsec.ai`)
- `AIChat` - Fő koordinátor osztály
- `AIChatBuilder` - Builder pattern inicializáláshoz
- `RouterAI` / `AgentAI` - Interfészek
- `impl.DefaultRouterAI` / `impl.AzureRouterAI` - Router implementációk
- `impl.DefaultAgentAI` / `impl.AzureAgentAI` - Agent implementációk
- `AsyncAIChat` - Aszinkron chat műveletek

### Vector Store és Embedding (`com.agentinsec.vector`)
- `VectorStore` - Interfész vektor tároláshoz
- `EmbeddingProvider` - Interfész embedding generáláshoz
- `impl.InMemoryVectorStore` - In-memory tároló
- `impl.QdrantVectorStore` - Qdrant database tároló
- `impl.OpenAIEmbeddingProvider` / `impl.AzureEmbeddingProvider`

### Registry és Toolok (`com.agentinsec.registry`)
- `FunctionRegistry` - Funkciók regisztrálása
- `FunctionDescriptor` - Funkció leírása
- `Tool` - Tool interfész
- `ParameterSchema` - Paraméter séma

### Security (`com.agentinsec.security`)
- `PermissionChecker` - Jogosultsági ellenőrzés
- `ConfirmationProvider` - Megerősítés kérés
- `ExecutionPlanProvider` - Execution plan generálás
- `impl.DatabaseConfirmationProvider` - Adatbázis alapú megerősítés

### Context és DTOs (`com.agentinsec.context`)
- `InfoBlock` - Információ blokk
- `SelectedContext` - Router AI választott kontextus
- `ConversationContext` - Beszélgetés kontextus
- `ConversationHistoryManager` - History kezelés

## Használati Minták

### 1. Alapvető Inicializálás (Factory metódusok - LEGEGYSZERŰBB)

```java
// OpenAI - egy sorban
AIChat openAIChat = AIChatBuilder.forOpenAI("your-openai-api-key");

// Azure - külön deployment nevekkel
AIChat azureChat = AIChatBuilder.forAzure(
    "https://your-resource.openai.azure.com/",
    "your-azure-api-key",
    "gpt-router",      // Router deployment
    "gpt-agent",       // Agent deployment
    "gpt-embedding"    // Embedding deployment
);

// Azure - közös deployment
AIChat azureChatSimple = AIChatBuilder.forAzure(
    "https://your-resource.openai.azure.com/",
    "your-azure-api-key",
    "gpt-4"  // Közös deployment name
);
```

### 2. Info Blokkok és Funkciók

```java
// Info blokk hozzáadása
InfoBlock block = InfoBlock.builder()
    .id("block_1")
    .title("Matematika")
    .content("A rendszer támogatja a matematikai műveleteket...")
    .category("math")
    .tag("math")
    .shouldChunk(true)      // Nagy szövegek chunking-ja
    .chunkSize(500)
    .build();

aiChat.addInfoBlock(block);

// Funkció regisztrálása
FunctionDescriptor func = FunctionDescriptor.builder()
    .name("calculate")
    .description("Két szám összeadása")
    .category("math")
    .requiredPermission("user")  // Biztonsági ellenőrzés
    .isDangerous(false)
    .tool(calculatorTool)
    .build();

aiChat.registerFunction(func);
```

### 3. Chat Használat

```java
// Alapvető chat
String response = aiChat.chat("Mi az összeadás?");

// Session alapú chat (history-val)
String response = aiChat.chatInSession(sessionId, userId, "Mi az összeadás?");

// Dinamikus context-tel
List<InfoBlock> contextBlocks = loadDynamicBlocks();
String response = aiChat.chatWithContext(userId, "Kérdés", contextBlocks);

// Router döntés követése
aiChat.chat("Mennyi 5 + 3?", (SelectedContext context) -> {
    System.out.println("Kiválasztott blokkok: " + context.getRelevantInfoBlockIds());
    System.out.println("Kiválasztott funkciók: " + context.getRelevantFunctionNames());
});
```

## Speciális Funkciók

### 1. ToolBuilder (1.6.0+) - 70-80% kevesebb kód
```java
// Régi módszer (sok boilerplate)
Tool oldTool = new Tool() { /* ... sok kód ... */ };

// Új ToolBuilder módszer
aiChat.registerTool(
    ToolBuilder.create("calculate")
        .description("Számítás")
        .category("math")
        .requiredString("operation", "Művelet típusa")
        .optionalNumber("precision", "Pontosság", 2)
        .execute(params -> {
            // Lambda-based execution
            return Map.of("result", calculate(params));
        })
);
```

### 2. ToolWorkflow - Tool Composition
```java
// Workflow létrehozása több tool összekötésével
ToolWorkflow workflow = ToolWorkflow.builder("user_management")
    .description("Felhasználó kezelési workflow")
    .step("validate_user", "Felhasználó validálás")
    .step("update_profile", "Profil frissítés") 
    .step("send_notification", "Értesítés küldés")
    .build();

aiChat.registerWorkflow(workflow);
```

### 3. Biztonsági Funkciók
```java
// Jogosultsági ellenőrzés
DefaultPermissionChecker permissionChecker = new DefaultPermissionChecker();
permissionChecker.addPermission("user1", "admin");

// Veszélyes műveletek megerősítése
DefaultConfirmationProvider confirmationProvider = new DefaultConfirmationProvider(
    (userId, action, reason) -> askUserConfirmation()
);

AIChat aiChat = AIChatBuilder.create()
    .withConfig(config)
    .withPermissionChecker(permissionChecker)
    .withConfirmationProvider(confirmationProvider)
    .build();
```

### 4. Qdrant Vector Store (Perzisztens tárolás)
```java
// Qdrant használata
VectorStore qdrantStore = new QdrantVectorStore(
    embeddingProvider,
    "http://localhost:6333",
    "agentinsec_vectors"
);

AIChat aiChat = AIChatBuilder.create()
    .withConfig(config)
    .withVectorStore(qdrantStore)
    .build();
```

### 5. Spring Boot Integráció
```java
@Service
public class ChatService {
    private final AIChat aiChat;
    
    @Autowired
    public ChatService(@Value("${openai.api.key}") String apiKey) {
        this.aiChat = AIChatBuilder.forOpenAI(apiKey);
        initializeGlobalInfoBlocks();
    }
    
    @PostMapping("/api/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        List<InfoBlock> dynamicBlocks = loadDynamicBlocks(request);
        String response = aiChat.chatWithContext(
            request.getUserId(),
            request.getMessage(),
            dynamicBlocks
        );
        return ResponseEntity.ok(response);
    }
}
```

## Fejlesztési Konvenciók

### Builder Pattern
- Minden komplex objektum Builder pattern-nel
- Factory metódusok egyszerű esetekhez

### Thread Safety
- Core komponensek thread-safe (ConcurrentHashMap)
- AsyncAIChat CompletableFuture-tel

### Error Handling
- `AgentInSecException` - Library specifikus kivételek
- `SecurityException` - Biztonsági hibák

### Azure vs OpenAI
- **Azure SDK mód** (`useAzureSDK(true)`): Közvetlen HTTP, teljes API verzió
- **LangChain4j mód**: LangChain4j integráció

## Tesztelés

### Teszt Struktúra
- `src/test/java/com/agentinsec/` - Teszt osztályok
- H2 in-memory adatbázis teszteléshez
- Valódi Azure OpenAI API kulcsokkal integrációs tesztek

### Teszt Futtatás
```bash
mvn test                    # Összes teszt
mvn test -Dtest=ClassName   # Konkrét teszt osztály
```

## Git Munkafolyamat

### Fejlesztési Ágak
- `main` - Production-ready kód
- `dev` - Fejlesztési ág (minden módosítás először ide!)

### Commit Konvenciók
- `feat: új funkció hozzáadása`
- `fix: bug javítás`
- `docs: dokumentáció frissítése`
- `test: teszt hozzáadása/javítása`
- `refactor: kód refaktorálás`
- `chore: verzió növelése, build konfiguráció`

### Release Folyamat
1. Fejlesztés `dev` ágban
2. Tesztelés (`mvn test` - 0 failures)
3. Verzió növelése `pom.xml`-ben
4. Merge `main`-be
5. Package build és deploy
6. Tag létrehozása

## Dokumentációs Fájlok

### Főbb Útmutatók
- `README.md` - Fő dokumentáció
- `USAGE_GUIDE.md` - Részletes használati útmutató
- `OPERATION_PRINCIPLES.md` - Működési elvek
- `INTEGRATION_GUIDE.md` - IMI integrációs útmutató
- `SPRING_BOOT_INTEGRATION.md` - Spring Boot integráció

### Speciális Funkciók
- `TOOL_BUILDER_GUIDE.md` - ToolBuilder használat
- `TOOL_WORKFLOW_GUIDE.md` - ToolWorkflow használat
- `TOOL_SUGGESTION_GUIDE.md` - ToolSuggestion rendszer
- `ASYNC_CONFIRMATION_GUIDE.md` - Aszinkron megerősítés
- `MULTI_STEP_LIST_SLICING.md` - Lista slicing multi-step execution-ben

### Optimalizálás és Teljesítmény
- `QDRANT_OPTIMIZATION_GUIDE.md` - Qdrant optimalizálások
- `COST_ANALYSIS.md` - Költség elemzés
- `CACHE_STRATEGY.md` - Cache stratégia
- `CONNECTION_POOLING.md` - HTTP connection pooling

## Gyakori Hibák és Megoldások

### 1. Azure 404 hiba
- Ellenőrizd az endpoint és deployment name-eket
- Azure SDK esetén NEM kell `/openai` az endpoint-ban

### 2. Thread safety hiba
- Használj `ConcurrentHashMap`-et
- Core komponensek már thread-safe-ek

### 3. Megerősítés nem működik
- Ellenőrizd, hogy `ExecutionPlanProvider` be van-e állítva
- `isDangerous(true)` és `dangerReason` megadása szükséges

### 4. Funkció nem hívódik
- Ellenőrizd a `FunctionDescriptor` regisztrációt
- Router AI prompt-ban szerepelnie kell a funkciónak

## Példa Projektek

### Egyszerű Példák
- `src/main/java/com/agentinsec/examples/SimpleExample.java`
- `src/main/java/com/agentinsec/examples/QuickAzureTest.java`

### Komplex Példák
- `src/main/java/com/agentinsec/examples/SecurityExample.java`
- `src/main/java/com/agentinsec/examples/SpringBootIntegrationExample.java`
- `src/main/java/com/agentinsec/examples/ParallelExecutionExample.java`

## Hasznos Parancsok

### Maven
```bash
mvn clean compile          # Fordítás
mvn test                   # Tesztelés
mvn clean package          # Package készítés
mvn clean deploy           # Deploy GitHub Packages-re
```

### Git
```bash
git checkout dev           # Dev ágra váltás
git checkout -b feature/xyz # Új feature ág
git merge dev             # Dev merge main-be
```

## Támogatás és Közreműködés

- **GitHub Issues**: Hibák és feature kérések
- **Pull Requests**: Kód hozzájárulások
- **Dokumentáció**: Markdown fájlok frissítése

---

Ez a prompt tartalmazza az AgentInSec library összes lényeges információját. A Cursor agent ezzel a tudással képes lesz segíteni a library használatában, fejlesztésében és integrációjában.
