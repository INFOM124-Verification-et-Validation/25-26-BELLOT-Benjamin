package nl.tudelft.jpacman.level;

/** Tests concrets pour PlayerCollisions. */
class PlayerCollisionsTest extends AbstractCollisionMapTest {

    @Override
    protected CollisionMap createCollisionMap() {
        return new PlayerCollisions();
    }
}
