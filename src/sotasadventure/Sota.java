package sotasadventure;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;

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
public class Sota extends Sprite {
    private double friction;

    private final int MAX_SPEED_X = 15;
    private final int MAX_SPEED_Y = 25;
    private final int JUMP_CUTOFF = -10;

    public boolean facingRight;

    public ArrayList<Bullet> bullets = new ArrayList<Bullet>();

    public boolean sword;
    public Vector swordPosition = new Vector();
    public int swordWidth = 30;
    public int swordHeight = 10;
    public int swordDamage = 2;
    private double swordTime = .3;
    private double swordStartTime;
    private double swordSuccession = .7;

    private Image standRightImage;
    private Image standLeftImage;
    private Image jumpRightImage;
    private Image jumpLeftImage;
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
        super(width, height);
        maxHP = 3;
        invincibleDuration = 2;
        healthColor = Color.BLUE;
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
        vy += Constants.GRAVITY;

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
            vx *= Constants.WATER_RESISTANCE;
            vy *= Constants.WATER_RESISTANCE;
        }

        // set the velocity and the position
        velocity.x = vx;
        velocity.y = vy;

        super.move(elapsedTime);
    }

    /**
     * Executed when Sota is attacked by an enemy.
     * @param elapsedTime the time elapsed since the game started.
     * @param damage the damage
     */
    @Override
    public void attacked(double elapsedTime, int damage) {
        int hpBeforeAttacked = hp;

        super.attacked(elapsedTime, damage);

        if (hpBeforeAttacked != hp) {
            attackedSound.setFramePosition(0);
            attackedSound.start();
        }
    }

    /**
     * Draws Sota, his weapons, and his health bar.
     * @param g the graphics
     * @param mapX the x coordinate of the map
     * @param mapY the y coordinate of the map
     */
    @Override
    public void draw(Graphics g, int mapX, int mapY) {
        // set Sota's image
        if (sword || !bullets.isEmpty()) {
            if (facingRight) {
                image = attackRightImage;
            } else {
                image = attackLeftImage;
            }
        } else if (jumping) {
            if (facingRight) {
                image = jumpRightImage;
            } else {
                image = jumpLeftImage;
            }
        } else {
            if (facingRight) {
                image = standRightImage;
            } else {
                image = standLeftImage;
            }
        }

        super.draw(g, mapX, mapY);

        // draw his weapons
        for (Bullet bullet: bullets) {
            g.drawImage(bulletImage, bullet.position.x - Constants.TILE_SIZE / 2 + mapX, bullet.position.y - Constants.TILE_SIZE / 2 + mapY, null);
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

        Bullet bullet = new Bullet(elapsedTime);

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
     * Loads resources.
     */
    private void loadResources() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/resources/images/sota.png"));
        standRightImage = Util.getScaledImage(ii.getImage(), width, height);

        standLeftImage = Util.getFlippedImage(standRightImage);

        ii = new ImageIcon(getClass().getResource("/resources/images/sota-jump.png"));
        jumpRightImage = Util.getScaledImage(ii.getImage(), width, height);

        jumpLeftImage = Util.getFlippedImage(jumpRightImage);

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
