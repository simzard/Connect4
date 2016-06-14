package dk.simonsteinaa.framework.implementation;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import dk.simonsteinaa.framework.interfaces.Game;
import dk.simonsteinaa.framework.interfaces.Graphics;
import dk.simonsteinaa.framework.interfaces.Input;
import dk.simonsteinaa.framework.interfaces.Screen;
import dk.simonsteinaa.framework.interfaces.Sockets;


/**
 * Created by simon on 4/24/16.
 */
public abstract class GameController extends Activity implements Game {

    private static final int BOTTOM_HEIGHT = 110;
    private static final int BUTTONS_HEIGHT = 110;

    private Graphics graphics;
    private Input input;
    private Screen screen;
    private Typeface font;
    private WakeLock wakeLock;

    protected String yourName = "MrNoName";
    protected String yourColor = "#ff0000";

    protected int renderScreenHeight;
    protected int totalHeight;
    protected int lowerScreenHeight;

    // default game configuration
    protected int numOfCols = 7;
    protected int numOfRows = 6;
    protected int connectK = 4;

    // the mother layout of the game consisting of many components
    protected LinearLayout parent;

    // ----- the UPPER half of the screen
    private ThreadedRenderView renderView;


    // ----- GUI references concerning the LOWER half of the screen -----


    // the middle row: where the 3 buttons in and the spinner resides
    protected LinearLayout buttons;
    protected Button AIGame;
    protected Button humanGame;
    protected Button chatButton;
    protected Spinner modeChooser;

    // where the chat messages and custom window resides
    protected LinearLayout lowerScreen;

    // spinners for the custom "Window"
    protected Spinner customRows;
    protected Spinner customCols;
    protected Spinner customConnectK;

    // textviews for the custom "Window"
    protected TextView tv1;
    protected TextView tv2;
    protected TextView tv3;

    // these are used for displaying the row with the name and the color button
    protected LinearLayout nameRow;
    protected TextView tv4;
    protected TextView tv5;
    protected Button colorButton;

    // these are used for the bottom row to enter a message (EditText) and send it(Button)
    protected LinearLayout bottom;
    protected EditText editMessage;
    protected Button sendButton;

    // the socket handler
    protected Sockets sockets;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize socket connection
        sockets = new GameSockets();
        sockets.connect();

        // make sure the game is set in full window mode
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // get the width of the current device - calculate a the height of the TOP HALF (the ThreadedRenderView)
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        totalHeight = size.y;

        // I want this lower screen to be the remaining pixels after calculating the height of the upper screen and i want the bottom to be BOTTOM_HEIGHT pixels.
        lowerScreenHeight = totalHeight - renderScreenHeight - BOTTOM_HEIGHT;

