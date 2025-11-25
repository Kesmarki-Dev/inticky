package com.kesmarki.inticky.controller;

import com.kesmarki.inticky.service.AITicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AI Chat Controller
 * 
 * Ez a controller biztosítja az AgentInSec-AI library funkcionalitásának
 * REST API végpontjait a ticketing rendszerhez.
 * 
 * Kétfázisú AI Pipeline:
 * 1. Router AI - kiválasztja a releváns info blokkokat és funkciókat
 * 2. Agent AI - generálja a választ és végrehajtja a funkciókat
 */
@RestController
@RequestMapping("/ai")
public class AIChatController {

    private final AITicketService aiTicketService;

    @Autowired
    public AIChatController(AITicketService aiTicketService) {
        this.aiTicketService = aiTicketService;
    }

    /**
     * Egyszerű AI chat - session nélkül
     * Gyors kérdések és válaszok
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> simpleChat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        
        if (message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Üzenet nem lehet üres"
            ));
        }

        try {
            String response = aiTicketService.chatWithAI(message);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", message);
            result.put("response", response);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "AI feldolgozási hiba: " + e.getMessage()
            ));
        }
    }

    /**
     * Session alapú AI chat - ajánlott ticketing rendszerhez
     * Kontextus megőrzés és személyre szabott válaszok
     */
    @PostMapping("/chat/session")
    public ResponseEntity<Map<String, Object>> sessionChat(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        String message = request.getOrDefault("message", "");
        String sessionId = request.getOrDefault("sessionId", "");
        String userId = request.getOrDefault("userId", "anonymous");
        
        // Session ID generálása ha nincs megadva
        if (sessionId.trim().isEmpty()) {
            sessionId = "session-" + UUID.randomUUID().toString().substring(0, 8);
        }
        
        if (message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Üzenet nem lehet üres"
            ));
        }

        try {
            String response = aiTicketService.chatWithAI(sessionId, userId, message);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("sessionId", sessionId);
            result.put("userId", userId);
            result.put("message", message);
            result.put("response", response);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "AI feldolgozási hiba: " + e.getMessage(),
                "sessionId", sessionId
            ));
        }
    }

    /**
     * AI segítség ticketing műveletekhez
     * Előre definiált promptok gyakori feladatokhoz
     */
    @PostMapping("/help/{action}")
    public ResponseEntity<Map<String, Object>> getHelp(@PathVariable String action) {
        
        String helpMessage = switch (action.toLowerCase()) {
            case "create" -> "Hogyan hozok létre egy új ticketet? Milyen információkat kell megadnom?";
            case "search" -> "Hogyan kereshetek ticketeket? Milyen szűrési lehetőségeim vannak?";
            case "assign" -> "Hogyan rendelek hozzá egy ticketet egy munkatárshoz?";
            case "escalate" -> "Mikor és hogyan eszkalálok egy ticketet?";
            case "priority" -> "Hogyan határozzam meg egy ticket prioritását?";
            case "status" -> "Milyen státuszok vannak és mit jelentenek?";
            default -> "Milyen funkciókat támogat a ticketing rendszer?";
        };

        try {
            String response = aiTicketService.chatWithAI(helpMessage);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("action", action);
            result.put("helpMessage", helpMessage);
            result.put("response", response);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "AI feldolgozási hiba: " + e.getMessage()
            ));
        }
    }

    /**
     * AI által javasolt műveletek egy adott kontextusban
     */
    @PostMapping("/suggest")
    public ResponseEntity<Map<String, Object>> getSuggestions(@RequestBody Map<String, String> request) {
        String context = request.getOrDefault("context", "");
        String userRole = request.getOrDefault("userRole", "user");
        
        String suggestionPrompt = String.format(
            "Milyen műveleteket javasolsz egy %s szerepkörű felhasználónak ebben a helyzetben: %s? " +
            "Adj konkrét, végrehajtható javaslatokat.",
            userRole, context
        );

        try {
            String response = aiTicketService.chatWithAI(suggestionPrompt);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("context", context);
            result.put("userRole", userRole);
            result.put("suggestions", response);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "AI feldolgozási hiba: " + e.getMessage()
            ));
        }
    }

    /**
     * AI státusz és konfiguráció információk
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAIStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "AgentInSec-AI Ticketing System");
        status.put("version", "1.8.0");
        status.put("status", "active");
        status.put("features", new String[]{
            "Ticket Creation", "Ticket Search", "Status Updates", 
            "Assignment", "Comments", "Escalation", "Help System"
        });
        status.put("architecture", "Router AI + Agent AI (Kétfázisú Pipeline)");
        status.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(status);
    }

    /**
     * AI képességek és elérhető funkciók listája
     */
    @GetMapping("/capabilities")
    public ResponseEntity<Map<String, Object>> getCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        
        capabilities.put("info_blocks", new String[]{
            "ticket_workflow", "sla_policies", "user_permissions", "ticket_categories"
        });
        
        capabilities.put("functions", new String[]{
            "create_ticket", "search_tickets", "update_ticket_status", 
            "assign_ticket", "add_comment", "escalate_ticket"
        });
        
        capabilities.put("chat_modes", new String[]{
            "simple_chat", "session_chat", "contextual_help", "suggestions"
        });
        
        capabilities.put("supported_languages", new String[]{"Hungarian", "English"});
        
        return ResponseEntity.ok(capabilities);
    }

    /**
     * Példa kérések és válaszok demonstrációhoz
     */
    @GetMapping("/examples")
    public ResponseEntity<Map<String, Object>> getExamples() {
        Map<String, Object> examples = new HashMap<>();
        
        examples.put("ticket_creation", Map.of(
            "request", "Hozz létre egy új ticketet: 'Email probléma' címmel, leírás: 'Nem tudok emailt küldeni'",
            "expected_action", "create_ticket function call"
        ));
        
        examples.put("ticket_search", Map.of(
            "request", "Keress rá az összes nyitott ticketre",
            "expected_action", "search_tickets function call with status=OPEN"
        ));
        
        examples.put("help_request", Map.of(
            "request", "Hogyan működik a ticket prioritás?",
            "expected_action", "Info block retrieval about priorities"
        ));
        
        examples.put("assignment", Map.of(
            "request", "Rendeld hozzá a TKT-12345 ticketet Kovács Péterhez",
            "expected_action", "assign_ticket function call"
        ));
        
        return ResponseEntity.ok(examples);
    }
}
