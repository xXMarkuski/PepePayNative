package pepepay.pepepaynative.backend.social31.handler.wifiDirect;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;

import java.util.ArrayList;
import java.util.Arrays;

import pepepay.pepepaynative.backend.social31.ConnectionManager;
import pepepay.pepepaynative.backend.social31.handler.IDevice;
import pepepay.pepepaynative.backend.social31.handler.IDeviceConnectionHandler;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.utils.function.Function;
import pepepay.pepepaynative.utils.types.StringUtils;

public abstract class WifiDirectBackend<T extends WifiDirectBackend> implements IDeviceConnectionHandler<WifiDirectBackend, WifiDirectBackend.WifiDirectBackendDevice> {

    protected WifiDirectBackendDevice device;
    protected ConnectionManager manager;
    private WifiDirectConnectionHandler handler;
    private WifiDirectDevice wifiDirectDevice;

    public WifiDirectBackend(WifiDirectConnectionHandler handler) {
        this.handler = handler;
        this.device = new WifiDirectBackendDevice(this);
    }

    public static String generateDeviceConnectionString(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();

        return StringUtils.multiplex("device", macAddress);
    }

    public static String generateWalletConnectionString(Wallet target, Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();

        String wallet = StringUtils.multiplex("wallet", macAddress, target.getIdentifier() + "");
        System.out.println(wallet);
        return wallet;
    }

    public static String generateTransactionConnectionString(Wallet target, Context context, float amount, String purpose) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();

        return StringUtils.multiplex("transaction", macAddress, target.getIdentifier(), amount + "", purpose);
    }

    @Override
    public boolean canInit() {
        return true;
    }

    @Override
    public void preInit(ConnectionManager manager) {

    }

    @Override
    public void init(ConnectionManager manager) {
        this.manager = manager;
    }

    @Override
    public void requestAvailableDevices(Function<Void, ArrayList<WifiDirectBackendDevice>> callback) {
        callback.eval(new ArrayList<WifiDirectBackendDevice>(Arrays.asList(device)));
    }

    @Override
    public void disconnect(WifiDirectBackendDevice target) {
        if (wifiDirectDevice != null) {
            handler.disconnect(wifiDirectDevice);
        }
    }

    @Override
    public void send(WifiDirectBackendDevice target, String data) {
        if (wifiDirectDevice != null) {
            handler.send(wifiDirectDevice, data);
        }
    }

    @Override
    public boolean canSend() {
        return wifiDirectDevice != null && handler.canSend();
    }

    @Override
    public Class<WifiDirectBackendDevice> getIDeviceType() {
        return WifiDirectBackendDevice.class;
    }

    protected boolean handleString(String string) {
        try {
            String[] data = StringUtils.demultiplex(string);
            if (data[0].equals("device")) {
                WifiP2pDevice device = new WifiP2pDevice();
                device.deviceAddress = data[1];
                wifiDirectDevice = new WifiDirectDevice(device);
                handler.connect(new WifiDirectDevice(device));
                return true;
            } else if (data[0].equals("wallet")) {
                WifiP2pDevice device = new WifiP2pDevice();
                device.deviceAddress = data[1];
                wifiDirectDevice = new WifiDirectDevice(device);
                handler.connect(new WifiDirectDevice(device));
                String walletID = data[2];
                manager.getConnection(this.device).setTargetWalletID(walletID);

                return true;
            } else if (data[0].equals("transaction")) {
                WifiP2pDevice device = new WifiP2pDevice();
                device.deviceAddress = data[1];
                wifiDirectDevice = new WifiDirectDevice(device);
                handler.connect(new WifiDirectDevice(device));
                String walletID = data[2];
                float amount = Float.parseFloat(data[3]);
                String purpose = data[4];

                return true;
            }
            return false;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }
    }

    protected abstract String getIDeviceName();

    protected static class WifiDirectBackendDevice implements IDevice<WifiDirectBackend> {

        WifiDirectBackend backend;

        public WifiDirectBackendDevice(WifiDirectBackend backend) {
            this.backend = backend;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public String getName() {
            return backend.getIDeviceName();
        }
    }
}
