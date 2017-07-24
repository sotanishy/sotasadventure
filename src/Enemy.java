package sotasadventure;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;

/**
 * The class that represents an enemy.
 * @author Sota Nishiyama
 */
public class Enemy {

    public Vector position = new Vector();
    public Vector velocity = new Vector();

    public int width;
    public int height;

    private final int MAX_SPEED_Y = 25;

    public boolean jumping;
    public boolean swimming;
    public boolean alive;

    public boolean invincible;
    private double attackedTime;
    private final double INVINCIBLE_DURATION = 1;

    private Image imageFacingLeft;
    private Image imageFacingRight;

    public int maxHP = 3;
    public int hp;

    public int damage = 1;

    /**
     * Sets the size of enemies and loads images of enemies.
     * @param width the width of enemies
     * @param height the height of enemies
     */
    public Enemy(int width, int height) {
        this.width = width;
        this.height = height;
        loadImages();
    }

    /**
     * Initializes the position, the velocity, the HP, and other conditions of the enemy.
     * @param x the x coordinate of the initial tile of the enemy
     * @param y the y coordinate of the initial tile of the enemy
     * @param speedX the speed of the enemy
     */
    public void init(int x, int y, int speedX) {
        // initialize the position
        position.x = x * Map.TILE_SIZE;
        position.y = y * Map.TILE_SIZE;

        // initialize the velocity
        velocity.x = -speedX;
        velocity.y = 0;

        // initialize the HP and other conditions
        hp = maxHP;

        // initialize conditions
        jumping = false;
        swimming = false;
        alive = true;
        invincible = false;
    }

    /**
     * Updates the position and other conditions of the enemy.
     * @param elapsedTime the time elapsed since the game started
     */
    public void move(double elapsedTime) {

        int vx = velocity.x;
        int vy = velocity.y;

        // y velocity
        // gravity
        vy += 3;

        // max speed
        if (vy > MAX_SPEED_Y) {
            vy = MAX_SPEED_Y;
        } else if (vy < -MAX_SPEED_Y) {
            vy = -MAX_SPEED_Y;
        }

        // resistance
        if (swimming) {
            vx *= 0.5;
            vy *= 0.5;
        }

        // set the velocity and the position
        velocity.set(velocity.x, vy);
        position.add(velocity);

        // make the enemy not invincible when a certain time has passed since attacked
        if (elapsedTime - attackedTime >= INVINCIBLE_DURATION) {
            invincible = false;
        }
    }

    /**
     * Executed when the enemy is attacked by Sota.
     * @param elapsedTime the time elapsed since the game started.
     * @param damage the damage
     */
    public void attacked(double elapsedTime, int damage) {
        if (invincible) return;

        invincible = true;
        attackedTime = elapsedTime;

        hp -= damage;
        if (hp <= 0) {
            alive = false;
        }
    }

    /**
     * Draws the enemy and its health bar.
     * @param g the graphics
     * @param mapX the x coordinate of the map
     * @param mapY the y coordinate of the map
     */
    public void draw(Graphics g, int mapX, int mapY) {
        // draw the enemy
        if (velocity.x < 0) {
            g.drawImage(imageFacingLeft, position.x + mapX, position.y + mapY, null);
        } else {
            g.drawImage(imageFacingRight, position.x + mapX, position.y + mapY, null);
        }

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
     * Loads images of enemies.
     */
    private void loadImages() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/resources/images/enemy.png"));
        imageFacingLeft = Util.getScaledImage(ii.getImage(), width, height);

        imageFacingRight = Util.getFlippedImage(imageFacingLeft);
    }
}
