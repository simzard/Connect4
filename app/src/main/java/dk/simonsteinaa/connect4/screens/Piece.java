package dk.simonsteinaa.connect4.screens;

import android.graphics.Color;

import dk.simonsteinaa.framework.interfaces.Graphics;

/**
 * Created by simon on 4/24/16.
 */
public class Piece {
    protected boolean falling = true;
    protected int column;
    protected int x;
    protected int y;
    protected float gravity;
    protected float vy;

    protected int width;
    protected int height;
    protected int color;
    protected Graphics graphics;

    public Piece(Graphics graphics, int x, int y, int gravity, int speed, int width, int height, int color) {
        this.graphics = graphics;
        this.x = x;
        this.y = y;
        this.gravity = gravity;
        this.vy = speed;

        this.width = width;
        this.height = height;
        this.color =  color;
    }

    public boolean isFalling() {
        return falling;
    }

    public void setFalling(boolean falling) {
        this.falling = falling;
    }

    public void setY(int y) {
        this.y = y;
    }


    public int getY() {
        return y;
    }

    // just draw a circle in the center of the x,y point
    // (which is always the top left in the piece rectangle pieceWidth * pieceHeight)
    public void draw() {

        //int circleRadiusHorz = height / 2;
        //int centerX = x + circleRadiusHorz;
        //int centerY = y + circleRadiusHorz;
        int centerX = (int)(x + 0.5 * width);
        int centerY = (int)(y + 0.5 * height);
        //graphics.drawCircle(centerX, centerY, width / 2 - width / 9, color);
        graphics.drawCircle(centerX, centerY, width / 2 - width / 9, color);
    }

    // update for animation
    public void update(float deltaTime) {
        if (falling) {
            // make the token fall by increasing its y-coord - and add a little gravity / acceleration
            this.y += this.vy * deltaTime;
            this.vy += this.gravity;
        } else {
            // this piece is not falling - just make it's accleration and speed zero
            this.vy = 0;
            this.gravity = 0;
        }
    };


}
