package nl.tudelft.jpacman.board;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests pour Unit.squaresAheadOf(amountToLookAhead).
 */
class UnitSquaresAheadOfTest {

    /**
     * Cas de base (partition "amount = 0") :
     *  - doit renvoyer la case courante (aucun déplacement).
     */
    @Test
    void zeroLookAheadReturnsCurrentSquare() {
        // Plateau 1x3: [ S0 ][ S1 ][ S2 ]
        BasicSquare[][] grid = {
            { new BasicSquare(), new BasicSquare(), new BasicSquare() }
        };
        Board board = new Board(grid);
        Unit u = new BasicUnit();

        // Place l'unité sur S1 (y=0, x=1) ; direction peu importe
        Square start = grid[0][1];
        u.occupy(start);

        Square dest = u.squaresAheadOf(0);

        assertThat(dest).isSameAs(start);
    }

    /**
     * Mouvement horizontal vers l'est (partition "direction = EAST", amount > 0) :
     *  - 2 cases devant doit amener sur la case x+2.
     */
    @Test
    void twoAheadEastReturnsCorrectSquare() {
        // Plateau 1x5: x=0..4
        BasicSquare[][] grid = {
            { new BasicSquare(), new BasicSquare(), new BasicSquare(), new BasicSquare(), new BasicSquare() }
        };
        Board board = new Board(grid);
        Unit u = new BasicUnit();

        // Start à x=1, on regarde à l'est
        Square start = grid[0][1];
        u.occupy(start);
        u.setDirection(Direction.EAST);

        Square dest = u.squaresAheadOf(2);

        assertThat(dest).isSameAs(grid[0][3]); // 1 + 2 = 3
    }

    /**
     * Mouvement vertical vers le nord (partition "direction = NORTH", amount > 0) :
     *  - 2 cases devant doit amener sur la case y-2 (même x).
     */
    @Test
    void twoAheadNorthReturnsCorrectSquare() {
        // Plateau 4x3 (y=0..3, x=0..2)
        BasicSquare[][] grid = {
            { new BasicSquare(), new BasicSquare(), new BasicSquare() }, // y=0
            { new BasicSquare(), new BasicSquare(), new BasicSquare() }, // y=1
            { new BasicSquare(), new BasicSquare(), new BasicSquare() }, // y=2
            { new BasicSquare(), new BasicSquare(), new BasicSquare() }  // y=3
        };
        Board board = new Board(grid);
        Unit u = new BasicUnit();

        // Start (x=1, y=2), direction NORTH → cible (x=1, y=0)
        Square start = grid[2][1];
        u.occupy(start);
        u.setDirection(Direction.NORTH);

        Square dest = u.squaresAheadOf(2);

        assertThat(dest).isSameAs(grid[0][1]);
    }

    /**
     * Mouvement horizontal vers l'ouest (autre partition) :
     *  - 3 cases devant depuis x=3 doit amener à x=0.
     */
    @Test
    void threeAheadWestReturnsCorrectSquare() {
        // Plateau 1x4: x=0..3
        BasicSquare[][] grid = {
            { new BasicSquare(), new BasicSquare(), new BasicSquare(), new BasicSquare() }
        };
        Board board = new Board(grid);
        Unit u = new BasicUnit();

        // Start x=3, direction WEST → cible x=0
        Square start = grid[0][3];
        u.occupy(start);
        u.setDirection(Direction.WEST);

        Square dest = u.squaresAheadOf(3);

        assertThat(dest).isSameAs(grid[0][0]);
    }
}
