package pepepay.pepepaynative.backend.social31.handler.wifiSalut;

import android.app.Activity;
import android.os.Build;

import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import java.util.ArrayList;

import pepepay.pepepaynative.backend.social31.ConnectionManager;
import pepepay.pepepaynative.backend.social31.handler.IDeviceConnectionHandler;
import pepepay.pepepaynative.utils.Function;



public class SalutConnectionHandler implements IDeviceConnectionHandler<SalutConnectionHandler, WifiSalutDevice>, SalutDataCallback
{

    public static final String TAG = "salutconnectionhandler";

    public static final String SERVICE_NAME = "pepepay";
    public static final int REFRESH_TIME = 5;

    public SalutDataReceiver dataReceiver;
    public SalutServiceData serviceData;
    public Salut network;

    private final Activity activity;
    private ConnectionManager connManager;

    private ArrayList<WifiSalutDevice> availableDevices = new ArrayList<>();



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

        while(true) {
            if(!network.isDiscovering) {
                discoverServices();
            }
        }
    }

    private void discoverServices(){
        final ArrayList<WifiSalutDevice> oldDevices = new ArrayList<>(availableDevices);
        final ArrayList<WifiSalutDevice> newDevices = new ArrayList<>();
        final ArrayList<WifiSalutDevice> goneDevices = new ArrayList<>();

        availableDevices.clear();
        network.discoverWithTimeout(new SalutCallback() {
            @Override
            public void call() {
                for(SalutDevice dev : network.foundDevices) {
                    WifiSalutDevice device = new WifiSalutDevice(dev);
                    availableDevices.add(device);
                    Boolean isNew = true;
                    for(WifiSalutDevice oldDev : oldDevices){
                        if(device.equals(oldDev)){
                            isNew = false;
                        }
                    }
                    if(isNew){
                        newDevices.add(device);
                    }
                }
                for(WifiSalutDevice dev : oldDevices) {
                    if(!availableDevices.contains(dev)) {
                        goneDevices.add(dev);
                    }
                }
                connManager.devicesChanged(newDevices, goneDevices);
            }
        }, new SalutCallback() {
            @Override
            public void call() {
                connManager.devicesChanged(newDevices, oldDevices);
            }
        }, REFRESH_TIME);
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

    }


    @Override
    public void onDataReceived(Object o) {

    }
}
