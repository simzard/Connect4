package dk.simonsteinaa.connect4.screens;

import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dk.simonsteinaa.connect4.utilities.Utils;
import dk.simonsteinaa.framework.interfaces.Game;
import dk.simonsteinaa.framework.interfaces.Screen;
import io.socket.emitter.Emitter;

/**
 * Created by simon on 6/3/16.
 */
public class WaitingScreen extends Screen {

    private static final int NUM_CIRCLES = 9;

    private int screenWidth;
    private int pieceSize;

    private int height;

    // math stuff
    private int angle = 0; // in degrees
    private int angleVel = 3;
    private int direction = 1;

    private List<IntroPiece> piecesHigh = new ArrayList();

    private int rows;
    private int columns;
    private int connect;

    private boolean youGoFirst;

    private Handler handler = new Handler();

    public WaitingScreen(final Game game, final int yourColor, final int opponentColor, final int connect, final int rows, final int columns, final String yourName, final JSONObject gameJSON) {
        this(game);

        String gamePlayer2 = null;
        String gameId = null;
        try {

            gamePlayer2 = gameJSON.getString("player2");
            gameId = gameJSON.getString("gameId");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // test if there is a player 2 connected
        // if noone else has connected (player2 == null) you must be player1
        // otherwise you must be player2
        if (gamePlayer2 != null) {

            if (yourName.equals(gamePlayer2)) {
                // the other player must be player 1
                game.getSockets().getSocket().emit("player 2 connected", gameId);
            } else {
                // you are player 1 and therefore goes first
                youGoFirst = true;

            }
        }

        this.connect = connect;
        this.rows = rows;
        this.columns = columns;

        game.getSockets().getSocket().on("player 2 connected", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                final JSONObject obj = (JSONObject) args[0];



                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // this will be filled out with the correct info from the JSON game object
                        game.setScreen(new InstructionsScreen(game, yourColor, opponentColor, connect, rows, columns, youGoFirst, yourName, obj));
                    }
                });

                Log.i("JSON", obj.toString());
            }
        });
    }


    public WaitingScreen(Game game) {
        super(game);
        screenWidth = game.getGraphics().getWidth();
        pieceSize = screenWidth / 7;

        height = pieceSize * 6;

        initPieces();
    }

    private void initPieces() {
        for (int i = 0; i < NUM_CIRCLES; i++) {
            piecesHigh.add(new IntroPiece(game.getGraphics(), 0, 0, 0, 200, pieceSize, pieceSize, Utils.generateRandomColor(false)));
        }
    }

    @Override
    public void update(float deltaTime) {
        // make the circles go around in a circle with some variation
        angle += angleVel;

        if (angle > 360) {
            angleVel *= -1;
            direction *= -1;
        } else if (angle < 0) {
            angleVel *= -1;
            direction *= -1;
        }

        float degToRad = 3.14f / 180.0f;

        for (int i = 0; i < NUM_CIRCLES; i++) {
            piecesHigh.get(i).setX((int) ((screenWidth / 2 - (pieceSize / 2 - pieceSize * Math.cos((angle * ((i + 1) / 4.0f)) * degToRad)))));
            piecesHigh.get(i).setY((int) ((screenWidth / 2 + direction * (pieceSize * Math.sin((angle * ((i + 1) / 4.0f)) * degToRad)))));
        }
    }

    @Override
    public void present(float deltaTime) {
        game.getGraphics().clear(Color.BLACK);


        game.getGraphics().drawText(screenWidth / 2, height / 8, "Waiting for opponents", Utils.TEXT_SHADE, height / 15);
        game.getGraphics().drawText(screenWidth / 2-Utils.TEXT_OFFSET, height / 8-Utils.TEXT_OFFSET, "Waiting for opponents", Color.WHITE, height / 15);

        game.getGraphics().drawText(screenWidth / 2, height / 4, "To play connect " + connect + " on a " + rows + "x" + columns + " board", Utils.TEXT_SHADE, height / 19);
        game.getGraphics().drawText(screenWidth / 2-Utils.TEXT_OFFSET, height / 4-Utils.TEXT_OFFSET, "To play connect " + connect + " on a " + rows + "x" + columns + " board", 0xff009900, height / 19);


        game.getGraphics().drawText(screenWidth / 2, height / 3 + height / 16, "Please wait...", Utils.TEXT_SHADE, height / 20);
        game.getGraphics().drawText(screenWidth / 2-Utils.TEXT_OFFSET, height / 3 + height / 16-Utils.TEXT_OFFSET, "Please wait...", Color.WHITE, height / 20);

        for (int i = 0; i < NUM_CIRCLES; i++) {
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
        return true;
    }
}
