package dk.simonsteinaa.framework.implementation;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        boolean isLandscape = getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
        int frameBufferWidth = isLandscape ? 480 : 320;
        int frameBufferHeight = isLandscape ? 320 : 480;
        Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth,
                frameBufferHeight, Bitmap.Config.RGB_565);
        float scaleX = (float) frameBufferWidth
                / getWindowManager().getDefaultDisplay().getWidth();
        float scaleY = (float) frameBufferHeight
                / getWindowManager().getDefaultDisplay().getHeight();
        renderView = new FastRenderView(this, frameBuffer);
        font = Typeface.createFromAsset(this.getAssets(), "font.ttf");
        graphics = new GameGraphics(frameBuffer, font);
        input = new GameInput(this, renderView, scaleX, scaleY);
        screen = getStartScreen();

        LinearLayout parent = new LinearLayout(this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.HORIZONTAL);

        //FrameLayout layout = new FrameLayout(this);
        //FrameLayout.LayoutParams layoutparams=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT, Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        //layout.setLayoutParams(layoutparams);
        //layout.setForegroundGravity(Gravity.CENTER);
        parent.addView(renderView);
        Button myButton = new Button(this);
        myButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        myButton.setPadding(100,100,100,100);

        parent.addView(myButton);
        setContentView(parent);
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
    }

    public Screen getCurrentScreen() {
        return screen;
    }
}
