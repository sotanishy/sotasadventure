package sotasadventure;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;

/**
 * The class that represents Sota.
 */

public class Sota {

    public Vector position = new Vector();
    public Vector velocity = new Vector();

    public int width;
    public int height;

    private double friction;

    private final int MAX_SPEED_X = 15;
    private final int MAX_SPEED_Y = 25;
    private final int JUMP_CUTOFF = -10;

    public boolean jumping;
    public boolean swimming;
    public boolean alive;
    public boolean facingRight;

    public boolean invincible;
    private float attackedTime;
    private final float INVINCIBLE_DURATION = 2;

    private final int PLAYER_STAND_RIGHT = 0;
    private final int PLAYER_STAND_LEFT = 1;
    private final int PLAYER_JUMP_RIGHT = 2;
    private final int PLAYER_JUMP_LEFT = 3;
    private final int PLAYER_WALK = 4;
    private final int WALK_POSES = 11;
    private int poseCount;
    private Image[] images = new Image[PLAYER_WALK + WALK_POSES * 2];

    public ArrayList<Bullet> bullets = new ArrayList<Bullet>();
    private Image bulletImage;

    public boolean sword;
    public int swordWidth = 30;
    public int swordHeight = 10;
    public float swordTime = (float) .3;
    public float swordSuccession = (float) .7;
    public int swordDamage = 2;

    public int maxHP = 3;
    public int hp;

    /**
     * Sets Sota's size and loads Sota's images.
     * @param width Sota's width
     * @param height Sota's height
     */

    public Sota(int width, int height) {
        this.width = width;
        this.height = height;
        loadImages();
    }

    /**
     * Initializes Sota's position, velocity, and other conditions.
     * @param map the current map
     */

    public void init(Map map) {
        // initialize the position
        Vector p = map.getStartingPoint();
        position.x = p.x;
        position.y = p.y;

        // initialize the velocity and other conditions
        velocity.x = velocity.y = 0;
        friction = map.getFriction();
        poseCount = 0;
        jumping = false;
        swimming = false;
        alive = true;
        facingRight = true;
        invincible = false;
        sword = false;
    }

    /**
     * Initializes Sota's HP.
     */

    public void initHP() {
        hp = maxHP;
    }

    /**
     * Updates Sota's position and other conditions.
     * @param movement The list of Sota's movements the player is ordering.
     * @param elapsedTime the time elapsed since the game started
     */

    public void move(ArrayList movement, float elapsedTime) {
        int vx = velocity.x;
        int vy = velocity.y;

        // x velocity
        if (movement.contains("right") && movement.contains("left")) {
            vx = 0;
        } else if (movement.contains("right")) {
            vx += 10;
            facingRight = true;
        } else if (movement.contains("left")) {
            vx -= 10;
            facingRight = false;
        }

        // frictional force
        if (!jumping && !swimming) vx *= friction;

        // max speed
        if (vx > MAX_SPEED_X) {
            vx = MAX_SPEED_X;
        } else if (vx < -MAX_SPEED_X) {
            vx = -MAX_SPEED_X;
        }

        // y velocity
        // jump
        if (movement.contains("up")) {
            if (!jumping) {
                jumping = true;
                vy = -27;
            }
        }

        // cut off jump
        if (!movement.contains("up") && vy < JUMP_CUTOFF) {
            vy = JUMP_CUTOFF;
        }

        // gravity
        vy += 3;

        // max speed
        if (vy > MAX_SPEED_Y) {
            vy = MAX_SPEED_Y;
        } else if (vy < -MAX_SPEED_Y) {
            vy = -MAX_SPEED_Y;
        }

        // resistance
        if (!swimming) {
            vx *= 0.9;
        } else {
            vx *= 0.5;
            vy *= 0.5;
        }

        // walking pose
        if (vx == 0) {
            poseCount = 0;
        } else {
            poseCount++;
            poseCount %= WALK_POSES;
        }

        // set the velocity and the position
        velocity.set(vx, vy);
        position.add(velocity);

        // make Sota not invincible when a certain time has passed since attacked
        if (elapsedTime - attackedTime >= INVINCIBLE_DURATION) {
            invincible = false;
        }
    }

    /**
     * Initializes the bullet.
     * @param elapsedTime the time elapsed since the game started
     */

