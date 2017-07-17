package sotasadventure;

import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Properties;

public class Stage {
	public String name;
	public int[][][] map;
	public int timeLimit;
	public int spaceShipSpeed;

	/**
	 * Constructs a stage with given name, time limit, and speed of space ship and loads the map from the properties file.
	 * @param name the name of the stage
	 * @param timeLimit the time limit of the stage
	 * @param spaceShipSpeed the speed of the space ship in the stage
	 */
	public Stage(String name, int timeLimit, int spaceShipSpeed) {
		this.name = name;
		this.timeLimit = timeLimit;
		this.spaceShipSpeed = spaceShipSpeed;

		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = getClass().getResourceAsStream("/stages.properties");
			prop.load(input);

			map = convertToArray(prop.getProperty(name));

			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts the map in properties file to three-dimensional array.
	 */
	private int[][][] convertToArray(String str) {
		ArrayList<String> temp = new ArrayList<String>();

		int start = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '[') {
				start = i + 1;
			} else if (str.charAt(i) == ']') {
				temp.add(str.substring(start, i));
			}
		}
		String[] maps = temp.toArray(new String[temp.size()]);

		String[][] maps2 = new String[maps.length][];
		for (int i = 0; i < maps.length; i++) {
			temp.clear();
			for (int j = 0; j < maps[i].length(); j++) {
				if (maps[i].charAt(j) == '{') {
					start = j + 1;
				} else if (maps[i].charAt(j) == '}') {
					temp.add(maps[i].substring(start, j));
				}
			}
			maps2[i] = temp.toArray(new String[temp.size()]);
		}

		String[][][] maps3 = new String[maps2.length][][];
		for (int i = 0; i < maps2.length; i++) {
			temp.clear();
			maps3[i] = new String[maps2[i].length][];
			for (int j = 0; j < maps2[i].length; j++) {
				temp = new ArrayList<String>(Arrays.asList(maps2[i][j].split(" ")));
				temp.removeAll(new ArrayList<String>(Arrays.asList(new String[] {""})));
				maps3[i][j] = temp.toArray(new String[temp.size()]);
			}
		}

		int[][][] intMap = new int[maps3.length][][];
		for (int i = 0; i < maps3.length; i++) {
			intMap[i] = new int[maps3[i].length][];
			for (int j = 0; j < maps3[i].length; j++) {
				intMap[i][j] = new int[maps3[i][j].length];
				for (int k = 0; k < maps3[i][j].length; k++) {
					intMap[i][j][k] = Integer.parseInt(maps3[i][j][k]);
				}
			}
		}

		return intMap;
	}
}