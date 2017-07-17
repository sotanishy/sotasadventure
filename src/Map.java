package sotasadventure;

import java.awt.Image;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * The class that represents the map.
 */
public class Map {
	public static final int GROUND_NONE = 0;
	public static final int GROUND_SURFACE = 1;
	public static final int GROUND_CENTER = 2;
	public static final int START = 3;
	public static final int DOOR_CLOSED = 5;
	public static final int DOOR_OPENED = 6;
	public static final int ENEMY = 7;
	public static final int COIN = 8;
	public static final int SHIP = 9;
	public static final int GROUND_HILL_RIGHT = 10;
	public static final int GROUND_HILL_LEFT = 11;
	public static final int WATER = 12;

	public static final int TILE_SIZE = 50;

	private Image[] mapImages = new Image[14];
	private Image signImage;
	private Image waterImage;

	private Stage currentStage;
	private int stageNum;

	private HashMap<String, Stage> maps = new HashMap<String, Stage>();

	/**
	 * Stores all stages in a hash map and loads images.
	 */
	public Map() {
		maps.put("sampleStage", new Stage("sampleStage", 180, 0));
		maps.put("northAmerica", new Stage("northAmerica", 180, 0));
		maps.put("southAmerica", new Stage("southAmerica", 300, 5));
		maps.put("africa", new Stage("africa", 300, 5));
		maps.put("europe", new Stage("europe", 300, 5));
		maps.put("asia", new Stage("asia", 300, 5));
		maps.put("oceania", new Stage("oceania", 300, 5));
		maps.put("antarctica", new Stage("antarctica", 300, 5));

		loadImage();
	}

