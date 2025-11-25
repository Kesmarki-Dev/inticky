package com.kesmarki.inticky.service;

import com.agentinsec.ai.AIChat;
import com.agentinsec.context.InfoBlock;
import com.agentinsec.registry.ToolBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AI Ticket Service
 * 
 * Ez a service osztály felelős az AgentInSec-AI library integrálásáért
 * a ticketing rendszerbe. Kétfázisú AI pipeline-t használ:
 * Router AI → Agent AI
 */
@Service
public class AITicketService {

    private final AIChat aiChat;

    @Autowired
    public AITicketService(AIChat aiChat) {
        this.aiChat = aiChat;
    }

    /**
     * Inicializálás után beállítja az AI-t ticketing funkcionalitáshoz
     */
    @PostConstruct
    public void initializeTicketingAI() {
        initializeTicketingInfoBlocks();
        registerTicketingFunctions();
    }

    /**
     * Ticketing specifikus info blokkok inicializálása
     * Ezeket a Router AI használja a releváns információk kiválasztásához
     */
    private void initializeTicketingInfoBlocks() {
        
        // Ticket workflow információk
        aiChat.addInfoBlock(InfoBlock.builder()
            .id("ticket_workflow")
            .title("Ticket Workflow és Folyamatok")
            .content("""
                Ticket Lifecycle:
                1. Létrehozás - Új ticket beérkezése vagy létrehozása
                2. Kategorizálás - Típus és prioritás meghatározása
                3. Hozzárendelés - Megfelelő szakember kijelölése
                4. Feldolgozás - Ticket megoldása
                5. Lezárás - Ticket befejezése és dokumentálása
                
                Prioritási szintek:
                - LOW: Nem sürgős, 5 munkanap
                - MEDIUM: Normál prioritás, 3 munkanap
                - HIGH: Sürgős, 1 munkanap
                - CRITICAL: Kritikus, 4 óra
                """)
            .category("workflow")
            .tag("process", "priority")
            .shouldChunk(true)
            .chunkSize(400)
            .build());

        // SLA szabályzatok
        aiChat.addInfoBlock(InfoBlock.builder()
            .id("sla_policies")
            .title("SLA Szabályzatok és Válaszidők")
            .content("""
                Service Level Agreement (SLA) szabályok:
                
                Válaszidők prioritás szerint:
                - CRITICAL: 1 óra első válasz, 4 óra megoldás
                - HIGH: 4 óra első válasz, 24 óra megoldás
                - MEDIUM: 24 óra első válasz, 72 óra megoldás
                - LOW: 72 óra első válasz, 120 óra megoldás
                
                Eszkalációs szabályok:
                - Ha a válaszidő 50%-át elérte: figyelmeztetés
                - Ha a válaszidő 80%-át elérte: automatikus eszkaláció
                - Ha a válaszidő lejárt: manager értesítés
                """)
            .category("sla")
            .tag("response-time", "escalation")
            .build());

        // Felhasználói jogosultságok
        aiChat.addInfoBlock(InfoBlock.builder()
            .id("user_permissions")
            .title("Felhasználói Jogosultságok és Szerepkörök")
            .content("""
                Szerepkörök és jogosultságok:
                
                USER (Alapfelhasználó):
                - Ticket létrehozás
                - Saját ticketek megtekintése
                - Kommentek hozzáadása saját ticketekhez
                
                AGENT (Ügyfélszolgálat):
                - Összes ticket megtekintése
                - Ticket hozzárendelés magához
                - Ticket státusz módosítása
                - Kommentek hozzáadása
                
                ADMIN (Adminisztrátor):
                - Összes funkció elérése
                - Felhasználó kezelés
                - Rendszer konfiguráció
                - Riportok generálása
                """)
            .category("permissions")
            .tag("roles", "security")
            .build());

        // Ticket kategóriák
        aiChat.addInfoBlock(InfoBlock.builder()
            .id("ticket_categories")
            .title("Ticket Kategóriák és Típusok")
            .content("""
                Fő kategóriák:
                
                TECHNICAL:
                - Szoftver hibák
                - Hardver problémák
                - Hálózati problémák
                - Teljesítmény problémák
                
                ACCOUNT:
                - Jelszó visszaállítás
                - Hozzáférési problémák
                - Fiók beállítások
                
                FEATURE_REQUEST:
                - Új funkció kérések
                - Fejlesztési javaslatok
                
                SUPPORT:
                - Általános támogatás
                - Használati kérdések
                - Dokumentáció kérések
                """)
            .category("categories")
            .tag("types", "classification")
            .build());
    }

