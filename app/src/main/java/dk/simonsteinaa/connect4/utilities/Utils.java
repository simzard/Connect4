package dk.simonsteinaa.connect4.utilities;

import android.graphics.Color;

import java.util.Random;

/**
 * Created by simon on 6/3/16.
 */

// just random useful color stuff
// plus a few globals
public class Utils {

    public static final int TEXT_OFFSET = 3;
    public static final int TEXT_SHADE = Color.GRAY;

    private static Random random = new Random();

    // generates a color that is more red if opponent is false (your color)
    // otherwise the color is more blue (the opponent's color)
    public static int generateRandomColor(boolean opponent) {
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        int color = 0xff000000;

        if (opponent) {
            color |= blue;
        } else {
            color |= red << 16;
        }

        color |= green << 8;
        return color;
    }

    // generates a color that is more red if opponent is false (your color)
    // otherwise the color is more blue (the opponent's color)
    // HTML color version
    public static String generateRandomColorString(boolean opponent) {
        int color = generateRandomColor(opponent);
        return "#" + Integer.toHexString(color);
    }
}