	/**
	 * Sets the current stage.
	 * @param stage the name of the stage
	 */
	public void set(String stageName) {
		currentStage = maps.get(stageName);
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
			y < 0 || y >= currentStage.map[stageNum].length) return GROUND_CENTER;
		return currentStage.map[stageNum][y][x];
	}

	/**
	 * Returns the position of the starting point
	 * @return Vector the position of the starting point
	 */
	public Vector getStartingPoint() {
		for (int i = 0; i < currentStage.map[stageNum].length; i++) {
			for (int j = 0; j < currentStage.map[stageNum][i].length; j++) {
				if (currentStage.map[stageNum][i][j] == Map.START || currentStage.map[stageNum][i][j] == Map.DOOR_OPENED) {
					return new Vector(j * TILE_SIZE, i * TILE_SIZE);
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
				if (currentStage.map[stageNum][i][j] == Map.SHIP) {
					return new Vector(j * TILE_SIZE, i * TILE_SIZE);
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
	 * Returns a list of enemies in the stage.
	 * @param width the width of enemies
	 * @param height the height of enemies
	 * @return ArrayList<Enemy> an enemy arraylist
	 */
	public ArrayList<Enemy> getEnemies(int width, int height) {
		ArrayList<Enemy> enemies = new ArrayList<Enemy>();

		for (int i = 0; i < currentStage.map[stageNum].length; i++) {
			for (int j = 0; j < currentStage.map[stageNum][i].length; j++) {
				if (currentStage.map[stageNum][i][j] == Map.ENEMY) {
					Enemy enemy = new Enemy(width, height);
					enemy.init(j, i);
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
				if (currentStage.map[stageNum][i][j] == Map.COIN) {
					coins.add(new int[] {j * TILE_SIZE, i * TILE_SIZE});
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
		return currentStage.map[stageNum][0].length * TILE_SIZE;

	}

	/**
	 * Returns the height of the map
	 * @return int the height of the map
	 */
	public int getHeight() {
		return currentStage.map[stageNum].length * TILE_SIZE;

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
	 * Checks if the given tile is ground.
	 * @param tile the type of the tile
	 * @return boolean true if the given tile is ground
	 */
	public boolean isGround(int tile) {
		if (tile == GROUND_CENTER || tile == GROUND_SURFACE) {
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
		if (!isGround(getTile(x, y)) && (getTile(x, y) == WATER || (getTile(x - 1, y) == WATER) || getTile(x + 1, y) == WATER || getTile(x, y - 1) == WATER)) {
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
			if (i * TILE_SIZE + mapY <= -TILE_SIZE || i * TILE_SIZE + mapY > getHeight()) continue;

			for (int j = 0; j < currentStage.map[stageNum][i].length; j++) {
				if (j * TILE_SIZE + mapX <= -TILE_SIZE || j * TILE_SIZE + mapX > getWidth()) continue;

				if (isWater(j, i)) {
					g.drawImage(waterImage, j * TILE_SIZE + mapX, i * TILE_SIZE + mapY, null);
				}

				switch (currentStage.map[stageNum][i][j]) {
					case GROUND_SURFACE:

					if (j > 0 && !isGround(currentStage.map[stageNum][i][j - 1]) && currentStage.map[stageNum][i][j - 1] != GROUND_HILL_LEFT &&
						j < currentStage.map[stageNum][i].length - 1 && !isGround(currentStage.map[stageNum][i][j + 1]) && currentStage.map[stageNum][i][j + 1] != GROUND_HILL_RIGHT) { // both
						g.drawImage(mapImages[6], j * TILE_SIZE + mapX, i * TILE_SIZE + mapY, null);
					} else if (j > 0 && !isGround(currentStage.map[stageNum][i][j - 1]) && currentStage.map[stageNum][i][j - 1] != GROUND_HILL_LEFT) { // left edge
						g.drawImage(mapImages[3], j * TILE_SIZE + mapX, i * TILE_SIZE + mapY, null);
					} else if (j < currentStage.map[stageNum][i].length - 1 && !isGround(currentStage.map[stageNum][i][j + 1]) && currentStage.map[stageNum][i][j + 1] != GROUND_HILL_RIGHT) { // right edge
						g.drawImage(mapImages[2], j * TILE_SIZE + mapX, i * TILE_SIZE + mapY, null);
					} else{
						g.drawImage(mapImages[0], j * TILE_SIZE + mapX, i * TILE_SIZE + mapY, null);
					}

					// fill the space between a ground surface and a ground center
					if (currentStage.map[stageNum][i + 1][j] == GROUND_CENTER) {
						g.drawImage(mapImages[1], j * TILE_SIZE + mapX, i * TILE_SIZE + TILE_SIZE / 2 + mapY, null);
					}
					break;

					case GROUND_CENTER:
					if (i > 0 && currentStage.map[stageNum][i - 1][j] == GROUND_HILL_RIGHT) {
						g.drawImage(mapImages[11], j * TILE_SIZE + mapX, i * TILE_SIZE + mapY, null);
					} else if (i > 0 && currentStage.map[stageNum][i - 1][j] == GROUND_HILL_LEFT) {
						g.drawImage(mapImages[13], j * TILE_SIZE + mapX, i * TILE_SIZE + mapY, null);
					} else {
						g.drawImage(mapImages[1], j * TILE_SIZE + mapX, i * TILE_SIZE + mapY, null);
					}
					break;

					case START:
					g.drawImage(signImage, j * TILE_SIZE + mapX, i * TILE_SIZE + mapY, null);
					break;

					case DOOR_CLOSED:
					g.drawImage(mapImages[4], j * TILE_SIZE + mapX, i * TILE_SIZE + mapY, null);
					g.drawImage(mapImages[5], j * TILE_SIZE + mapX, i * TILE_SIZE - TILE_SIZE + mapY, null);
					break;

					case DOOR_OPENED:
					g.drawImage(mapImages[8], j * TILE_SIZE + mapX, i * TILE_SIZE + mapY, null);
					g.drawImage(mapImages[9], j * TILE_SIZE + mapX, i * TILE_SIZE - TILE_SIZE + mapY, null);
					break;

					case GROUND_HILL_RIGHT:
					g.drawImage(mapImages[10], j * TILE_SIZE + mapX, i * TILE_SIZE + mapY, null);
					break;

					case GROUND_HILL_LEFT:
					g.drawImage(mapImages[12], j * TILE_SIZE + mapX, i * TILE_SIZE + mapY, null);
					break;
				}
			}
		}
	}

	/**
	 * Loads images of the map.
	 */
	private void loadImage() {
		ImageIcon ii;

		ii = new ImageIcon(getClass().getResource("/images/Tiles/grassMid.png"));
		mapImages[0] = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

		ii = new ImageIcon(getClass().getResource("/images/Tiles/grassCenter.png"));
		mapImages[1] = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

		ii = new ImageIcon(getClass().getResource("/images/Tiles/grassRight.png"));
		mapImages[2] = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

		ii = new ImageIcon(getClass().getResource("/images/Tiles/grassLeft.png"));
		mapImages[3] = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

		ii = new ImageIcon(getClass().getResource("/images/Tiles/door_closedMid.png"));
		mapImages[4] = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

		ii = new ImageIcon(getClass().getResource("/images/Tiles/door_closedTop.png"));
		mapImages[5] = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

		ii = new ImageIcon(getClass().getResource("/images/Tiles/grass.png"));
		mapImages[6] = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

		ii = new ImageIcon(getClass().getResource("/images/Tiles/door_openMid.png"));
		mapImages[8] = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

		ii = new ImageIcon(getClass().getResource("/images/Tiles/door_openTop.png"));
		mapImages[9] = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

		ii = new ImageIcon(getClass().getResource("/images/Tiles/grassHillRight.png"));
		mapImages[10] = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

		ii = new ImageIcon(getClass().getResource("/images/Tiles/grassHillRight2.png"));
		mapImages[11] = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

		ii = new ImageIcon(getClass().getResource("/images/Tiles/grassHillLeft.png"));
		mapImages[12] = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

		ii = new ImageIcon(getClass().getResource("/images/Tiles/grassHillLeft2.png"));
		mapImages[13] = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

		ii = new ImageIcon(getClass().getResource("/images/sign.png"));
		signImage = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);

		ii = new ImageIcon(getClass().getResource("/images/Tiles/liquidWater.png"));
		waterImage = Util.getScaledImage(ii.getImage(), TILE_SIZE, TILE_SIZE);
	}
}