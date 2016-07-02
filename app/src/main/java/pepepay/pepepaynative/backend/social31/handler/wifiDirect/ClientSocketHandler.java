package pepepay.pepepaynative.backend.social31.handler.wifiDirect;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSocketHandler extends Thread {

    private static final String TAG = "ClientSocketHandler";
    private Handler handler;
    private CommunicationManager comms;
    private InetAddress mAddress;
    private WifiDirectConnectionHandler wifiDirectConnectionHandler;

    public ClientSocketHandler(Handler handler, InetAddress groupOwnerAddress, WifiDirectConnectionHandler wifiDirectConnectionHandler) {
        this.handler = handler;
        this.mAddress = groupOwnerAddress;
        this.wifiDirectConnectionHandler = wifiDirectConnectionHandler;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mAddress.getHostAddress(),
                    WifiDirectConnectionHandler.SERVER_PORT), 5000);
            Log.d(TAG, "Launching the I/O handler");
            comms = new CommunicationManager(socket, handler);
            new Thread(comms).start();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public CommunicationManager getComms() {
        return comms;
    }

}
