package pepepay.pepepaynative.backend.social31.wifiDirect;

import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class CommunicationManager implements Runnable {

    private static final String TAG = "CommHandler";
    private Socket socket = null;
    private Handler handler;
    private InputStream iStream;
    private OutputStream oStream;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public CommunicationManager(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            dataInputStream = new DataInputStream(iStream);
            dataOutputStream = new DataOutputStream(oStream);
            handler.obtainMessage(WifiDirectConnectionHandler.MY_HANDLE, this)
                    .sendToTarget();

            while (true) {
                try {
                    String data = dataInputStream.readUTF();
                    handler.obtainMessage(WifiDirectConnectionHandler.MESSAGE_READ, 0, 0, data).sendToTarget();
                } catch (Throwable e) {
                    //Log.e(TAG, "disconnected", e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(String data) {
        try {
            dataOutputStream.writeUTF(data);
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

}