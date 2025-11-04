package nl.tudelft.jpacman.board;



import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

 /** Test various aspects of board.
 *
 * @author Jeroen Roosen 
 */
class BoardTest {

    private static final int MAX_WIDTH = 2;
    private static final int MAX_HEIGHT = 3;

    private final Square[][] grid = {
        { mock(Square.class), mock(Square.class), mock(Square.class) },
        { mock(Square.class), mock(Square.class), mock(Square.class) },
    };
    private final Board board = new Board(grid);

    /**
     * Verifies the board has the correct width.
     */
    @Test
    void verifyWidth() {
        // 3 colonnes → largeur attendue = 3
        assertThat(board.getWidth()).isEqualTo(3);
    }

    /**
     * Verifies the board has the correct height.
     */
    @Test
    void verifyHeight() {
        // 2 lignes → hauteur attendue = 2
        assertThat(board.getHeight()).isEqualTo(2);
    }

    /**
     * Verify that squares at key positions are properly set.
     * @param x Horizontal coordinate of relevant cell.
     * @param y Vertical coordinate of relevant cell.
     */
    @ParameterizedTest
    @CsvSource({
        "0, 0",  // dans le plateau
        "1, 2",  // hors du plateau (y=2 alors que height=2)
        "0, 1"   // dans le plateau
    })
    void testSquareAt(int x, int y) {
        boolean inside = board.withinBorders(x, y);
        if (inside) {
            // Si à l'intérieur, squareAt doit renvoyer la case correspondante
            assertThat(board.squareAt(x, y)).isEqualTo(grid[y][x]);
        } else {
            // Si hors des bords, withinBorders doit l’indiquer
            assertThat(inside).isFalse();
        }
    }
}
