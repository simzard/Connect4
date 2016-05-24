package dk.simonsteinaa.connect4.utilities;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by simon on 5/1/16.
 */
public class SocketHandler  {
    private Socket socket;

    public void connect() {
        try {
            socket = IO.socket("http://simonsteinaa.pagekite.me");
        } catch (URISyntaxException e) {
            System.out.println("No connection!");
            //e.printStackTrace();
        }

        socket.connect();

    }

    public Socket getSocket() {
        return socket;
    }

}
