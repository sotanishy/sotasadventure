package sotasadventure;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;

/**
 * The class that represents enemies' space ship.
 * @author Sota Nishiyama
 */
public class SpaceShip extends Sprite {
    private double deployedTime;
    private final double DEPLOY_INTERVAL = 8;
    private final int DEPLOY_MAX = 20;
    private int deployCount = 0;

    /**
     * Sets the size of the space ship and loads images of the space ship.
     * @param width the width of space ship
     * @param height the height of space ship
     */
    public SpaceShip(int width, int height) {
        super(width, height);
        maxHP = 10;
        damage = 1;
        invincibleDuration = 2;
        healthColor = Color.RED;
        deployedTime = -DEPLOY_INTERVAL;
        loadResources();
    }

    /**
     * Initializes the position, the velocity, the HP, and other conditions of the space ship.
     * @param p the position of the initial tile of the space ship
     * @param speedX the speed of the space ship
     */
    public void init(Vector p, int speedX) {
        super.init(p.x, p.y);

        velocity.x = -speedX;

        if (position.x > 0) {
            alive = true;
        } else {
            alive = false;
        }
        deployCount = 0;
    }

    /**
     * Solves collision between the space ship and walls.
     * @param map the map
     */
    @Override
    public void solveCollisionAgainstWalls(Map map) {
        for (int i = position.y / Constants.TILE_SIZE; i <= (position.y + height) / Constants.TILE_SIZE; i++) {
            if (map.getTile(position.x / Constants.TILE_SIZE, i) == Map.GROUND ||
                map.getTile((position.x + width) / Constants.TILE_SIZE, i) == Map.GROUND) {
                velocity.x *= -1;
                break;
            }
        }
    }

    /**
     * Deploys enemies.
     * @param enemies an arraylist of enemies
     * @param elapsedTime the time elapsed since the game started
     * @param enemyWidth the width of enemies
     * @param enemyHeight the height of enemies
     * @param enemySpeed the speed of enemies
     */
    public ArrayList<Enemy> deployEnemies(ArrayList<Enemy> enemies, double elapsedTime, int enemyWidth, int enemyHeight, int enemySpeed) {
        if (elapsedTime - deployedTime > DEPLOY_INTERVAL && deployCount < DEPLOY_MAX) {
            deployedTime = elapsedTime;

            Enemy enemy = new Enemy(enemyWidth, enemyHeight);
            enemy.init(position.x + width / 2 - Constants.TILE_SIZE / 2, position.y + height, enemySpeed);
            enemies.add(enemy);

            deployCount++;
        }
        return enemies;
    }

    /**
     * Loads images of the space ship.
     */
    private void loadResources() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/resources/images/spaceship.png"));
        image = Util.getScaledImage(ii.getImage(), width, (int) (height * 1.33));
    }
}
