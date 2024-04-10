public class EnemyBullet extends Bullet {
    public EnemyBullet(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void move() {
        y += 20;
    }
}
