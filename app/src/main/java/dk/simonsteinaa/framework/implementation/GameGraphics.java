package dk.simonsteinaa.framework.implementation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import dk.simonsteinaa.framework.interfaces.Graphics;

/**
 * Created by simon on 4/24/16.
 */
public class GameGraphics implements Graphics {

    Bitmap frameBuffer;
    Canvas canvas;
    Paint paint;
    Rect srcRect =  new Rect();
    Rect dstRect =  new Rect();
    Typeface font;

    public GameGraphics(Bitmap frameBuffer, Typeface font) {
        this.frameBuffer = frameBuffer;
        this.canvas =  new Canvas(frameBuffer);
        this.paint =  new Paint();
        this.font = font;

    }

    @Override
    public void clear(int color) {
        canvas.drawRGB((color & 0xff0000) >> 16, (color & 0xff00) >> 8,
                (color & 0xff));
    }

    @Override
    public void drawPixel(int x, int y, int color) {
        paint.setColor(color);
        canvas.drawPoint(x, y, paint);
    }

    @Override
    public void drawLine(int x, int y, int x2, int y2, int color) {
        paint.setColor(color);
        canvas.drawLine(x, y, x2, y2, paint);
    }

    @Override
    public void drawRect(int x, int y, int width, int height, int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(x, y, x + width - 1, y + width - 1, paint);

    }

    @Override
    public void drawCircle(int x, int y, int radius, int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x, y, radius, paint);
    }

    @Override
    public void drawText(int x, int y, String text, int color, int textSize) {
        paint.setColor(color);
        paint.setTypeface(font);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, x, y, paint);
    }

    @Override
    public int getWidth() {
        return frameBuffer.getWidth();
    }

    @Override
    public int getHeight() {
        return frameBuffer.getHeight();
    }
}
