package dk.simonsteinaa.connect4.screens;

import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.TextView;

import dk.simonsteinaa.framework.interfaces.Game;
import dk.simonsteinaa.framework.interfaces.Screen;

/**
 * Created by simon on 4/24/16.
 */
public class InstructionsScreen extends Screen {
    private int screenWidth;
    private int screenHeight;

    private int pieceSize;

    private Piece yourPiece;
    private Piece opponentPiece;
    private int connect;
    private int yourColor;
    private int opponentColor;
    private boolean youGoFirst;
    private int rows;
    private int columns;

    public boolean sendEnableButton = false;

    public InstructionsScreen(Game game, int yourColor, int opponentColor, int connect, int rows, int columns, boolean youGoFirst) {
        this(game);

        this.rows = rows;
        this.columns = columns;
        this.connect = connect;
        this.yourColor = yourColor;
        this.opponentColor = opponentColor;
        this.youGoFirst = youGoFirst;
        yourPiece = new Piece(game.getGraphics(),pieceSize +  (pieceSize * 4) + pieceSize / 16, pieceSize - pieceSize / 8,
                200, 500,
                pieceSize, pieceSize, yourColor);

        opponentPiece = new Piece(game.getGraphics(),pieceSize +  (pieceSize * 4) + pieceSize / 2, pieceSize * 2 + pieceSize / 4 + pieceSize / 8,
                200, 500,
                pieceSize, pieceSize, opponentColor);

    }


    public InstructionsScreen(Game game) {
        super(game);

        screenWidth = game.getGraphics().getWidth();
        screenHeight = game.getGraphics().getHeight();
        pieceSize = screenWidth / 7;

        //piece = new Piece(game.getGraphics(),100, 100, 2, 3, 30, 30, Color.BLUE);



    }

    @Override
    public void update(float deltaTime) {
        if (game.getInput().isTouchDown(0)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } {
                game.setScreen(new GameScreen(game, rows, columns, yourColor, opponentColor, connect));
            }

        }
    }

    @Override
    public void present(float deltaTime) {
        game.getGraphics().clear(Color.BLACK);
        game.getGraphics().drawText(pieceSize + screenWidth / 3, screenHeight / 8, "INSTRUCTIONS", Color.YELLOW, screenHeight / 20);
        game.getGraphics().drawText(pieceSize + screenWidth / 3, screenHeight / 4, "You are this color:  ", Color.WHITE, screenHeight / 20);
        game.getGraphics().drawText(pieceSize + screenWidth / 3, screenHeight / 2, "Opponent is this color:  ", Color.WHITE, screenHeight / 20);
        game.getGraphics().drawText(pieceSize + screenWidth / 3, screenHeight / 2 + screenHeight / 6, "Connect " + connect + " on a line to win!", Color.WHITE, screenHeight / 20);

        if (youGoFirst) {
            game.getGraphics().drawText(pieceSize + screenWidth / 3, screenHeight / 2 + screenHeight / 4 + screenHeight / 16, "You go first!", Color.CYAN, screenHeight / 20);
        } else {
            game.getGraphics().drawText(pieceSize + screenWidth / 3, screenHeight / 2 + screenHeight / 4 + screenHeight / 16, "Opponent goes first!", Color.CYAN, screenHeight / 20);
        }

        game.getGraphics().drawText(pieceSize + screenWidth / 3, screenHeight - screenHeight / 16, "Hit this screen to begin!", Color.LTGRAY, screenHeight / 25);

        yourPiece.draw();
        opponentPiece.draw();
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
        return true;
    }
}
