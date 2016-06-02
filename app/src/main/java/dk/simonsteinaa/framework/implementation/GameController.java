package dk.simonsteinaa.framework.implementation;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import dk.simonsteinaa.connect4.utilities.SocketHandler;
import dk.simonsteinaa.framework.interfaces.Game;
import dk.simonsteinaa.framework.interfaces.Graphics;
import dk.simonsteinaa.framework.interfaces.Input;
import dk.simonsteinaa.framework.interfaces.Screen;

/**
 * Created by simon on 4/24/16.
 */
public abstract class GameController extends Activity implements Game {
    private FastRenderView renderView;
    private Graphics graphics;
    private Input input;
    private Screen screen;
    private Typeface font;
    private WakeLock wakeLock;

    private int numOfCols = 7;
    private int numOfRows = 6;

    protected LinearLayout parent;
    public LinearLayout chatMessages;
    protected int gameScreenHeight;
    protected int totalHeight;


    protected Button AIGame;
    protected Button humanGame;
    protected Button sendButton;
    protected Button chatButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AIGame = new Button(this);
        AIGame.setText("VS AI");



        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        boolean isLandscape = getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
        //int frameBufferWidth = isLandscape ? 480 : 320;
        //int frameBufferHeight = isLandscape ? 320 : 480;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        totalHeight = size.y;

        int frameBufferWidth = size.x;
        int squareSize = frameBufferWidth / numOfCols;
        gameScreenHeight = numOfRows * squareSize;
        Log.i("height: ", "" + gameScreenHeight);
        Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth,
                gameScreenHeight, Bitmap.Config.RGB_565);

        //float scaleX = (float) frameBufferWidth / getWindowManager().getDefaultDisplay().getWidth();
        //float scaleY = (float) frameBufferHeight / getWindowManager().getDefaultDisplay().getHeight();

        float scaleX = 1;
        float scaleY = 1;

        renderView = new FastRenderView(this, frameBuffer);
        font = Typeface.createFromAsset(this.getAssets(), "font.ttf");
        graphics = new GameGraphics(frameBuffer, font);
        input = new GameInput(this, renderView, scaleX, scaleY);
        screen = getStartScreen();



        parent = new LinearLayout(this);
        parent.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams linLayoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        //RenderView gameScreen = new RenderView(this);

        //int desiredHeight = width - width / 7;

        LinearLayout.LayoutParams lpGameScreen = new LinearLayout.LayoutParams(frameBufferWidth, gameScreenHeight);

        //parent.setBackgroundColor(Color.BLACK);
        parent.addView(renderView, lpGameScreen);


        setContentView(parent, linLayoutParam);
        //setContentView(renderView);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GLGame");


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
        if (getMessageFromScreen()) {

        }
    }

    public Screen getCurrentScreen() {
        return screen;
    }

    public boolean getMessageFromScreen() {
        return screen.getBoolean();
    }

}
