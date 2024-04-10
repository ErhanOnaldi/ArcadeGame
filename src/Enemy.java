import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Enemy {
    private int x;
    private int y;
    private int width;
    private int height;
    private int health;
    private boolean canShoot;
    private int screenHeight;
    private List<EnemyBullet> bullets;
    private ImageIcon enemyAircraftImage;
    private ImageIcon bulletImage;
    private ImageIcon enemyFireAnimationImage;
    private boolean isFireAnimationActive;
    private Timer fireAnimationTimer;
    private int fireAnimationDuration = 100;

    public Enemy(int x, int y, int width, int height, int screenHeight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.health = 3;
        this.canShoot = true;
        this.screenHeight = screenHeight;
        this.bullets = new ArrayList<>();
        enemyAircraftImage = new ImageIcon("EnemyAircraft.png");
        bulletImage = new ImageIcon("BulletEnemy.png");
        enemyFireAnimationImage = new ImageIcon("EnemyFireAnimation.gif");
        isFireAnimationActive = false;

        Timer shootingTimer = new Timer();
        shootingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                canShoot = true;
            }
        }, 0, 1000);
    }

    public void moveDown(int speed) {
        y += speed;
    }

    public void draw(Graphics g) {
        //g.setColor(Color.BLUE);
       // g.fillRect(x, y, width, height);
        g.drawImage(enemyAircraftImage.getImage(), x -80, y, null);
        //g.setColor(Color.BLACK);
        for (EnemyBullet bullet : bullets) {
            g.drawImage(bulletImage.getImage(), bullet.getX() - 20, bullet.getY()+20, null);
        }
        if (isFireAnimationActive) {
            g.drawImage(enemyFireAnimationImage.getImage(), x -5, y +100, null);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public int getHeight() {
        return height;
    }

    public int getHealth() {
        return health;
    }

    public void decreaseHealth() {
        health--;

        if (health < 0) {
            health = 0;
        }
    }

    public boolean canShoot() {
        return canShoot;
    }

    public void setCanShoot(boolean canShoot) {
        this.canShoot = canShoot;
    }

    public List<EnemyBullet> getBullets() {
        return bullets;
    }

    public void shoot() {
        if (canShoot) {
            EnemyBullet bullet = new EnemyBullet(x + 20, y + height, 10, 20);
            bullets.add(bullet);
            triggerFireAnimation();
            canShoot = false;
        }
    }
    private void triggerFireAnimation() {
        isFireAnimationActive = true;
        fireAnimationTimer = new Timer();
        fireAnimationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                isFireAnimationActive = false;
            }
        }, fireAnimationDuration);
    }

    public void updateBullets() {
        Iterator<EnemyBullet> iterator = bullets.iterator();
        while (iterator.hasNext()) {
            EnemyBullet bullet = iterator.next();
            bullet.move();

            if (bullet.getY() > screenHeight) {
                iterator.remove();
            }
        }
    }
}







