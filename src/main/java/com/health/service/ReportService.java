package com.health.service;

import com.health.model.HealthReport;
import com.health.storage.StorageService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReportService {

    private final StorageService storage;

    public ReportService(StorageService storage) {
        this.storage = storage;
    }

    public HealthReport addReport(String reportType, LocalDate date,
                                  java.util.Map<String, String> metrics,
                                  String notes, String doctorName) {
        String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        HealthReport report = new HealthReport(id, reportType.toUpperCase(), date, metrics, notes, doctorName);
        storage.addReport(report);
        return report;
    }

    public List<HealthReport> getAllReports() {
        return storage.loadReports().stream()
                .sorted(Comparator.comparing(HealthReport::getDate).reversed())
                .collect(Collectors.toList());
    }

    public List<HealthReport> getReportsByType(String type) {
        return getAllReports().stream()
                .filter(r -> r.getReportType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public Optional<HealthReport> findById(String id) {
        return storage.loadReports().stream()
                .filter(r -> r.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    public boolean deleteReport(String id) {
        return storage.deleteReport(id);
    }

    public List<HealthReport> getRecentReports(int count) {
        return getAllReports().stream().limit(count).collect(Collectors.toList());
    }
}
