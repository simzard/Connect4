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

import java.util.Random;

import dk.simonsteinaa.connect4.utilities.SocketHandler;
import dk.simonsteinaa.framework.interfaces.Game;
import dk.simonsteinaa.framework.interfaces.Screen;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by simon on 4/24/16.
 */
public class GameScreen extends Screen {

    private Random random = new Random();

    private Board board;
    // variable that says when you are allowed to touch
    private boolean touchEnabled = true;

    private boolean yourMove;

    private boolean waitingForWinText;

    private String yourName = "";
    private String opponentName = "";

    // this flag determines if you are player 2 according to the server
    private boolean isPlayer2;


    private JSONObject gameJSON; // the actual game object for passing on

    // represents the json game object
    private String gameId;
    private String gamePlayer1;
    private String gamePlayer2;
    private String gameWinner = "";
    private boolean gamePlayer2IsServer;
    private int gameLastMoveCol;

    private int numberOfCols = 7;
    private int numberOfRows = 6;

    private int yourColor;
    private int opponentColor;
    private int connect = 4;

    private boolean INTERNET = true;

    private SocketHandler socketHandler;

    public GameScreen(Game game, int rows, int cols, int yourColor, int opponentColor, int connect ) {
        super(game);
        if (INTERNET) {
            socketHandler = new SocketHandler();
            socketHandler.connect();
            setupSocketLogic();
        }
        board = new Board(game.getGraphics(), game.getGraphics().getWidth(), game.getGraphics().getHeight(), rows, cols);
        numberOfCols = cols;
        numberOfRows = rows;
        this.yourColor = yourColor;
        this.opponentColor = opponentColor;
        this.connect = connect;
        reset();
    }

    public GameScreen(Game game) {
        super(game);
        if (INTERNET) {
            socketHandler = new SocketHandler();
            socketHandler.connect();
            setupSocketLogic();
        }

        //board = new Board(game.getGraphics(), 320, 240, 6, 7);
        board = new Board(game.getGraphics(), game.getGraphics().getWidth(), game.getGraphics().getHeight(), 6, 7);

        // TODO: change into multiplayer
        // this automatically starts a game versus the AI
        //socketHandler.getSocket().emit("new AI game", "AndroidPlayer");
        if (INTERNET) {
            socketHandler.getSocket().emit("new game", "AndroidPlayer");
        }

        reset();


    }

