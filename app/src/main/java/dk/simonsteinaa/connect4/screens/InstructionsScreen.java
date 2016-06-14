package dk.simonsteinaa.connect4.screens;

import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

import dk.simonsteinaa.connect4.utilities.Utils;
import dk.simonsteinaa.framework.interfaces.Game;
import dk.simonsteinaa.framework.interfaces.Screen;
import io.socket.emitter.Emitter;

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
    private String yourName;
    private boolean youGoFirst;
    private int rows;
    private int columns;

    private JSONObject gameJSON;

    private int p1MovedBeforeP2WasReady = -1; // this is used to store the col p1 did while p2 was not ready

    public InstructionsScreen(Game game, int yourColor, int opponentColor, int connect, int rows, int columns, boolean youGoFirst, String yourName, final JSONObject gameJSON) {
        this(game);


        this.gameJSON = gameJSON;
        this.yourName = yourName;
        this.rows = rows;
        this.columns = columns;
        this.connect = connect;
        this.yourColor = yourColor;
        this.opponentColor = opponentColor;
        this.youGoFirst = youGoFirst;

        yourPiece = new Piece(game.getGraphics(), pieceSize + (pieceSize * 4) + pieceSize / 16, pieceSize - pieceSize / 8,
                200, 500,
                pieceSize, pieceSize, yourColor);

        opponentPiece = new Piece(game.getGraphics(), pieceSize + (pieceSize * 4) + pieceSize / 2, pieceSize * 2 + pieceSize / 4 + pieceSize / 8,
                200, 500,
                pieceSize, pieceSize, opponentColor);

        // if player 1 moves before player 2 has touched the InstructionsScreen, we need to save that move
        game.getSockets().getSocket().on("player 1 moved", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                // here args is the game object returned

                final JSONObject gameJSONcp = gameJSON;
                JSONObject gameJSON2 = (JSONObject) args[0];
                String newGameId;
                String gameId;
                try {
                    gameId = gameJSONcp.getString("gameId");
                    newGameId = gameJSON2.getString("gameId");
                    JSONObject lastMove = gameJSON2.getJSONObject("lastMove");
                    int lastMoveCol = lastMove.getInt("col");

                    if (gameId.equals(newGameId)) {
                        p1MovedBeforeP2WasReady = lastMoveCol;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public InstructionsScreen(Game game) {
        super(game);

        screenWidth = game.getGraphics().getWidth();
        screenHeight = game.getGraphics().getHeight();
        pieceSize = screenWidth / 7;
    }

    @Override
    public void update(float deltaTime) {
        if (game.getInput().isTouchDown(0)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            game.setScreen(new GameScreen(game, rows, columns, yourColor, opponentColor, yourName, gameJSON, p1MovedBeforeP2WasReady));
        }
    }

    @Override
    public void present(float deltaTime) {
        game.getGraphics().clear(Color.BLACK);
        game.getGraphics().drawText(pieceSize + screenWidth / 3, screenHeight / 8, "INSTRUCTIONS", Utils.TEXT_SHADE, screenHeight / 20);
        game.getGraphics().drawText(pieceSize + screenWidth / 3-Utils.TEXT_OFFSET, screenHeight / 8-Utils.TEXT_OFFSET, "INSTRUCTIONS", Color.YELLOW, screenHeight / 20);
        game.getGraphics().drawText(pieceSize + screenWidth / 3, screenHeight / 4, "You are this color:  ", Utils.TEXT_SHADE, screenHeight / 20);
        game.getGraphics().drawText(pieceSize + screenWidth / 3-Utils.TEXT_OFFSET, screenHeight / 4-Utils.TEXT_OFFSET, "You are this color:  ", Color.WHITE, screenHeight / 20);
        game.getGraphics().drawText(pieceSize + screenWidth / 3, screenHeight / 2, "Opponent is this color:  ", Utils.TEXT_SHADE, screenHeight / 20);
        game.getGraphics().drawText(pieceSize + screenWidth / 3-Utils.TEXT_OFFSET, screenHeight / 2-Utils.TEXT_OFFSET, "Opponent is this color:  ", Color.WHITE, screenHeight / 20);
        game.getGraphics().drawText(pieceSize + screenWidth / 3, screenHeight / 2 + screenHeight / 6, "Connect " + connect + " on a line to win!", Utils.TEXT_SHADE, screenHeight / 20);
        game.getGraphics().drawText(pieceSize + screenWidth / 3-Utils.TEXT_OFFSET, screenHeight / 2 + screenHeight / 6-Utils.TEXT_OFFSET, "Connect " + connect + " on a line to win!", Color.WHITE, screenHeight / 20);

        if (youGoFirst) {
            game.getGraphics().drawText(pieceSize + screenWidth / 3, screenHeight / 2 + screenHeight / 4 + screenHeight / 16, "You go first!", Utils.TEXT_SHADE, screenHeight / 20);
            game.getGraphics().drawText(pieceSize + screenWidth / 3-Utils.TEXT_OFFSET, screenHeight / 2 + screenHeight / 4 + screenHeight / 16-Utils.TEXT_OFFSET, "You go first!", Color.CYAN, screenHeight / 20);
        } else {
            game.getGraphics().drawText(pieceSize + screenWidth / 3, screenHeight / 2 + screenHeight / 4 + screenHeight / 16, "Opponent goes first!", Utils.TEXT_SHADE, screenHeight / 20);
            game.getGraphics().drawText(pieceSize + screenWidth / 3-Utils.TEXT_OFFSET, screenHeight / 2 + screenHeight / 4 + screenHeight / 16-Utils.TEXT_OFFSET, "Opponent goes first!", Color.CYAN, screenHeight / 20);
        }

        game.getGraphics().drawText(pieceSize + screenWidth / 3, screenHeight - screenHeight / 16, "Hit this screen to begin!", Utils.TEXT_SHADE, screenHeight / 18);
        game.getGraphics().drawText(pieceSize + screenWidth / 3-Utils.TEXT_OFFSET, screenHeight - screenHeight / 16-Utils.TEXT_OFFSET, "Hit this screen to begin!", Color.LTGRAY, screenHeight / 18);

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
        // this means that the VS AI and VS HUMAN buttons in the GUI will be available to push again
        return true;
    }
}
