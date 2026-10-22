package lv.bootcamp.shelter.service.data;

import lv.bootcamp.shelter.model.Animal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Everything the report needs. Maps are sorted by species (TreeMap).
 *
 * @author Karyna Litvinova
 */
public record ShelterReportData(
        LocalDateTime generatedAt,
        ImportResult importResult,
        Set<String> uniqueSpecies,
        Map<String, List<Animal>> animalsBySpecies,
        Map<String, SpeciesStats> statsBySpecies,
        Map<String, Animal> oldestBySpecies,
        List<String> animalsNeedingVetInput) {

    public int totalImported() {
        return importResult.allAnimals().size();
    }

    public int totalSkipped() {
        return importResult.skippedRows();
    }
}