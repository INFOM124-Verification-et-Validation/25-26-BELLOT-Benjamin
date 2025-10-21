package nl.tudelft.jpacman.npc.ghost;

import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.LevelFactory;
import nl.tudelft.jpacman.level.Navigation;
import nl.tudelft.jpacman.level.PelletFactory;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.level.PlayerFactory;
import nl.tudelft.jpacman.npc.Ghost;
import nl.tudelft.jpacman.sprite.PacManSprites;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de comportement d'Inky (nextAiMove) selon la spécification.
 */
class InkyTest {

    /** GhostMapParser étendu pour supporter 'I' (Inky) et 'B' (Blinky). */
    private static class TestGhostMapParser extends GhostMapParser {
        private final GhostFactory ghostFactory;

        TestGhostMapParser(LevelFactory levelFactory, BoardFactory boardFactory, GhostFactory ghostFactory) {
            super(levelFactory, boardFactory, ghostFactory);
            this.ghostFactory = ghostFactory;
        }

        @Override
        protected void addSquare(Square[][] grid, List<Ghost> ghosts,
                                 List<Square> startPositions, int x, int y, char c) {
            if (c == 'I') {
                grid[x][y] = makeGhostSquare(ghosts, ghostFactory.createInky());
            }
            else if (c == 'B') {
                grid[x][y] = makeGhostSquare(ghosts, ghostFactory.createBlinky());
            }
            else {
                super.addSquare(grid, ghosts, startPositions, x, y, c);
            }
        }


        private TestGhostMapParser newParser(PacManSprites sprites) {
        BoardFactory boardFactory   = new BoardFactory(sprites);
        GhostFactory ghostFactory   = new GhostFactory(sprites);
        PelletFactory pelletFactory = new PelletFactory(sprites);
        LevelFactory levelFactory   = new LevelFactory(sprites, ghostFactory, pelletFactory);
        return new TestGhostMapParser(levelFactory, boardFactory, ghostFactory);
    }

    private static Player newAndRegisterPlayer(Level level, PlayerFactory pf, Direction dir) {
        Player p = pf.createPacMan();
        level.registerPlayer(p);
        p.setDirection(dir);
        return p;
    }

    private static Inky findInky(Level level) {
        Board board = level.getBoard();
        return Navigation.findUnitInBoard(Inky.class, board);
    }

    /* ---------------------- CAS FAVORABLES ---------------------- */

    /**
     * Favorable #1 : Pac-Man regarde à l’est, Inky doit aller EAST
     * Carte : couloir horizontal, Blinky à gauche, Pac-Man, puis Inky.
     * B --(vers B=2 cases devant P à l'est)--> destination plus à l'est, donc Inky doit aller vers l'est.
     */
    @Test
    void inkyMovesEastWhenTargetIsToTheEast() {
        PacManSprites sprites = new PacManSprites();
        TestGhostMapParser parser = newParser(sprites);
        PlayerFactory pf = new PlayerFactory(sprites);

        List<String> map = Arrays.asList(
            "#################",
            "#B P     I     #",
            "#################"
        );

        Level level = parser.parseMap(map);
        newAndRegisterPlayer(level, pf, Direction.EAST);

        Inky inky = findInky(level);
        Optional<Direction> move = inky.nextAiMove();

        assertThat(move).contains(Direction.EAST);
    }

    /**
     * Favorable #2 : Pac-Man regarde à l’ouest, Inky doit aller WEST
     * Carte : couloir horizontal, Inky à gauche, Pac-Man, Blinky plus à droite.
     * Le point visé (doublement du segment Blinky -> 2 cases devant Pac-Man vers l'ouest) est à l'ouest d'Inky.
     */
    @Test
    void inkyMovesWestWhenTargetIsToTheWest() {
        PacManSprites sprites = new PacManSprites();
        TestGhostMapParser parser = newParser(sprites);
        PlayerFactory pf = new PlayerFactory(sprites);

        List<String> map = Arrays.asList(
            "#################",
            "#I     P   B    #",
            "#################"
        );

        Level level = parser.parseMap(map);
        newAndRegisterPlayer(level, pf, Direction.WEST);

        Inky inky = findInky(level);
        Optional<Direction> move = inky.nextAiMove();

        assertThat(move).contains(Direction.WEST);
    }

    /* ---------------------- CAS DÉFAVORABLES ---------------------- */

    /**
     * Défavorable #1 : Aucun Blinky sur la carte -> Optional.empty()
     */
    @Test
    void inkyNoMoveWhenNoBlinky() {
        PacManSprites sprites = new PacManSprites();
        TestGhostMapParser parser = newParser(sprites);
        PlayerFactory pf = new PlayerFactory(sprites);

        List<String> map = Arrays.asList(
            "#########",
            "#P   I  #",
            "#########"
        );

        Level level = parser.parseMap(map);
        newAndRegisterPlayer(level, pf, Direction.EAST);

        Inky inky = findInky(level);
        Optional<Direction> move = inky.nextAiMove();

        assertThat(move).isEmpty();
    }

    /**
     * Défavorable #2 : Aucun joueur enregistré (même si la carte a 'P') -> Optional.empty()
     */
    @Test
    void inkyNoMoveWhenNoPlayerRegistered() {
        PacManSprites sprites = new PacManSprites();
        TestGhostMapParser parser = newParser(sprites);

        List<String> map = Arrays.asList(
            "#########",
            "#B  P I #",
            "#########"
        );

        Level level = parser.parseMap(map);
        // Ne pas enregistrer de joueur

        Inky inky = findInky(level);
        Optional<Direction> move = inky.nextAiMove();

        assertThat(move).isEmpty();
    }

    /**
     * Défavorable #3 : Pas de chemin entre Blinky et la case B=2 cases devant Pac-Man -> Optional.empty()
     * On isole Blinky avec des murs pour que shortestPath(blinky, B) renvoie null.
     */
    @Test
    void inkyNoMoveWhenBlinkyCannotReachPlayerAheadSquare() {
        PacManSprites sprites = new PacManSprites();
        TestGhostMapParser parser = newParser(sprites);
        PlayerFactory pf = new PlayerFactory(sprites);

        List<String> map = Arrays.asList(
            "###########",
            "#B####    #",
            "# #### P I#",
            "# ####    #",
            "###########"
        );
        // Couloir séparé par des murs "####" entre Blinky et la zone de P/I

        Level level = parser.parseMap(map);
        newAndRegisterPlayer(level, pf, Direction.EAST);

        Inky inky = findInky(level);
        Optional<Direction> move = inky.nextAiMove();

        assertThat(move).isEmpty();
    }
}
