import java.awt.*;

public abstract class Bullet {
    private int x;
    protected int y;
    private int width;
    private int height;

    public Bullet(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean intersects(Rectangle other) {
        return getBounds().intersects(other);
    }

    public abstract void move();
}



