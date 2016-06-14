package dk.simonsteinaa.connect4.screens;

import android.graphics.Color;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import dk.simonsteinaa.framework.interfaces.Game;
import dk.simonsteinaa.framework.interfaces.Screen;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by simon on 4/24/16.
 */
public class GameScreen extends Screen {

    private Board board;

    // variable that says when you are allowed to touch
    private boolean touchEnabled = true;

    // variable that says if the current falling piece is yours or not
    private boolean yourMove;

    private String yourName = "";
    private String opponentName = "";

    // this flag determines if you are player 2 according to the server
    private boolean isPlayer2;

    // this flag is used to make the VS AI and VS HUMAN buttons accessible again if set to true
    private boolean state;

    private JSONObject gameJSON; // the actual game object for passing on
    private int p1MovedBeforeP2WasReady = -1; // stores the last move of player 1, if your are a slow p2 - it is a column - will be updated by the GameController

    // represents the json game object
    private String gameId;
    private String gamePlayer1;
    private String gameWinner = "";
    private boolean gamePlayer2IsServer;
    private int gameLastMoveCol;

    private int yourColor;
    private int opponentColor;

    public GameScreen(Game game, int rows, int cols, int yourColor, int opponentColor, String yourName, JSONObject gameJSON, int p1MovedBeforeP2WasReady) {
        super(game);

        this.p1MovedBeforeP2WasReady = p1MovedBeforeP2WasReady;

        this.gameJSON = gameJSON;
        String gamePlayer2 = null;
        try {
            gamePlayer1 = gameJSON.getString("player1");
            gamePlayer2 = gameJSON.getString("player2");
            gameId = gameJSON.getString("gameId");
            gamePlayer2IsServer = gameJSON.getBoolean("player2IsServer");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (gamePlayer2 != null) {

            if (yourName.equals(gamePlayer1)) {
                // your name matches player 1 according to the server
                // so the other player must be player 2
                opponentName = gamePlayer2;

            } else {
                // the other player must be player 1
                opponentName = gamePlayer1;
                this.isPlayer2 = true;

            }
        }
        setupSocketLogic();

        board = new Board(game.getGraphics(), game.getGraphics().getWidth(), game.getGraphics().getHeight(), rows, cols);

        this.yourColor = yourColor;
        this.opponentColor = opponentColor;
        this.yourName = yourName;

        reset();
    }

    public void reset() {
        touchEnabled = true;
        gameWinner = "";
        state = false;

        board.reset();

        if (isPlayer2) {
            touchEnabled = false;
            // send waiting signal to screen
            board.waitingForOpponent = new PlayerObject(opponentName, Color.WHITE);
            // if this variable is greater than -1  it means you are p2 and p1 moved before you
            if (p1MovedBeforeP2WasReady != -1) {
                // move
                placeToken(p1MovedBeforeP2WasReady, opponentColor);
                yourMove = false;
            }
        }
    }

    public void placeToken(int column, int color) {
        board.placeToken(column, color);
        // when a token is falling down, it should not be possible to press the screen
        touchEnabled = false;
    }

    public void processTouchEvents() {
        if (touchEnabled) {
            if (game.getInput().isTouchDown(0)) {
                touchEnabled = false;
                int x = game.getInput().getTouchX(0);
                int y = game.getInput().getTouchY(0);
                if (y < board.getHeight() && x < board.getWidth()) {
                    if (board.isMoveValid(x / board.getPieceWidth())) {
                        placeToken((int) (x / board.getPieceWidth()), yourColor);
                        yourMove = true;
                    } else {
                        touchEnabled = true;
                    }
                }
            }
        }
    }


    private void handleBottomCollision() {
        // if a token has hit the bottom on its column, do something interesting here
        // concerning the logic of the game
        if (board.checkBottom() != null) {

            if (board.waitingForOpponent != null) {
                board.waitingForOpponent = null;
            }

            if (this.winnerFound())
                ;
            else {

                if (!yourMove) {
                    this.touchEnabled = true;
                } else {
                    // this means that it is your piece/token who hit the bottom
                    touchEnabled = false;

                    String playerToken;
                    if (gamePlayer2IsServer) {
                        playerToken = gamePlayer1.equals("Server") ? "B" : "R";
                    } else {
                        playerToken = isPlayer2 ? "B" : "R";
                    }

                    // compose the move
                    JsonObject moveData = new JsonObject();
                    moveData.addProperty("gameId", gameId);
                    moveData.addProperty("row", board.hitBottom.row);
                    moveData.addProperty("col", board.hitBottom.column);
                    moveData.addProperty("player", playerToken);

                    Gson gson = new GsonBuilder().create();

                    JsonParser jp = new JsonParser();
                    JsonElement je = jp.parse(String.valueOf(moveData));

                    // emit the move to the server
                    if (gamePlayer2IsServer) {
                        game.getSockets().getSocket().emit("human move versus AI", je);
                    } else {
                        if (isPlayer2) {
                            game.getSockets().getSocket().emit("player 2 move", je);
                        } else {
                            game.getSockets().getSocket().emit("player 1 move", je);
                        }
                    }

                    // send waiting signal to screen
                    board.waitingForOpponent = new PlayerObject(opponentName, Color.WHITE);
                }
            }
            // reset
            board.nullBottom();

        }
    }

    public boolean winnerFound() {
        if (!gameWinner.equals("") && !(gameWinner.equals("null") || gameWinner.equals(""))) {
            if (board.waitingForOpponent != null) {
                board.waitingForOpponent = null;
            }
            // after the win messages is displayed disable the touch
            touchEnabled = false;

            board.winnerFound = new PlayerObject(gameWinner, Color.YELLOW);

            // make the VS AI and VS HUMAN buttons accessible again
            state = true;

            return true;
        }
        return false;
    }


    @Override
    public void update(float deltaTime) {
        processTouchEvents();
        handleBottomCollision();

        board.update(deltaTime);

    }


    // this method draws the board with the pieces/tokens
    @Override
    public void present(float deltaTime) {
        board.draw();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        // send a message to the server that you disconnected from the game

        try {
            if (gameJSON != null) {
                gameJSON.put("fromPlayer", yourName);
                Gson gson = new GsonBuilder().create();

                JsonParser jp = new JsonParser();
                JsonElement je = jp.parse(String.valueOf(gameJSON));
                game.getSockets().getSocket().emit("game disconnected", je);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean getBoolean() {
        return state;
    }

    public void setupSocketLogic() {

        final Socket finalSocket = game.getSockets().getSocket();
        finalSocket.on("you won", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject) args[0];
                gameJSON = obj;
                String newGameId;
                try {
                    newGameId = obj.getString("gameId");
                    if (gameId.equals(newGameId)) {
                        gameWinner = obj.getString("winner");

                        winnerFound();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).on("return from AI move", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                // here args is the game object returned
                JSONObject obj = (JSONObject) args[0];
                gameJSON = obj;
                String newGameId;
                try {
                    newGameId = obj.getString("gameId");
                    if (gameId.equals(newGameId)) {
                        gamePlayer1 = obj.getString("player1");
                        gamePlayer2IsServer = obj.getBoolean("player2IsServer");
                        JSONObject lastMove = obj.getJSONObject("lastMove");
                        gameLastMoveCol = lastMove.getInt("col");
                        gameWinner = obj.getString("winner");

                        yourMove = false;
                        placeToken(gameLastMoveCol, opponentColor);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

        }).on("player 1 moved", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                // here args is the game object returned
                JSONObject obj = (JSONObject) args[0];
                gameJSON = obj;
                String newGameId;
                try {

                    newGameId = obj.getString("gameId");

                    if (gameId.equals(newGameId) && isPlayer2) {
                        gamePlayer1 = obj.getString("player1");
                        gamePlayer2IsServer = obj.getBoolean("player2IsServer");
                        JSONObject lastMove = obj.getJSONObject("lastMove");
                        gameLastMoveCol = lastMove.getInt("col");
                        gameWinner = obj.getString("winner");

                        yourMove = false;
                        placeToken(gameLastMoveCol, opponentColor);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

        }).on("player 2 moved", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                // here args is the game object returned
                JSONObject obj = (JSONObject) args[0];
                gameJSON = obj;

                String newGameId;
                try {
                    newGameId = obj.getString("gameId");
                    if (gameId.equals(newGameId) && !isPlayer2) {

                        gamePlayer1 = obj.getString("player1");
                        gamePlayer2IsServer = obj.getBoolean("player2IsServer");
                        JSONObject lastMove = obj.getJSONObject("lastMove");
                        gameLastMoveCol = lastMove.getInt("col");
                        gameWinner = obj.getString("winner");

                        yourMove = false;
                        placeToken(gameLastMoveCol, opponentColor);


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }
}
