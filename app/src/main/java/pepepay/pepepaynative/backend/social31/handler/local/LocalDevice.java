package pepepay.pepepaynative.backend.social31.handler.local;


import pepepay.pepepaynative.backend.social31.ConnectionManager;
import pepepay.pepepaynative.backend.social31.handler.IDevice;

public class LocalDevice implements IDevice<LocalConnectionHandler> {

    private ConnectionManager manager;

    public LocalDevice(ConnectionManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getName() {
        return "localhost";
    }

    public void receive(LocalDevice from, String data) {
        manager.receive(data, from);
    }
}
