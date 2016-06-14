package dk.simonsteinaa.connect4.screens;

import dk.simonsteinaa.connect4.utilities.Utils;
import dk.simonsteinaa.framework.interfaces.Graphics;

/**
 * Created by simon on 6/3/16.
 */
// this class is used in the IntroScreen and WaitingScreen
// it has slightly different behaviour from the original Piece
public class IntroPiece extends Piece {

    private int screenHeight;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    // for WaitingScreen
    public IntroPiece(Graphics graphics, int x, int y, int gravity, int speed, int width, int height, int color) {
        super(graphics, x, y, gravity, speed, width, height, color);
    }

    // for IntroScreen
    public IntroPiece(Graphics graphics, int x, int y, int gravity, int vy, int width, int height, int color, int screenHeight) {
        super(graphics, x, y, gravity, vy, width, height, color);

        this.screenHeight = screenHeight;
    }

    // used for animation in the IntroScreen
    // just hover up and down within the bounds
    @Override
    public void update(float deltaTime) {
        y += vy * deltaTime;

        if (y - height / 2 < screenHeight / 3) {
            y = screenHeight / 3 + height/2;
            vy *= -1;
            color = Utils.generateRandomColor(false);

        } else if (y + height / 2 >  3 *( screenHeight / 4)) {
            y = 3 *( screenHeight / 4) - height / 2;
            vy *= -1;
            color = Utils.generateRandomColor(false);
        }
    }
}