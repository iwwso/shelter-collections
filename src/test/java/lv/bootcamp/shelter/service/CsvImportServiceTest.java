package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Karyna Litvinova
 */
class CsvImportServiceTest {

    private final CsvImportService service = new CsvImportService();

    @Test
    void parseRow_validRow_mapsAllFields() {
        Animal a = service.parseRow("Buddy,Dog,3,true,15.01.2026");
        assertEquals("Buddy", a.getName());
        assertEquals("Dog", a.getSpecies());
        assertEquals(Integer.valueOf(3), a.getAge());
        assertTrue(a.isVaccinated());
        assertEquals(LocalDate.of(2026, 1, 15), a.getIntakeDate());
    }

    @Test
    void parseRow_blankAge_isUnknown() {
        Animal a = service.parseRow("Milo,Dog,,true,20.01.2026");
        assertNull(a.getAge());
        assertTrue(a.isAgeUnknown());
        assertFalse(a.getKnownAge().isPresent());
    }

    @Test
    void parseRow_nonNumericAge_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.parseRow("Daisy,Dog,bad_age,true,21.01.2026"));
    }

    @Test
    void parseRow_impossibleDate_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.parseRow("Rex,Dog,4,true,32.02.2026"));
    }

    @Test
    void parseRow_missingName_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.parseRow(",Cat,2,true,04.02.2026"));
    }

    @Test
    void parseRow_wrongColumnCount_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.parseRow("Buddy,Dog,3,true"));
    }

    @Test
    void importAnimals_skipsInvalidRowsAndRemembersLineNumbers() throws Exception {
        Path file = Files.createTempFile("intake", ".csv");
        Files.write(file, List.of(
                "name,species,age,vaccinated,intakeDate",
                "Buddy,Dog,3,true,15.01.2026",
                "Daisy,Dog,bad_age,true,21.01.2026",
                "Luna,Cat,,false,16.01.2026"), StandardCharsets.UTF_8);

        ImportResult result = service.importAnimals(file);

        assertEquals(2, result.allAnimals().size());
        assertEquals(1, result.skippedRows());
        assertEquals(List.of(3), result.invalidRowNumbers());
        Files.deleteIfExists(file);
    }
}