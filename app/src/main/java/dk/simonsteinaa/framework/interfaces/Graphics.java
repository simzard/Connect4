package dk.simonsteinaa.framework.interfaces;

/**
 * Created by simon on 4/24/16.
 */
public interface Graphics {

    void clear(int color);

    void drawLine(int x, int y, int x2, int y2, int color);

    void drawCircle(int x, int y, int radius, int color);

    void drawText(int x, int y, String text, int color, int textSize);

    int getWidth();

    int getHeight();
}