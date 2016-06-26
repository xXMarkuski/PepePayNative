package pepepay.pepepaynative.backend.social31.handler;

public interface IDevice<T extends IDeviceConnectionHandler> {
    boolean isAvailable();

    String getName();
}
