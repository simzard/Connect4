package dk.simonsteinaa.framework.implementation;

import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import dk.simonsteinaa.framework.interfaces.Input.TouchEvent;
import dk.simonsteinaa.framework.interfaces.TouchHandler;
import dk.simonsteinaa.framework.implementation.Pool.PoolObjectFactory;


/**
 * Created by simon on 4/24/16.
 */
public class SingleTouchHandler implements TouchHandler {

    boolean isTouched;
    int touchX;
    int touchY;
    Pool<TouchEvent> touchEventPool;
    List<TouchEvent> touchEventsBuffer = new ArrayList<TouchEvent>();

    public SingleTouchHandler(View view) {
        PoolObjectFactory<TouchEvent> factory =  new PoolObjectFactory<TouchEvent>() {
            @Override
            public TouchEvent createObject() {
                return new TouchEvent();
            }
        };
        touchEventPool =  new Pool <TouchEvent> (factory, 100);
        view.setOnTouchListener(this);
    }

    @Override
    public boolean isTouchDown(int pointer) {
        synchronized(this) {
            if(pointer == 0)
                return isTouched;
            else
                return false;
        }
    }

    @Override
    public int getTouchX(int pointer) {
        synchronized(this) {
            return touchX;
        }
    }

    @Override
    public int getTouchY(int pointer) {
        synchronized(this) {
            return touchY;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        synchronized(this) {
            TouchEvent touchEvent = touchEventPool.newObject();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchEvent.type = TouchEvent.TOUCH_DOWN;
                    isTouched =  true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    touchEvent.type = TouchEvent.TOUCH_DRAGGED;
                    isTouched =  true;
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    touchEvent.type = TouchEvent.TOUCH_UP;
                    isTouched =  false;
                    break;
            }
            touchEvent.x = touchX = (int)(event.getX());
            touchEvent.y = touchY = (int)(event.getY());
            touchEventsBuffer.add(touchEvent);
            return true;
        }
    }

}
