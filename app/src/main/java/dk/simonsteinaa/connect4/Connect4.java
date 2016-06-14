package dk.simonsteinaa.connect4;

import android.graphics.Color;

import android.os.Bundle;
import android.os.Handler;

import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import dk.simonsteinaa.connect4.screens.InstructionsScreen;
import dk.simonsteinaa.connect4.screens.IntroScreen;
import dk.simonsteinaa.connect4.screens.WaitingScreen;
import dk.simonsteinaa.connect4.utilities.Utils;
import dk.simonsteinaa.framework.implementation.GameController;
import dk.simonsteinaa.framework.interfaces.Game;
import dk.simonsteinaa.framework.interfaces.Screen;

import io.socket.emitter.Emitter;

/**
 * Created by simon on 4/24/16.
 */

// This class is mainly an extension of the GameController
// It intializes the listeners for the buttons, spinners and sockets
public class Connect4 extends GameController {

    private static final int MAX_VIEWS = 12;

    // this is a reference to the current Screen being displayed in the UPPER part of the game (the ThreadedRenderView)
    private Screen gameScreen;

    // every time I want to update the GUI components I can use this handler to post changes
    private Handler handler = new Handler();

    // variables used for chat
    private String chatFrom = "";
    private String chatText = "";
    private String chatColor = "#000000";

    // this is where the current game state is stored
    private JSONObject gameJSON;

    // this variable represents the current amount of chat messages being displayed
    private int textViews = 0;

    // this variable tells if you are in the custom "window" or not
    private boolean inCustomScreen;

