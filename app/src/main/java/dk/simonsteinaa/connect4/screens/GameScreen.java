package dk.simonsteinaa.connect4.screens;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import dk.simonsteinaa.connect4.utilities.SocketHandler;
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

    private boolean yourMove;

    private boolean waitingForWinText;

    // represents the json game object
    private String gameId;
    private String gamePlayer1;
    private String gamePlayer2;
    private String gameWinner = "";
    private int gameLastMoveCol;


    private SocketHandler socketHandler;

    public GameScreen(Game game) {
        super(game);
        socketHandler = new SocketHandler();
        socketHandler.connect();
        setupSocketLogic();
        board = new Board(game.getGraphics(), 320, 240, 6, 7);

        // TODO: change into multiplayer
        // this automatically starts a game versus the AI
        socketHandler.getSocket().emit("new AI game", "AndroidPlayer");
    }

    public void reset() {
        touchEnabled = false;
        gameId = "";
        gameWinner = "";
        waitingForWinText = false;
        board.reset();
    }

    public void placeToken(int column, int color) {
        board.placeToken(column, color);
        // when a token is falling down, it should not be possible to affect the screen
        touchEnabled = false;
    }

    public void processTouchEvents() {
        // process touch
        if (touchEnabled) {
            if (game.getInput().isTouchDown(0)) {
                touchEnabled = false;
                if (!waitingForWinText) {
                    int x = game.getInput().getTouchX(0);
                    int y = game.getInput().getTouchY(0);
                    if (y < board.getHeight() && x < board.getWidth()) {
                        if (board.isMoveValid(x / board.getPieceWidth())) {
                            placeToken((int)(x / board.getPieceWidth()), Color.BLUE);
                            yourMove = true;
                        } else {
                            touchEnabled = true;
                        }
                    }

                } else {
                    // this means that the win screen should change
                    board.winnerFound = null;
                    //SCREENS.INSTRUCTIONS_SCREEN.updatePlayerColors();
                    //this.graphics.screen = SCREENS.CONNECTION_SCREEN;
                    // reset
                    waitingForWinText = false;

                    reset();

                    //socketHandler.getSocket().disconnect();
                    //socketHandler.getSocket().close();

                    // connect for a new game
                    //socketHandler.connect();
                    //setupSocketLogic();

                    socketHandler.getSocket().emit("new AI game", "AndroidPlayer");;

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
                    touchEnabled = false;

                    JsonObject moveData = new JsonObject();
                    moveData.addProperty("gameId", gameId);
                    moveData.addProperty("row", board.hitBottom.row);
                    moveData.addProperty("col", board.hitBottom.column);
                    moveData.addProperty("player", "R");

                    Gson gson = new GsonBuilder().create();

                    JsonParser jp = new JsonParser();
                    JsonElement je = jp.parse(String.valueOf(moveData));

                    Log.i("moveData: ", moveData.toString());
                    socketHandler.getSocket().emit("human move versus AI", je);

                    // send waiting signal to screen
                    board.waitingForOpponent = new PlayerObject(gamePlayer2, Color.MAGENTA);


                }



            }
            // reset
            board.nullBottom();

        }

    }

    public boolean winnerFound() {
        if (!gameWinner.equals("") && !(gameWinner.equals("null") || gameWinner.equals(""))) {
            Log.i("Winner", gameWinner);
            if (board.waitingForOpponent != null) {
                board.waitingForOpponent = null;
            }
            // reset
            touchEnabled = true;
            //this.graphics.screen.hitBottom = null;



            //alert("Winner: " + this.game.winner);
            board.winnerFound = new PlayerObject(gameWinner, Color.YELLOW);


            // now set this flag that prevents the screen from changing
            waitingForWinText = true;



            return true;
        }
        return false;
    }


    @Override
    public void update(float deltaTime) {
        processTouchEvents();
        handleBottomCollision();


        board.update();

    }



    // this method draws the board and the pieces/tokens
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
        socketHandler.getSocket().emit("game disconnected", this.gameId);
        socketHandler.getSocket();
    }



    public void setupSocketLogic() {


        final Socket finalSocket = socketHandler.getSocket();
        finalSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {


            }

        }).on("return from new AI game", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                // here args is the game object returned
                Log.i("return new game", "...");
                JSONObject obj = (JSONObject)args[0];
                try {
                    gameId = obj.getString("gameId");
                    gamePlayer1 = obj.getString("player1");
                    gamePlayer2 = obj.getString("player2");
                    Log.i("return init ai game: ", "\nGameId: " + gameId + "\nplayer1: " + gamePlayer1 +
                            "\nplayer2: " + gamePlayer2);

                    touchEnabled = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }




            }

        }).on("return new game", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                // here args is the game object returned
                Log.i("return new game", "...");
                JSONObject obj = (JSONObject)args[0];
                try {
                    gameId = obj.getString("gameId");
                    gamePlayer1 = obj.getString("player1");
                    gamePlayer2 = obj.getString("player2");
                    Log.i("return new game name: ", "\nGameId: " + gameId + "\nplayer1: " + gamePlayer1 +
                            "\nplayer2: " + gamePlayer2);

                    finalSocket.emit("init ai", gameId);

                } catch (JSONException e) {
                    e.printStackTrace();
                }




            }

        }).on("you won", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject)args[0];
                try {
                    gameWinner = obj.getString("winner");

                    winnerFound();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).on("return from AI move", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                // here args is the game object returned
                Log.i("return ai move game", "...");
                JSONObject obj = (JSONObject)args[0];
                try {
                    gameId = obj.getString("gameId");
                    gamePlayer1 = obj.getString("player1");
                    gamePlayer2 = obj.getString("player2");
                    JSONObject lastMove = obj.getJSONObject("lastMove");
                    gameLastMoveCol = lastMove.getInt("col");
                    gameWinner = obj.getString("winner");
                    Log.i("ret ai move game: ", "\nGameId: " + gameId + "\nplayer1: " + gamePlayer1 +
                            "\nplayer2: " + gamePlayer2);
                    Log.i("Last move col: ", gameLastMoveCol + "" );

                    yourMove = false;
                    placeToken(gameLastMoveCol, Color.WHITE);


                } catch (JSONException e) {
                    e.printStackTrace();
                }




            }

        }).on("return init ai game", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                // here args is the game object returned
                Log.i("return init ai game", "...");
                JSONObject obj = (JSONObject)args[0];
                try {
                    gameId = obj.getString("gameId");
                    gamePlayer1 = obj.getString("player1");
                    gamePlayer2 = obj.getString("player2");
                    Log.i("return init ai game: ", "\nGameId: " + gameId + "\nplayer1: " + gamePlayer1 +
                            "\nplayer2: " + gamePlayer2);

                    touchEnabled = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }




            }

        }).on("return human move game", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                // here args is the game object returned
                Log.i("return human move game", "...");
                JSONObject obj = (JSONObject)args[0];
                try {
                    gameId = obj.getString("gameId");
                    gamePlayer1 = obj.getString("player1");
                    gamePlayer2 = obj.getString("player2");
                    JSONObject lastMove = obj.getJSONObject("lastMove");
                    gameLastMoveCol = lastMove.getInt("col");
                    gameWinner = obj.getString("winner");
                    Log.i("ret human move game: ", "\nGameId: " + gameId + "\nplayer1: " + gamePlayer1 +
                            "\nplayer2: " + gamePlayer2);
                    Log.i("Last move col: ", gameLastMoveCol + "" );


                    if (winnerFound())
                        ;
                    else {
                        finalSocket.emit("ai move", gameId);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }




            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }

        });
    }
}
