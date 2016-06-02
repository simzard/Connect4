package dk.simonsteinaa.connect4;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;

import dk.simonsteinaa.connect4.screens.GameScreen;
import dk.simonsteinaa.connect4.screens.InstructionsScreen;
import dk.simonsteinaa.connect4.screens.IntroScreen;
import dk.simonsteinaa.framework.implementation.GameController;
import dk.simonsteinaa.framework.interfaces.Screen;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by simon on 4/24/16.
 */
public class Connect4 extends GameController {

    private LinearLayout bottom;

    private EditText editMessage;

    private Spinner modeChooser;

    private Spinner customRows;
    private Spinner customCols;
    private Spinner customConnectK;

    private String yourName = "MrNoName";


    private Random random = new Random();

    private Screen gameScreen;

    private int rows = 6;
    private int columns = 7;
    private int connectK = 4;

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


    // this helper method adjust the spinner for the connect K - to always choose sensible values
    private void adjustConnectK(int selectedNumber) {
        ArrayList<Integer> spinnerArray = new ArrayList();
        for (int i = 3; i <= selectedNumber; i++) {
            spinnerArray.add(i);
        }
        ArrayAdapter<Integer> spinnerArrayAdapter = new ArrayAdapter(Connect4.this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        customConnectK.setAdapter(spinnerArrayAdapter);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int messageBoxHeight = totalHeight - gameScreenHeight - 110;


        customCols = new Spinner(this);
        customRows = new Spinner(this);
        customConnectK = new Spinner(this);

        humanGame = new Button(this);
        humanGame.setText("VS Human");

        chatButton = new Button(this);
        chatButton.setText("Chat");

        modeChooser = new Spinner(this);

        ArrayList<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("Connect 4 - 6 x 7");
        spinnerArray.add("Connect 4 - 7 x 8");
        spinnerArray.add("Connect 5 - 7 x 8");
        spinnerArray.add("Connect 5 - 8 x 9");
        spinnerArray.add("Connect 3 - 3 x 3");
        spinnerArray.add("Custom");

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        modeChooser.setAdapter(spinnerArrayAdapter);

        modeChooser.setSelection(0);

        modeChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 5) {

                    chatMessages.removeAllViews();


                    ArrayList<Integer> spinnerArray = new ArrayList();
                    for (int i = 3; i <= 10; i++) {
                        spinnerArray.add(i);
                    }

                    ArrayAdapter<Integer> spinnerArrayAdapter = new ArrayAdapter<Integer>(Connect4.this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
                    customRows.setAdapter(spinnerArrayAdapter);
                    customCols.setAdapter(spinnerArrayAdapter);


                    customCols.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            // get the value and
                            int value = (int) customCols.getSelectedItem();
                            adjustConnectK(value);
                            columns = value;
                            AIGame.setEnabled(true);
                            humanGame.setEnabled(true);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            AIGame.setEnabled(true);
                            humanGame.setEnabled(true);
                        }
                    });

                    customRows.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            // get the value and
                            int value = (int) customRows.getSelectedItem();
                            adjustConnectK(value);
                            rows = value;
                            AIGame.setEnabled(true);
                            humanGame.setEnabled(true);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            AIGame.setEnabled(true);
                            humanGame.setEnabled(true);
                        }
                    });


                    ArrayList<Integer> spinnerArray2 = new ArrayList();
                    for (int i = 3; i <= 7; i++) {
                        spinnerArray2.add(i);
                    }
                    ArrayAdapter<Integer> spinnerArrayAdapter2 = new ArrayAdapter<Integer>(Connect4.this, android.R.layout.simple_spinner_dropdown_item, spinnerArray2);
                    customConnectK.setAdapter(spinnerArrayAdapter2);

                    customConnectK.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            // get the value and
                            int value = (int) customConnectK.getSelectedItem();
                            connectK = value;
                            AIGame.setEnabled(true);
                            humanGame.setEnabled(true);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            AIGame.setEnabled(true);
                            humanGame.setEnabled(true);
                        }
                    });


                    TextView tv1 = new TextView(Connect4.this);
                    tv1.setText("Choose number of rows");

                    TextView tv2 = new TextView(Connect4.this);
                    tv2.setText("Choose number of columns");

                    TextView tv3 = new TextView(Connect4.this);
                    tv3.setText("Choose how many pieces connected on a line to win");

                    chatMessages.addView(tv1);
                    chatMessages.addView(customRows);
                    chatMessages.addView(tv2);
                    chatMessages.addView(customCols);
                    chatMessages.addView(tv3);
                    chatMessages.addView(customConnectK);
                }
                AIGame.setEnabled(true);
                humanGame.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                AIGame.setEnabled(true);
                humanGame.setEnabled(true);
            }
        });

        LinearLayout buttons = new LinearLayout(this);

        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(AIGame);
        buttons.addView(humanGame);
        buttons.addView(chatButton);
        buttons.addView(modeChooser);

        buttons.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        parent.addView(buttons);

        AIGame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                AIGame.setEnabled(false);
                humanGame.setEnabled(false);
                String choice = (String) modeChooser.getSelectedItem();



                if (!choice.equals("Custom")) {
                    String tokens[] = choice.split(" ");

                    connectK = Integer.parseInt(tokens[1]);
                    rows = Integer.parseInt(tokens[3]);
                    columns = Integer.parseInt(tokens[5]);


                }

                setScreen(new InstructionsScreen(Connect4.this, generateRandomColor(true), generateRandomColor(false), connectK, rows, columns, true));
                return false;
            }
        });

        humanGame.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //setScreen(new GameScreen(Connect4.this, 2, 2));

                return false;
            }
        });

        chatMessages = new LinearLayout(this);
        chatMessages.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lpMessages = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, messageBoxHeight - 110);
        parent.addView(chatMessages, lpMessages);

        TextView tv = new TextView(this);
        tv.setText("HEJ MED DIG");
        tv.setTextColor(Color.WHITE);

        chatMessages.addView(tv);


        bottom = new LinearLayout(this);
        //bottom.setBackgroundColor(Color.BLACK);
        bottom.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams lpBottom = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 110);
        //        lpBottom.weight = 1.0f;


        parent.addView(bottom, lpBottom);


        editMessage = new EditText(this);
        //editMessage.setBackgroundColor(Color.BLACK);
        editMessage.setTextColor(Color.CYAN);
        LinearLayout.LayoutParams lpEdit = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpEdit.weight = 0.8f;
        sendButton = new Button(this);
        LinearLayout.LayoutParams lpButton = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpEdit.weight = 0.2f;
        //lpEdit.setMargins(50, 0, 0,0);
        sendButton.setText("ENTER");
        //sendButton.setBackgroundColor(Color.BLACK);
        sendButton.setTextColor(Color.RED);
        bottom.addView(editMessage, lpEdit);
        bottom.addView(sendButton, lpButton);

    }


    @Override
    public Screen getStartScreen() {

        gameScreen = new IntroScreen(this);
        return gameScreen;
    }




}
