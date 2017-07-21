package sotasadventure;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 * The class that deals with each stage.
 */

public class StageState extends State {

    private StateMachine gameMode;

    private boolean run;
    private boolean gameOver;
    private boolean gameClear;

    private Sota sota;
    private ArrayList<Enemy> enemies;
    private SpaceShip spaceShip;

    private int timeLimit;
    private float startTime = -1;
    private float elapsedTime;
    private int timeRemaining;

    private float swordStartTime;

    private int lives;

    private ArrayList<int[]> coins = new ArrayList<int[]>();
    private int earnedCoins = 0;

    private ArrayList<int[]> gems = new ArrayList<int[]>();
    private int earnedGems = 0;

    private final int TILE_SIZE = 50;

    private ArrayList<String> movement = new ArrayList<String>();

    private Image bg;

    private Map map = new Map();

    private Image heartImage;
    private Image coinImage;
    private Image gemImage;
    private Image clockImage;

    /**
     * Instanciates characters, loads images, and adds key listener.
     * @param gameMode The state machine of the game
     */

    public StageState(StateMachine gameMode) {

        // set the state machine
        this.gameMode = gameMode;

        // characters
        sota = new Sota(TILE_SIZE, TILE_SIZE);
        spaceShip = new SpaceShip(TILE_SIZE * 5, TILE_SIZE * 3);

        // load images
        loadImages();

        // add key listener to move Sota
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT:
                    if (!movement.contains("right")) movement.add("right");
                    break;

                    case KeyEvent.VK_LEFT:
                    if (!movement.contains("left")) movement.add("left");
                    break;

                    case KeyEvent.VK_UP:
                    if (!movement.contains("up")) movement.add("up");
                    break;

                    case KeyEvent.VK_ENTER:
                    if (gameOver || gameClear) {
                        gameMode.change("worldmap");
                    }
                    break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT:
                    movement.remove("right");
                    break;

                    case KeyEvent.VK_LEFT:
                    movement.remove("left");
                    break;

                    case KeyEvent.VK_UP:
                    movement.remove("up");
                    break;

                    case KeyEvent.VK_F:
                    if (!movement.contains("fire")) movement.add("fire");
                    break;

                    case KeyEvent.VK_A:
                    if (!movement.contains("attack")) movement.add("attack");
                }
            }
        });
        setFocusable(true);
    }

    @Override
    public void update(float elapsedTime) {

        this.elapsedTime = elapsedTime;

        if (!run) return;

        if (startTime == -1) {
            startTime = elapsedTime;
        }
        timeRemaining = (int) (timeLimit - elapsedTime + startTime);
        if (timeRemaining <= 0) {
            sota.alive = false;
        }

        if (movement.contains("fire")) {
            sota.initBullet(elapsedTime);
            movement.remove("fire");
        }

        if (movement.contains("attack")) {
            sota.sword = true;
            if (elapsedTime - swordStartTime >= sota.swordSuccession) {
                swordStartTime = elapsedTime;
            }
            movement.remove("attack");
        }

        // Sota
        if (movement.contains("up") &&
            (map.getTile(sota.position.x / TILE_SIZE, sota.position.y / TILE_SIZE) == Map.DOOR_CLOSED || map.getTile(sota.position.x / TILE_SIZE + 1, sota.position.y / TILE_SIZE) == Map.DOOR_CLOSED)) {
            map.next();
            sota.init(map);
            spaceShip.init(map.getSpaceShipPosition(), map.getSpaceShipSpeed());
            enemies = map.getEnemies(TILE_SIZE, TILE_SIZE);
            coins = map.getCoins();
            gems.clear();
        }
        sota.move(movement, elapsedTime);

        // move the bullet
        sota.moveBullets(elapsedTime);
        bulletLoop: for (int i = 0; i < sota.bullets.size(); i++) {
            Bullet bullet = sota.bullets.get(i);

            int x = bullet.position.x;
            int y = bullet.position.y;

            if (map.isGround(map.getTile(x / Map.TILE_SIZE, y / Map.TILE_SIZE)) ||
                (map.getTile(x / Map.TILE_SIZE, y / Map.TILE_SIZE) == Map.GROUND_HILL_LEFT && TILE_SIZE - x % TILE_SIZE < y % TILE_SIZE) ||
                (map.getTile(x / Map.TILE_SIZE, y / Map.TILE_SIZE) == Map.GROUND_HILL_RIGHT && x % TILE_SIZE < y % TILE_SIZE)) {
                sota.bullets.remove(bullet);
                i--;
                continue;
            }

            for (Enemy enemy: enemies) {
                if (!enemy.alive) continue;

                if (x > enemy.position.x && x < enemy.position.x + Map.TILE_SIZE &&
                    y > enemy.position.y && y < enemy.position.y + Map.TILE_SIZE) {
                    enemy.attacked(elapsedTime, bullet.damage);
                    if (!enemy.alive) {
                        gems.add(new int[] {enemy.position.x, enemy.position.y});
                    }
                    sota.bullets.remove(bullet);
                    i--;
                    continue bulletLoop;
                }
            }
            if (spaceShip.alive &&
                x > spaceShip.position.x && x < spaceShip.position.x + spaceShip.width &&
                y > spaceShip.position.y && y < spaceShip.position.y + spaceShip.height) {
                spaceShip.attacked(elapsedTime, bullet.damage);
                if (!spaceShip.alive) {
                    gameClear = true;
                    run = false;
                }
                sota.bullets.remove(bullet);
                i--;
                continue;
            }
        }

        // sword
        if (sota.sword) {
            if (elapsedTime - swordStartTime >= sota.swordTime) {
                sota.sword = false;
            } else {
                for (Enemy enemy: enemies) {
                    if (!enemy.alive) continue;

                    if (sota.position.y + TILE_SIZE / 2 + sota.swordHeight / 2 >= enemy.position.y && sota.position.y + TILE_SIZE / 2 - sota.swordHeight / 2 <= enemy.position.y + TILE_SIZE) {
                        if (sota.facingRight && sota.position.x + TILE_SIZE <= enemy.position.x + TILE_SIZE && sota.position.x + TILE_SIZE + sota.swordWidth >= enemy.position.x ||
                            !sota.facingRight && sota.position.x - sota.swordWidth <= enemy.position.x + TILE_SIZE && sota.position.x >= enemy.position.x) {
                            enemy.attacked(elapsedTime, sota.swordDamage);
                            if (!enemy.alive) {
                                gems.add(new int[] {enemy.position.x, enemy.position.y});
                            }
                        }
                    }
                }
                if (spaceShip.alive &&
                    sota.position.y + TILE_SIZE / 2 + sota.swordHeight / 2 >= spaceShip.position.y && sota.position.y + TILE_SIZE / 2 - sota.swordHeight / 2 <= spaceShip.position.y + spaceShip.height) {
                    if (sota.facingRight && sota.position.x + TILE_SIZE <= spaceShip.position.x + TILE_SIZE * spaceShip.width && sota.position.x + TILE_SIZE + sota.swordWidth >= spaceShip.position.x ||
                        !sota.facingRight && sota.position.x - sota.swordWidth <= spaceShip.position.x + TILE_SIZE * spaceShip.width && sota.position.x >= spaceShip.position.x) {
                        spaceShip.attacked(elapsedTime, sota.swordDamage);
                        if (!spaceShip.alive) {
                            gameClear = true;
                            run = false;
                        }
                    }
                }
            }

        }

        // update the space ship
        spaceShip.update(elapsedTime);

        // deploy enemies from the ship
        if (spaceShip.alive) {
            spaceShip.deployEnemies(enemies, elapsedTime, TILE_SIZE, TILE_SIZE, map.getEnemySpeed());
        }

        // enemies
        for (Enemy enemy: enemies) {
            enemy.move(elapsedTime);
        }

        // collision detection with walls
        solveSotasCollision();

        // collision detection with walls

        int tileX, tileY;
        int ground;
        int groundRight;

        int ceiling;
        int ceilingRight;

        int rightWall;
        int rightWallBottom;

        int leftWall;
        int leftWallBottom;

        for (int i = 0; i < enemies.size() - 1; i++) {
            Enemy enemy1 = enemies.get(i);
            if (!enemy1.alive) continue;

            for (int j = i + 1; j < enemies.size(); j++) {
                Enemy enemy2 = enemies.get(j);
                if (!enemy2.alive) continue;

                if ((enemy1.position.y <= enemy2.position.y && enemy2.position.y <= enemy1.position.y + TILE_SIZE ||
                        enemy2.position.y <= enemy1.position.y && enemy1.position.y <= enemy2.position.y + TILE_SIZE) &&
                    (enemy1.position.x < enemy2.position.x && enemy2.position.x < enemy1.position.x + TILE_SIZE ||
                        enemy2.position.x < enemy1.position.x && enemy1.position.x < enemy2.position.x + TILE_SIZE)) {
                    enemy1.velocity.x *= -1;
                    enemy2.velocity.x *= -1;
                    if (enemy1.position.x > enemy2.position.x) {
                        enemy1.position.x = enemy2.position.x + TILE_SIZE;
                    } else {
                        enemy2.position.x = enemy1.position.x + TILE_SIZE;
                    }
                }
            }
        }

        // get the tile position where the enemy is
        for (Enemy enemy: enemies) {

            if (!enemy.alive) continue;

            tileX = enemy.position.x / TILE_SIZE;
            tileY = enemy.position.y / TILE_SIZE;

            // an enemy falling out of the map
            if (map.isOutOfMap(tileY + 1)) {
                enemy.alive = false;
                continue;
            }

            if (enemy.position.x < 0) {
                tileX = -1;
            }

            if (tileX == -1) {
                ground = Map.GROUND_CENTER;
                ceiling = Map.GROUND_CENTER;
            } else {
                ground = map.getTile(tileX, tileY + 1);
                ceiling = map.getTile(tileX, tileY);
            }

            if (enemy.position.x + TILE_SIZE >= map.getWidth()) {
                groundRight = Map.GROUND_CENTER;
                ceilingRight = Map.GROUND_CENTER;
            } else {
                groundRight = map.getTile(tileX + 1, tileY + 1);
                ceilingRight = map.getTile(tileX + 1, tileY);
            }

            rightWall = ceilingRight;
            rightWallBottom = groundRight;

            leftWall = ceiling;
            leftWallBottom = ground;

            enemy.jumping = true;
            enemy.swimming = false;

            // turn around at the edge
            if (!map.isGround(ground) && ground != Map.GROUND_HILL_LEFT && map.isGround(groundRight) && enemy.velocity.x < 0 ||
                !map.isGround(groundRight) && groundRight != Map.GROUND_HILL_RIGHT && map.isGround(ground) && enemy.velocity.x > 0) {
                enemy.velocity.x *= -1;
            }

            // check the ground
            if (map.isGround(ground) && map.isGround(groundRight)) {
                enemy.position.y = tileY * TILE_SIZE;
                enemy.jumping = false;
            }

            // check the ceiling
            if (map.isGround(ceiling) && map.isGround(ceilingRight)) {
                enemy.position.y = (tileY + 1) * TILE_SIZE;
            }

            // check the right wall
            if (map.isGround(rightWall) && map.isGround(rightWallBottom) && enemy.velocity.x > 0) {
                enemy.position.x = tileX * TILE_SIZE;
                enemy.velocity.x *= -1;
            }

            // check the left wall
            if (map.isGround(leftWall) && map.isGround(leftWallBottom) && enemy.velocity.x < 0) {
                enemy.position.x = (tileX + 1) * TILE_SIZE;
                enemy.velocity.x *= -1;
            }

            // hill
            if (enemy.velocity.x < 0) {
                if (rightWall == Map.GROUND_HILL_LEFT) {
                    enemy.position.y = tileY * TILE_SIZE + tileX * TILE_SIZE - enemy.position.x;
                    enemy.jumping = false;
                } else if (ground == Map.GROUND_HILL_LEFT) {
                    enemy.position.y = tileY * TILE_SIZE;
                    enemy.jumping = false;
                } else if (groundRight == Map.GROUND_HILL_LEFT) {
                    if (enemy.position.y >= (tileX + 1) * TILE_SIZE - enemy.position.x + tileY * TILE_SIZE) {
                        enemy.position.y = (tileX + 1) * TILE_SIZE - enemy.position.x + tileY * TILE_SIZE;
                        enemy.jumping = false;
                    }
                }

                if (leftWall == Map.GROUND_HILL_RIGHT) {
                    enemy.position.y = tileY * TILE_SIZE - (tileX + 1) * TILE_SIZE + enemy.position.x;
                    enemy.jumping = false;
                } else if (groundRight == Map.GROUND_HILL_RIGHT) {
                    enemy.position.y = tileY * TILE_SIZE;
                    enemy.jumping = false;
                } else if (ground == Map.GROUND_HILL_RIGHT) {
                    if (enemy.position.y >= tileY * TILE_SIZE - tileX * TILE_SIZE + enemy.position.x) {
                        enemy.position.y = tileY * TILE_SIZE - tileX * TILE_SIZE + enemy.position.x;
                        enemy.jumping = false;
                    }
                }
            } else {
                if (leftWall == Map.GROUND_HILL_RIGHT) {
                    enemy.position.y = tileY * TILE_SIZE - (tileX + 1) * TILE_SIZE + enemy.position.x;
                    enemy.jumping = false;
                } else if (groundRight == Map.GROUND_HILL_RIGHT) {
                    enemy.position.y = tileY * TILE_SIZE;
                    enemy.jumping = false;
                } else if (ground == Map.GROUND_HILL_RIGHT) {
                    if (enemy.position.y >= tileY * TILE_SIZE - tileX * TILE_SIZE + enemy.position.x) {
                        enemy.position.y = tileY * TILE_SIZE - tileX * TILE_SIZE + enemy.position.x;
                        enemy.jumping = false;
                    }
                }

                if (rightWall == Map.GROUND_HILL_LEFT) {
                    enemy.position.y = tileY * TILE_SIZE + tileX * TILE_SIZE - enemy.position.x;
                    enemy.jumping = false;
                } else if (ground == Map.GROUND_HILL_LEFT) {
                    enemy.position.y = tileY * TILE_SIZE;
                    enemy.jumping = false;
                } else if (groundRight == Map.GROUND_HILL_LEFT) {
                    if (enemy.position.y >= (tileX + 1) * TILE_SIZE - enemy.position.x + tileY * TILE_SIZE) {
                        enemy.position.y = (tileX + 1) * TILE_SIZE - enemy.position.x + tileY * TILE_SIZE;
                        enemy.jumping = false;
                    }
                }
            }

            // diagonal collision
            if (!map.isGround(ground) && map.isGround(groundRight) && !map.isGround(rightWall) && ground != Map.GROUND_HILL_LEFT && rightWall != Map.GROUND_HILL_LEFT) {
                if (enemy.position.x - tileX * TILE_SIZE < enemy.position.y - tileY * TILE_SIZE) {
                    enemy.position.x = tileX * TILE_SIZE;
                    if (enemy.velocity.x > 0) enemy.velocity.x *= -1;
                } else {
                    enemy.position.y = tileY * TILE_SIZE;
                }
            }

            if (!map.isGround(ceiling) && map.isGround(ceilingRight) && !map.isGround(rightWallBottom)) {
                if (enemy.position.x - tileX * TILE_SIZE < (tileY + 1) * TILE_SIZE - enemy.position.y) {
                    enemy.position.x = tileX * TILE_SIZE;
                    if (enemy.velocity.x > 0) enemy.velocity.x *= -1;
                } else {
                    enemy.position.y = (tileY + 1) * TILE_SIZE;
                }
            }

            if (map.isGround(ground) && !map.isGround(groundRight) && !map.isGround(leftWall) && groundRight != Map.GROUND_HILL_RIGHT && leftWall != Map.GROUND_HILL_RIGHT) {
                if ((tileX + 1) * TILE_SIZE - enemy.position.x < enemy.position.y - tileY * TILE_SIZE) {
                    enemy.position.x = (tileX + 1) * TILE_SIZE;
                    if (enemy.velocity.x < 0) enemy.velocity.x *= -1;
                } else {
                    enemy.position.y = tileY * TILE_SIZE;
                    enemy.jumping = false;
                }
            }

            if (map.isGround(ceiling) && !map.isGround(ceilingRight) && !map.isGround(leftWallBottom)) {
                if ((tileX + 1) * TILE_SIZE - enemy.position.x < (tileY + 1) * TILE_SIZE - enemy.position.y) {
                    enemy.position.x = (tileX + 1) * TILE_SIZE;
                    if (enemy.velocity.x < 0) enemy.velocity.x *= -1;
                } else {
                    enemy.position.y = (tileY + 1) * TILE_SIZE;
                }
            }

            // in the water
            if (map.isWater(enemy.position.x / TILE_SIZE, enemy.position.y / TILE_SIZE) || map.isWater(enemy.position.x / TILE_SIZE + 1, enemy.position.y / TILE_SIZE) || enemy.jumping && (map.isWater(enemy.position.x / TILE_SIZE, enemy.position.y / TILE_SIZE + 1) || map.isWater(enemy.position.x / TILE_SIZE + 1, enemy.position.y / TILE_SIZE + 1))) {
                enemy.swimming = true;
                enemy.jumping = false;
            }

        }

        // collision detection against the enemy

        for (Enemy enemy: enemies) {
            if (!enemy.alive) continue;

            if ((sota.position.x <= enemy.position.x && sota.position.x + TILE_SIZE > enemy.position.x ||
                    sota.position.x >= enemy.position.x && sota.position.x < enemy.position.x + TILE_SIZE) &&
                (sota.position.y <= enemy.position.y && sota.position.y + TILE_SIZE > enemy.position.y ||
                    sota.position.y >= enemy.position.y && sota.position.y < enemy.position.y + TILE_SIZE)) {
                sota.attacked(elapsedTime, enemy.damage);
            }
        }

        // collision detection between the space ship and walls
        for (int i = spaceShip.position.y / TILE_SIZE; i <= (spaceShip.position.y + spaceShip.height) / TILE_SIZE; i++) {
            if (map.isGround(map.getTile(spaceShip.position.x / TILE_SIZE, i)) ||
                map.isGround(map.getTile((spaceShip.position.x + spaceShip.width) / TILE_SIZE, i))) {
                spaceShip.velocity.x *= -1;
                break;
            }
        }

        // collision detection against the space ship
        if (spaceShip.alive) {
            solveSotasCollisionAgainstShip(elapsedTime);
        }

        // collision detection with walls

        solveSotasCollision();

        // collision detection with gems
        for (int i = 0; i < gems.size(); i++) {
            int[] gemPosition = gems.get(i);
            if (sota.position.x <= gemPosition[0] + Map.TILE_SIZE / 2 && gemPosition[0] + Map.TILE_SIZE / 2 <= sota.position.x + Map.TILE_SIZE &&
                sota.position.y <= gemPosition[1] + Map.TILE_SIZE / 2 && gemPosition[1] + Map.TILE_SIZE / 2 <= sota.position.y + Map.TILE_SIZE) {
                gems.remove(i);
                earnedGems++;
                if (earnedGems == 10) {
                    earnedGems -= 10;
                    lives++;
                    if (lives == 100) {
                        lives = 99;
                    }
                }
            }
        }

        // collision detection with coins
        for (int i = 0; i < coins.size(); i++) {
            int[] coinPosition = coins.get(i);
            if (sota.position.x <= coinPosition[0] + Map.TILE_SIZE / 2 && coinPosition[0] + Map.TILE_SIZE / 2 <= sota.position.x + Map.TILE_SIZE &&
                sota.position.y <= coinPosition[1] + Map.TILE_SIZE / 2 && coinPosition[1] + Map.TILE_SIZE / 2 <= sota.position.y + Map.TILE_SIZE) {
                coins.remove(i);
                earnedCoins++;
            }
        }

        if (!sota.alive) {
            gameOver = true;
            lives--;
            run = false;
        }

    }

    @Override
    public void render() {
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Dimension d = getSize();
        int height = (int) d.getHeight();
        int width = (int) d.getWidth();

        int mapWidth = map.getWidth();
        int mapHeight = map.getHeight();

        int mapX;
        int mapY;

        if (sota.position.x < width / 2) {
            mapX = 0;
        } else if (sota.position.x > mapWidth - width / 2) {
            mapX = width - mapWidth;
        } else {
            mapX = width / 2 - sota.position.x;
        }

        if (sota.position.y < height / 2) {
            mapY = 0;
        } else if (sota.position.y > (mapHeight - TILE_SIZE) - height / 2) {
            mapY = height - (mapHeight - TILE_SIZE);
        } else {
            mapY = height / 2 - sota.position.y;
        }

        g.clearRect(0, 0, width, height);

        // background
        int i, j;
        for (i = 0; i < (int) (mapWidth / 256) + 1; i++) {
            for (j = 0; j < (int) (mapHeight / 256) + 1; j++) {
                g.drawImage(bg, i * 256, j * 256, null);
            }
        }

        // draw the map
        map.draw(g, mapX, mapY);

        // draw the enemy ship and its health bar
        if (spaceShip.alive) {
            spaceShip.draw(g, mapX, mapY);
        }

        // draw Sota, his weapons, and his health bar
        sota.draw(g, mapX, mapY);

        // draw enemies and health bar
        for (Enemy enemy: enemies) {
            if (enemy.alive) {
                enemy.draw(g, mapX, mapY);
            }
        }

        // draw gems
        for (int[] gemPosition: gems) {
            g.drawImage(gemImage, gemPosition[0] + mapX, gemPosition[1] + mapY, null);
        }

        // draw coins
        for (int[] coinPosition: coins) {
            g.drawImage(coinImage, coinPosition[0] + mapX, coinPosition[1] + mapY, null);
        }

        // draw the number of lives
        g.setFont(new Font("Consolas", Font.PLAIN, 30));
        g.setColor(Color.WHITE);
        g.drawImage(heartImage, 10, 5, null);
        g.drawString(" x " + lives, 50, 40);

        // draw the number of earned gems
        g.drawImage(gemImage, 150, 5, null);
        g.drawString(" x " + earnedGems, 190, 40);

        // draw the number of earned coins
        g.drawImage(coinImage, 290, 5, null);
        g.drawString(" x " + earnedCoins, 330, 40);

        // draw the time remaining
        g.setFont(new Font("Consolas", Font.PLAIN, 30));
        g.setColor(Color.WHITE);
        g.drawImage(clockImage, 1000, 5, null);
        g.drawString(timeRemaining + "", 1040, 40);

        // draw messages
        if (gameOver) {
            g.setFont(new Font("Consolas", Font.PLAIN, 150));
            g.setColor(Color.WHITE);
            g.drawString("GAME OVER!", 250, 200);
            g.setFont(new Font("Consolas", Font.PLAIN, 50));
            if (lives == 0) {
                g.drawString("YOU LOST ALL LIVES. ALL PROGRESS WILL BE RESET.", 100, 400);
            }
            g.drawString("PRESS ENTER TO GO TO THE WORLD MAP", 150, 500);
        } else if (gameClear) {
            g.setFont(new Font("Consolas", Font.PLAIN, 150));
            g.setColor(Color.WHITE);
            g.drawString("GAME CLEAR!", 250, 200);
            g.setFont(new Font("Consolas", Font.PLAIN, 50));
            g.drawString("PRESS ENTER TO GO TO THE WORLD MAP", 150, 400);
        }

    }

    @Override
    public void enter(String stage) {
        requestFocus();

        map.set(stage);

        timeLimit = map.getTimeLimit();

        sota.init(map);
        sota.initHP();
        spaceShip.init(map.getSpaceShipPosition(), map.getSpaceShipSpeed());
        enemies = map.getEnemies(TILE_SIZE, TILE_SIZE);
        coins = map.getCoins();
        gems.clear();

        run = true;
        gameOver = gameClear = false;

        Properties prop = new Properties();
        FileInputStream input = null;

        try {
            URL url = getClass().getResource("/data.properties");
            File resource = new File(url.toURI());
            input = new FileInputStream(resource);

            prop.load(input);

            lives = Integer.parseInt(prop.getProperty("lives"));
            earnedCoins = Integer.parseInt(prop.getProperty("coin"));
            earnedGems = Integer.parseInt(prop.getProperty("gem"));

            input.close();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exit() {
        movement.clear();
        run = false;
        startTime = -1;

        if (lives == 0) {
            lives = 5;
            earnedCoins = earnedGems = 0;
        }

        Properties prop = new Properties();
        FileInputStream input = null;
        FileOutputStream output = null;

        try {
            URL url = getClass().getResource("/data.properties");
            File resource = new File(url.toURI());
            input = new FileInputStream(resource);
            prop.load(input);
            input.close();

            output = new FileOutputStream(resource);

            prop.setProperty("lives", lives + "");
            prop.setProperty("coin", earnedCoins + "");
            prop.setProperty("gem", earnedGems + "");

            if (gameClear) {
                prop.setProperty(map.getName(), "cleared");
            }

            prop.store(output, null);

            output.close();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Solves Sota's collision against the wall.
     */

    private void solveSotasCollision() {

        // get the tile position where the player is
        int tileX = sota.position.x / TILE_SIZE;
        int tileY = sota.position.y / TILE_SIZE;

        // left edge of the map
        if (sota.position.x < 0) {
            tileX = -1;
        }
        if (sota.position.y < 0) {
            tileY = -1;
        }

        int ground;
        int groundRight;

        int ceiling;
        int ceilingRight;

        int rightWall;
        int rightWallBottom;

        int leftWall;
        int leftWallBottom;

        if (tileX == -1) { // left edge of the map
            ground = Map.GROUND_CENTER;
            ceiling = Map.GROUND_CENTER;
        } else {
            ground = map.getTile(tileX, tileY + 1);
            ceiling = map.getTile(tileX, tileY);
        }

        if (sota.position.x + TILE_SIZE >= map.getWidth()) { // right edge of the map
            groundRight = Map.GROUND_CENTER;
            ceilingRight = Map.GROUND_CENTER;
        } else {
            groundRight = map.getTile(tileX + 1, tileY + 1);
            ceilingRight = map.getTile(tileX + 1, tileY);
        }

        if (tileY == -1) {
            ceiling = Map.GROUND_CENTER;
            ceilingRight = Map.GROUND_CENTER;
        }

        rightWall = ceilingRight;
        rightWallBottom = groundRight;

        leftWall = ceiling;
        leftWallBottom = ground;

        sota.jumping = true;
        sota.swimming = false;

        // check the ground
        if (map.isGround(ground) && map.isGround(groundRight)) {
            sota.position.y = tileY * TILE_SIZE;
            sota.jumping = false;
        }

        // check the ceiling
        if (map.isGround(ceiling) && map.isGround(ceilingRight)) {
            sota.position.y = (tileY + 1) * TILE_SIZE;
        }

        // check the right wall
        if (map.isGround(rightWall) && map.isGround(rightWallBottom)) {
            sota.position.x = tileX * TILE_SIZE;
        }

        // check the left wall
        if (map.isGround(leftWall) && map.isGround(leftWallBottom)) {
            sota.position.x = (tileX + 1) * TILE_SIZE;
        }

        // hill
        if (rightWall == Map.GROUND_HILL_LEFT) {
            sota.position.y = tileY * TILE_SIZE + tileX * TILE_SIZE - sota.position.x;
            sota.jumping = false;
        } else if (ground == Map.GROUND_HILL_LEFT) {
            sota.position.y = tileY * TILE_SIZE;
            sota.jumping = false;
        } else if (groundRight == Map.GROUND_HILL_LEFT) {
            if (sota.position.y >= (tileX + 1) * TILE_SIZE - sota.position.x + tileY * TILE_SIZE) {
                sota.position.y = (tileX + 1) * TILE_SIZE - sota.position.x + tileY * TILE_SIZE;
                sota.jumping = false;
            }
        }

        if (leftWall == Map.GROUND_HILL_RIGHT) {
            sota.position.y = tileY * TILE_SIZE - (tileX + 1) * TILE_SIZE + sota.position.x;
            sota.jumping = false;
        } else if (groundRight == Map.GROUND_HILL_RIGHT) {
            sota.position.y = tileY * TILE_SIZE;
            sota.jumping = false;
        } else if (ground == Map.GROUND_HILL_RIGHT) {
            if (sota.position.y >= tileY * TILE_SIZE - tileX * TILE_SIZE + sota.position.x) {
                sota.position.y = tileY * TILE_SIZE - tileX * TILE_SIZE + sota.position.x;
                sota.jumping = false;
            }
        }

        // diagonal collision
        if (!map.isGround(ground) && map.isGround(groundRight) && !map.isGround(rightWall) && ground != Map.GROUND_HILL_LEFT && rightWall != Map.GROUND_HILL_LEFT) {
            if (sota.position.x - tileX * TILE_SIZE < sota.position.y - tileY * TILE_SIZE) {
                sota.position.x = tileX * TILE_SIZE;
            } else {
                sota.position.y = tileY * TILE_SIZE;
                sota.jumping = false;
            }
        }

        if (!map.isGround(ceiling) && map.isGround(ceilingRight) && !map.isGround(rightWallBottom)) {
            if (sota.position.x - tileX * TILE_SIZE < (tileY + 1) * TILE_SIZE - sota.position.y) {
                sota.position.x = tileX * TILE_SIZE;
            } else {
                sota.position.y = (tileY + 1) * TILE_SIZE;
            }
        }

        if (map.isGround(ground) && !map.isGround(groundRight) && !map.isGround(leftWall) && groundRight != Map.GROUND_HILL_RIGHT && leftWall != Map.GROUND_HILL_RIGHT) {
            if ((tileX + 1) * TILE_SIZE - sota.position.x < sota.position.y - tileY * TILE_SIZE) {
                sota.position.x = (tileX + 1) * TILE_SIZE;
            } else {
                sota.position.y = tileY * TILE_SIZE;
                sota.jumping = false;
            }
        }

        if (map.isGround(ceiling) && !map.isGround(ceilingRight) && !map.isGround(leftWallBottom)) {
            if ((tileX + 1) * TILE_SIZE - sota.position.x < (tileY + 1) * TILE_SIZE - sota.position.y) {
                sota.position.x = (tileX + 1) * TILE_SIZE;
            } else {
                sota.position.y = (tileY + 1) * TILE_SIZE;
            }
        }

        // in the water
        if (map.isWater(sota.position.x / TILE_SIZE, sota.position.y / TILE_SIZE) || map.isWater(sota.position.x / TILE_SIZE + 1, sota.position.y / TILE_SIZE) || sota.jumping && (map.isWater(sota.position.x / TILE_SIZE, sota.position.y / TILE_SIZE + 1) || map.isWater(sota.position.x / TILE_SIZE + 1, sota.position.y / TILE_SIZE + 1))) {
            sota.swimming = true;
            sota.jumping = false;
        }

        // game over
        if (map.isOutOfMap(tileY + 1)) {
            sota.alive = false;
        }
    }

    /**
     * Solves Sota's collision against the space ship.
     */

    private void solveSotasCollisionAgainstShip(float elapsedTime) {
        if (!sota.alive) return;

        sota.jumping = true;

        // check the ground and the ceiling
        if (spaceShip.position.x <= sota.position.x && sota.position.x + sota.width <= spaceShip.position.x + spaceShip.width) {
            if (sota.position.y < spaceShip.position.y && spaceShip.position.y < sota.position.y + sota.height) {
                sota.attacked(elapsedTime, spaceShip.damage);
                sota.position.y = spaceShip.position.y - sota.height;
                sota.jumping = false;
            } else if (sota.position.y < spaceShip.position.y + spaceShip.height && spaceShip.position.y + spaceShip.height < sota.position.y + sota.height) {
                sota.attacked(elapsedTime, spaceShip.damage);
                sota.position.y = spaceShip.position.y + spaceShip.height;
            }
        }

        // check the sides
        if (spaceShip.position.y <= sota.position.y && sota.position.y + sota.height <= spaceShip.position.y + spaceShip.height) {
            if (sota.position.x < spaceShip.position.x && spaceShip.position.x < sota.position.x + sota.width) {
                sota.attacked(elapsedTime, spaceShip.damage);
                sota.position.x = spaceShip.position.x - sota.width;
            } else if (sota.position.x < spaceShip.position.x + spaceShip.width && spaceShip.position.x + spaceShip.width < sota.position.x + sota.width) {
                sota.attacked(elapsedTime, spaceShip.damage);
                sota.position.x = spaceShip.position.x + spaceShip.width;
            }
        }

        // diagonal collision
        if (sota.position.x < spaceShip.position.x && spaceShip.position.x < sota.position.x + sota.width) {
            if (sota.position.y < spaceShip.position.y && spaceShip.position.y < sota.position.y + sota.height) {
                sota.attacked(elapsedTime, spaceShip.damage);
                if (sota.position.x + sota.width - spaceShip.position.x < sota.position.y + sota.height - spaceShip.position.y) {
                    sota.position.x = spaceShip.position.x - sota.width;
                    sota.jumping = false;
                } else {
                    sota.position.y = spaceShip.position.y - sota.height;
                }
            } else if (sota.position.y < spaceShip.position.y + spaceShip.height && spaceShip.position.y + spaceShip.height < sota.position.y + sota.height) {
                sota.attacked(elapsedTime, spaceShip.damage);
                if (sota.position.x + sota.width - spaceShip.position.x < spaceShip.position.y + spaceShip.height - sota.position.y) {
                    sota.position.x = spaceShip.position.x - sota.width;
                } else {
                    sota.position.y = spaceShip.position.y + spaceShip.height;
                }
            }
        }

        if (sota.position.x < spaceShip.position.x + spaceShip.width && spaceShip.position.x + spaceShip.width < sota.position.x + sota.width) {
            if (sota.position.y < spaceShip.position.y && spaceShip.position.y < sota.position.y + sota.height) {
                sota.attacked(elapsedTime, spaceShip.damage);
                if (sota.position.x + sota.width - spaceShip.position.x < sota.position.y + sota.height - spaceShip.position.y) {
                    sota.position.x = spaceShip.position.x + spaceShip.width;
                    sota.jumping = false;
                } else {
                    sota.position.y = spaceShip.position.y - sota.height;
                }
            } else if (sota.position.y < spaceShip.position.y + spaceShip.height && spaceShip.position.y + spaceShip.height < sota.position.y + sota.height) {
                sota.attacked(elapsedTime, spaceShip.damage);
                if (sota.position.x + sota.width - spaceShip.position.x < spaceShip.position.y + spaceShip.height - sota.position.y) {
                    sota.position.x = spaceShip.position.x + spaceShip.width;
                } else {
                    sota.position.y = spaceShip.position.y + spaceShip.height;
                }
            }
        }

        if (!sota.alive) {
            gameOver = true;
            lives--;
            run = false;
        }

    }

    /**
     * Loads images used in this state.
     */

    private void loadImages() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/images/bg.png"));
        bg = ii.getImage();

        ii = new ImageIcon(getClass().getResource("/images/heart.png"));
        heartImage = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/images/coin.png"));
        coinImage = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/images/gem.png"));
        gemImage = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/images/clock.png"));
        clockImage = Util.getScaledImage(ii.getImage(), Map.TILE_SIZE, Map.TILE_SIZE);
    }
}
