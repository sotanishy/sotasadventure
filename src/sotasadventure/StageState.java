package sotasadventure;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;

/**
 * The class that deals with each stage.
 * @author Sota Nishiyama
 */
public class StageState extends State {
    private StateMachine gameMode;

    private boolean run;
    private boolean gameOver;
    private boolean gameClear;

    private Sota sota;
    private ArrayList<Enemy> enemies;
    private SpaceShip spaceShip;

    private ArrayList<String> movement = new ArrayList<String>();

    private Map map = new Map();

    private int timeLimit;
    private double startTime;
    private int timeRemaining;

    private int lives;

    private ArrayList<int[]> coins = new ArrayList<int[]>();
    private int earnedCoins = 0;

    private ArrayList<int[]> gems = new ArrayList<int[]>();
    private int earnedGems = 0;

    private Image bg;
    private Image healthImage;
    private Image coinImage;
    private Image gemImage;
    private Image clockImage;

    private Clip healthSound;
    private Clip coinSound;
    private Clip gemSound;
    private Clip gameStartSound;
    private Clip gameClearSound;
    private Clip gameOverSound;

    /**
     * Instanciates characters, loads resources, and adds key listener.
     * @param gameMode The state machine of the game
     */
    public StageState(StateMachine gameMode) {
        this.gameMode = gameMode;

        loadResources();

        // characters
        sota = new Sota(Constants.TILE_SIZE, Constants.TILE_SIZE);
        spaceShip = new SpaceShip(Constants.TILE_SIZE * 5, Constants.TILE_SIZE * 3);

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
    public void update(double elapsedTime) {
        if (!run) return;

        if (startTime == -1) {
            startTime = elapsedTime;
        }
        timeRemaining = (int) (timeLimit - elapsedTime + startTime);
        if (timeRemaining <= 0) {
            sota.alive = false;
        }

        // enter the door
        if (movement.contains("up") &&
            (map.getTile(sota.position.x / Constants.TILE_SIZE, sota.position.y / Constants.TILE_SIZE) == Map.DOOR_CLOSED || map.getTile(sota.position.x / Constants.TILE_SIZE + 1, sota.position.y / Constants.TILE_SIZE) == Map.DOOR_CLOSED)) {
            map.next();
            sota.init(map);
            spaceShip.init(map.getSpaceShipPosition(), map.getSpaceShipSpeed());
            enemies = map.getEnemies(Constants.TILE_SIZE, Constants.TILE_SIZE);
            coins = map.getCoins();
            gems.clear();
        }

        // move objects
        sota.move(movement, elapsedTime);

        for (Enemy enemy: enemies) {
            if (enemy.alive) {
                enemy.move(elapsedTime);
            }
        }

        if (spaceShip.alive) {
            spaceShip.move(elapsedTime);
            spaceShip.deployEnemies(enemies, elapsedTime, Constants.TILE_SIZE, Constants.TILE_SIZE, map.getEnemySpeed());
        }

        // solve collision between objects and walls
        sota.solveCollisionAgainstWalls(map);

        for (Enemy enemy: enemies) {
            if (enemy.alive) {
                enemy.solveCollisionAgainstWalls(map);
            }
        }

        if (spaceShip.alive) {
            spaceShip.solveCollisionAgainstWalls(map);
        }

        // solve collision between an enemy and another enemy
        for (int i = 0; i < enemies.size() - 1; i++) {
            Enemy enemy1 = enemies.get(i);
            if (!enemy1.alive) continue;

            for (int j = i + 1; j < enemies.size(); j++) {
                Enemy enemy2 = enemies.get(j);
                if (!enemy2.alive) continue;

                if (enemy1.position.y <= enemy2.position.y + enemy2.height && enemy1.position.y + enemy1.height >= enemy2.position.y &&
                    enemy1.position.x <= enemy2.position.x + enemy2.width && enemy1.position.x + enemy1.width >= enemy2.position.x) {
                    enemy1.velocity.x *= -1;
                    enemy2.velocity.x *= -1;
                    if (enemy1.position.x < enemy2.position.x) {
                        enemy1.position.x = enemy2.position.x - enemy1.width;
                    } else {
                        enemy2.position.x = enemy1.position.x - enemy2.width;
                    }
                }
            }
        }

        // solve collision between Sota and an enemy
        for (Enemy enemy: enemies) {
            if (!enemy.alive) continue;

            if (sota.position.x <= enemy.position.x + enemy.width && sota.position.x + sota.width >= enemy.position.x &&
                sota.position.y <= enemy.position.y + enemy.height && sota.position.y + sota.height >= enemy.position.y) {
                sota.attacked(elapsedTime, enemy.damage);
            }
        }

        // collision detection against the space ship
        if (spaceShip.alive) {
            solveSotasCollisionAgainstShip(elapsedTime);
        }

        // solve collision between Sota and walls
        sota.solveCollisionAgainstWalls(map);

        // bullets
        bulletLoop: for (int i = 0; i < sota.bullets.size(); i++) {
            Bullet bullet = sota.bullets.get(i);

            bullet.move(elapsedTime);
            if (!bullet.alive) {
                sota.bullets.remove(bullet);
                i--;
                continue;
            }

            int x = bullet.position.x;
            int y = bullet.position.y;

            // hit the wall
            if (map.getTile(x / Constants.TILE_SIZE, y / Constants.TILE_SIZE) == Map.GROUND ||
                (map.getTile(x / Constants.TILE_SIZE, y / Constants.TILE_SIZE) == Map.GROUND_HILL_LEFT && Constants.TILE_SIZE - x % Constants.TILE_SIZE < y % Constants.TILE_SIZE) ||
                (map.getTile(x / Constants.TILE_SIZE, y / Constants.TILE_SIZE) == Map.GROUND_HILL_RIGHT && x % Constants.TILE_SIZE < y % Constants.TILE_SIZE)) {
                sota.bullets.remove(bullet);
                i--;
                continue;
            }

            for (Enemy enemy: enemies) {
                if (!enemy.alive) continue;

                if (enemy.position.x <= x && x <= enemy.position.x + enemy.width &&
                    enemy.position.y <= y && y <= enemy.position.y + enemy.height) {
                    enemy.attacked(elapsedTime, bullet.damage);
                    if (!enemy.alive) {
                        gems.add(new int[] {enemy.position.x, enemy.position.y});
                    }
                    sota.bullets.remove(bullet);
                    i--;
                    continue bulletLoop;
                }
            }
            if (spaceShip.alive) {
                if (spaceShip.position.x <= x && x <= spaceShip.position.x + spaceShip.width &&
                    spaceShip.position.y <= y && y <= spaceShip.position.y + spaceShip.height) {
                    spaceShip.attacked(elapsedTime, bullet.damage);
                    if (!spaceShip.alive) {
                        gameClear = true;
                        run = false;
                        gameClearSound.setFramePosition(0);
                        gameClearSound.start();
                    }
                    sota.bullets.remove(bullet);
                    i--;
                    continue;
                }
            }
        }

        // sword
        if (sota.sword) {
            if (sota.facingRight) {
                sota.swordPosition.x = sota.position.x + sota.width;
            } else {
                sota.swordPosition.x = sota.position.x - sota.swordWidth;
            }
            sota.swordPosition.y = sota.position.y + sota.height / 2 - sota.swordHeight / 2;

            for (Enemy enemy: enemies) {
                if (!enemy.alive) continue;

                if (sota.swordPosition.y <= enemy.position.y + enemy.height && sota.swordPosition.y + sota.swordHeight >= enemy.position.y &&
                    sota.swordPosition.x <= enemy.position.x + enemy.width && sota.swordPosition.x + sota.swordWidth >= enemy.position.x) {
                    enemy.attacked(elapsedTime, sota.swordDamage);
                    if (!enemy.alive) {
                        gems.add(new int[] {enemy.position.x, enemy.position.y});
                    }
                }
            }
            if (spaceShip.alive) {
                if (sota.swordPosition.y <= spaceShip.position.y + spaceShip.height && sota.swordPosition.y + sota.swordHeight >= spaceShip.position.y &&
                    sota.swordPosition.x <= spaceShip.position.x + spaceShip.width && sota.swordPosition.x + sota.swordWidth >= spaceShip.position.x) {
                    spaceShip.attacked(elapsedTime, sota.swordDamage);
                    if (!spaceShip.alive) {
                        gameClear = true;
                        run = false;
                        gameClearSound.setFramePosition(0);
                        gameClearSound.start();
                    }
                }
            }
        }

        // get coins
        for (int i = 0; i < coins.size(); i++) {
            int[] coinPosition = coins.get(i);
            if (sota.position.x <= coinPosition[0] + Constants.TILE_SIZE / 2 && coinPosition[0] + Constants.TILE_SIZE / 2 <= sota.position.x + Constants.TILE_SIZE &&
                sota.position.y <= coinPosition[1] + Constants.TILE_SIZE / 2 && coinPosition[1] + Constants.TILE_SIZE / 2 <= sota.position.y + Constants.TILE_SIZE) {
                coins.remove(i--);
                earnedCoins++;
                coinSound.setFramePosition(0);
                coinSound.start();
                if (earnedCoins == Constants.COIN_MAX) {
                    earnedCoins = 0;
                    lives++;
                    if (lives == Constants.LIFE_MAX) {
                        lives = Constants.LIFE_MAX - 1;
                    }
                    healthSound.setFramePosition(0);
                    healthSound.start();
                }
            }
        }

        // get gems
        for (int i = 0; i < gems.size(); i++) {
            int[] gemPosition = gems.get(i);
            if (sota.position.x <= gemPosition[0] + Constants.TILE_SIZE / 2 && gemPosition[0] + Constants.TILE_SIZE / 2 <= sota.position.x + Constants.TILE_SIZE &&
                sota.position.y <= gemPosition[1] + Constants.TILE_SIZE / 2 && gemPosition[1] + Constants.TILE_SIZE / 2 <= sota.position.y + Constants.TILE_SIZE) {
                gems.remove(i--);
                earnedGems++;
                gemSound.setFramePosition(0);
                gemSound.start();
                if (earnedGems == Constants.GEM_MAX) {
                    earnedGems = 0;
                    lives++;
                    if (lives == Constants.LIFE_MAX) {
                        lives = Constants.LIFE_MAX - 1;
                    }
                    healthSound.setFramePosition(0);
                    healthSound.start();
                }
            }
        }

        if (!sota.alive) {
            gameOver = true;
            lives--;
            run = false;
            gameOverSound.setFramePosition(0);
            gameOverSound.start();
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
        } else if (sota.position.y > (mapHeight - Constants.TILE_SIZE) - height / 2) {
            mapY = height - (mapHeight - Constants.TILE_SIZE);
        } else {
            mapY = height / 2 - sota.position.y;
        }

        g.clearRect(0, 0, width, height);

        // draw background
        int i, j;
        for (i = 0; i < (int) (mapWidth / 256) + 1; i++) {
            for (j = 0; j < (int) (mapHeight / 256) + 1; j++) {
                g.drawImage(bg, i * 256, j * 256, null);
            }
        }

        // draw the map
        map.draw(g, mapX, mapY);

        // draw objects
        if (spaceShip.alive) {
            spaceShip.draw(g, mapX, mapY);
        }

        for (Enemy enemy: enemies) {
            if (enemy.alive) {
                enemy.draw(g, mapX, mapY);
            }
        }

        sota.draw(g, mapX, mapY);

        // draw resources
        for (int[] coinPosition: coins) {
            g.drawImage(coinImage, coinPosition[0] + mapX, coinPosition[1] + mapY, null);
        }

        for (int[] gemPosition: gems) {
            g.drawImage(gemImage, gemPosition[0] + mapX, gemPosition[1] + mapY, null);
        }

        g.setFont(new Font("Consolas", Font.PLAIN, 30));
        g.setColor(Color.WHITE);
        g.drawImage(healthImage, 10, 5, null);
        g.drawString(" x " + lives, 50, 40);

        g.drawImage(coinImage, 150, 5, null);
        g.drawString(" x " + earnedCoins, 190, 40);

        g.drawImage(gemImage, 290, 5, null);
        g.drawString(" x " + earnedGems, 330, 40);

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

        movement.clear();

        timeLimit = map.getTimeLimit();
        startTime = -1;

        sota.init(map);
        sota.initHP();

        spaceShip.init(map.getSpaceShipPosition(), map.getSpaceShipSpeed());

        enemies = map.getEnemies(Constants.TILE_SIZE, Constants.TILE_SIZE);

        coins = map.getCoins();
        gems.clear();

        run = true;
        gameOver = gameClear = false;

        Preferences prefs = Preferences.userNodeForPackage(StageState.class);
        lives = prefs.getInt("lives", 5);
        earnedCoins = prefs.getInt("coin", 0);
        earnedGems = prefs.getInt("gem", 0);

        gameStartSound.setFramePosition(0);
        gameStartSound.start();
    }

    @Override
    public void exit() {
        Preferences prefs = Preferences.userNodeForPackage(StageState.class);
        if (lives == 0) {
            lives = 5;
            earnedCoins = earnedGems = 0;

            for (String stage: map.stages.keySet()) {
                prefs.put(stage, "not cleared");
            }
        }

        prefs.putInt("lives", lives);
        prefs.putInt("coin", earnedCoins);
        prefs.putInt("gem", earnedGems);

        if (gameClear) {
            prefs.put(map.getName(), "cleared");
        }
    }

    /**
     * Solves Sota's collision against the space ship.
     */
    private void solveSotasCollisionAgainstShip(double elapsedTime) {
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
    }

    /**
     * Loads images and sounds.
     */
    private void loadResources() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/resources/images/bg.png"));
        bg = ii.getImage();

        ii = new ImageIcon(getClass().getResource("/resources/images/heart.png"));
        healthImage = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/coin.png"));
        coinImage = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/gem.png"));
        gemImage = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/clock.png"));
        clockImage = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource("/resources/audio/coin.wav"));
            coinSound = AudioSystem.getClip();
            coinSound.open(audioIn);

            audioIn = AudioSystem.getAudioInputStream(getClass().getResource("/resources/audio/gem.wav"));
            gemSound = AudioSystem.getClip();
            gemSound.open(audioIn);

            audioIn = AudioSystem.getAudioInputStream(getClass().getResource("/resources/audio/health.wav"));
            healthSound = AudioSystem.getClip();
            healthSound.open(audioIn);

            audioIn = AudioSystem.getAudioInputStream(getClass().getResource("/resources/audio/game-start.wav"));
            gameStartSound = AudioSystem.getClip();
            gameStartSound.open(audioIn);

            audioIn = AudioSystem.getAudioInputStream(getClass().getResource("/resources/audio/game-clear.wav"));
            gameClearSound = AudioSystem.getClip();
            gameClearSound.open(audioIn);

            audioIn = AudioSystem.getAudioInputStream(getClass().getResource("/resources/audio/game-over.wav"));
            gameOverSound = AudioSystem.getClip();
            gameOverSound.open(audioIn);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
