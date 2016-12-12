package pepepay.pepepaynative.backend.social31.handler;

import java.util.ArrayList;

import pepepay.pepepaynative.backend.social31.ConnectionManager;
import pepepay.pepepaynative.utils.function.Function;

public interface IDeviceConnectionHandler<T extends IDeviceConnectionHandler, U extends IDevice<T>> {

    boolean canInit();

    void preInit(ConnectionManager manager);

    void init(ConnectionManager manager);

    void onResume();

    void onPause();

    void requestAvailableDevices(Function<Void, ArrayList<U>> callback);

    void connect(U target);

    void disconnect(U target);

    void send(U target, String data);

    boolean canSend();

    Class<U> getIDeviceType();
}
