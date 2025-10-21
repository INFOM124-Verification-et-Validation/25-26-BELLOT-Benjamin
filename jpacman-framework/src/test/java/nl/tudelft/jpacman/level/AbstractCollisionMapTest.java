package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.npc.ghost.GhostFactory;
import nl.tudelft.jpacman.sprite.PacManSprites;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests génériques pour toute implémentation de CollisionMap.
 * (PlayerCollisions, DefaultPlayerInteractionMap, etc.)
 */
abstract class AbstractCollisionMapTest {

    protected CollisionMap collisions;
    protected Player player;
    protected Ghost ghost;
    protected Pellet pellet;

    /** Chaque sous-classe doit fournir la CollisionMap à tester. */
    protected abstract CollisionMap createCollisionMap();

    @BeforeEach
    void setUp() {
        PacManSprites sprites = new PacManSprites();
        PlayerFactory playerFactory = new PlayerFactory(sprites);
        GhostFactory ghostFactory = new GhostFactory(sprites);
        PelletFactory pelletFactory = new PelletFactory(sprites);

        player = playerFactory.createPacMan();
        ghost = ghostFactory.createBlinky();
        pellet = pelletFactory.createPellet();
        collisions = createCollisionMap();
    }

    /** (Player, Pellet) : le joueur gagne des points et la pellet disparaît. */
    @Test
    void playerEatsPellet() {
        int scoreBefore = player.getScore();

        collisions.collide(player, pellet);

        assertThat(player.getScore()).isGreaterThan(scoreBefore);
        // selon implémentation : la pellet quitte sa case ou est "consommée"
    }

    /** (Player, Ghost) : le joueur meurt. */
    @Test
    void playerDiesWhenHitsGhost() {
        collisions.collide(player, ghost);
        assertThat(player.isAlive()).isFalse();
    }

    /** (Ghost, Player) : effet symétrique, le joueur meurt aussi. */
    @Test
    void ghostKillsPlayer() {
        collisions.collide(ghost, player);
        assertThat(player.isAlive()).isFalse();
    }

    /** (Player, autre joueur) : pas d'effet. */
    @Test
    void playerCollidesWithPlayerDoesNothing() {
        Player another = new PlayerFactory(new PacManSprites()).createPacMan();
        collisions.collide(player, another);
        assertThat(player.isAlive()).isTrue();
        assertThat(player.getScore()).isZero();
    }
}
