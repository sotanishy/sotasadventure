package sotasadventure;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

/**
 * The class that represents sprites.
 * This class is the parent class of Sota, Enemy, and SpaceShip class.
 * @author Sota Nishiyama
 */
public class Sprite {
    public Vector position = new Vector();
    public Vector velocity = new Vector();

    public int width;
    public int height;

    public int damage;

    public int hp;
    public int maxHP;

    public boolean alive;
    public boolean jumping;
    public boolean swimming;

    public boolean invincible;
    protected double attackedTime;
    protected double invincibleDuration;

    public Image image;
    protected Color healthColor;

    /**
     * Sets the size of the sprite.
     * @param width the width of the sprite.
     * @param height the height of the sprite.
     */
    public Sprite(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Initializes the position, the velocity, the HP, and other conditions of the sprite.
     * @param x the x coordinate of the initial tile of the sprite
     * @param y the y coordinate of the initial tile of the sprite
     */
    public void init(int x, int y) {
        position.x = x;
        position.y = y;

        velocity.x = velocity.y = 0;

        hp = maxHP;

        alive = true;
        jumping = false;
        swimming = false;
        invincible = false;
    }

    /**
     * Updates the position and other conditions of the sprite.
     * @param elapsedTime the time elapsed since the game started
     */
    public void move(double elapsedTime) {
        // set the velocity and the position
        position.add(velocity);

        // make the enemy not invincible when a certain time has passed since attacked
        if (elapsedTime - attackedTime >= invincibleDuration) {
            invincible = false;
        }
    }

    /**
     * Solves collision agains walls
     * @param map the map
     */
    public void solveCollisionAgainstWalls(Map map) {
        int tileX = position.x / Constants.TILE_SIZE;
        int tileY = position.y / Constants.TILE_SIZE;

        if (position.x < 0) {
            tileX = -1;
        }
        if (position.y < 0) {
            tileY = -1;
        }

        if (map.isOutOfMap(tileY + 1)) {
            alive = false;
            return;
        }

        int upperLeft;
        int upperRight;
        int lowerLeft;
        int lowerRight;

        if (tileX == -1) {
            lowerLeft = Map.GROUND;
            upperLeft = Map.GROUND;
        } else {
            lowerLeft = map.getTile(tileX, tileY + 1);
            upperLeft = map.getTile(tileX, tileY);
        }

        if (position.x + width >= map.getWidth()) {
            lowerRight = Map.GROUND;
            upperRight = Map.GROUND;
        } else {
            lowerRight = map.getTile(tileX + 1, tileY + 1);
            upperRight = map.getTile(tileX + 1, tileY);
        }

        if (tileY == -1) {
            upperLeft = Map.GROUND;
            upperRight = Map.GROUND;
        }

        jumping = true;
        swimming = false;

        // check the ground
        if (lowerLeft == Map.GROUND && lowerRight == Map.GROUND) {
            position.y = tileY * Constants.TILE_SIZE;
            jumping = false;
        }

        // check the ceiling
        if (upperLeft == Map.GROUND && upperRight == Map.GROUND) {
            position.y = (tileY + 1) * Constants.TILE_SIZE;
        }

        // check the right wall
        if (upperRight == Map.GROUND && lowerRight == Map.GROUND) {
            position.x = tileX * Constants.TILE_SIZE;
            hitWall("right");
        }

        // check the left wall
        if (upperLeft == Map.GROUND && lowerLeft == Map.GROUND) {
            position.x = (tileX + 1) * Constants.TILE_SIZE;
            hitWall("left");
        }

        // hill
        if (upperRight == Map.GROUND_HILL_LEFT) {
            position.y = tileY * Constants.TILE_SIZE + tileX * Constants.TILE_SIZE - position.x;
            jumping = false;
        } else if (lowerLeft == Map.GROUND_HILL_LEFT) {
            position.y = tileY * Constants.TILE_SIZE;
            jumping = false;
        } else if (lowerRight == Map.GROUND_HILL_LEFT) {
            if (position.y >= (tileX + 1) * Constants.TILE_SIZE - position.x + tileY * Constants.TILE_SIZE) {
                position.y = (tileX + 1) * Constants.TILE_SIZE - position.x + tileY * Constants.TILE_SIZE;
                jumping = false;
            }
        }

        if (upperLeft == Map.GROUND_HILL_RIGHT) {
            position.y = tileY * Constants.TILE_SIZE - (tileX + 1) * Constants.TILE_SIZE + position.x;
            jumping = false;
        } else if (lowerRight == Map.GROUND_HILL_RIGHT) {
            position.y = tileY * Constants.TILE_SIZE;
            jumping = false;
        } else if (lowerLeft == Map.GROUND_HILL_RIGHT) {
            if (position.y >= tileY * Constants.TILE_SIZE - tileX * Constants.TILE_SIZE + position.x) {
                position.y = tileY * Constants.TILE_SIZE - tileX * Constants.TILE_SIZE + position.x;
                jumping = false;
            }
        }

        // diagonal collision
        if (lowerLeft != Map.GROUND && lowerRight == Map.GROUND && upperRight != Map.GROUND && lowerLeft != Map.GROUND_HILL_LEFT && upperRight != Map.GROUND_HILL_LEFT) {
            if (position.x - tileX * Constants.TILE_SIZE < position.y - tileY * Constants.TILE_SIZE) {
                position.x = tileX * Constants.TILE_SIZE;
                hitWall("right");
            } else {
                position.y = tileY * Constants.TILE_SIZE;
                jumping = false;
            }
        }

        if (upperLeft != Map.GROUND && upperRight == Map.GROUND && lowerRight != Map.GROUND) {
            if (position.x - tileX * Constants.TILE_SIZE < (tileY + 1) * Constants.TILE_SIZE - position.y) {
                position.x = tileX * Constants.TILE_SIZE;
                hitWall("right");
            } else {
                position.y = (tileY + 1) * Constants.TILE_SIZE;
            }
        }

        if (lowerLeft == Map.GROUND && lowerRight != Map.GROUND && upperLeft != Map.GROUND && lowerRight != Map.GROUND_HILL_RIGHT && upperLeft != Map.GROUND_HILL_RIGHT) {
            if ((tileX + 1) * Constants.TILE_SIZE - position.x < position.y - tileY * Constants.TILE_SIZE) {
                position.x = (tileX + 1) * Constants.TILE_SIZE;
                hitWall("left");
            } else {
                position.y = tileY * Constants.TILE_SIZE;
                jumping = false;
            }
        }

        if (upperLeft == Map.GROUND && upperRight != Map.GROUND && lowerLeft != Map.GROUND) {
            if ((tileX + 1) * Constants.TILE_SIZE - position.x < (tileY + 1) * Constants.TILE_SIZE - position.y) {
                position.x = (tileX + 1) * Constants.TILE_SIZE;
                hitWall("left");
            } else {
                position.y = (tileY + 1) * Constants.TILE_SIZE;
            }
        }

        // in the water
        if (map.isWater(position.x / Constants.TILE_SIZE, position.y / Constants.TILE_SIZE) || map.isWater(position.x / Constants.TILE_SIZE + 1, position.y / Constants.TILE_SIZE) || jumping && (map.isWater(position.x / Constants.TILE_SIZE, position.y / Constants.TILE_SIZE + 1) || map.isWater(position.x / Constants.TILE_SIZE + 1, position.y / Constants.TILE_SIZE + 1))) {
            swimming = true;
            jumping = false;
        }
    }

    /**
     * Called on hitting a wall.
     * @param side the side which hit the wall
     */
    protected void hitWall(String side) {}

    /**
     * Executed when the sprite is attacked.
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
     * Draws the sprite and its health bar.
     * @param g the graphics
     * @param mapX the x coordinate of the map
     * @param mapY the y coordinate of the map
     */
    public void draw(Graphics g, int mapX, int mapY) {
        g.drawImage(image, position.x + mapX, position.y + mapY, null);

        if (invincible) {
            g.setColor(new Color(255, 0, 0, 50));
            g.fillRect(position.x + mapX, position.y + mapY, width, height);
        }

        // draw the sprite's health bar
        g.setColor(Color.BLACK);
        g.fillRect(position.x + mapX, position.y + mapY - 10, width, 5);
        g.setColor(healthColor);
        g.fillRect(position.x + mapX, position.y + mapY - 10, width * hp / maxHP, 5);
    }
}