    /**
     * Ticketing specifikus funkciók regisztrálása
     * Ezeket az Agent AI használja a műveletek végrehajtásához
     */
    private void registerTicketingFunctions() {

        // Ticket létrehozás
        aiChat.registerTool(
            ToolBuilder.create("create_ticket")
                .description("Új support ticket létrehozása a rendszerben")
                .category("ticket_management")
                .requiredString("title", "A ticket rövid, leíró címe")
                .requiredString("description", "A probléma részletes leírása")
                .optionalString("priority", "Prioritás szint (LOW, MEDIUM, HIGH, CRITICAL)", "MEDIUM")
                .optionalString("category", "Ticket kategória (TECHNICAL, ACCOUNT, FEATURE_REQUEST, SUPPORT)", "SUPPORT")
                .requiredString("reporter", "Ticket bejelentő felhasználó neve vagy ID-ja")
                .requiredPermission("user")
                .execute(params -> {
                    return createTicket(
                        params.get("title").toString(),
                        params.get("description").toString(),
                        params.get("priority").toString(),
                        params.get("category").toString(),
                        params.get("reporter").toString()
                    );
                })
        );

        // Ticket keresés
        aiChat.registerTool(
            ToolBuilder.create("search_tickets")
                .description("Ticketek keresése különböző kritériumok alapján")
                .category("ticket_search")
                .optionalString("status", "Ticket státusz (OPEN, IN_PROGRESS, RESOLVED, CLOSED)", "")
                .optionalString("priority", "Prioritás szint", "")
                .optionalString("assignee", "Hozzárendelt személy", "")
                .optionalString("reporter", "Bejelentő", "")
                .optionalString("keyword", "Kulcsszó a címben vagy leírásban", "")
                .requiredPermission("user")
                .execute(params -> {
                    return searchTickets(params);
                })
        );

        // Ticket státusz frissítés
        aiChat.registerTool(
            ToolBuilder.create("update_ticket_status")
                .description("Ticket státuszának módosítása")
                .category("ticket_management")
                .requiredString("ticketId", "A módosítandó ticket egyedi azonosítója")
                .requiredString("newStatus", "Új státusz (OPEN, IN_PROGRESS, RESOLVED, CLOSED)")
                .optionalString("comment", "Opcionális komment a státusz változáshoz", "")
                .requiredPermission("agent")
                .execute(params -> {
                    return updateTicketStatus(
                        params.get("ticketId").toString(),
                        params.get("newStatus").toString(),
                        params.get("comment").toString()
                    );
                })
        );

        // Ticket hozzárendelés
        aiChat.registerTool(
            ToolBuilder.create("assign_ticket")
                .description("Ticket hozzárendelése egy ügyfélszolgálati munkatárshoz")
                .category("ticket_management")
                .requiredString("ticketId", "A hozzárendelendő ticket ID-ja")
                .requiredString("assignee", "A munkatárs neve vagy ID-ja")
                .optionalString("comment", "Hozzárendelési megjegyzés", "")
                .requiredPermission("agent")
                .execute(params -> {
                    return assignTicket(
                        params.get("ticketId").toString(),
                        params.get("assignee").toString(),
                        params.get("comment").toString()
                    );
                })
        );

        // Komment hozzáadás
        aiChat.registerTool(
            ToolBuilder.create("add_comment")
                .description("Komment vagy válasz hozzáadása egy tickethez")
                .category("communication")
                .requiredString("ticketId", "A ticket egyedi azonosítója")
                .requiredString("comment", "A hozzáadandó komment szövege")
                .requiredString("author", "A komment írójának neve")
                .optionalString("visibility", "Komment láthatósága (PUBLIC, INTERNAL)", "PUBLIC")
                .requiredPermission("user")
                .execute(params -> {
                    return addComment(
                        params.get("ticketId").toString(),
                        params.get("comment").toString(),
                        params.get("author").toString(),
                        params.get("visibility").toString()
                    );
                })
        );

        // Ticket eszkaláció
        aiChat.registerTool(
            ToolBuilder.create("escalate_ticket")
                .description("Ticket eszkalálása magasabb szintre sürgős esetekben")
                .category("escalation")
                .requiredString("ticketId", "Az eszkalálandó ticket ID-ja")
                .requiredString("reason", "Az eszkaláció indoklása")
                .optionalString("targetLevel", "Cél eszkalációs szint (SUPERVISOR, MANAGER)", "SUPERVISOR")
                .requiredPermission("agent")
                .isDangerous(true)
                .execute(params -> {
                    return escalateTicket(
                        params.get("ticketId").toString(),
                        params.get("reason").toString(),
                        params.get("targetLevel").toString()
                    );
                })
        );
    }

