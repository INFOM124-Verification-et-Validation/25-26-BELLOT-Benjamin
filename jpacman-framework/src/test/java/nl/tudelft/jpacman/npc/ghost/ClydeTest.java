package nl.tudelft.jpacman.npc.ghost;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.Navigation;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.level.PlayerFactory;
import nl.tudelft.jpacman.level.LevelFactory;
import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.npc.ghost.GhostMapParser;
import nl.tudelft.jpacman.npc.ghost.GhostFactory;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.level.PelletFactory;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de comportement de Clyde (nextAiMove) basés sur une carte construite via GhostMapParser.
 *
 * Hypothèses (conforme à la spec classique de Clyde) :
 * - Si la distance à Pac-Man est > 8 cases, Clyde poursuit Pac-Man (se rapproche).
 * - Si la distance est ≤ 8 cases, Clyde s'éloigne (va vers son coin/scatter ; effet observable: s'éloigne de Pac-Man).
 */
class ClydeTest {

    private GhostMapParser newParser(PacManSprites sprites) {
        BoardFactory boardFactory   = new BoardFactory(sprites);
        GhostFactory ghostFactory   = new GhostFactory(sprites);
        PelletFactory pelletFactory = new PelletFactory(sprites);
        LevelFactory levelFactory   = new LevelFactory(sprites, ghostFactory, pelletFactory);
        return new GhostMapParser(levelFactory, boardFactory, ghostFactory);
    }

    private static Player newAndRegisterPlayer(Level level, PlayerFactory pf, Direction dir) {
        Player p = pf.createPacMan();
        level.registerPlayer(p);
        p.setDirection(dir);
        return p;
    }

    private static Clyde findClyde(Level level) {
        Board board = level.getBoard();
        return Navigation.findUnitInBoard(Clyde.class, board);
    }

    /**
     * Cas 1 : Pac-Man est LOIN (> 8 cases) sur la même ligne, à gauche de Clyde.
     * Attendu : Clyde se rapproche → direction WEST.
     */
    @Test
    void clydeChasesWhenFarHorizontal() {
        PacManSprites sprites = new PacManSprites();
        GhostMapParser parser = newParser(sprites);
        PlayerFactory pf = new PlayerFactory(sprites);

        // 15 colonnes, P à x=1 et C à x=13 -> distance 12 (>8)
        List<String> map = Arrays.asList(
            "###############",
            "#P           C#",
            "###############"
        );

        Level level = parser.parseMap(map);
        newAndRegisterPlayer(level, pf, Direction.EAST);

        Clyde clyde = findClyde(level);
        Optional<Direction> next = clyde.nextAiMove();

        assertThat(next).contains(Direction.WEST); // vers Pac-Man (à gauche)
    }

    /**
     * Cas 2 : Pac-Man est PROCHE (≤ 8 cases) sur la même ligne, à gauche de Clyde.
     * Attendu : Clyde s'éloigne → direction EAST.
     */
    @Test
    void clydeFleesWhenNearHorizontal() {
        PacManSprites sprites = new PacManSprites();
        GhostMapParser parser = newParser(sprites);
        PlayerFactory pf = new PlayerFactory(sprites);

        // 11 colonnes, P à x=1 et C à x=6 -> distance 5 (≤8)
        List<String> map = Arrays.asList(
            "###########",
            "#P    C   #",
            "###########"
        );

        Level level = parser.parseMap(map);
        newAndRegisterPlayer(level, pf, Direction.EAST);

        Clyde clyde = findClyde(level);
        Optional<Direction> next = clyde.nextAiMove();

        assertThat(next).contains(Direction.EAST); // s'éloigner de P (qui est à gauche)
    }

    /**
     * Cas 3 : Pac-Man est LOIN (> 8) au-dessus de Clyde (même colonne).
     * Attendu : Clyde se rapproche → direction NORTH.
     */
    @Test
    void clydeChasesWhenFarVertical() {
        PacManSprites sprites = new PacManSprites();
        GhostMapParser parser = newParser(sprites);
        PlayerFactory pf = new PlayerFactory(sprites);

        // 9 lignes: P tout en haut, C vers le bas même colonne -> distance > 8 ?
        // Ici on prend une distance verticale de 8+ (P ligne 1, C ligne 9 encadré par murs)
        List<String> map = Arrays.asList(
            "#####",
            "# P #",
            "#   #",
            "#   #",
            "#   #",
            "#   #",
            "#   #",
            "#   #",
            "# C #",
            "#####"
        );

        Level level = parser.parseMap(map);
        newAndRegisterPlayer(level, pf, Direction.SOUTH);

        Clyde clyde = findClyde(level);
        Optional<Direction> next = clyde.nextAiMove();

        assertThat(next).contains(Direction.NORTH); // vers Pac-Man (au-dessus)
    }

    /**
     * Cas 4 : Aucun joueur sur la carte.
     * Attendu : Clyde ne sait pas où aller → Optional.empty.
     */
    @Test
    void clydeNoMoveWhenNoPlayerPresent() {
        PacManSprites sprites = new PacManSprites();
        GhostMapParser parser = newParser(sprites);

        List<String> map = Arrays.asList(
            "#####",
            "#  C#",
            "#####"
        );

        Level level = parser.parseMap(map);

        Clyde clyde = findClyde(level);
        Optional<Direction> next = clyde.nextAiMove();

        assertThat(next).isEmpty();
    }
}
