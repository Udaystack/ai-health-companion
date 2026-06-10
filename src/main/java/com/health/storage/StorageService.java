package com.health.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.health.model.HealthReport;
import com.health.model.UserProfile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StorageService {

    private static final String DATA_DIR = System.getProperty("user.home") + "/.health-companion";
    private static final String REPORTS_FILE = DATA_DIR + "/reports.json";
    private static final String PROFILE_FILE = DATA_DIR + "/profile.json";

    private final ObjectMapper mapper;

    public StorageService() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        ensureDataDir();
    }

    private void ensureDataDir() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    // ── Reports ───────────────────────────────────────────────────────────────

    public List<HealthReport> loadReports() {
        File file = new File(REPORTS_FILE);
        if (!file.exists()) return new ArrayList<>();
        try {
            HealthReport[] reports = mapper.readValue(file, HealthReport[].class);
            return new ArrayList<>(Arrays.asList(reports));
        } catch (IOException e) {
            System.err.println("Warning: could not load reports — " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveReports(List<HealthReport> reports) {
        try {
            mapper.writeValue(new File(REPORTS_FILE), reports);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save reports: " + e.getMessage(), e);
        }
    }

    public void addReport(HealthReport report) {
        List<HealthReport> reports = loadReports();
        reports.add(report);
        saveReports(reports);
    }

    public boolean deleteReport(String id) {
        List<HealthReport> reports = loadReports();
        boolean removed = reports.removeIf(r -> r.getId().equals(id));
        if (removed) saveReports(reports);
        return removed;
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    public UserProfile loadProfile() {
        File file = new File(PROFILE_FILE);
        if (!file.exists()) return null;
        try {
            return mapper.readValue(file, UserProfile.class);
        } catch (IOException e) {
            System.err.println("Warning: could not load profile — " + e.getMessage());
            return null;
        }
    }

    public void saveProfile(UserProfile profile) {
        try {
            mapper.writeValue(new File(PROFILE_FILE), profile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile: " + e.getMessage(), e);
        }
    }

    public String getDataDirectory() {
        return DATA_DIR;
    }
}
