package sotasadventure;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;

/**
 * The class that represents Sota.
 * @author Sota Nishiyama
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
    private double attackedTime;
    private final double INVINCIBLE_DURATION = 2;

    public ArrayList<Bullet> bullets = new ArrayList<Bullet>();

    public boolean sword;
    public int swordWidth = 30;
    public int swordHeight = 10;
    public int swordDamage = 2;
    private double swordTime = .3;
    private double swordStartTime;
    private double swordSuccession = .7;

    public int maxHP = 3;
    public int hp;

    private final int PLAYER_STAND_RIGHT = 0;
    private final int PLAYER_STAND_LEFT = 1;
    private final int PLAYER_JUMP_RIGHT = 2;
    private final int PLAYER_JUMP_LEFT = 3;
    private final int PLAYER_WALK = 4;
    private final int WALK_POSES = 11;
    private int poseCount;

    private Image[] images = new Image[PLAYER_WALK + WALK_POSES * 2];
    private Image attackRightImage;
    private Image attackLeftImage;
    private Image bulletImage;
    private Image swordRightImage;
    private Image swordLeftImage;
    private Image gunRightImage;
    private Image gunLeftImage;

    private Clip gunSound;
    private Clip swordSound;
    private Clip attackedSound;

    /**
     * Sets Sota's size and loads Sota's images.
     * @param width Sota's width
     * @param height Sota's height
     */
    public Sota(int width, int height) {
        this.width = width;
        this.height = height;
        loadResources();
    }

    /**
     * Initializes Sota's position, velocity, HP, and other conditions.
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
     * @param movement The list of Sota's movements.
     * @param elapsedTime the time elapsed since the game started
     */
    public void move(ArrayList<String> movement, double elapsedTime) {

        if (movement.contains("fire")) {
            initBullet(elapsedTime);
            movement.remove("fire");
        }

        if (movement.contains("attack")) {
            sword = true;
            if (elapsedTime - swordStartTime >= swordSuccession) {
                swordStartTime = elapsedTime;
            }
            swordSound.setFramePosition(0);
            swordSound.start();
            movement.remove("attack");
        }

        if (elapsedTime - swordStartTime >= swordTime) {
            sword = false;
        }

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
    public void initBullet(double elapsedTime) {
        // check if a certain time has elapsed since the last bullet was fired
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

        gunSound.setFramePosition(0);
        gunSound.start();
    }

    /**
     * Moves bullets.
     * @param elapsedTime the time elapsed since the game started
     */
    public void moveBullets(double elapsedTime) {
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
    public void attacked(double elapsedTime, int damage) {
        if (invincible) return;

        invincible = true;
        attackedTime = elapsedTime;

        hp -= damage;
        if (hp <= 0) {
            alive = false;
        }

        attackedSound.setFramePosition(0);
        attackedSound.start();
    }

    /**
     * Draws Sota, his weapons, and his health bar.
     * @param g the graphics
     * @param mapX the x coordinate of the map
     * @param mapY the y coordinate of the map
     */
    public void draw(Graphics g, int mapX, int mapY) {
        // draw Sota
        Image image;
        if (sword || !bullets.isEmpty()) {
            if (facingRight) {
                image = attackRightImage;
            } else {
                image = attackLeftImage;
            }
        } else if (jumping) {
            if (facingRight) {
                image = images[PLAYER_JUMP_RIGHT];
            } else {
                image = images[PLAYER_JUMP_LEFT];
            }
        } else if (velocity.x == 0) {
            if (facingRight) {
                image = images[PLAYER_STAND_RIGHT];
            } else {
                image = images[PLAYER_STAND_LEFT];
            }
        } else {
            image = images[PLAYER_WALK + ((velocity.x > 0) ? 0 : WALK_POSES) + poseCount];
        }

        g.drawImage(image, position.x + mapX, position.y + mapY, null);

        if (invincible) {
            g.setColor(new Color(255, 0, 0, 50));
            g.fillRect(position.x + mapX, position.y + mapY, width, height);
        }

        // draw his weapons
        for (Bullet bullet: bullets) {
            g.drawImage(bulletImage, bullet.position.x - Map.TILE_SIZE / 2 + mapX, bullet.position.y - Map.TILE_SIZE / 2 + mapY, null);
        }
        if (!bullets.isEmpty()) {
            if (facingRight) {
                g.drawImage(gunRightImage, position.x + width + mapX, position.y + mapY, null);
            } else {
                g.drawImage(gunLeftImage, position.x - width + mapX, position.y + mapY, null);
            }
        }
        if (sword) {
            if (facingRight) {
                g.drawImage(swordRightImage, position.x + width + mapX, position.y + height / 2 - swordHeight / 2 + mapY, null);
            } else {
                g.drawImage(swordLeftImage, position.x - swordWidth + mapX, position.y + height / 2 - swordHeight / 2 + mapY, null);
            }
        }

        // draw his health bar
        g.setColor(Color.BLACK);
        g.fillRect(position.x + mapX, position.y + mapY - 10, width, 5);
        g.setColor(Color.BLUE);
        g.fillRect(position.x + mapX, position.y + mapY - 10, width * hp / maxHP, 5);
    }

    /**
     * Loads resources.
     */
    private void loadResources() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/resources/images/sota.png"));
        images[PLAYER_STAND_RIGHT] = Util.getScaledImage(ii.getImage(), width, height);

        images[PLAYER_STAND_LEFT] = Util.getFlippedImage(images[PLAYER_STAND_RIGHT]);

        ii = new ImageIcon(getClass().getResource("/resources/images/sota-jump.png"));
        images[PLAYER_JUMP_RIGHT] = Util.getScaledImage(ii.getImage(), width, height);

        images[PLAYER_JUMP_LEFT] = Util.getFlippedImage(images[PLAYER_JUMP_RIGHT]);

        for (int i = 0; i < WALK_POSES; i++) {
            // String url = "../resources/images/Player/p1_walk/PNG/p1_walk" + ((i < 9) ? "0" : "") + (i + 1) + ".png";
            String url = "/resources/images/sota.png";
            ii = new ImageIcon(getClass().getResource(url));
            // facing right
            images[PLAYER_WALK + i] = Util.getScaledImage(ii.getImage(), width, height);
            // facing left
            images[PLAYER_WALK + WALK_POSES + i] = Util.getFlippedImage(images[PLAYER_WALK + i]);
        }

        ii = new ImageIcon(getClass().getResource("/resources/images/sota-attack.png"));
        attackRightImage = Util.getScaledImage(ii.getImage(), width, height);

        attackLeftImage = Util.getFlippedImage(attackRightImage);

        ii = new ImageIcon(getClass().getResource("/resources/images/bullet.png"));
        bulletImage = Util.getScaledImage(ii.getImage(), width, height);

        ii = new ImageIcon(getClass().getResource("/resources/images/sword.png"));
        swordRightImage = Util.getScaledImage(ii.getImage(), swordWidth, swordHeight);

        swordLeftImage = Util.getFlippedImage(swordRightImage);

        ii = new ImageIcon(getClass().getResource("/resources/images/gun.png"));
        gunRightImage = Util.getScaledImage(ii.getImage(), width, height);

        gunLeftImage = Util.getFlippedImage(gunRightImage);

        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource("/resources/audio/gun.wav"));
            gunSound = AudioSystem.getClip();
            gunSound.open(audioIn);

            audioIn = AudioSystem.getAudioInputStream(getClass().getResource("/resources/audio/sword.wav"));
            swordSound = AudioSystem.getClip();
            swordSound.open(audioIn);

            audioIn = AudioSystem.getAudioInputStream(getClass().getResource("/resources/audio/attacked.wav"));
            attackedSound = AudioSystem.getClip();
            attackedSound.open(audioIn);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