    public void initBullet(float elapsedTime) {
        // a certain time has to elapse since the last bullet was fired to fire a new bullet
        if (bullets.size() > 0 && elapsedTime - bullets.get(bullets.size() - 1).firedTime < Bullet.SUCCESSION) {
            return;
        }

        Bullet bullet = new Bullet();

        bullet.firedTime = elapsedTime;

        if (facingRight) {
            bullet.position.x = position.x + height;
            bullet.position.y = position.y + height / 2;
            bullet.speed = 30;
        } else {
            bullet.position.x = position.x;
            bullet.position.y = position.y + height / 2;
            bullet.speed = -30;
        }

        bullets.add(bullet);
    }

    /**
     * Moves bullets.
     * @param elapsedTime the time elapsed since the game started
     */

    public void moveBullets(float elapsedTime) {
        Iterator<Bullet> i = bullets.iterator();

        while (i.hasNext()) {
            Bullet bullet = i.next();

            if (elapsedTime - bullet.firedTime > Bullet.DURATION) {
                i.remove();
            }

            bullet.position.x += bullet.speed;
        }
    }

    /**
     * Executed when Sota is attacked by an enemy.
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
     * Draws Sota, his weapons, and his health bar.
     * @param g the graphics
     * @param mapX the x coordinate of the map
     * @param mapY the y coordinate of the map
     */

    public void draw(Graphics g, int mapX, int mapY) {
        // draw Sota
        int i;
        if (jumping) {
            if (facingRight) {
                i = PLAYER_JUMP_RIGHT;
            } else {
                i = PLAYER_JUMP_LEFT;
            }
        } else if (velocity.x == 0) {
            if (facingRight) {
                i = PLAYER_STAND_RIGHT;
            } else {
                i = PLAYER_STAND_LEFT;
            }

        } else {
            i = PLAYER_WALK + ((velocity.x > 0) ? 0 : WALK_POSES) + poseCount;
        }

        g.drawImage(images[i], position.x + mapX, position.y + mapY, null);

        if (invincible) {
            g.setColor(new Color(255, 0, 0, 50));
            g.fillRect(position.x + mapX, position.y + mapY, width, height);
        }

        // draw his weapons
        for (Bullet bullet: bullets) {
            g.drawImage(bulletImage, bullet.position.x - Map.TILE_SIZE / 2 + mapX, bullet.position.y - Map.TILE_SIZE / 2 + mapY, null);
        }
        if (sword) {
            g.setColor(Color.PINK);
            if (facingRight) {
                g.fillRect(position.x + width + mapX, position.y + height / 2 - swordHeight + mapY, swordWidth, swordHeight);
            } else {
                g.fillRect(position.x - swordWidth + mapX, position.y + height / 2 - swordHeight + mapY, swordWidth, swordHeight);
            }
        }

        // draw his health bar
        g.setColor(Color.BLACK);
        g.fillRect(position.x + mapX, position.y + mapY - 10, width, 5);
        g.setColor(Color.BLUE);
        g.fillRect(position.x + mapX, position.y + mapY - 10, width * hp / maxHP, 5);
    }

    /**
     * Loads Sota's images.
     */

    private void loadImages() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/images/sota.png"));
        images[PLAYER_STAND_RIGHT] = Util.getScaledImage(ii.getImage(), width, height);

        images[PLAYER_STAND_LEFT] = Util.getFlippedImage(images[PLAYER_STAND_RIGHT]);

        ii = new ImageIcon(getClass().getResource("/images/sota.png"));
        images[PLAYER_JUMP_RIGHT] = Util.getScaledImage(ii.getImage(), width, height);

        images[PLAYER_JUMP_LEFT] = Util.getFlippedImage(images[PLAYER_JUMP_RIGHT]);

        for (int i = 0; i < WALK_POSES; i++) {
            // String url = "../resources/images/Player/p1_walk/PNG/p1_walk" + ((i < 9) ? "0" : "") + (i + 1) + ".png";
            String url = "/images/sota.png";
            ii = new ImageIcon(getClass().getResource(url));
            // facing right
            images[PLAYER_WALK + i] = Util.getScaledImage(ii.getImage(), width, height);
            // facing left
            images[PLAYER_WALK + WALK_POSES + i] = Util.getFlippedImage(images[PLAYER_WALK + i]);
        }

        ii = new ImageIcon(getClass().getResource("/images/bullet.png"));
        bulletImage = Util.getScaledImage(ii.getImage(), Map.TILE_SIZE, Map.TILE_SIZE);
    }
}
