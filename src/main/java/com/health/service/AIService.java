package com.health.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.model.HealthReport;
import com.health.model.UserProfile;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

public class AIService {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-opus-4-6";
    private static final MediaType JSON_MEDIA = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper mapper;
    private final String apiKey;

    public AIService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient.Builder()
                .callTimeout(java.time.Duration.ofSeconds(60))
                .build();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    /**
     * Analyze all reports and return lifestyle suggestions.
     */
    public String analyzeHealthTrends(UserProfile profile, List<HealthReport> reports) throws IOException {
        String prompt = buildAnalysisPrompt(profile, reports);
        return callClaude(prompt);
    }

    /**
     * Ask a specific health question.
     */
    public String askHealthQuestion(UserProfile profile, List<HealthReport> reports, String question) throws IOException {
        String context = buildContextSummary(profile, reports);
        String prompt = String.format("""
                You are a knowledgeable, empathetic health advisor. Here is the user's health context:
                
                %s
                
                The user asks: %s
                
                Answer clearly and helpfully. Always remind them to consult a licensed doctor for medical decisions.
                """, context, question);
        return callClaude(prompt);
    }

    /**
     * Generate a weekly health goal based on recent data.
     */
    public String generateWeeklyGoals(UserProfile profile, List<HealthReport> reports) throws IOException {
        String context = buildContextSummary(profile, reports);
        String prompt = String.format("""
                You are a supportive health coach. Based on this user's health data:
                
                %s
                
                Generate 3–5 specific, actionable weekly health goals tailored to their profile and recent reports.
                Format each goal with:
                - Goal title
                - Why it matters for them specifically
                - How to achieve it (concrete steps)
                
                Be motivating, realistic, and personalized.
                """, context);
        return callClaude(prompt);
    }

    /**
     * Summarize a single report in plain language.
     */
    public String summarizeReport(UserProfile profile, HealthReport report) throws IOException {
        String prompt = String.format("""
                You are a health educator. Explain the following health report in simple, plain language
                that the patient can understand. Highlight any values that may be concerning and what they mean.
                
                Patient: %s, Age %d, %s, Blood Group: %s
                Existing conditions: %s
                
                Report Type: %s
                Date: %s
                Doctor: %s
                Metrics:
                %s
                Notes: %s
                
                Provide a clear, friendly explanation. Note any values outside normal ranges and suggest
                questions the patient might want to ask their doctor.
                """,
                profile != null ? profile.getName() : "Unknown",
                profile != null ? profile.getAge() : 0,
                profile != null ? profile.getGender() : "Unknown",
                profile != null ? profile.getBloodGroup() : "Unknown",
                profile != null ? profile.getExistingConditions() : "None",
                report.getReportType(),
                report.getDate(),
                report.getDoctorName() != null ? report.getDoctorName() : "N/A",
                formatMetrics(report),
                report.getNotes() != null ? report.getNotes() : "None"
        );
        return callClaude(prompt);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String buildAnalysisPrompt(UserProfile profile, List<HealthReport> reports) {
        String context = buildContextSummary(profile, reports);
        return String.format("""
                You are an experienced health advisor reviewing a patient's complete health history.
                
                %s
                
                Please provide:
                1. **Overall Health Summary** — a brief assessment of the patient's current health status
                2. **Key Trends** — notable patterns or changes across reports (positive and concerning)
                3. **Risk Areas** — any metrics or patterns that warrant attention
                4. **Lifestyle Recommendations** — 5–7 specific, evidence-based suggestions covering diet, exercise, sleep, stress management, and preventive care
                5. **Next Steps** — what follow-up tests or consultations might be beneficial
                
                Be thorough but accessible. Remind the user to consult their doctor before making major changes.
                """, context);
    }

    private String buildContextSummary(UserProfile profile, List<HealthReport> reports) {
        StringBuilder sb = new StringBuilder();

        if (profile != null) {
            sb.append("=== PATIENT PROFILE ===\n");
            sb.append("Name: ").append(profile.getName()).append("\n");
            sb.append("Age: ").append(profile.getAge()).append("\n");
            sb.append("Gender: ").append(profile.getGender()).append("\n");
            sb.append("Height: ").append(profile.getHeightCm()).append(" cm\n");
            sb.append("Weight: ").append(profile.getWeightKg()).append(" kg\n");
            sb.append("BMI: ").append(profile.getBmi()).append(" (").append(profile.getBmiCategory()).append(")\n");
            sb.append("Blood Group: ").append(profile.getBloodGroup()).append("\n");
            sb.append("Existing Conditions: ").append(profile.getExistingConditions()).append("\n");
            sb.append("Current Medications: ").append(profile.getMedications()).append("\n\n");
        }

        sb.append("=== HEALTH REPORTS (").append(reports.size()).append(" total) ===\n");
        for (HealthReport report : reports) {
            sb.append("\n--- ").append(report.getReportType())
              .append(" | ").append(report.getDate())
              .append(" | Doctor: ").append(report.getDoctorName() != null ? report.getDoctorName() : "N/A")
              .append(" ---\n");
            sb.append(formatMetrics(report));
            if (report.getNotes() != null && !report.getNotes().isBlank()) {
                sb.append("Notes: ").append(report.getNotes()).append("\n");
            }
        }

        return sb.toString();
    }

    private String formatMetrics(HealthReport report) {
        if (report.getMetrics() == null || report.getMetrics().isEmpty()) return "No metrics recorded.\n";
        StringBuilder sb = new StringBuilder();
        report.getMetrics().forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
        return sb.toString();
    }

    private String callClaude(String userMessage) throws IOException {
        String requestBody = mapper.writeValueAsString(new ClaudeRequest(MODEL, 2048, userMessage));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(requestBody, JSON_MEDIA))
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String body = response.body() != null ? response.body().string() : "empty";
                throw new IOException("API error " + response.code() + ": " + body);
            }
            String responseBody = response.body().string();
            JsonNode root = mapper.readTree(responseBody);
            return root.path("content").get(0).path("text").asText();
        }
    }

    // ── Inner classes for JSON serialisation ─────────────────────────────────

    static class ClaudeRequest {
        public String model;
        public int max_tokens;
        public Message[] messages;

        ClaudeRequest(String model, int maxTokens, String userMessage) {
            this.model = model;
            this.max_tokens = maxTokens;
            this.messages = new Message[]{new Message("user", userMessage)};
        }
    }

    static class Message {
        public String role;
        public String content;

        Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