    // this helper method adjust the spinner for the connect K - to always choose "sensible" values
    // so you for instance CANNOT play connect 5 on a 3 x 2 board :)
    private void adjustConnectK(int selectedNumber) {
        ArrayList<Integer> spinnerArray = new ArrayList();
        for (int i = 3; i <= selectedNumber; i++) {
            spinnerArray.add(i);
        }
        ArrayAdapter<Integer> spinnerArrayAdapter = new ArrayAdapter(Connect4.this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        customConnectK.setAdapter(spinnerArrayAdapter);

        customConnectK.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(0xff81FD0C);

                int value = (int) customConnectK.getSelectedItem();
                connectK = value;

                AIGame.setEnabled(true);
                humanGame.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // This helper method presents the lower screen
    // If it is given a false it is the custom "window"
    // Otherwise, it is the chat "window"
    private void presentWindow(boolean chat) {
        lowerScreen.removeAllViews();

        if (chat) {

            inCustomScreen = false;

            if (modeChooser.getSelectedItemPosition() == 6) {
                modeChooser.setSelection(0);
            }
            sendButton.setText("Send");
            editMessage.setText("");
            editMessage.setHint("Enter your message here!");


        } else { // Custom "window"

            lowerScreen.addView(tv1);
            lowerScreen.addView(customRows);
            lowerScreen.addView(tv2);
            lowerScreen.addView(customCols);
            lowerScreen.addView(tv3);
            lowerScreen.addView(customConnectK);
            LinearLayout.LayoutParams lpNameRow = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lowerScreen.addView(nameRow, lpNameRow);

            // update the button in the bottom to update your name
            sendButton.setText("Update name!");
            editMessage.setText("");
            editMessage.setHint("Enter your name here!");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Spinner listeners
        modeChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ((TextView) parent.getChildAt(0)).setTextColor(0xff81FD0C);

                if (position == 6) {
                    // this means you have activated the custom "window"
                    inCustomScreen = true;
                    presentWindow(false);

                } else if (position >= 1) {
                    // this means you have chosen one of the valid premade game modes
                    // update the dimensions accordingly
                    String choice = (String) modeChooser.getSelectedItem();
                    String tokens[] = choice.split(" ");
                    connectK = Integer.parseInt(tokens[1]);
                    numOfRows = Integer.parseInt(tokens[3]);
                    numOfCols = Integer.parseInt(tokens[5]);
                    Toast toast = Toast.makeText(Connect4.this, "Changed next game to " + connectK + " on " + numOfRows + "x" + numOfCols, Toast.LENGTH_SHORT);
                    toast.show();
                }

                AIGame.setEnabled(true);
                humanGame.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        customCols.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(0xff81FD0C);

                int value = (int) customCols.getSelectedItem();
                adjustConnectK(value);
                numOfCols = value;

                AIGame.setEnabled(true);
                humanGame.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        customRows.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(0xff81FD0C);

                int value = (int) customRows.getSelectedItem();
                adjustConnectK(value);
                numOfRows = value;

                AIGame.setEnabled(true);
                humanGame.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // ----- Button listeners
        // you have pressed the VS AI button
        AIGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AIGame.setEnabled(false);
                humanGame.setEnabled(false);

                JSONObject obj = new JSONObject();

                try {
                    // send a system message informing other players that you are playing a game versus the AI
                    obj.put("message", yourName + " is playing against the AI");
                    sockets.getSocket().emit("send system message", obj);

                    // send the new AI game request to the server
                    obj = new JSONObject();
                    obj.put("playerName", yourName);
                    obj.put("numberOfColumns", numOfCols);
                    obj.put("numberOfRows", numOfRows);
                    obj.put("connectK", connectK);

                    Gson gson = new GsonBuilder().create();
                    JsonParser jp = new JsonParser();
                    JsonElement je = jp.parse(String.valueOf(obj));

                    sockets.getSocket().emit("new AI game", je);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // your have pressed the VS HUMAN button
        humanGame.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AIGame.setEnabled(false);
                humanGame.setEnabled(false);

                JSONObject obj = new JSONObject();

                try {
                    // send a system message informing other players that you are waiting for them to join you
                    obj.put("message", yourName + " wants to play " + connectK + " on " + numOfRows + "x" + numOfCols);
                    sockets.getSocket().emit("send system message", obj);

                    // send the new game request to the server
                    obj = new JSONObject();
                    obj.put("playerName", yourName);
                    obj.put("numberOfColumns", numOfCols);
                    obj.put("numberOfRows", numOfRows);
                    obj.put("connectK", connectK);
                    Gson gson = new GsonBuilder().create();

                    JsonParser jp = new JsonParser();
                    JsonElement je = jp.parse(String.valueOf(obj));
                    sockets.getSocket().emit("new game", je);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // change the color of your name by pressing this button
        colorButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                yourColor = Utils.generateRandomColorString(false);
                tv5.setTextColor(Color.parseColor(yourColor));
            }
        });

        // now the user has pressed the CHAT button which means
        // that if the current "window" is the custom one it should be made to the chat window
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presentWindow(true);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int duration = Toast.LENGTH_SHORT;

                if (inCustomScreen) {
                    // this means the button is used to update your player name
                    String theName = editMessage.getText().toString();

                    // validate the name
                    if (theName.length() < 3) {
                        editMessage.setHint("Enter your name here!");
                        Toast toast = Toast.makeText(Connect4.this, "Invalid length: Name can minimum be 3 characters", duration);
                        toast.show();
                    } else if (theName.length() > 30) {
                        editMessage.setHint("Enter your name here!");
                        Toast toast = Toast.makeText(Connect4.this, "Invalid length: Name can maximum be 30 characters", duration);
                        toast.show();

                    } else if (theName.contains("\n")) {
                        editMessage.setHint("Enter your name here!");
                        Toast toast = Toast.makeText(Connect4.this, "Invalid character: Name can not contain newlines", duration);
                        toast.show();
                    } else {
                        tv5.setText(theName);
                        CharSequence text = "Name updated!";
                        yourName = theName;

                        Toast toast = Toast.makeText(Connect4.this, text, duration);
                        toast.show();

                    }

                } else { // chat - this means the button is used to send chat messages
                    String chatText = editMessage.getText().toString();

                    JSONObject user = new JSONObject();
                    try {
                        user.put("name", yourName);
                        user.put("color", yourColor);

                        JSONObject obj = new JSONObject();
                        obj.put("message", chatText);
                        obj.put("user", user);

                        sockets.getSocket().emit("chat message", obj);
                        Toast toast = Toast.makeText(Connect4.this, "SENT", duration);
                        toast.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // ----- Socket listeners
        sockets.getSocket().on("chat message", new Emitter.Listener() {

            @Override
            public void call(Object... args) {

                JSONObject obj = (JSONObject) args[0];

                try {
                    chatText = obj.getString("message");
                    JSONObject tmp = (JSONObject) obj.get("user");
                    chatColor = tmp.getString("color");
                    chatFrom = tmp.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView tv = new TextView(Connect4.this);
                        tv.setText(chatFrom + ": " + chatText);
                        tv.setTextColor(Color.parseColor(chatColor));

                        // display your opponent to the right and in cyan
                        if (!chatFrom.equals(yourName)) {
                            tv.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            tv.setTextColor(Color.CYAN);
                        }

                        // after MAX_VIEWS messages reset the chat window
                        textViews++;
                        if (textViews >= MAX_VIEWS) {
                            lowerScreen.removeAllViews();
                            textViews = 0;
                        }
                        lowerScreen.addView(tv);

                    }
                });
            }
        });

        // display messages from the system
        sockets.getSocket().on("system message", new Emitter.Listener() {

            @Override
            public void call(Object... args) {

                JSONObject obj = (JSONObject) args[0];

                try {
                    chatText = obj.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView tv = new TextView(Connect4.this);
                        tv.setText("System: " + chatText);
                        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        tv.setTextColor(Color.WHITE);

                        // after MAX_VIEWS messages reset the chat window
                        textViews++;
                        if (textViews >= MAX_VIEWS) {
                            lowerScreen.removeAllViews();
                            textViews = 0;
                        }
                        lowerScreen.addView(tv);
                    }
                });
            }
        });


        // This means that you have pressed the VS AI game button and a game is available
        // so change the screen to the InstructionsScreen along with all the necessary information
        sockets.getSocket().on("return from new AI game", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                gameJSON = (JSONObject) args[0];

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setScreen(new InstructionsScreen(Connect4.this, Color.parseColor(yourColor), Utils.generateRandomColor(true), connectK, numOfRows, numOfCols, true, yourName, gameJSON));

                    }
                });
            }
        });

        // This means that you have pressed the VS HUMAN game button and you should now wait for another human to connect to your game
        // so change the screen to the WaitingScreen along with all the necessary information
        sockets.getSocket().on("return new game", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                gameJSON = (JSONObject) args[0];

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setScreen(new WaitingScreen(Connect4.this, Color.parseColor(yourColor), Utils.generateRandomColor(true), connectK, numOfRows, numOfCols, yourName, gameJSON));
                    }
                });

            }
        });
    }


    // the main entry for the UPPER part of the game
    @Override
    public Screen getStartScreen() {
        gameScreen = new IntroScreen(this);
        return gameScreen;
    }
}
