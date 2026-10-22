package lv.bootcamp.shelter.service;

import lombok.extern.slf4j.Slf4j;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads intake.csv, validates every row and maps valid rows to {@link Animal}.
 *
 * @author Karyna Litvinova
 */
@Slf4j
public class CsvImportService {

    private static final int EXPECTED_COLUMNS = 5;

    // STRICT + "uuuu": impossible dates like 32.02.2026 are rejected instead of being "fixed"
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);

    public ImportResult importAnimals(Path inputPath) {
        log.info("Starting import from {}", inputPath);

        List<Animal> allAnimals = new ArrayList<>();
        List<Integer> invalidRowNumbers = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8)) {
            String header = reader.readLine(); // skip header row
            if (header == null) {
                log.warn("File {} is empty", inputPath);
                return new ImportResult(List.of(), 0, List.of());
            }

            String line;
            int lineNumber = 1; // header is line 1
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    allAnimals.add(parseRow(line));
                } catch (IllegalArgumentException e) {
                    log.warn("Skipping line {}: {} -> \"{}\"", lineNumber, e.getMessage(), line);
                    invalidRowNumbers.add(lineNumber);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + inputPath, e);
        }

        log.info("Import finished: {} imported, {} skipped", allAnimals.size(), invalidRowNumbers.size());
        return new ImportResult(List.copyOf(allAnimals), invalidRowNumbers.size(), List.copyOf(invalidRowNumbers));
    }

    /**
     * Converts one CSV line to an Animal.
     *
     * @throws IllegalArgumentException with a readable reason if the row is invalid
     */
    Animal parseRow(String line) {
        String[] parts = line.split(",", -1); // -1 keeps empty trailing columns
        if (parts.length != EXPECTED_COLUMNS) {
            throw new IllegalArgumentException(
                    "expected " + EXPECTED_COLUMNS + " columns but found " + parts.length);
        }

        String name = parts[0].trim();
        String species = parts[1].trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is missing");
        }
        if (species.isEmpty()) {
            throw new IllegalArgumentException("species is missing");
        }

        Integer age = parseAge(parts[2].trim());
        boolean vaccinated = parseVaccinated(parts[3].trim());
        LocalDate intakeDate = parseDate(parts[4].trim());

        return new Animal(name, species, age, vaccinated, intakeDate);
    }

    /** Blank age = unknown (null); non-numeric or negative age is invalid. */
    private Integer parseAge(String value) {
        if (value.isEmpty()) {
            return null;
        }
        int age;
        try {
            age = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("age is not a number: '" + value + "'");
        }
        if (age < 0) {
            throw new IllegalArgumentException("age cannot be negative: " + age);
        }
        return age;
    }

    private boolean parseVaccinated(String value) {
        if (value.equalsIgnoreCase("true")) {
            return true;
        }
        if (value.equalsIgnoreCase("false")) {
            return false;
        }
        throw new IllegalArgumentException("vaccinated must be true or false, got '" + value + "'");
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("invalid intake date '" + value + "', expected dd.MM.yyyy");
        }
    }
}