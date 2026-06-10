package com.health.cli;

import com.health.model.HealthReport;
import com.health.model.UserProfile;
import com.health.service.AIService;
import com.health.service.ReportService;
import com.health.storage.StorageService;
import com.health.util.ConsoleUI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class HealthCompanionApp {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Scanner scanner;
    private final StorageService storage;
    private final ReportService reportService;
    private AIService aiService;

    public HealthCompanionApp() {
        this.scanner = new Scanner(System.in);
        this.storage = new StorageService();
        this.reportService = new ReportService(storage);
    }

    public static void main(String[] args) {
        ConsoleUI.init();
        HealthCompanionApp app = new HealthCompanionApp();
        app.run();
        ConsoleUI.shutdown();
    }

    public void run() {
        ConsoleUI.printBanner();
        setupApiKey();
        ensureProfile();

        boolean running = true;
        while (running) {
            ConsoleUI.printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> addReport();
                case "2" -> viewReports();
                case "3" -> analyzeWithAI();
                case "4" -> weeklyGoals();
                case "5" -> explainReport();
                case "6" -> askQuestion();
                case "7" -> manageProfile();
                case "8" -> deleteReport();
                case "0" -> {
                    ConsoleUI.printInfo("Stay healthy! Goodbye.");
                    running = false;
                }
                default -> ConsoleUI.printWarning("Invalid choice. Please enter 0–8.");
            }
        }
    }

    // ── API Key ───────────────────────────────────────────────────────────────

    private void setupApiKey() {
        String key = System.getenv("ANTHROPIC_API_KEY");
        if (key == null || key.isBlank()) {
            ConsoleUI.printWarning("ANTHROPIC_API_KEY environment variable not set.");
            System.out.print("Enter your Anthropic API key (or press Enter to skip AI features): ");
            key = scanner.nextLine().trim();
        }
        if (!key.isBlank()) {
            aiService = new AIService(key);
            ConsoleUI.printSuccess("AI features enabled.");
        } else {
            ConsoleUI.printWarning("AI features disabled. You can still add and view reports.");
        }
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    private void ensureProfile() {
        UserProfile profile = storage.loadProfile();
        if (profile == null) {
            ConsoleUI.printInfo("No profile found. Let's set yours up first!");
            manageProfile();
        } else {
            ConsoleUI.printSuccess("Welcome back, " + profile.getName() + "!");
        }
    }

    private void manageProfile() {
        ConsoleUI.printHeader("YOUR PROFILE");
        UserProfile existing = storage.loadProfile();

        System.out.print("Name" + (existing != null ? " [" + existing.getName() + "]" : "") + ": ");
        String name = readOrDefault(existing != null ? existing.getName() : "");

        System.out.print("Age" + (existing != null ? " [" + existing.getAge() + "]" : "") + ": ");
        int age = readIntOrDefault(existing != null ? existing.getAge() : 0);

        System.out.print("Gender (Male/Female/Other)" + (existing != null ? " [" + existing.getGender() + "]" : "") + ": ");
        String gender = readOrDefault(existing != null ? existing.getGender() : "Unknown");

        System.out.print("Height in cm" + (existing != null ? " [" + existing.getHeightCm() + "]" : "") + ": ");
        double height = readDoubleOrDefault(existing != null ? existing.getHeightCm() : 0);

        System.out.print("Weight in kg" + (existing != null ? " [" + existing.getWeightKg() + "]" : "") + ": ");
        double weight = readDoubleOrDefault(existing != null ? existing.getWeightKg() : 0);

        System.out.print("Blood group" + (existing != null ? " [" + existing.getBloodGroup() + "]" : "") + ": ");
        String blood = readOrDefault(existing != null ? existing.getBloodGroup() : "Unknown");

        System.out.print("Existing conditions (or 'None')" + (existing != null ? " [" + existing.getExistingConditions() + "]" : "") + ": ");
        String conditions = readOrDefault(existing != null ? existing.getExistingConditions() : "None");

        System.out.print("Current medications (or 'None')" + (existing != null ? " [" + existing.getMedications() + "]" : "") + ": ");
        String meds = readOrDefault(existing != null ? existing.getMedications() : "None");

        UserProfile profile = new UserProfile(name, age, gender, height, weight, blood, conditions, meds);
        storage.saveProfile(profile);
        ConsoleUI.printSuccess("Profile saved! BMI: " + profile.getBmi() + " (" + profile.getBmiCategory() + ")");
    }

    // ── Add Report ────────────────────────────────────────────────────────────

    private void addReport() {
        ConsoleUI.printHeader("ADD HEALTH REPORT");

        System.out.println("Report types: BLOOD_WORK, VITALS, SYMPTOMS, GENERAL, DENTAL, VISION, OTHER");
        System.out.print("Report type: ");
        String type = scanner.nextLine().trim().toUpperCase();
        if (type.isBlank()) type = "GENERAL";

        System.out.print("Date (yyyy-MM-dd) [today]: ");
        LocalDate date = readDateOrToday();

        System.out.print("Doctor name (optional): ");
        String doctor = scanner.nextLine().trim();

        Map<String, String> metrics = new LinkedHashMap<>();
        ConsoleUI.printInfo("Enter metrics (e.g. 'Blood Pressure: 120/80'). Empty line to finish.");
        while (true) {
            System.out.print("  Metric: ");
            String line = scanner.nextLine().trim();
            if (line.isBlank()) break;
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                metrics.put(parts[0].trim(), parts[1].trim());
            } else {
                ConsoleUI.printWarning("Format should be 'Name: Value'. Skipping.");
            }
        }

        System.out.print("Additional notes (optional): ");
        String notes = scanner.nextLine().trim();

        HealthReport report = reportService.addReport(type, date, metrics, notes, doctor.isBlank() ? null : doctor);
        ConsoleUI.printSuccess("Report saved with ID: " + report.getId());
    }

    // ── View Reports ──────────────────────────────────────────────────────────

    private void viewReports() {
        ConsoleUI.printHeader("ALL HEALTH REPORTS");
        List<HealthReport> reports = reportService.getAllReports();

        if (reports.isEmpty()) {
            ConsoleUI.printInfo("No reports yet. Add your first report with option 1.");
            return;
        }

        System.out.printf("  %-8s %-18s %-12s %s%n", "ID", "TYPE", "DATE", "METRICS");
        System.out.println("  " + "─".repeat(52));
        for (HealthReport r : reports) {
            ConsoleUI.printReportRow(r.getId(), r.getReportType(), r.getDate().toString(),
                    r.getMetrics() != null ? r.getMetrics().size() : 0);
        }
        System.out.println();

        System.out.print("Enter report ID to view details (or Enter to go back): ");
        String id = scanner.nextLine().trim();
        if (!id.isBlank()) {
            reportService.findById(id).ifPresentOrElse(this::printReportDetail,
                    () -> ConsoleUI.printError("Report not found: " + id));
        }
    }

    private void printReportDetail(HealthReport r) {
        ConsoleUI.printHeader("REPORT DETAIL — " + r.getId());
        System.out.println("Type    : " + r.getReportType());
        System.out.println("Date    : " + r.getDate());
        System.out.println("Doctor  : " + (r.getDoctorName() != null ? r.getDoctorName() : "N/A"));
        System.out.println("Metrics :");
        if (r.getMetrics() != null) r.getMetrics().forEach((k, v) -> System.out.println("  " + k + ": " + v));
        System.out.println("Notes   : " + (r.getNotes() != null ? r.getNotes() : "—"));
    }

    // ── AI Features ───────────────────────────────────────────────────────────

    private void analyzeWithAI() {
        if (!checkAI()) return;
        List<HealthReport> reports = reportService.getAllReports();
        if (reports.isEmpty()) { ConsoleUI.printInfo("Add some reports first."); return; }
        UserProfile profile = storage.loadProfile();
        ConsoleUI.printInfo("Analyzing " + reports.size() + " report(s)... (this may take a moment)");
        try {
            String result = aiService.analyzeHealthTrends(profile, reports);
            ConsoleUI.printAIResponse(result);
        } catch (Exception e) {
            ConsoleUI.printError("AI error: " + e.getMessage());
        }
    }

    private void weeklyGoals() {
        if (!checkAI()) return;
        List<HealthReport> reports = reportService.getRecentReports(10);
        UserProfile profile = storage.loadProfile();
        ConsoleUI.printInfo("Generating personalized weekly goals...");
        try {
            String result = aiService.generateWeeklyGoals(profile, reports);
            ConsoleUI.printAIResponse(result);
        } catch (Exception e) {
            ConsoleUI.printError("AI error: " + e.getMessage());
        }
    }

    private void explainReport() {
        if (!checkAI()) return;
        viewReports();
        System.out.print("Enter report ID to explain: ");
        String id = scanner.nextLine().trim();
        Optional<HealthReport> opt = reportService.findById(id);
        if (opt.isEmpty()) { ConsoleUI.printError("Report not found."); return; }
        ConsoleUI.printInfo("Explaining report in plain language...");
        try {
            String result = aiService.summarizeReport(storage.loadProfile(), opt.get());
            ConsoleUI.printAIResponse(result);
        } catch (Exception e) {
            ConsoleUI.printError("AI error: " + e.getMessage());
        }
    }

    private void askQuestion() {
        if (!checkAI()) return;
        System.out.print("Your health question: ");
        String question = scanner.nextLine().trim();
        if (question.isBlank()) return;
        ConsoleUI.printInfo("Consulting AI...");
        try {
            String result = aiService.askHealthQuestion(storage.loadProfile(), reportService.getAllReports(), question);
            ConsoleUI.printAIResponse(result);
        } catch (Exception e) {
            ConsoleUI.printError("AI error: " + e.getMessage());
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    private void deleteReport() {
        viewReports();
        System.out.print("Enter report ID to delete: ");
        String id = scanner.nextLine().trim();
        if (id.isBlank()) return;
        System.out.print("Are you sure? (yes/no): ");
        if ("yes".equalsIgnoreCase(scanner.nextLine().trim())) {
            if (reportService.deleteReport(id)) ConsoleUI.printSuccess("Report deleted.");
            else ConsoleUI.printError("Report not found.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean checkAI() {
        if (aiService == null) {
            ConsoleUI.printError("AI features are not enabled. Restart and provide your API key.");
            return false;
        }
        return true;
    }

    private String readOrDefault(String defaultVal) {
        String val = scanner.nextLine().trim();
        return val.isBlank() ? defaultVal : val;
    }

    private int readIntOrDefault(int defaultVal) {
        String val = scanner.nextLine().trim();
        if (val.isBlank()) return defaultVal;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return defaultVal; }
    }

    private double readDoubleOrDefault(double defaultVal) {
        String val = scanner.nextLine().trim();
        if (val.isBlank()) return defaultVal;
        try { return Double.parseDouble(val); } catch (NumberFormatException e) { return defaultVal; }
    }

    private LocalDate readDateOrToday() {
        String val = scanner.nextLine().trim();
        if (val.isBlank()) return LocalDate.now();
        try { return LocalDate.parse(val, DATE_FMT); }
        catch (DateTimeParseException e) {
            ConsoleUI.printWarning("Invalid date format, using today.");
            return LocalDate.now();
        }
    }
}
