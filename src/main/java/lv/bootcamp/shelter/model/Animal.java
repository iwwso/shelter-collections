package lv.bootcamp.shelter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Optional;

/**
 * One animal from the intake CSV. Immutable: all fields are final, no setters.
 *
 * @author Karyna Litvinova
 */
@Getter
@ToString
@AllArgsConstructor
public class Animal {

    private final String name;
    private final String species;
    /** Age in years, {@code null} when the age is unknown (blank in CSV). */
    private final Integer age;
    private final boolean vaccinated;
    private final LocalDate intakeDate;

    /** Age as Optional: empty when the age is unknown. */
    public Optional<Integer> getKnownAge() {
        return Optional.ofNullable(age);
    }

    public boolean isAgeUnknown() {
        return age == null;
    }
}