    /**
     * AI Chat a ticketing rendszerhez - session alapú
     */
    public String chatWithAI(String sessionId, String userId, String message) {
        try {
            return aiChat.chatInSession(sessionId, userId, message);
        } catch (Exception e) {
            return "Sajnálom, hiba történt az AI feldolgozás során: " + e.getMessage();
        }
    }

    /**
     * Egyszerű AI chat session nélkül
     */
    public String chatWithAI(String message) {
        try {
            return aiChat.chat(message);
        } catch (Exception e) {
            return "Sajnálom, hiba történt az AI feldolgozás során: " + e.getMessage();
        }
    }

    // Ticket műveletek implementációja (demo célokra)
    
    private Map<String, Object> createTicket(String title, String description, String priority, String category, String reporter) {
        String ticketId = "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("ticketId", ticketId);
        result.put("title", title);
        result.put("status", "OPEN");
        result.put("priority", priority);
        result.put("category", category);
        result.put("reporter", reporter);
        result.put("createdAt", LocalDateTime.now().toString());
        result.put("message", "Ticket sikeresen létrehozva: " + ticketId);
        
        return result;
    }

    private Map<String, Object> searchTickets(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalFound", 0);
        result.put("tickets", new Object[0]);
        result.put("message", "Keresés végrehajtva - demo módban nincs találat");
        
        return result;
    }

    private Map<String, Object> updateTicketStatus(String ticketId, String newStatus, String comment) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("ticketId", ticketId);
        result.put("oldStatus", "OPEN");
        result.put("newStatus", newStatus);
        result.put("updatedAt", LocalDateTime.now().toString());
        result.put("message", "Ticket státusz frissítve: " + ticketId + " -> " + newStatus);
        
        return result;
    }

    private Map<String, Object> assignTicket(String ticketId, String assignee, String comment) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("ticketId", ticketId);
        result.put("assignee", assignee);
        result.put("assignedAt", LocalDateTime.now().toString());
        result.put("message", "Ticket hozzárendelve: " + ticketId + " -> " + assignee);
        
        return result;
    }

    private Map<String, Object> addComment(String ticketId, String comment, String author, String visibility) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("ticketId", ticketId);
        result.put("commentId", "CMT-" + System.currentTimeMillis());
        result.put("author", author);
        result.put("visibility", visibility);
        result.put("createdAt", LocalDateTime.now().toString());
        result.put("message", "Komment hozzáadva a tickethez: " + ticketId);
        
        return result;
    }

    private Map<String, Object> escalateTicket(String ticketId, String reason, String targetLevel) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("ticketId", ticketId);
        result.put("escalatedTo", targetLevel);
        result.put("reason", reason);
        result.put("escalatedAt", LocalDateTime.now().toString());
        result.put("message", "Ticket eszkalálva: " + ticketId + " -> " + targetLevel);
        
        return result;
    }
}
