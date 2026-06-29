package com.health.service;

import com.health.model.HealthReport;
import com.health.model.UserProfile;
import com.health.storage.StorageService;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReportServiceTest {

    private static StorageService storage;
    private static ReportService service;
    private static String savedId;

    @BeforeAll
    static void setup() {
        // Use a temp directory so tests don't pollute real data
        System.setProperty("user.home", System.getProperty("java.io.tmpdir") + "/health-test");
        storage = new StorageService();
        service = new ReportService(storage);
    }

    @Test
    @Order(1)
    void testAddReport() {
        Map<String, String> metrics = Map.of(
                "Blood Pressure", "118/76",
                "Heart Rate", "72 bpm",
                "Hemoglobin", "14.2 g/dL"
        );
        HealthReport report = service.addReport("BLOOD_WORK", LocalDate.of(2024, 6, 1),
                metrics, "Routine checkup", "Dr. Smith");

        assertNotNull(report.getId());
        assertEquals("BLOOD_WORK", report.getReportType());
        assertEquals(3, report.getMetrics().size());
        savedId = report.getId();
    }

    @Test
    @Order(2)
    void testGetAllReports() {
        List<HealthReport> reports = service.getAllReports();
        assertFalse(reports.isEmpty());
        // Should be sorted by date descending
        if (reports.size() > 1) {
            assertTrue(reports.get(0).getDate().isAfter(reports.get(1).getDate())
                    || reports.get(0).getDate().isEqual(reports.get(1).getDate()));
        }
    }

    @Test
    @Order(3)
    void testFindById() {
        Optional<HealthReport> found = service.findById(savedId);
        assertTrue(found.isPresent());
        assertEquals("BLOOD_WORK", found.get().getReportType());
    }

    @Test
    @Order(4)
    void testFindByIdNotFound() {
        Optional<HealthReport> found = service.findById("XXXXXXXX");
        assertFalse(found.isPresent());
    }

    @Test
    @Order(5)
    void testGetReportsByType() {
        service.addReport("VITALS", LocalDate.now(), Map.of("BP", "120/80"), null, null);
        List<HealthReport> bloodWork = service.getReportsByType("BLOOD_WORK");
        assertTrue(bloodWork.stream().allMatch(r -> r.getReportType().equals("BLOOD_WORK")));
    }

    @Test
    @Order(6)
    void testDeleteReport() {
        boolean deleted = service.deleteReport(savedId);
        assertTrue(deleted);
        assertFalse(service.findById(savedId).isPresent());
    }

    @Test
    @Order(7)
    void testDeleteNonExistentReport() {
        boolean deleted = service.deleteReport("NOTEXIST");
        assertFalse(deleted);
    }

    @Test
    void testUserProfileBmi() {
        UserProfile profile = new UserProfile("Alice", 30, "Female", 165.0, 65.0,
                "A+", "None", "None");
        double bmi = profile.getBmi();
        assertTrue(bmi > 0, "BMI should be positive");
        assertEquals("Normal", profile.getBmiCategory());
    }

    @Test
    void testUserProfileBmiUnderweight() {
        UserProfile profile = new UserProfile("Bob", 22, "Male", 180.0, 55.0,
                "O+", "None", "None");
        assertEquals("Underweight", profile.getBmiCategory());
    }
}
