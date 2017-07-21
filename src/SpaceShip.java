package sotasadventure;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;

/**
 * The class that represents enemies' space ship.
 */

public class SpaceShip {
    public Vector position = new Vector();
    public Vector velocity = new Vector();

    public int width;
    public int height;

    public int type;

    public boolean alive;

    public boolean invincible;
    private float attackedTime;
    private final float INVINCIBLE_DURATION = 2;

    public Image image;

    public int maxHP = 10;
    public int hp;

    public int damage = 1;

    private float deployedTime;
    private final float DEPLOY_INTERVAL = 8;

    /**
     * Sets the size of the space ship and loads images of the enemy ship.
     * @param width the width of space ship
     * @param height the height of space ship
     * @param type the type of space ship
     */

    public SpaceShip(int width, int height) {
        this.width = width;
        this.height = height;
        deployedTime = -DEPLOY_INTERVAL;
        loadImages();
    }

    /**
     * Updates conditions of the space ship.
     * @param elapsedTime the time elapsed since the game started
     */

    public void update(float elapsedTime) {
        // make the enemy not invincible when a certain time has passed since attacked
        if (elapsedTime - attackedTime >= INVINCIBLE_DURATION) {
            invincible = false;
        }

        position.add(velocity);
    }

    /**
     * Initializes the position, the velocity, the HP, and other conditions of the space ship.
     * @param p the position of the initial tile of the space ship
     */

    public void init(Vector p, int speedX) {
        position.x = p.x;
        position.y = p.y;

        velocity.x = -speedX;

        hp = maxHP;
        if (position.x > 0) {
            alive = true;
        } else {
            alive = false;
        }
        invincible = false;
    }

    /**
     * Deploys enemies.
     * @param enemies an arraylist of enemies
     * @param map the map
     * @param elapsedTime the time elapsed since the game started
     * @param enemyWidth the width of enemies
     * @param enemyHeight the height of enemies
     * @param enemySpeed the speed of enemies
     */

    public ArrayList<Enemy> deployEnemies(ArrayList<Enemy> enemies, float elapsedTime, int enemyWidth, int enemyHeight, int enemySpeed) {
        if (elapsedTime - deployedTime > DEPLOY_INTERVAL) {
            deployedTime = elapsedTime;

            Enemy enemy = new Enemy(enemyWidth, enemyHeight);
            enemy.init((position.x + width / 2) / Map.TILE_SIZE, (position.y + height) / Map.TILE_SIZE, enemySpeed);
            enemies.add(enemy);
        }
        return enemies;
    }

    /**
     * Executed when the space ship is attacked by Sota.
     * @param elapsedTime the time elapsed since the game started.
     * @param damage the damage
     */

    public void attacked(float elapsedTime, int damage) {
        if (invincible) return;

        invincible = true;
        attackedTime = elapsedTime;

        hp -= damage;
        if (hp <= 0) {
            alive = false;
        }
    }

    /**
     * Draws the space ship and its health bar.
     * @param g the graphics
     * @param mapX the x coordinate of the map
     * @param mapY the y coordinate of the map
     */

    public void draw(Graphics g, int mapX, int mapY) {
        // draw the space ship
        g.drawImage(image, position.x + mapX, position.y + mapY, null);

        if (invincible) {
            g.setColor(new Color(255, 0, 0, 50));
            g.fillRect(position.x + mapX, position.y + mapY, width, height);
        }

        // draw its health bar
        g.setColor(Color.BLACK);
        g.fillRect(position.x + mapX, position.y + mapY - 10, width, 5);
        g.setColor(Color.RED);
        g.fillRect(position.x + mapX, position.y + mapY - 10, width * hp / maxHP, 5);
    }

    /**
     * Loads images of the space ship.
     */

    private void loadImages() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/images/spaceship.png"));
        image = Util.getScaledImage(ii.getImage(), width, (int) (height * 1.33));
    }
}
