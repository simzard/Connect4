package dk.simonsteinaa.connect4;

import java.net.URISyntaxException;

import dk.simonsteinaa.connect4.screens.GameScreen;
import dk.simonsteinaa.framework.implementation.GameController;
import dk.simonsteinaa.framework.interfaces.Screen;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by simon on 4/24/16.
 */
public class Connect4 extends GameController {
    @Override
    public Screen getStartScreen() {


        return new GameScreen(this);
    }
}
