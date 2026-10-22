package lv.bootcamp.shelter.service.data;

import lv.bootcamp.shelter.model.Animal;

import java.util.List;

/**
 * Result of the CSV import.
 *
 * @param allAnimals        valid animals in file order
 * @param skippedRows       number of invalid rows
 * @param invalidRowNumbers line numbers (header = line 1) of the skipped rows
 */
public record ImportResult(List<Animal> allAnimals, int skippedRows, List<Integer> invalidRowNumbers) {

}