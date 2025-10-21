package nl.tudelft.jpacman.board;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test suite to confirm that {@link Unit}s correctly (de)occupy squares.
 *
 * @author Jeroen Roosen 
 *
 */
class OccupantTest {

    /**
     * The unit under test.
     */
    private Unit unit;

    /**
     * Resets the unit under test.
     */
    @BeforeEach
    void setUp() {
        unit = new BasicUnit();
    }

    /**
     * Asserts that a unit has no square to start with.
     */
    @Test
    void noStartSquare() {
        // Une nouvelle unité ne doit pas être placée sur une case
        assertThat(unit.hasSquare()).isFalse();
        assertThat(unit.getSquare()).isNull();
    }

    /**
     * Tests that the unit indeed has the target square as its base after
     * occupation.
     */
    @Test
    void testOccupy() {
        Square target = new BasicSquare();
        unit.occupy(target);

        // L’unité doit être sur la case
        assertThat(unit.hasSquare()).isTrue();
        assertThat(unit.getSquare()).isEqualTo(target);

        // La case doit contenir l’unité
        assertThat(target.getOccupants()).contains(unit);
    }

    /**
     * Test that the unit indeed has the target square as its base after
     * double occupation.
     */
    @Test
    void testReoccupy() {
        Square first = new BasicSquare();
        Square second = new BasicSquare();

        // L’unité occupe d’abord la première case
        unit.occupy(first);
        assertThat(first.getOccupants()).contains(unit);

        // Puis elle se déplace sur une autre case
        unit.occupy(second);

        // La première ne contient plus l’unité
        assertThat(first.getOccupants()).doesNotContain(unit);

        // La deuxième la contient
        assertThat(second.getOccupants()).contains(unit);

        // Et l’unité a bien la deuxième comme base
        assertThat(unit.getSquare()).isEqualTo(second);
    }
}

