package dk.simonsteinaa.connect4.screens;

import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.simonsteinaa.framework.interfaces.Game;
import dk.simonsteinaa.framework.interfaces.Graphics;
import dk.simonsteinaa.framework.interfaces.Screen;

/**
 * Created by simon on 4/24/16.
 */
public class IntroScreen extends Screen {

    Random random = new Random();
    private int pieceSize;
    private int height;
    private int screenWidth;
    private int screenHeight;

    private int waitCounter = 0;

    private int hitCounter = 0;

    private int globalCounter = 0;


    private List<IntroPiece> piecesHigh = new ArrayList();


    public IntroScreen(Game game) {
        super(game);
        screenWidth = game.getGraphics().getWidth();
        screenHeight = game.getGraphics().getHeight();
        pieceSize = screenWidth / 7;

        height = pieceSize * 6;
        //piece = new Piece(game.getGraphics(),100, 100, 2, 3, 30, 30, Color.BLUE);

        initPieces();





    }

    private void initPieces() {
        for (int i = 0; i < 4; i++) {
            IntroPiece p = new IntroPiece(game.getGraphics(), i * pieceSize + screenWidth / 5, (i * screenHeight / 30) + (screenHeight / 2 + screenHeight / 40), 0, 200, pieceSize, pieceSize, generateRandomColor(false));
            piecesHigh.add(p);

        }
    }

    private int generateRandomColor(boolean opponent) {
        int red = random.nextInt(256);
        int green = random.nextInt(128);
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

    @Override
    public void update(float deltaTime) {



    }
    @Override
    public void present(float deltaTime) {
        game.getGraphics().clear(Color.BLACK);
        game.getGraphics().drawText(screenWidth / 2, height / 4, "Welcome to Connect! ", Color.WHITE, height / 15);

        game.getGraphics().drawText(screenWidth / 2, height / 2, "Choose game below to the right! ", Color.WHITE, height / 20);

        game.getGraphics().drawText(screenWidth / 2, height / 2 + height/4 + height / 8, "And press one of the buttons to start! ", Color.WHITE, height / 25);

        for (int i = 0; i < 4; i++) {
            piecesHigh.get(i).draw();
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean getBoolean() {
        return false;
    }

    private class IntroPiece extends Piece {
        private float speed;
        private int counter = 0;

        private int state = 0;
        private boolean counted = false;


        public IntroPiece(Graphics graphics, int x, int y, int gravity, int speed, int width, int height, int color) {
            super(graphics, x, y, gravity, speed, width, height, color);

        }


    }
}
