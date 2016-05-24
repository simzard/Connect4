package dk.simonsteinaa.framework.interfaces;

/**
 * Created by simon on 4/24/16.
 */

import java.util.List;
import android.view.View.OnTouchListener;
import dk.simonsteinaa.framework.interfaces.Input.TouchEvent;

public interface TouchHandler extends OnTouchListener {

    public boolean isTouchDown(int pointer);

    public int getTouchX(int pointer);

    public int getTouchY(int pointer);

    public List <TouchEvent> getTouchEvents();
}