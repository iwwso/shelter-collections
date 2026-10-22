package lv.bootcamp.shelter.service;

import lombok.extern.slf4j.Slf4j;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ShelterReportData;
import lv.bootcamp.shelter.service.data.SpeciesStats;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Writes the text report (and a JSON summary).
 *
 * @author Karyna Litvinova
 */
@Slf4j
public class ReportExportService {

    private static final DateTimeFormatter GENERATED_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public void writeReport(Path outputPath, ShelterReportData reportData) {
        writeFile(outputPath, buildReport(reportData));
        log.info("Report written to {}", outputPath.toAbsolutePath());
    }

    public void writeJsonSummary(Path outputPath, ShelterReportData reportData) {
        writeFile(outputPath, buildJsonSummary(reportData));
        log.info("JSON summary written to {}", outputPath.toAbsolutePath());
    }

    String buildReport(ShelterReportData data) {
        StringBuilder sb = new StringBuilder();
        String nl = System.lineSeparator();

        sb.append("=== Shelter Upload Report ===").append(nl);
        sb.append("Generated: ").append(data.generatedAt().format(GENERATED_FORMAT)).append(nl);
        sb.append("Total imported: ").append(data.totalImported()).append(nl);
        sb.append("Total skipped: ").append(data.totalSkipped()).append(nl);
        sb.append("Invalid rows: ").append(joinOrNone(data.importResult().invalidRowNumbers().stream()
                .map(String::valueOf).collect(Collectors.joining(", ")))).append(nl);
        sb.append(nl);

        sb.append("Unique species (").append(data.uniqueSpecies().size()).append("): ")
                .append(joinOrNone(String.join(", ", data.uniqueSpecies()))).append(nl);
        sb.append(nl);

        sb.append("Per species:").append(nl);
        for (Map.Entry<String, SpeciesStats> entry : data.statsBySpecies().entrySet()) {
            SpeciesStats s = entry.getValue();
            sb.append(String.format("  %s: total=%d, vaccinated=%d, unvaccinated=%d",
                    entry.getKey(), s.total(), s.vaccinated(), s.unvaccinated())).append(nl);
        }
        sb.append(nl);

        sb.append("Oldest animal per species (unknown ages excluded):").append(nl);
        for (String species : data.uniqueSpecies()) {
            Animal oldest = data.oldestBySpecies().get(species);
            String value = (oldest == null) ? "no known ages" : oldest.getName() + " (" + oldest.getAge() + " years)";
            sb.append("  ").append(species).append(": ").append(value).append(nl);
        }
        sb.append(nl);

        sb.append("Needs vet input: ").append(joinOrNone(String.join(", ", data.animalsNeedingVetInput()))).append(nl);
        return sb.toString();
    }

    String buildJsonSummary(ShelterReportData data) {
        String perSpecies = data.statsBySpecies().entrySet().stream()
                .map(e -> String.format("    \"%s\": {\"total\": %d, \"vaccinated\": %d, \"unvaccinated\": %d}",
                        escape(e.getKey()), e.getValue().total(), e.getValue().vaccinated(), e.getValue().unvaccinated()))
                .collect(Collectors.joining("," + System.lineSeparator()));
        String needsVet = data.animalsNeedingVetInput().stream()
                .map(s -> "\"" + escape(s) + "\"")
                .collect(Collectors.joining(", "));
        String invalidRows = data.importResult().invalidRowNumbers().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        String nl = System.lineSeparator();
        return "{" + nl
                + "  \"generated\": \"" + data.generatedAt().format(GENERATED_FORMAT) + "\"," + nl
                + "  \"totalImported\": " + data.totalImported() + "," + nl
                + "  \"totalSkipped\": " + data.totalSkipped() + "," + nl
                + "  \"invalidRows\": [" + invalidRows + "]," + nl
                + "  \"perSpecies\": {" + nl + perSpecies + nl + "  }," + nl
                + "  \"needsVetInput\": [" + needsVet + "]" + nl
                + "}" + nl;
    }

    private void writeFile(Path outputPath, String content) {
        try {
            Path parent = outputPath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                writer.write(content);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + outputPath, e);
        }
    }

    private static String joinOrNone(String joined) {
        return joined.isEmpty() ? "none" : joined;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}