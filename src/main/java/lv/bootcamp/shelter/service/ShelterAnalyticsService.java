package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;
import lv.bootcamp.shelter.service.data.ShelterReportData;
import lv.bootcamp.shelter.service.data.SpeciesStats;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds collections and report metrics with stream pipelines.
 *
 * @author Karyna Litvinova
 */
public class ShelterAnalyticsService {

    public ShelterReportData buildReportData(ImportResult importResult) {
        List<Animal> allAnimals = importResult.allAnimals();

        // Step 2: collections
        // TreeSet - no duplicates, sorted alphabetically
        Set<String> uniqueSpecies = allAnimals.stream()
                .map(Animal::getSpecies)
                .collect(Collectors.toCollection(TreeSet::new));

        // TreeMap - species sorted alphabetically
        Map<String, List<Animal>> animalsBySpecies = allAnimals.stream()
                .collect(Collectors.groupingBy(Animal::getSpecies, TreeMap::new, Collectors.toList()));

        List<String> animalsNeedingVetInput = allAnimals.stream()
                .filter(Animal::isAgeUnknown)
                .map(a -> a.getName() + "(" + a.getSpecies() + ")")
                .toList();

        // Step 3: metrics
        Map<String, SpeciesStats> statsBySpecies = animalsBySpecies.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toStats(entry.getValue()),
                        (a, b) -> a,
                        TreeMap::new));

        // animals with unknown age are excluded; on equal age the first one in the file wins
        Map<String, Animal> oldestBySpecies = allAnimals.stream()
                .filter(a -> a.getKnownAge().isPresent())
                .collect(Collectors.toMap(
                        Animal::getSpecies,
                        Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparingInt(Animal::getAge)),
                        TreeMap::new));

        return new ShelterReportData(
                LocalDateTime.now(),
                importResult,
                uniqueSpecies,
                animalsBySpecies,
                statsBySpecies,
                oldestBySpecies,
                animalsNeedingVetInput);
    }

    private SpeciesStats toStats(List<Animal> animals) {
        // partitioningBy -> {true=vaccinated count, false=unvaccinated count}
        Map<Boolean, Long> byVaccination = animals.stream()
                .collect(Collectors.partitioningBy(Animal::isVaccinated, Collectors.counting()));
        return new SpeciesStats(animals.size(), byVaccination.get(true), byVaccination.get(false));
    }
}