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
import nl.tudelft.jpacman.sprite.PacManSprites;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InkyTest {

    /** GhostMapParser étendu pour supporter 'I' (Inky) et 'B' (Blinky). */
    private static class TestGhostMapParser extends GhostMapParser {
        private final GhostFactory ghostFactory;

        TestGhostMapParser(LevelFactory levelFactory, BoardFactory boardFactory, GhostFactory ghostFactory) {
            super(levelFactory, boardFactory, ghostFactory);
            this.ghostFactory = ghostFactory;
        }

        @Override
        protected void addSquare(Square[][] grid, java.util.List<nl.tudelft.jpacman.npc.Ghost> ghosts,
                                 java.util.List<Square> startPositions, int x, int y, char c) {
            if (c == 'I') {
                grid[x][y] = makeGhostSquare(ghosts, ghostFactory.createInky());
            } else if (c == 'B') {
                grid[x][y] = makeGhostSquare(ghosts, ghostFactory.createBlinky());
            } else {
                super.addSquare(grid, ghosts, startPositions, x, y, c);
            }
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

    /* ---------- Favorables ---------- */

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

    /* ---------- Défavorables ---------- */

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
        // pas de registerPlayer

        Inky inky = findInky(level);
        Optional<Direction> move = inky.nextAiMove();

        assertThat(move).isEmpty();
    }

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

        Level level = parser.parseMap(map);
        newAndRegisterPlayer(level, pf, Direction.EAST);

        Inky inky = findInky(level);
        Optional<Direction> move = inky.nextAiMove();

        assertThat(move).isEmpty();
    }
}