    public void reset() {
        touchEnabled = true;
        gameId = "";
        gameWinner = "";
        waitingForWinText = false;
        board.reset();


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
                            placeToken((int) (x / board.getPieceWidth()), yourColor);
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
                    if (INTERNET)
                        socketHandler.getSocket().emit("new AI game", "AndroidPlayer");
                    ;

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


                    String playerToken;
                    if (gamePlayer2IsServer) {
                        playerToken = gamePlayer1.equals("Server") ? "B" : "R";
                    } else {
                        playerToken = isPlayer2 ? "B" : "R";
                    }

                    JsonObject moveData = new JsonObject();
                    moveData.addProperty("gameId", gameId);
                    moveData.addProperty("row", board.hitBottom.row);
                    moveData.addProperty("col", board.hitBottom.column);
                    moveData.addProperty("player", playerToken);

                    Gson gson = new GsonBuilder().create();

                    JsonParser jp = new JsonParser();
                    JsonElement je = jp.parse(String.valueOf(moveData));

                    Log.i("moveData: ", moveData.toString());

                    // deleteme
                    yourMove = false;

                    placeToken(random.nextInt(numberOfCols), opponentColor);
                    // end deleteme
                    if (INTERNET) {
                        if (gamePlayer2IsServer) {
                            socketHandler.getSocket().emit("human move versus AI", je);
                        } else {
                            if (isPlayer2) {
                                socketHandler.getSocket().emit("player 2 move", je);
                            } else {
                                socketHandler.getSocket().emit("player 1 move", je);
                            }
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


        board.update(deltaTime);

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
        if (INTERNET) {
            try {
                gameJSON.put("fromPlayer", yourName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socketHandler.getSocket().emit("game disconnected", gameJSON);
            socketHandler.getSocket();
        }
    }

    @Override
    public boolean getBoolean() {
        return false;
    }


    public void setupSocketLogic() {

        final Socket finalSocket = socketHandler.getSocket();
        finalSocket.on("return from new AI game", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                // here args is the game object returned
                Log.i("return new game", "...");
                JSONObject obj = (JSONObject) args[0];
                gameJSON = obj;
                try {
                    gameId = obj.getString("gameId");
                    gamePlayer1 = obj.getString("player1");
                    gamePlayer2 = obj.getString("player2");
                    gamePlayer2IsServer = obj.getBoolean("player2IsServer");
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
                JSONObject obj = (JSONObject) args[0];
                gameJSON = obj;
                try {
                    gameId = obj.getString("gameId");
                    gamePlayer1 = obj.getString("player1");
                    gamePlayer2 = obj.getString("player2");
                    gamePlayer2IsServer = obj.getBoolean("player2IsServer");
                    // test if there is a player 2 connected
                    // if noone else has connected (player2 == null) you must be player1
                    // otherwise you must be player2
                    if (gamePlayer2 != null) {

                        if (yourName.equals(gamePlayer1)) {
                            // your name matches player 1 according to the server
                            // so the other player must be player 2
                            opponentName = gamePlayer2;
                        } else {
                            // the other player must be player 1
                            opponentName = gamePlayer1;
                            isPlayer2 = true;

                            // send waiting signal to screen
                            board.waitingForOpponent = new PlayerObject(opponentName, Color.MAGENTA);
                            touchEnabled = false;


                        }


                    }

                    finalSocket.emit("player 2 connected", gameId);

                    Log.i("return new game name: ", "\nGameId: " + gameId + "\nplayer1: " + gamePlayer1 +
                            "\nplayer2: " + gamePlayer2);

                    // finalSocket.emit("init ai", gameId);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

        }).on("you won", new Emitter.Listener() {

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
                Log.i("return ai move game", "...");
                JSONObject obj = (JSONObject) args[0];
                gameJSON = obj;
                String newGameId;
                try {
                    newGameId = obj.getString("gameId");
                    if (gameId.equals(newGameId)) {
                        gamePlayer1 = obj.getString("player1");
                        gamePlayer2 = obj.getString("player2");
                        gamePlayer2IsServer = obj.getBoolean("player2IsServer");
                        JSONObject lastMove = obj.getJSONObject("lastMove");
                        gameLastMoveCol = lastMove.getInt("col");
                        gameWinner = obj.getString("winner");
                        Log.i("ret ai move game: ", "\nGameId: " + gameId + "\nplayer1: " + gamePlayer1 +
                                "\nplayer2: " + gamePlayer2);
                        Log.i("Last move col: ", gameLastMoveCol + "");

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

                    if (gameId.equals(newGameId)) {
                        gamePlayer1 = obj.getString("player1");
                        gamePlayer2 = obj.getString("player2");
                        gamePlayer2IsServer = obj.getBoolean("player2IsServer");
                        JSONObject lastMove = obj.getJSONObject("lastMove");
                        gameLastMoveCol = lastMove.getInt("col");
                        gameWinner = obj.getString("winner");
//                    Log.i("ret ai move game: ", "\nGameId: " + gameId + "\nplayer1: " + gamePlayer1 +
//                            "\nplayer2: " + gamePlayer2);
//                    Log.i("Last move col: ", gameLastMoveCol + "");


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
                Log.i("return ai move game", "...");
                JSONObject obj = (JSONObject) args[0];
                gameJSON = obj;

                String newGameId;
                try {
                    newGameId = obj.getString("gameId");
                    if (gameId.equals(newGameId)) {

                        gamePlayer1 = obj.getString("player1");
                        gamePlayer2 = obj.getString("player2");
                        gamePlayer2IsServer = obj.getBoolean("player2IsServer");
                        JSONObject lastMove = obj.getJSONObject("lastMove");
                        gameLastMoveCol = lastMove.getInt("col");
                        gameWinner = obj.getString("winner");
                        Log.i("ret ai move game: ", "\nGameId: " + gameId + "\nplayer1: " + gamePlayer1 +
                                "\nplayer2: " + gamePlayer2);
                        Log.i("Last move col: ", gameLastMoveCol + "");

                        yourMove = false;
                        placeToken(gameLastMoveCol, opponentColor);


                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }).on("return init ai game", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                // here args is the game object returned
                Log.i("return init ai game", "...");
                JSONObject obj = (JSONObject) args[0];
                gameJSON = obj;
                String newGameId;
                try {
                    newGameId = obj.getString("gameId");

                    if (game.equals(newGameId)) {
                        gamePlayer1 = obj.getString("player1");
                        gamePlayer2 = obj.getString("player2");
                        gamePlayer2IsServer = obj.getBoolean("player2IsServer");
                        Log.i("return init ai game: ", "\nGameId: " + gameId + "\nplayer1: " + gamePlayer1 +
                                "\nplayer2: " + gamePlayer2);

                        touchEnabled = true;
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
