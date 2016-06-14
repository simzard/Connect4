package dk.simonsteinaa.connect4.screens;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import dk.simonsteinaa.connect4.utilities.Utils;
import dk.simonsteinaa.framework.interfaces.Game;
import dk.simonsteinaa.framework.interfaces.Screen;

/**
 * Created by simon on 4/24/16.
 */
// The intro splash screen
public class IntroScreen extends Screen {

    private static final int NUM_OF_CIRCLES = 4;

    private int pieceSize;
    private int height;
    private int screenWidth;
    private int screenHeight;

    private List<IntroPiece> pieces = new ArrayList();

    public IntroScreen(Game game) {
        super(game);
        screenWidth = game.getGraphics().getWidth();
        screenHeight = game.getGraphics().getHeight();
        pieceSize = screenWidth / 7;
        height = pieceSize * 6;

        initPieces();
    }

    private void initPieces() {
        for (int i = 0; i < NUM_OF_CIRCLES; i++) {
            int newVy = 200;
            IntroPiece p = new IntroPiece(game.getGraphics(), i * pieceSize + screenWidth / 5, (i * screenHeight / 30) + (screenHeight / 2 + screenHeight / 40), 0, newVy, pieceSize, pieceSize, Utils.generateRandomColor(false), screenHeight);
            pieces.add(p);
        }
    }

    @Override
    public void update(float deltaTime) {
        for (int i = 0; i < NUM_OF_CIRCLES; i++) {
            IntroPiece p = pieces.get(i);
            p.update(deltaTime);
        }
    }

    @Override
    public void present(float deltaTime) {
        game.getGraphics().clear(Color.BLACK);
        game.getGraphics().drawText(screenWidth / 2, height / 5, "Welcome to Connect! ", Utils.TEXT_SHADE, height / 15);
        game.getGraphics().drawText(screenWidth / 2-Utils.TEXT_OFFSET, height / 5-Utils.TEXT_OFFSET, "Welcome to Connect! ", Color.WHITE, height / 15);

        game.getGraphics().drawText(screenWidth / 2, height / 3 + height / 16, "Choose game below to the right! ", Utils.TEXT_SHADE, height / 20);
        game.getGraphics().drawText(screenWidth / 2-Utils.TEXT_OFFSET, height / 3 + height / 16-Utils.TEXT_OFFSET, "Choose game below to the right! ", Color.WHITE, height / 20);

        game.getGraphics().drawText(screenWidth / 2, height / 2 + height/4 + height / 6, "And press one of the buttons to start! ", Utils.TEXT_SHADE, height / 25);
        game.getGraphics().drawText(screenWidth / 2-Utils.TEXT_OFFSET, height / 2 + height/4 + height / 6-Utils.TEXT_OFFSET, "And press one of the buttons to start! ", Color.WHITE, height / 25);

        for (int i = 0; i < NUM_OF_CIRCLES; i++) {
            pieces.get(i).draw();
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
}