        // initialize the main container View
        parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);

        // ----- Intialize the UPPER part of the screen - the actual animated game screen -----

        int frameBufferWidth = size.x;
        int squareSize = frameBufferWidth / numOfCols;
        renderScreenHeight = numOfRows * squareSize;

        // create the framebuffer for the upper screen needed by renderView
        Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth,
                renderScreenHeight, Bitmap.Config.RGB_565);

        renderView = new ThreadedRenderView(this, frameBuffer);
        font = Typeface.createFromAsset(this.getAssets(), "font.ttf");
        graphics = new GameGraphics(frameBuffer, font);
        input = new GameInput(this, renderView);
        screen = getStartScreen();

        // make sure to use the wake lock
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GLGame");


        // Fancy gradients used on the GUI components to enhance the user experience
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                //new int[] {0xFF616261,0xFF131313});
                new int[] {0xff000000,0xffffffff, 0xff000000});
        gd.setCornerRadius(0f);

        GradientDrawable gd2 = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                //new int[] {0xFF616261,0xFF131313});
                new int[] {0xff021618, 0xff113311,0xff021618});
        gd2.setCornerRadius(0f);

        GradientDrawable gd3 = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                //new int[] {0xFF616261,0xFF131313});
                new int[] {0xff000000,0xffffffff, 0xff000000});
        gd3.setCornerRadius(0f);

        GradientDrawable gd4 = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                //new int[] {0xFF616261,0xFF131313});
                new int[] {0xffffffff,0xff000000, 0xffffffff});
        gd4.setCornerRadius(0f);


        // Initialize GUI componenets (LOWER half of the game screen)
        // initialization occurs ALMOST in the order top to bottom

        // the 3 buttons and the mode chooser
        buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        AIGame = new Button(this);
        AIGame.setText("VS AI");
        AIGame.setTextColor(Color.BLUE);
        AIGame.setBackground(gd);
        humanGame = new Button(this);
        humanGame.setText("VS Human");
        humanGame.setTextColor(0xff660000);
        humanGame.setBackground(gd);
        chatButton = new Button(this);
        chatButton.setText("Chat");
        chatButton.setTextColor(0xff711564);
        chatButton.setBackground(gd);
        modeChooser = new Spinner(this);
        LinearLayout.LayoutParams lpAIGame = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpAIGame.weight = 1.0f;
        buttons.addView(AIGame, lpAIGame);
        LinearLayout.LayoutParams lpHumanGame = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpHumanGame.weight = 1.0f;
        buttons.addView(humanGame, lpHumanGame);
        LinearLayout.LayoutParams lpChatButton = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpChatButton.weight = 1.0f;
        buttons.addView(chatButton, lpChatButton);
        LinearLayout.LayoutParams lpModeChooser = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpModeChooser.weight = 1.0f;
        buttons.addView(modeChooser, lpModeChooser);
        buttons.setPadding(0, 5, 0, 0);

        // the lower screen
        lowerScreen = new LinearLayout(this);
        lowerScreen.setOrientation(LinearLayout.VERTICAL);
        lowerScreen.setBackground(gd2);

        customCols = new Spinner(this);
        customRows = new Spinner(this);
        customConnectK = new Spinner(this);

        customCols.setBackground(gd3);
        customRows.setBackground(gd3);
        customConnectK.setBackground(gd3);

        // Initialize the custom "window"
        // make prefabricated spinner options for the top spinner
        String[] spinnerArray = { "Choose mode",
                "Connect 4 - 6 x 7",
                "Connect 4 - 7 x 8",
                "Connect 5 - 7 x 8",
                "Connect 5 - 8 x 9",
                "Connect 3 - 3 x 3",
                "Custom"
        };

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        modeChooser.setAdapter(spinnerArrayAdapter);
        modeChooser.setSelection(1);
        modeChooser.setBackground(gd2);

        tv1 = new TextView(this);
        tv1.setText("Choose number of rows:");
        tv1.setTextColor(0xff81FD0C);
        tv1.setPadding(10, 0, 0, 0);

        tv2 = new TextView(this);
        tv2.setText("Choose number of columns:");
        tv2.setTextColor(0xff81FD0C);
        tv2.setPadding(10, 0, 0, 0);

        tv3 = new TextView(this);
        tv3.setText("Choose how many pieces connected on a line to win:");
        tv3.setTextColor(0xff81FD0C);
        tv3.setPadding(10, 0, 0, 0);

        tv4 = new TextView(this);
        tv4.setText("Your name: ");
        tv4.setTextColor(0xff81FD0C);
        tv4.setPadding(10, 35, 0, 0);
        LinearLayout.LayoutParams lpTv4 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpTv4.weight = 1.0f;

        nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);

        tv5 = new TextView(this);
        tv5.setTextColor(Color.parseColor(yourColor));
        tv5.setPadding(0, 35, 0, 0);
        LinearLayout.LayoutParams lpTv5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpTv5.weight = 1.0f;

        tv5.setText(yourName);

        colorButton = new Button(this);
        colorButton.setText("Change color!");
        colorButton.setTextColor(0xffFFC0CB);
        colorButton.setBackgroundColor(0xff021618);

        nameRow.addView(tv4, lpTv4);
        nameRow.addView(tv5, lpTv5);
        LinearLayout.LayoutParams lpCB = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpCB.weight = 1.0f;
        nameRow.addView(colorButton, lpCB);

        ArrayList<Integer> spinnerArray2 = new ArrayList();
        for (int i = 3; i <= 10; i++) {
            spinnerArray2.add(i);
        }

        ArrayAdapter<Integer> spinnerArrayAdapter2 = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray2);
        customRows.setAdapter(spinnerArrayAdapter2);
        customCols.setAdapter(spinnerArrayAdapter2);

        ArrayList<Integer> spinnerArray3 = new ArrayList();
        for (int i = 3; i <= 7; i++) {
            spinnerArray3.add(i);
        }
        ArrayAdapter<Integer> spinnerArrayAdapter3 = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray3);
        customConnectK.setAdapter(spinnerArrayAdapter3);

        // bottom with the edit text message field and send button
        bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lpBottom = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, BOTTOM_HEIGHT);
        editMessage = new EditText(this);
        editMessage.setTextColor(0xff81FD0C);
        editMessage.setHint("Enter message here!");
        editMessage.clearComposingText();
        sendButton = new Button(this);
        sendButton.setText("SEND");
        sendButton.setTextColor(0xffFFC0CB);
        sendButton.setBackgroundColor(0xff000000);

        LinearLayout.LayoutParams lpEdit = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpEdit.weight = 0.2f;
        bottom.addView(editMessage, lpEdit);
        LinearLayout.LayoutParams lpButton = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, BOTTOM_HEIGHT);
        bottom.addView(sendButton, lpButton);

        // ---- now finally add the views to the main View ----
        LinearLayout.LayoutParams lpGameScreen = new LinearLayout.LayoutParams(frameBufferWidth, renderScreenHeight);
        parent.addView(renderView, lpGameScreen);
        LinearLayout.LayoutParams lpButtons = new LinearLayout.LayoutParams(frameBufferWidth, BUTTONS_HEIGHT);
        parent.addView(buttons, lpButtons);
        LinearLayout.LayoutParams lpLowerScreen = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, totalHeight - lowerScreenHeight - BOTTOM_HEIGHT - BUTTONS_HEIGHT);
        lpLowerScreen.weight = 1.0f;
        parent.addView(lowerScreen, lpLowerScreen);
        parent.addView(bottom, lpBottom);
        LinearLayout.LayoutParams linLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        parent.setBackgroundColor(Color.BLACK);

        setContentView(parent, linLayoutParam);
    }

    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire();
        screen.resume();
        renderView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        wakeLock.release();
        renderView.pause();
        screen.pause();
        if (isFinishing())
            screen.dispose();
    }

    public Input getInput() {
        return input;
    }

    public Graphics getGraphics() {
        return graphics;
    }

    public void setScreen(Screen screen) {
        if (screen == null)
            throw new IllegalArgumentException("Screen must not be null");
        this.screen.pause();
        this.screen.dispose();
        screen.resume();
        screen.update(0);

        this.screen = screen;

        // workaround for making the UPPER screen send simple messages to the GUI componenets (in this case the two buttons VS AI and VS HUMAN)
        if (getMessageFromScreen()) {
            AIGame.setEnabled(true);
            humanGame.setEnabled(true);
        }
    }

    public Screen getCurrentScreen() {
        return screen;
    }

    public boolean getMessageFromScreen() {
        return screen.getBoolean();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sockets.getSocket().disconnect();
    }


    @Override
    public Sockets getSockets() {
        return sockets;
    }
}
