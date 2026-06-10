package com.health.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.Map;

public class HealthReport {

    private String id;
    private String reportType;   // BLOOD_WORK, VITALS, SYMPTOMS, GENERAL

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    private Map<String, String> metrics;  // e.g. {"hemoglobin": "13.5 g/dL", "bp": "120/80"}
    private String notes;
    private String doctorName;

    public HealthReport() {}

    public HealthReport(String id, String reportType, LocalDate date,
                        Map<String, String> metrics, String notes, String doctorName) {
        this.id = id;
        this.reportType = reportType;
        this.date = date;
        this.metrics = metrics;
        this.notes = notes;
        this.doctorName = doctorName;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Map<String, String> getMetrics() { return metrics; }
    public void setMetrics(Map<String, String> metrics) { this.metrics = metrics; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    @Override
    public String toString() {
        return String.format("[%s] %s on %s | Metrics: %d | Doctor: %s",
                id, reportType, date, metrics != null ? metrics.size() : 0,
                doctorName != null ? doctorName : "N/A");
    }
}
