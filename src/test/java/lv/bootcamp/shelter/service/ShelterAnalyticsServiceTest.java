package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;
import lv.bootcamp.shelter.service.data.ShelterReportData;
import lv.bootcamp.shelter.service.data.SpeciesStats;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Karyna Litvinova
 */
class ShelterAnalyticsServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 1, 15);

    private final ShelterReportData data = new ShelterAnalyticsService().buildReportData(new ImportResult(List.of(
            new Animal("Buddy", "Dog", 3, true, DATE),
            new Animal("Shadow", "Dog", 9, true, DATE),
            new Animal("Milo", "Dog", null, false, DATE),
            new Animal("Luna", "Cat", 2, false, DATE),
            new Animal("Pepper", "Bird", null, true, DATE)
    ), 1, List.of(5)));

    @Test
    void uniqueSpecies_sortedWithoutDuplicates() {
        assertEquals(List.of("Bird", "Cat", "Dog"), List.copyOf(data.uniqueSpecies()));
    }

    @Test
    void animalsBySpecies_groupsAnimals() {
        assertEquals(3, data.animalsBySpecies().get("Dog").size());
        assertEquals(1, data.animalsBySpecies().get("Cat").size());
    }

    @Test
    void statsBySpecies_countsVaccinated() {
        assertEquals(new SpeciesStats(3, 2, 1), data.statsBySpecies().get("Dog"));
        assertEquals(new SpeciesStats(1, 0, 1), data.statsBySpecies().get("Cat"));
    }

    @Test
    void oldestBySpecies_ignoresUnknownAges() {
        assertEquals("Shadow", data.oldestBySpecies().get("Dog").getName());
        // the only bird has unknown age -> no oldest bird
        assertFalse(data.oldestBySpecies().containsKey("Bird"));
    }

    @Test
    void animalsNeedingVetInput_formatNameSpecies() {
        assertEquals(List.of("Milo(Dog)", "Pepper(Bird)"), data.animalsNeedingVetInput());
    }
}