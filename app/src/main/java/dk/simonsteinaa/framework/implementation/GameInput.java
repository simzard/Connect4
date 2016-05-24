package dk.simonsteinaa.framework.implementation;

import android.content.Context;
import android.view.View;

import java.util.List;

import dk.simonsteinaa.framework.interfaces.Input;
import dk.simonsteinaa.framework.interfaces.TouchHandler;

/**
 * Created by simon on 4/24/16.
 */
public class GameInput implements Input {
    private TouchHandler touchHandler;

    public GameInput(Context context, View view, float scaleX, float scaleY) {
        touchHandler =  new SingleTouchHandler(view, scaleX, scaleY);
    }

    @Override
    public boolean isTouchDown(int pointer) {
        return touchHandler.isTouchDown(pointer);
    }

    @Override
    public int getTouchX(int pointer) {
        return touchHandler.getTouchX(pointer);
    }

    @Override
    public int getTouchY(int pointer) {
        return touchHandler.getTouchY(pointer);
    }

    @Override
    public List<TouchEvent> getTouchEvents() {
        return touchHandler.getTouchEvents();
    }
}
