import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;


public class CharacterShootingGUI extends JPanel implements KeyListener, ActionListener,Runnable {
    private int characterX = 400;
    private int characterY = 600;
    private List<Bullet> playerBullets;
    private List<Enemy> enemies;
    private boolean upPressed, downPressed, leftPressed, rightPressed, spacePressed;
    private boolean gameRunning;
    private int playerHealth;
    private int maxHealth;
    private int currentHealth;
    private int healthBarWidth;
    private int healthBarHeight;
    private int score;
    private ImageIcon playerAircraftImage;
    private ImageIcon explosionImage;
    private ImageIcon bulletImage;
    private ImageIcon backGroundImage;
    private JButton resetButton;
    private JLabel gameOverLabel;
    private JLabel scoreLabel;
    private Point explosionLocation;
    private boolean isExplosionActive;
    private Timer explosionTimer;
    private ImageIcon fireAnimationImage;
    private boolean isFireAnimationActive;
    private Timer fireAnimationTimer;
    private Point fireAnimationLocation;
    private int backgroundY = 0;
    private Timer backgroundTimer;


    private JetFighterGame jetFighterGame;
    private String currentUsername;

    public CharacterShootingGUI() {

        setPreferredSize(new Dimension(1650, 800));
        setFocusable(true);
        backGroundImage = new ImageIcon("backGroundImage.jpg");
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
        playerAircraftImage = new ImageIcon("PlayerAircraft.png");
        explosionImage = new ImageIcon("Explosion.gif");
        bulletImage =  new ImageIcon("BulletPlayer.png");
        explosionImage = new ImageIcon("Explosion.gif");
        isExplosionActive = false;
        fireAnimationImage = new ImageIcon("FireAnimation.gif");
        isFireAnimationActive = false;

        playerBullets = new ArrayList<>();
        enemies = new ArrayList<>();
        gameRunning = true;
        playerHealth = 25;
        maxHealth = 25;
        currentHealth = playerHealth;
        healthBarWidth = 100;
        healthBarHeight = 20;
        score = 0;

        Timer timer = new Timer(20, this);
        timer.start();

        backgroundTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveBackground();
            }
        });
        backgroundTimer.start();

        Timer enemySpawnTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameRunning) {
                    spawnEnemy();
                }
            }
        });
        enemySpawnTimer.start();

        resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });

        gameOverLabel = new JLabel("Game Over");
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gameOverLabel.setVisible(false);

        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(gameOverLabel);
        buttonPanel.add(scoreLabel);

        add(buttonPanel);

        Timer enemyBulletTimer = new Timer(1750, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameRunning) {
                    enemyShoot();
                }
            }
        });
        enemyBulletTimer.start();
    }

    private void moveBackground() {
        backgroundY += 1;

        if (backgroundY >= getHeight()) {
            backgroundY = 0;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameRunning) {
            g.drawImage(backGroundImage.getImage(), 0, backgroundY - getHeight(), this);
                    g.drawImage(backGroundImage.getImage(), 0, backgroundY, this);
            g.drawImage(playerAircraftImage.getImage(), characterX -80 , characterY, this);
            for (Bullet bullet : playerBullets) {
                g.drawImage(bulletImage.getImage(), bullet.getX() -20 , bullet.getY(), this);
            }

            for (Enemy enemy : enemies) {
                enemy.draw(g);
            }
            if (isExplosionActive) {
                g.drawImage(explosionImage.getImage(), explosionLocation.x, explosionLocation.y, this);
            }
            if (isFireAnimationActive) {
                g.drawImage(fireAnimationImage.getImage(), fireAnimationLocation.x, fireAnimationLocation.y, this);
            }
            drawHealthBar(g);

        }
    }

    private void drawHealthBar(Graphics g) {
        Color healthBarColor = Color.GREEN;
        g.setColor(healthBarColor);
        g.fillRect(getWidth() - healthBarWidth - 100, getHeight() - healthBarHeight - 100, currentHealth * healthBarWidth / maxHealth, healthBarHeight);

        g.setColor(Color.BLACK);
        g.drawRect(getWidth() - healthBarWidth - 100, getHeight() - healthBarHeight - 100, healthBarWidth, healthBarHeight);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameRunning) {
            updateCharacterPosition();
            updateBulletPositions();
            updateEnemyPositions();
            checkCollisions();
            repaint();
        }
    }

    private void updateCharacterPosition() {
        if (upPressed && characterY > 0) characterY -= 12;
        if (downPressed && characterY < getHeight() - 50) characterY += 12;
        if (leftPressed && characterX > 0) characterX -= 12;
        if (rightPressed && characterX < getWidth() - 50) characterX += 12;
    }

    private void updateBulletPositions() {
        Iterator<Bullet> iterator = playerBullets.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.move();
            if (bullet.getY() + bullet.getHeight() < 0) {
                iterator.remove();
            }
        }
    }


    private void updateEnemyPositions() {
        for (Enemy enemy : enemies) {
            enemy.moveDown(4);
            enemy.updateBullets();
        }
    }


    private void checkBulletEnemyCollisions() {
        Iterator<Bullet> bulletIterator = playerBullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            Iterator<Enemy> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                if (bullet.intersects(enemy.getBounds())) {
                    bulletIterator.remove();
                    enemy.decreaseHealth();
                    if (enemy.getHealth() <= 0) {
                        enemyIterator.remove();
                        increaseScore();
                    }
                }


                Iterator<EnemyBullet> enemyBulletIterator = enemy.getBullets().iterator();
                while (enemyBulletIterator.hasNext()) {
                    EnemyBullet enemyBullet = enemyBulletIterator.next();
                    if (enemyBullet.intersects(getPlayerBounds())) {
                        decreasePlayerHealth();
                        enemyBulletIterator.remove();
                    }
                }
            }
        }
    }
    private void checkCollisions() {
        checkBulletEnemyCollisions();
        checkPlayerCollisions();
        checkEnemyBulletPlayerCollisions();
    }


    private void checkEnemyBulletPlayerCollisions() {
        for (Enemy enemy : enemies) {
            Iterator<EnemyBullet> enemyBulletIterator = enemy.getBullets().iterator();
            while (enemyBulletIterator.hasNext()) {
                EnemyBullet enemyBullet = enemyBulletIterator.next();
                if (enemyBullet.intersects(getPlayerBounds())) {
                    decreasePlayerHealth();
                    enemyBulletIterator.remove();
                }
            }
        }
    }


    private void checkPlayerCollisions() {
        Rectangle playerBounds = getPlayerBounds();
        for (Enemy enemy : enemies) {
            if (playerBounds.intersects(enemy.getBounds())) {
                decreasePlayerHealth();
                triggerExplosion(enemy.getX(), enemy.getY());
                enemies.remove(enemy);
                break;
            }
        }
    }

    private void triggerExplosion(int x, int y) {
        explosionLocation = new Point(x, y);
        isExplosionActive = true;
        explosionTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isExplosionActive = false;
                explosionTimer.stop();
            }
        });
        explosionTimer.start();
    }



    private Rectangle getPlayerBounds() {
        return new Rectangle(characterX, characterY, 50, 50);
    }

    private void decreasePlayerHealth() {
        playerHealth--;
        if (playerHealth <= 0) endGame();
        currentHealth = playerHealth;
        repaint();
    }

    private void increaseScore() {
        score += 10;
        updateScoreLabel();
    }

    private void updateScoreLabel() {
        scoreLabel.setText("Score: " + score);
    }

    private void endGame() {
        gameRunning = false;
        resetButton.setVisible(true);
        gameOverLabel.setVisible(true);
    }

    public int getScore()
    {
        return score;
    }
    private void resetGame() {
        playerBullets.clear();
        enemies.clear();
        characterX = 250;
        characterY = 400;
        gameRunning = true;
        playerHealth = maxHealth;
        currentHealth = playerHealth;
        score = 0;
        resetButton.setVisible(false);
        gameOverLabel.setVisible(false);
        updateScoreLabel();
        repaint();
    }

    private void spawnEnemy() {
        Random random = new Random();
        int enemyX = random.nextInt(getWidth() - 50);
        int enemyY = -50;
        int enemyWidth = 50;
        int enemyHeight = 50;
        int screenHeight = getHeight();

        Enemy newEnemy = new Enemy(enemyX, enemyY, enemyWidth, enemyHeight, screenHeight);
        enemies.add(newEnemy);
    }

    private void enemyShoot() {
        for (Enemy enemy : enemies) {
            enemy.shoot();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                upPressed = true;
                break;
            case KeyEvent.VK_DOWN:
                downPressed = true;
                break;
            case KeyEvent.VK_LEFT:
                leftPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = true;
                break;
            case KeyEvent.VK_SPACE:
                spacePressed = true;
                Bullet bullet = new PlayerBullet(characterX + 20, characterY - 20, 10, 20);
                playerBullets.add(bullet);
                triggerFireAnimation();
                break;
        }
    }
    private void triggerFireAnimation() {
        fireAnimationLocation = new Point(characterX , characterY -70 );
        isFireAnimationActive = true;
        fireAnimationTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isFireAnimationActive = false;
                fireAnimationTimer.stop();
            }
        });
        fireAnimationTimer.start();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                upPressed = false;
                break;
            case KeyEvent.VK_DOWN:
                downPressed = false;
                break;
            case KeyEvent.VK_LEFT:
                leftPressed = false;
                break;
            case KeyEvent.VK_RIGHT:
                rightPressed = false;
                break;
            case KeyEvent.VK_SPACE:
                spacePressed = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }


    @Override
    public void run() {
        JFrame frame = new JFrame("Character Shooting");
        CharacterShootingGUI panel = new CharacterShootingGUI();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}




