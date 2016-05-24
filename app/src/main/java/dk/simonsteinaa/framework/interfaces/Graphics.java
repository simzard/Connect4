package dk.simonsteinaa.framework.interfaces;

import android.graphics.Color;

/**
 * Created by simon on 4/24/16.
 */
public interface Graphics {

    public void clear(int color);

    public void drawPixel(int x, int y, int color);

    public void drawLine(int x, int y, int x2, int y2, int color);

    public void drawRect(int x, int y, int width, int height, int color);

    public void drawCircle(int x, int y, int radius, int color);

    public void drawText(int x, int y, String text, int color, int textSize);


    public int getWidth();

    public int getHeight();
}