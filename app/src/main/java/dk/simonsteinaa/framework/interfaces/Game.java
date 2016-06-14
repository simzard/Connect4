package dk.simonsteinaa.framework.interfaces;

/**
 * Created by simon on 4/24/16.
 */
public interface Game {
    Input getInput();

    Graphics getGraphics();

    void setScreen(Screen screen);

    Screen getCurrentScreen();

    Screen getStartScreen();

    boolean getMessageFromScreen();

    Sockets getSockets();
}