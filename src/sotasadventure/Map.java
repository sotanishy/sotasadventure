package sotasadventure;

import java.awt.Image;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * The class that represents the map.
 * @author Sota Nishiyama
 */
public class Map {
    public static final int NONE = 0;
    public static final int GROUND = 1;
    public static final int START = 3;
    public static final int DOOR_CLOSED = 5;
    public static final int DOOR_OPENED = 6;
    public static final int ENEMY = 7;
    public static final int COIN = 8;
    public static final int SHIP = 9;
    public static final int GROUND_HILL_RIGHT = 10;
    public static final int GROUND_HILL_LEFT = 11;
    public static final int WATER = 12;

    private Image doorOpenImage;
    private Image doorOpenTopImage;
    private Image doorClosedImage;
    private Image doorClosedTopImage;
    private Image signImage;
    private Image waterImage;

    private Stage currentStage;
    private int stageNum;

    public HashMap<String, Stage> stages = new HashMap<String, Stage>();

    /**
     * Stores all stages in a hash map and loads images.
     */
    public Map() {
        stages.put("northAmerica", new Stage("northAmerica"));
        stages.put("southAmerica", new Stage("southAmerica"));
        stages.put("africa", new Stage("africa"));
        stages.put("europe", new Stage("europe"));
        stages.put("asia", new Stage("asia"));
        stages.put("oceania", new Stage("oceania"));
        stages.put("antarctica", new Stage("antarctica"));

        loadResources();
    }

    /**
     * Sets the current stage.
     * @param stage the name of the stage
     */
    public void set(String stageName) {
        currentStage = stages.get(stageName);
        currentStage.name = stageName;
        stageNum = 0;
    }

    /**
     * Sets the current map to the next one.
     */
    public void next() {
        stageNum++;
    }

    /**
     * Returns the name of the current stage.
     * @return String the name of the current stage
     */

    public String getName() {
        return currentStage.name;
    }

    /**
     * Returns the type of the given tile.
     * @param x the x coordinate of the tile
     * @param y the y coordinate of the tile
     * @return int the type of the tile
     */
    public int getTile(int x, int y) {
        if (x < 0 || x >= currentStage.map[stageNum][0].length ||
            y < 0 || y >= currentStage.map[stageNum].length) return GROUND;
        return currentStage.map[stageNum][y][x];
    }

    /**
     * Returns the position of the starting point
     * @return Vector the position of the starting point
     */
    public Vector getStartingPoint() {
        for (int i = 0; i < currentStage.map[stageNum].length; i++) {
            for (int j = 0; j < currentStage.map[stageNum][i].length; j++) {
                if (currentStage.map[stageNum][i][j] == START || currentStage.map[stageNum][i][j] == DOOR_OPENED) {
                    return new Vector(j * Constants.TILE_SIZE, i * Constants.TILE_SIZE);
                }
            }
        }
        return new Vector();
    }

    /**
     * Returns the position of the space ship.
     * @return Vector the position of the enemy ship. If there is no space ship in the stage, returns an empty array.
     */
    public Vector getSpaceShipPosition() {
        for (int i = 0; i < currentStage.map[stageNum].length; i++) {
            for (int j = 0; j < currentStage.map[stageNum][i].length; j++) {
                if (currentStage.map[stageNum][i][j] == SHIP) {
                    return new Vector(j * Constants.TILE_SIZE, i * Constants.TILE_SIZE);
                }
            }
        }
        return new Vector();
    }

    /**
     * Returns the speed of the space ship.
     * @return int the speed of the enemy ship.
     */
    public int getSpaceShipSpeed() {
        return currentStage.spaceShipSpeed;
    }

    /**
     * Returns the speed of enemies.
     * @return int the speed of enemies
     */
    public int getEnemySpeed() {
        return currentStage.enemySpeed;
    }

    /**
     * Returns the friction of the current stage.
     * @return double the friction of the current stage
     */
    public double getFriction() {
        return currentStage.friction;
    }

    /**
     * Returns a list of enemies in the stage.
     * @param width the width of enemies
     * @param height the height of enemies
     * @return ArrayList<Enemy> an enemy arraylist
     */
    public ArrayList<Enemy> getEnemies(int width, int height) {
        ArrayList<Enemy> enemies = new ArrayList<Enemy>();

        for (int i = 0; i < currentStage.map[stageNum].length; i++) {
            for (int j = 0; j < currentStage.map[stageNum][i].length; j++) {
                if (currentStage.map[stageNum][i][j] == ENEMY) {
                    Enemy enemy = new Enemy(width, height);
                    enemy.init(j * Constants.TILE_SIZE, i * Constants.TILE_SIZE, getEnemySpeed());
                    enemies.add(enemy);
                }
            }
        }
        return enemies;
    }

    /**
     * Returns a list of coins in the stage.
     * @return ArrayList<int[][]> an arraylist of positions of coins
     */
    public ArrayList<int[]> getCoins() {
        ArrayList<int[]> coins = new ArrayList<int[]>();

        for (int i = 0; i < currentStage.map[stageNum].length; i++) {
            for (int j = 0; j < currentStage.map[stageNum][i].length; j++) {
                if (currentStage.map[stageNum][i][j] == COIN) {
                    coins.add(new int[] {j * Constants.TILE_SIZE, i * Constants.TILE_SIZE});
                }
            }
        }
        return coins;
    }

