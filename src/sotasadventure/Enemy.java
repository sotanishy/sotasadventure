package sotasadventure;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;

/**
 * The class that represents an enemy.
 * @author Sota Nishiyama
 */
public class Enemy extends Sprite {
    private final int MAX_SPEED_Y = 25;

    private int speedX;

    private Image leftImage;
    private Image rightImage;

    /**
     * Sets the size of enemies and loads images of enemies.
     * @param width the width of enemies
     * @param height the height of enemies
     */
    public Enemy(int width, int height) {
        super(width, height);
        maxHP = 3;
        damage = 1;
        invincibleDuration = 1;
        healthColor = Color.RED;
        loadResources();
    }

    /**
     * Initializes the position, the velocity, the HP, and other conditions of the enemy.
     * @param x the x coordinate of the initial tile of the enemy
     * @param y the y coordinate of the initial tile of the enemy
     * @param speedX the speed of the enemy
     */
    public void init(int x, int y, int speedX) {
        super.init(x, y);

        this.speedX = speedX;
        velocity.x = -speedX;
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
        vy += Constants.GRAVITY;

        // max speed
        if (vy > MAX_SPEED_Y) {
            vy = MAX_SPEED_Y;
        } else if (vy < -MAX_SPEED_Y) {
            vy = -MAX_SPEED_Y;
        }

        // resistance
        if (swimming) {
            vx = (int) (speedX * Constants.WATER_RESISTANCE * (velocity.x > 0 ? 1 : -1));
            vy *= Constants.WATER_RESISTANCE;
        }

        // set the velocity and the position
        velocity.x = vx;
        velocity.y = vy;

        super.move(elapsedTime);
    }

    /**
     *
     */
    @Override
    public void solveCollisionAgainstWalls(Map map) {
        int tileX = position.x / Constants.TILE_SIZE;
        int tileY = position.y / Constants.TILE_SIZE;

        if (position.x < 0) {
            tileX = -1;
        }
        if (position.y < 0) {
            tileY = -1;
        }

        int upperLeft;
        int upperRight;
        int lowerLeft;
        int lowerRight;

        if (tileX == -1) {
            upperLeft = Map.GROUND;
            lowerLeft = Map.GROUND;
        } else {
            upperLeft = map.getTile(tileX, tileY);
            lowerLeft = map.getTile(tileX, tileY + 1);
        }

        if (position.x + width >= map.getWidth()) {
            upperRight = Map.GROUND;
            lowerRight = Map.GROUND;
        } else {
            upperRight = map.getTile(tileX + 1, tileY);
            lowerRight = map.getTile(tileX + 1, tileY + 1);
        }

        if (tileY == -1) {
            upperLeft = Map.GROUND;
            upperRight = Map.GROUND;
        }

        // turn around at the edge
        if (lowerLeft != Map.GROUND && lowerLeft != Map.GROUND_HILL_LEFT && lowerRight == Map.GROUND && velocity.x < 0 ||
            lowerRight != Map.GROUND && lowerRight != Map.GROUND_HILL_RIGHT && lowerLeft == Map.GROUND && velocity.x > 0) {
            velocity.x *= -1;
        }

        // hill
        if (velocity.x < 0) {
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
        } else {
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
        }

        super.solveCollisionAgainstWalls(map);
    }

    /**
     * Change the direction the enemy is going on hitting a wall.
     * @param side the side which hit the wall
     */
    @Override
    protected void hitWall(String side) {
        if (side.equals("right") && velocity.x > 0 ||
            side.equals("left") && velocity.x < 0) {
            velocity.x *= -1;
        }
    }

    /**
     * Draws the enemy and its health bar.
     * @param g the graphics
     * @param mapX the x coordinate of the map
     * @param mapY the y coordinate of the map
     */
    @Override
    public void draw(Graphics g, int mapX, int mapY) {
        // set the enemy's image
        if (velocity.x < 0) {
            image = leftImage;
        } else {
            image = rightImage;
        }

        super.draw(g, mapX, mapY);
    }

    /**
     * Loads images of enemies.
     */
    private void loadResources() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/resources/images/enemy.png"));
        leftImage = Util.getScaledImage(ii.getImage(), width, height);

        rightImage = Util.getFlippedImage(leftImage);
    }
}
