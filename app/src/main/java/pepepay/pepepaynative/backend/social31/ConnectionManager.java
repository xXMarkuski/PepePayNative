package pepepay.pepepaynative.backend.social31;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pepepay.pepepaynative.backend.social31.connection.Connection;
import pepepay.pepepaynative.backend.social31.handler.IDevice;
import pepepay.pepepaynative.backend.social31.handler.IDeviceConnectionHandler;
import pepepay.pepepaynative.utils.function.Function;
import pepepay.pepepaynative.utils.function.Function2;

public class ConnectionManager {

    private HashMap<IDevice, Connection> activeConnections;
    private HashMap<Connection, IDeviceConnectionHandler> connectionToHandler;
    private ArrayList<IDeviceConnectionHandler> handlers;
    private ArrayList<IDeviceConnectionHandler> handlersToInit;
    //first array are the new devices, second the gone ones
    private ArrayList<Function2<Void, ArrayList<? extends IDevice>, ArrayList<? extends IDevice>>> deviceChangeListener;

    public ConnectionManager() {
        activeConnections = new HashMap<IDevice, Connection>();
        handlers = new ArrayList<IDeviceConnectionHandler>();
        handlersToInit = new ArrayList<IDeviceConnectionHandler>();
        connectionToHandler = new HashMap<Connection, IDeviceConnectionHandler>();
        deviceChangeListener = new ArrayList<Function2<Void, ArrayList<? extends IDevice>, ArrayList<? extends IDevice>>>();
    }

    /**
     * May be null if no corresponding IDeviceConnectionHandler could be found for IDevice
     *
     * @param device
     * @return
     */
    public Connection connect(IDevice device) {
        for (IDeviceConnectionHandler handler : handlers) {
            if (canHandle(device, handler)) return connect(device, handler);
        }

        return null;
    }

    public void devicesChanged(ArrayList<? extends IDevice> newDevices, ArrayList<? extends IDevice> goneDevices) {
        for (Function2<Void, ArrayList<? extends IDevice>, ArrayList<? extends IDevice>> func : deviceChangeListener) {
            func.eval(newDevices, goneDevices);
        }
    }

    public void addDeviceChangeListener(final Function2<Void, ArrayList<? extends IDevice>, ArrayList<? extends IDevice>> listener) {
        deviceChangeListener.add(listener);
        this.getDevices(new Function<Void, ArrayList<IDevice>>() {
            @Override
            public Void eval(ArrayList<IDevice> iDevices) {
                listener.eval(iDevices, new ArrayList<IDevice>());
                return null;
            }
        });
    }

    public void removeDeviceChangeListener(Function2<Void, ArrayList<? extends IDevice>, ArrayList<? extends IDevice>> listener) {
        deviceChangeListener.remove(listener);
    }

    public Connection connect(IDevice device, IDeviceConnectionHandler handler) {
        if (activeConnections.get(device) != null) return activeConnections.get(device);
        handler.connect(device);
        Connection connection = new Connection(device, this);
        activeConnections.put(device, connection);
        connectionToHandler.put(connection, handler);
        return connection;
    }

    public void incomingConnection(IDevice device, IDeviceConnectionHandler handler) {
        Connection connection = new Connection(device, this);
        activeConnections.put(device, connection);
        connectionToHandler.put(connection, handler);
    }

    public boolean canHandle(IDevice device, IDeviceConnectionHandler handler) {
        return handler.getIDeviceType().isInstance(device);
    }

    public void receive(String data, IDevice from) {
        Connection connection = activeConnections.get(from);
        connection.receive(data);
    }

    public void send(IDevice target, String data, Connection connection) {
        connectionToHandler.get(connection).send(target, data);
    }

    public boolean canSend(Connection connection) {
        IDeviceConnectionHandler handler = connectionToHandler.get(connection);
        if (handler == null) return false;
        return handler.canSend();
    }

    public void addConnectionHandler(IDeviceConnectionHandler handler) {
        handler.preInit(this);
        handlersToInit.add(handler);
    }

    public void getDevices(final Function<Void, ArrayList<IDevice>> callback) {
        final ArrayList<IDevice> result = new ArrayList<IDevice>();
        int size = handlers.size();
        final int[] cursize = {0};
        for (IDeviceConnectionHandler handler : handlers) {
            handler.requestAvailableDevices(new Function<Void, ArrayList<IDevice>>() {
                @Override
                public Void eval(ArrayList<IDevice> array) {
                    result.addAll(array);
                    cursize[0]++;
                    return null;
                }
            });
        }
        while (cursize[0] != size) {

        }
        callback.eval(result);

    }

    public void addConnectionHandlers(List<IDeviceConnectionHandler> handlers) {
        for (IDeviceConnectionHandler handler : handlers) {
            addConnectionHandler(handler);
        }
    }

    public void update() {
        for (Connection connection : (new HashMap<>(activeConnections)).values()) {
            connection.update();
        }

        ArrayList<IDeviceConnectionHandler> copy = new ArrayList<>(handlersToInit);
        for (IDeviceConnectionHandler handler : copy) {
            if (handler.canInit()) {
                handler.init(this);
                handlersToInit.remove(handler);
                handlers.add(handler);
            }
        }
    }

    public void onResume() {
        for (IDeviceConnectionHandler handler : handlers) {
            handler.onResume();
        }
    }

    public void onPause() {
        for (IDeviceConnectionHandler handler : handlers) {
            handler.onPause();
        }
    }

    public void disconnect(IDevice<?> device) {
        activeConnections.remove(device);

        for (IDeviceConnectionHandler handler : handlers) {
            if (canHandle(device, handler)) {
                handler.disconnect(device);
                return;
            }
        }
    }

    public Connection getConnection(IDevice iDevice) {
        return activeConnections.get(iDevice);
    }

}
