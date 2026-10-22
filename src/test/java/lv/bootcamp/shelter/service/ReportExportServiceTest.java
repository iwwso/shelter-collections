package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;
import lv.bootcamp.shelter.service.data.ShelterReportData;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Karyna Litvinova
 */
class ReportExportServiceTest {

    private final ShelterReportData data = new ShelterAnalyticsService().buildReportData(new ImportResult(List.of(
            new Animal("Buddy", "Dog", 3, true, LocalDate.of(2026, 1, 15)),
            new Animal("Leo", "Cat", null, true, LocalDate.of(2026, 1, 25))
    ), 2, List.of(8, 9)));

    private final ReportExportService service = new ReportExportService();

    @Test
    void buildReport_containsAllSections() {
        String report = service.buildReport(data);
        assertTrue(report.contains("Total imported: 2"));
        assertTrue(report.contains("Total skipped: 2"));
        assertTrue(report.contains("Invalid rows: 8, 9"));
        assertTrue(report.contains("Unique species (2): Cat, Dog"));
        assertTrue(report.contains("Dog: total=1, vaccinated=1, unvaccinated=0"));
        assertTrue(report.contains("Dog: Buddy (3 years)"));
        assertTrue(report.contains("Cat: no known ages"));
        assertTrue(report.contains("Needs vet input: Leo(Cat)"));
    }

    @Test
    void writeReport_createsUtf8File() throws Exception {
        Path dir = Files.createTempDirectory("report");
        Path file = dir.resolve("sub").resolve("upload-report.txt");

        service.writeReport(file, data);

        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.startsWith("=== Shelter Upload Report ==="));
    }
}