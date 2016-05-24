package dk.simonsteinaa.connect4.screens;

import android.graphics.Color;

import dk.simonsteinaa.framework.interfaces.Game;
import dk.simonsteinaa.framework.interfaces.Screen;

/**
 * Created by simon on 4/24/16.
 */
public class IntroScreen extends Screen {

    Piece piece;

    public IntroScreen(Game game) {
        super(game);
        piece = new Piece(game.getGraphics(),100, 100, 2, 3, 30, 30, Color.BLUE);
    }

    @Override
    public void update(float deltaTime) {
        piece.update();
    }

    @Override
    public void present(float deltaTime) {
        game.getGraphics().clear(Color.BLACK);
        piece.draw();
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
}
