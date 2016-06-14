package dk.simonsteinaa.framework.implementation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;

import dk.simonsteinaa.framework.interfaces.Graphics;

/**
 * Created by simon on 4/24/16.
 */
public class GameGraphics implements Graphics {

    Bitmap frameBuffer;
    Canvas canvas;
    Paint paint;
    Typeface font;

    public GameGraphics(Bitmap frameBuffer, Typeface font) {
        this.frameBuffer = frameBuffer;
        this.canvas =  new Canvas(frameBuffer);
        this.paint =  new Paint();
        this.font = font;

    }

    @Override
    public void clear(int color) {
        // paint the screen with the chosen color
        canvas.drawRGB((color & 0xff0000) >> 16, (color & 0xff00) >> 8,
                (color & 0xff));
    }

    @Override
    public void drawLine(int x, int y, int x2, int y2, int color) {
        paint.setColor(color);
        canvas.drawLine(x, y, x2, y2, paint);
    }

    @Override
    public void drawCircle(int x, int y, int radius, int color) {
        // paint a circle with an applied gradient
        RadialGradient gradient = new RadialGradient(x, y, radius - 5, 0xffffffff,
                color, Shader.TileMode.CLAMP);
        paint.setColor(color);
        paint.setDither(true);
        paint.setShader(gradient);
        canvas.drawCircle(x, y, radius, paint);
        paint.setShader(null);
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
