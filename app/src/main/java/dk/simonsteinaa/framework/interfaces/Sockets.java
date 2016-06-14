package dk.simonsteinaa.framework.interfaces;

import io.socket.client.Socket;

/**
 * Created by simon on 6/3/16.
 */
public interface Sockets {
    void connect();

    Socket getSocket();
}
