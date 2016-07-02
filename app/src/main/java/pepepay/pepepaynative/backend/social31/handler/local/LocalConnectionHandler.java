package pepepay.pepepaynative.backend.social31.handler.local;


import java.util.ArrayList;
import java.util.Arrays;

import pepepay.pepepaynative.backend.social31.ConnectionManager;
import pepepay.pepepaynative.backend.social31.handler.IDeviceConnectionHandler;
import pepepay.pepepaynative.utils.Function;

public class LocalConnectionHandler implements IDeviceConnectionHandler<LocalConnectionHandler, LocalDevice> {

    public LocalDevice device;

    @Override
    public boolean canInit() {
        return true;
    }

    @Override
    public void preInit(ConnectionManager manager) {

    }

    @Override
    public void init(ConnectionManager manager) {
        device = new LocalDevice(manager);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void requestAvailableDevices(Function<Void, ArrayList<LocalDevice>> callback) {
        callback.eval(new ArrayList<>(Arrays.asList(device)));
    }

    @Override
    public void connect(LocalDevice target) {

    }

    @Override
    public void disconnect(LocalDevice target) {

    }


    @Override
    public void send(LocalDevice target, String data) {
        target.receive(device, data);
    }

    @Override
    public boolean canSend() {
        return true;
    }

    @Override
    public Class<LocalDevice> getIDeviceType() {
        return LocalDevice.class;
    }

}
