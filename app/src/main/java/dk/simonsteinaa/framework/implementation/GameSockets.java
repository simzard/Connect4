package dk.simonsteinaa.framework.implementation;

import java.net.URISyntaxException;

import dk.simonsteinaa.framework.interfaces.Sockets;
import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by simon on 5/1/16.
 */
public class GameSockets implements Sockets {
    private Socket socket;

    public void connect() {
        try {
            socket = IO.socket("http://connect4-ssteinaa.rhcloud.com");
        } catch (URISyntaxException e) {
           e.printStackTrace();
        }

        socket.connect();

    }

    public Socket getSocket() {
        return socket;
    }
}
