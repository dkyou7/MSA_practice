package microservice.book.socialmultiplication.multiplication.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * This class represents a Multiplication (a * b).
 */
@Getter
@ToString
@EqualsAndHashCode
@Entity
@AllArgsConstructor
@RequiredArgsConstructor
public final class Multiplication {

    @Id @GeneratedValue
    @Column(name = "multiplication_id")
    private Long id;

    // Both factors
    private final int factorA;
    private final int factorB;

    // Empty constructor for JSON/JPA
    public Multiplication() {
        this(0, 0);
    }

}
