package pepepay.pepepaynative.backend.social31.handler.wifiSalut;

import android.app.Activity;
import android.os.Build;

import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import pepepay.pepepaynative.backend.social31.ConnectionManager;
import pepepay.pepepaynative.backend.social31.handler.IDeviceConnectionHandler;
import pepepay.pepepaynative.utils.Function;



public class SalutConnectionHandler implements IDeviceConnectionHandler<SalutConnectionHandler, WifiSalutDevice>, SalutDataCallback
{

    public static final String TAG = "salutconnectionhandler";

    public static final String SERVICE_NAME = "pepepay";

    public SalutDataReceiver dataReceiver;
    public SalutServiceData serviceData;
    public Salut network;

    private final Activity activity;
    private ConnectionManager connManager;



    public SalutConnectionHandler(Activity activity) {
        this.activity = activity;
    }


    @Override
    public boolean canInit() {
        return true;
    }

    @Override
    public void preInit(ConnectionManager manager) {
        connManager = manager;
    }

    @Override
    public void init(ConnectionManager manager) {

        //TODO: change instanceName to user defined string
        dataReceiver = new SalutDataReceiver(activity, this);
        serviceData = new SalutServiceData(SERVICE_NAME, 60606, Build.MODEL);
        network = new Salut(dataReceiver, serviceData, null);

        network.startNetworkService(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice device) {
                connManager.incomingConnection(new WifiSalutDevice(device), SalutConnectionHandler.this);
            }
        });
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void connect(WifiSalutDevice target) {

    }

    @Override
    public void disconnect(WifiSalutDevice target) {

    }

    @Override
    public void send(WifiSalutDevice target, String data) {

    }

    @Override
    public boolean canSend() {
        return false;
    }

    @Override
    public Class getIDeviceType() {
        return null;
    }

    @Override
    public void requestAvailableDevices(final Function callback) {
        network.discoverNetworkServices(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice salutDevice) {

            }
        });
    }


    @Override
    public void onDataReceived(Object o) {

    }
}
