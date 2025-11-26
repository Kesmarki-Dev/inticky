package com.kesmarki.inticky.ai.service;

import com.agentinsec.ai.AIChat;
import com.kesmarki.inticky.ai.config.AgentInSecAIConfig;
import com.kesmarki.inticky.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service for AI-powered ticket analysis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketAnalysisService {

    private final AgentInSecAIConfig aiConfig;
    private final WebClient.Builder webClientBuilder;

    // Patterns for keyword extraction
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");
    private static final Pattern ERROR_CODE_PATTERN = Pattern.compile("\\b(error|err|exception)\\s*:?\\s*\\d+\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Comprehensive ticket analysis
     */
    public Map<String, Object> analyzeTicket(UUID ticketId, String title, String description) {
        String tenantId = TenantContext.getTenantId();
        log.info("Performing comprehensive analysis for ticket: {} in tenant: {}", ticketId, tenantId);

        try {
            AIChat aiChat = aiConfig.getTenantAIChat(tenantId);

            // Prepare analysis prompt
            String analysisPrompt = String.format("""
                    Please analyze this support ticket and provide structured insights:
                    
                    Title: %s
                    Description: %s
                    
                    Provide analysis in the following format:
                    1. Category: [TECHNICAL/ACCOUNT/FEATURE_REQUEST/BUG/SUPPORT/DOCUMENTATION/SECURITY/PERFORMANCE/INTEGRATION/OTHER]
                    2. Priority: [CRITICAL/HIGH/MEDIUM/LOW]
                    3. Urgency: [URGENT/HIGH/MEDIUM/LOW]
                    4. Complexity: [SIMPLE/MEDIUM/COMPLEX/VERY_COMPLEX]
                    5. Estimated Resolution Time: [hours]
                    6. Required Skills: [list of skills needed]
                    7. Similar Issues: [any patterns or similar issues]
                    8. Suggested Actions: [recommended next steps]
                    9. Risk Level: [LOW/MEDIUM/HIGH/CRITICAL]
                    10. Customer Impact: [LOW/MEDIUM/HIGH/CRITICAL]
                    """, title, description);

            String analysis = aiChat.chat(analysisPrompt);

            // Parse AI response and extract structured data
            Map<String, Object> structuredAnalysis = parseAnalysisResponse(analysis);

            // Add additional analysis
            structuredAnalysis.put("sentiment", analyzeSentiment(title + " " + description));
            structuredAnalysis.put("keywords", extractKeywords(title + " " + description));
            structuredAnalysis.put("technicalIndicators", extractTechnicalIndicators(title + " " + description));
            structuredAnalysis.put("ticketId", ticketId);
            structuredAnalysis.put("tenantId", tenantId);
            structuredAnalysis.put("analysisTimestamp", java.time.LocalDateTime.now());

            return structuredAnalysis;

        } catch (Exception e) {
            log.error("Error analyzing ticket {}: {}", ticketId, e.getMessage(), e);
            return createFallbackAnalysis(title, description);
        }
    }

    /**
     * Suggest ticket category
     */
    @Cacheable(value = "ai-responses", key = "'category:' + #title.hashCode() + ':' + #description.hashCode()")
    public String suggestCategory(String title, String description) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Suggesting category for ticket in tenant: {}", tenantId);

        try {
            AIChat aiChat = aiConfig.getTenantAIChat(tenantId);

            String prompt = String.format("""
                    Based on this ticket information, suggest the most appropriate category:
                    
                    Title: %s
                    Description: %s
                    
                    Choose from: TECHNICAL, ACCOUNT, FEATURE_REQUEST, BUG, SUPPORT, DOCUMENTATION, SECURITY, PERFORMANCE, INTEGRATION, OTHER
                    
                    Respond with only the category name.
                    """, title, description);

            String response = aiChat.chat(prompt);
            return extractCategoryFromResponse(response);

        } catch (Exception e) {
            log.error("Error suggesting category: {}", e.getMessage(), e);
            return fallbackCategoryPrediction(title, description);
        }
    }

    /**
     * Suggest ticket priority
     */
    @Cacheable(value = "ai-responses", key = "'priority:' + #title.hashCode() + ':' + #description.hashCode()")
    public String suggestPriority(String title, String description) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Suggesting priority for ticket in tenant: {}", tenantId);

        try {
            AIChat aiChat = aiConfig.getTenantAIChat(tenantId);

            String prompt = String.format("""
                    Based on this ticket information, suggest the appropriate priority level:
                    
                    Title: %s
                    Description: %s
                    
                    Consider:
                    - Business impact (system down, user blocked, feature request)
                    - Urgency indicators (urgent, asap, critical, emergency)
                    - Scope (single user vs multiple users vs system-wide)
                    
                    Choose from: CRITICAL, HIGH, MEDIUM, LOW
                    
                    Respond with only the priority level.
                    """, title, description);

            String response = aiChat.chat(prompt);
            return extractPriorityFromResponse(response);

        } catch (Exception e) {
            log.error("Error suggesting priority: {}", e.getMessage(), e);
            return fallbackPriorityPrediction(title, description);
        }
    }

    /**
     * Suggest ticket assignee
     */
    public Map<String, Object> suggestAssignee(String title, String description, String category) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Suggesting assignee for ticket in tenant: {}", tenantId);

        try {
            // TODO: Integrate with User Service to get available agents and their skills
            // For now, return a mock suggestion based on category

            return Map.of(
                    "suggestedAssignee", getSuggestedAssigneeByCategory(category),
                    "confidence", 0.75,
                    "reasoning", "Based on ticket category and agent expertise",
                    "alternativeAssignees", Arrays.asList("agent2", "agent3"),
                    "category", category
            );

        } catch (Exception e) {
            log.error("Error suggesting assignee: {}", e.getMessage(), e);
            return Map.of(
                    "suggestedAssignee", "unassigned",
                    "confidence", 0.0,
                    "reasoning", "Error occurred during analysis",
                    "error", e.getMessage()
            );
        }
    }

    /**
     * Generate ticket summary
     */
    public String generateSummary(UUID ticketId) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Generating summary for ticket: {} in tenant: {}", ticketId, tenantId);

        try {
            // TODO: Fetch ticket details from Ticket Service
            // For now, return a placeholder

            AIChat aiChat = aiConfig.getTenantAIChat(tenantId);

            String prompt = String.format("""
                    Generate a concise summary for ticket ID: %s
                    
                    Include:
                    - Main issue or request
                    - Current status
                    - Key actions taken
                    - Next steps
                    
                    Keep it under 200 words.
                    """, ticketId);

            return aiChat.chat(prompt);

        } catch (Exception e) {
            log.error("Error generating summary for ticket {}: {}", ticketId, e.getMessage(), e);
            return "Unable to generate summary at this time.";
        }
    }

    /**
     * Analyze sentiment of content
     */
    public Map<String, Object> analyzeSentiment(String content) {
        try {
            // Simple sentiment analysis based on keywords
            String lowerContent = content.toLowerCase();
            
            int positiveScore = countMatches(lowerContent, new String[]{
                    "thank", "please", "appreciate", "good", "great", "excellent", "satisfied", "happy"
            });
            
            int negativeScore = countMatches(lowerContent, new String[]{
                    "angry", "frustrated", "terrible", "awful", "hate", "worst", "unacceptable", 
                    "disappointed", "urgent", "critical", "emergency", "broken", "failed"
            });
            
            int neutralScore = Math.max(0, content.split("\\s+").length - positiveScore - negativeScore);
            
            String sentiment;
            double confidence;
            
            if (negativeScore > positiveScore * 2) {
                sentiment = "NEGATIVE";
                confidence = Math.min(0.9, 0.5 + (negativeScore * 0.1));
            } else if (positiveScore > negativeScore * 2) {
                sentiment = "POSITIVE";
                confidence = Math.min(0.9, 0.5 + (positiveScore * 0.1));
            } else {
                sentiment = "NEUTRAL";
                confidence = 0.6;
            }
            
            return Map.of(
                    "sentiment", sentiment,
                    "confidence", confidence,
                    "positiveScore", positiveScore,
                    "negativeScore", negativeScore,
                    "neutralScore", neutralScore
            );
            
        } catch (Exception e) {
            log.error("Error analyzing sentiment: {}", e.getMessage(), e);
            return Map.of(
                    "sentiment", "NEUTRAL",
                    "confidence", 0.0,
                    "error", e.getMessage()
            );
        }
    }

    /**
     * Extract keywords from content
     */
    public String[] extractKeywords(String content) {
        try {
            // Simple keyword extraction
            String[] commonWords = {"the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "can", "this", "that", "these", "those", "i", "you", "he", "she", "it", "we", "they", "me", "him", "her", "us", "them"};
            
            return Arrays.stream(content.toLowerCase().split("\\W+"))
                    .filter(word -> word.length() > 3)
                    .filter(word -> !Arrays.asList(commonWords).contains(word))
                    .distinct()
                    .limit(10)
                    .toArray(String[]::new);
                    
        } catch (Exception e) {
            log.error("Error extracting keywords: {}", e.getMessage(), e);
            return new String[0];
        }
    }

    /**
     * Generate response suggestion
     */
    public String generateResponseSuggestion(UUID ticketId, String context) {
        String tenantId = TenantContext.getTenantId();
        log.debug("Generating response suggestion for ticket: {} in tenant: {}", ticketId, tenantId);

        try {
            AIChat aiChat = aiConfig.getTenantAIChat(tenantId);

            String prompt = String.format("""
                    Generate a professional response template for this support ticket:
                    
                    Context: %s
                    
                    The response should:
                    - Be professional and empathetic
                    - Address the customer's concern
                    - Provide clear next steps
                    - Include appropriate placeholders for specific details
                    
                    Keep it concise but helpful.
                    """, context);

            return aiChat.chat(prompt);

        } catch (Exception e) {
            log.error("Error generating response suggestion for ticket {}: {}", ticketId, e.getMessage(), e);
            return "Thank you for contacting support. We have received your request and will investigate the issue. We will update you as soon as we have more information.";
        }
    }

    /**
     * Extract technical indicators from content
     */
    private Map<String, Object> extractTechnicalIndicators(String content) {
        return Map.of(
                "hasErrorCodes", ERROR_CODE_PATTERN.matcher(content).find(),
                "hasEmails", EMAIL_PATTERN.matcher(content).find(),
                "hasUrls", URL_PATTERN.matcher(content).find(),
                "hasStackTrace", content.toLowerCase().contains("stacktrace") || content.contains("    at "),
                "hasLogEntries", content.contains("[") && content.contains("]") && content.contains(":"),
                "technicalTerms", countTechnicalTerms(content)
        );
    }

    /**
     * Parse AI analysis response into structured data
     */
    private Map<String, Object> parseAnalysisResponse(String response) {
        // Simple parsing - in production, use more robust parsing
        return Map.of(
                "rawAnalysis", response,
                "category", extractValueFromResponse(response, "Category:"),
                "priority", extractValueFromResponse(response, "Priority:"),
                "urgency", extractValueFromResponse(response, "Urgency:"),
                "complexity", extractValueFromResponse(response, "Complexity:"),
                "estimatedHours", extractValueFromResponse(response, "Estimated Resolution Time:"),
                "requiredSkills", extractValueFromResponse(response, "Required Skills:"),
                "riskLevel", extractValueFromResponse(response, "Risk Level:"),
                "customerImpact", extractValueFromResponse(response, "Customer Impact:")
        );
    }

    /**
     * Create fallback analysis when AI fails
     */
    private Map<String, Object> createFallbackAnalysis(String title, String description) {
        return Map.of(
                "category", fallbackCategoryPrediction(title, description),
                "priority", fallbackPriorityPrediction(title, description),
                "urgency", "MEDIUM",
                "complexity", "MEDIUM",
                "estimatedHours", "4",
                "riskLevel", "MEDIUM",
                "customerImpact", "MEDIUM",
                "fallback", true,
                "error", "AI analysis unavailable"
        );
    }

    // Helper methods
    private String extractCategoryFromResponse(String response) {
        String[] validCategories = {"TECHNICAL", "ACCOUNT", "FEATURE_REQUEST", "BUG", "SUPPORT", "DOCUMENTATION", "SECURITY", "PERFORMANCE", "INTEGRATION", "OTHER"};
        for (String category : validCategories) {
            if (response.toUpperCase().contains(category)) {
                return category;
            }
        }
        return "SUPPORT";
    }

    private String extractPriorityFromResponse(String response) {
        String[] validPriorities = {"CRITICAL", "HIGH", "MEDIUM", "LOW"};
        for (String priority : validPriorities) {
            if (response.toUpperCase().contains(priority)) {
                return priority;
            }
        }
        return "MEDIUM";
    }

    private String fallbackCategoryPrediction(String title, String description) {
        String content = (title + " " + description).toLowerCase();
        if (content.contains("bug") || content.contains("error") || content.contains("broken")) return "BUG";
        if (content.contains("feature") || content.contains("enhancement")) return "FEATURE_REQUEST";
        if (content.contains("account") || content.contains("login") || content.contains("password")) return "ACCOUNT";
        if (content.contains("security") || content.contains("hack") || content.contains("breach")) return "SECURITY";
        if (content.contains("slow") || content.contains("performance") || content.contains("timeout")) return "PERFORMANCE";
        return "SUPPORT";
    }

    private String fallbackPriorityPrediction(String title, String description) {
        String content = (title + " " + description).toLowerCase();
        if (content.contains("critical") || content.contains("urgent") || content.contains("emergency") || content.contains("down")) return "CRITICAL";
        if (content.contains("important") || content.contains("asap") || content.contains("high")) return "HIGH";
        if (content.contains("low") || content.contains("minor") || content.contains("enhancement")) return "LOW";
        return "MEDIUM";
    }

    private String getSuggestedAssigneeByCategory(String category) {
        return switch (category) {
            case "TECHNICAL", "BUG", "PERFORMANCE" -> "tech-team";
            case "SECURITY" -> "security-team";
            case "ACCOUNT" -> "account-team";
            case "FEATURE_REQUEST" -> "product-team";
            default -> "support-team";
        };
    }

    private int countMatches(String content, String[] keywords) {
        int count = 0;
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                count++;
            }
        }
        return count;
    }

    private int countTechnicalTerms(String content) {
        String[] techTerms = {"api", "database", "server", "error", "exception", "log", "debug", "configuration", "deployment", "integration"};
        return countMatches(content.toLowerCase(), techTerms);
    }

    private String extractValueFromResponse(String response, String key) {
        try {
            int startIndex = response.indexOf(key);
            if (startIndex == -1) return "N/A";
            
            startIndex += key.length();
            int endIndex = response.indexOf("\n", startIndex);
            if (endIndex == -1) endIndex = response.length();
            
            return response.substring(startIndex, endIndex).trim();
        } catch (Exception e) {
            return "N/A";
        }
    }
}
