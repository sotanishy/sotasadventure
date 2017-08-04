package sotasadventure;

import java.awt.Image;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.ImageIcon;

public class Stage {
    public String name;
    public int[][][] map;
    public int timeLimit;
    public int spaceShipSpeed;
    public int enemySpeed;
    public double friction;

    public Image surface;
    public Image center;
    public Image right;
    public Image left;
    public Image mid;
    public Image hillRight;
    public Image hillRight2;
    public Image hillLeft;
    public Image hillLeft2;

    /**
     * Constructs a stage with given name, time limit, and speed of space ship and loads the map from the properties file.
     * @param name the name of the stage
     */
    public Stage(String name) {
        this.name = name;

        Properties prop = new Properties();
        InputStream input = null;

        try {
            input = getClass().getResourceAsStream("/resources/stages.properties");
            prop.load(input);

            String data = prop.getProperty(name);
            map = convertToArray(data);
            timeLimit = Integer.parseInt(read(data, "timeLimit"));
            spaceShipSpeed = Integer.parseInt(read(data, "spaceShipSpeed"));
            enemySpeed = Integer.parseInt(read(data, "enemySpeed"));
            friction = Double.parseDouble(read(data, "friction"));

            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadResources();
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

    /**
     * Reads the value of the key in a data
     * @param data the string which contains the key
     * @param key the key of the value to be read
     */
    private String read(String data, String key) {
        int i = data.indexOf(key) + key.length() + 1;
        StringBuilder ret = new StringBuilder("");
        while (i < data.length() && (Character.isDigit(data.charAt(i)) || data.charAt(i) == '.')) {
            ret.append(data.charAt(i));
            i++;
        }
        return ret.toString();
    }

    /**
     * Loads images of the stage.
     */
    private void loadResources() {
        ImageIcon ii;

        ii = new ImageIcon(getClass().getResource("/resources/images/Tiles/" + name +"/surface.png"));
        surface = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/Tiles/" + name +"/center.png"));
        center = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/Tiles/" + name +"/right.png"));
        right = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        left = Util.getFlippedImage(right);

        ii = new ImageIcon(getClass().getResource("/resources/images/Tiles/" + name + "/mid.png"));
        mid = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/Tiles/" + name + "/hillRight.png"));
        hillRight = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        ii = new ImageIcon(getClass().getResource("/resources/images/Tiles/" + name + "/hillRight2.png"));
        hillRight2 = Util.getScaledImage(ii.getImage(), Constants.TILE_SIZE, Constants.TILE_SIZE);

        hillLeft = Util.getFlippedImage(hillRight);

        hillLeft2 = Util.getFlippedImage(hillRight2);
    }
}
