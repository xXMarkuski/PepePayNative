package pepepay.pepepaynative.backend.social31.handler.wifiDirect;

import java.util.ArrayList;
import java.util.Arrays;

import pepepay.pepepaynative.backend.social31.ConnectionManager;
import pepepay.pepepaynative.backend.social31.handler.IDevice;
import pepepay.pepepaynative.backend.social31.handler.IDeviceConnectionHandler;
import pepepay.pepepaynative.utils.Function;

public abstract class WifiDirectBackend<T extends WifiDirectBackend> implements IDeviceConnectionHandler<WifiDirectBackend, WifiDirectBackend.WifiDirectBackendDevice> {

    private WifiDirectConnectionHandler handler;
    private WifiDirectBackendDevice device;
    private WifiDirectDevice wifiDirectDevice;

    public WifiDirectBackend(WifiDirectConnectionHandler handler) {
        this.handler = handler;
        this.device = new WifiDirectBackendDevice(this);
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
        return handler.canSend();
    }

    @Override
    public Class<WifiDirectBackendDevice> getIDeviceType() {
        return WifiDirectBackendDevice.class;
    }

    protected void handleString(String string) {

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
