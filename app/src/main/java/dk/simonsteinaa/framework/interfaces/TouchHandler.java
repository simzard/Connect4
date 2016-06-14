package dk.simonsteinaa.framework.interfaces;

/**
 * Created by simon on 4/24/16.
 */
import android.view.View.OnTouchListener;

public interface TouchHandler extends OnTouchListener {

    boolean isTouchDown(int pointer);

    int getTouchX(int pointer);

    int getTouchY(int pointer);
}