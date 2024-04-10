public class PlayerBullet extends Bullet {
    public PlayerBullet(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void move() {
        y -= 30;
    }
}