    /**
     * Returns the time limit of the stage.
     * @return int time limit
     */
    public int getTimeLimit() {
        return currentStage.timeLimit;
    }

    /**
     * Returns the width of the map
     * @return int the width of the map
     */
    public int getWidth() {
        return currentStage.map[stageNum][0].length * Constants.TILE_SIZE;

    }

    /**
     * Returns the height of the map
     * @return int the height of the map
     */
    public int getHeight() {
        return currentStage.map[stageNum].length * Constants.TILE_SIZE;

    }

    /**
     * Checks if the given tile is out of the map.
     * @param y the y coordinate of the tile
     * @return boolean true if the given tile is out of the map
     */
    public boolean isOutOfMap(int y) {
        if (y >= currentStage.map[stageNum].length) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the given tile is water.
     * @param x the x coordinate of the tile
     * @param y the y coordinate of the tile
     * @return boolean true if the given tile is water
     */
    public boolean isWater(int x, int y) {
        if (getTile(x, y) != GROUND && (getTile(x, y) == WATER || (getTile(x - 1, y) == WATER) || getTile(x + 1, y) == WATER || getTile(x, y - 1) == WATER)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Draws the map.
     * @param g Graphics
     * @param mapX the x coordinate of the map
     * @param mapY the y coordinate of the map
     */
    public void draw(Graphics g, int mapX, int mapY) {
        for (int i = 0; i < currentStage.map[stageNum].length; i++) {
            if (i * Constants.TILE_SIZE + mapY <= -Constants.TILE_SIZE || i * Constants.TILE_SIZE + mapY > getHeight()) continue;

            for (int j = 0; j < currentStage.map[stageNum][i].length; j++) {
                if (j * Constants.TILE_SIZE + mapX <= -Constants.TILE_SIZE || j * Constants.TILE_SIZE + mapX > getWidth()) continue;

                if (isWater(j, i)) {
                    g.drawImage(waterImage, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE + mapY, null);
                }

                switch (currentStage.map[stageNum][i][j]) {
                    case GROUND:
                    if (i > 0 && currentStage.map[stageNum][i - 1][j] != GROUND && currentStage.map[stageNum][i - 1][j] != GROUND_HILL_LEFT && currentStage.map[stageNum][i - 1][j] != GROUND_HILL_RIGHT) { // surface

                        if (j > 0 && currentStage.map[stageNum][i][j - 1] != GROUND && currentStage.map[stageNum][i][j - 1] != GROUND_HILL_LEFT &&
                            j < currentStage.map[stageNum][i].length - 1 && currentStage.map[stageNum][i][j + 1] != GROUND && currentStage.map[stageNum][i][j + 1] != GROUND_HILL_RIGHT) { // both
                            g.drawImage(currentStage.surface, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE + mapY, null);
                        } else if (j > 0 && currentStage.map[stageNum][i][j - 1] != GROUND && currentStage.map[stageNum][i][j - 1] != GROUND_HILL_LEFT) { // left edge
                            g.drawImage(currentStage.left, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE + mapY, null);
                        } else if (j < currentStage.map[stageNum][i].length - 1 && currentStage.map[stageNum][i][j + 1] != GROUND && currentStage.map[stageNum][i][j + 1] != GROUND_HILL_RIGHT) { // right edge
                            g.drawImage(currentStage.right, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE + mapY, null);
                        } else {
                            g.drawImage(currentStage.mid, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE + mapY, null);
                        }

                    } else {

                        if (i > 0 && currentStage.map[stageNum][i - 1][j] == GROUND_HILL_RIGHT) {
                            g.drawImage(currentStage.hillRight2, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE + mapY, null);
                        } else if (i > 0 && currentStage.map[stageNum][i - 1][j] == GROUND_HILL_LEFT) {
                            g.drawImage(currentStage.hillLeft2, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE + mapY, null);
                        } else {
                            g.drawImage(currentStage.center, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE + mapY, null);
                        }

                    }

                    break;

                    case START:
                    g.drawImage(signImage, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE + mapY, null);
                    break;

                    case DOOR_CLOSED:
                    g.drawImage(doorClosedImage, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE + mapY, null);
                    g.drawImage(doorClosedTopImage, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE - Constants.TILE_SIZE + mapY, null);
                    break;

                    case DOOR_OPENED:
                    g.drawImage(doorOpenImage, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE + mapY, null);
                    g.drawImage(doorOpenTopImage, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE - Constants.TILE_SIZE + mapY, null);
                    break;

                    case GROUND_HILL_RIGHT:
                    g.drawImage(currentStage.hillRight, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE + mapY, null);
                    break;

                    case GROUND_HILL_LEFT:
                    g.drawImage(currentStage.hillLeft, j * Constants.TILE_SIZE + mapX, i * Constants.TILE_SIZE + mapY, null);
                    break;
                }
            }
        }
    }

    /**
     * Loads images of the map.
     */
    private void loadResources() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/resources/images/Tiles/door_closedMid.png"));
        doorClosedImage = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/Tiles/door_closedTop.png"));
        doorClosedTopImage = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/Tiles/door_openMid.png"));
        doorOpenImage = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/Tiles/door_openTop.png"));
        doorOpenTopImage = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/Tiles/sign.png"));
        signImage = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/Tiles/liquidWater.png"));
        waterImage = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);
    }
}
