package dk.simonsteinaa.framework.interfaces;

/**
 * Created by simon on 4/24/16.
 */
public interface Game {
    public Input getInput();

    public Graphics getGraphics();

    public void setScreen(Screen screen);

    public Screen getCurrentScreen();

    public Screen getStartScreen();

    public boolean getMessageFromScreen();
}