package pepepay.pepepaynative.backend.social3.connection.processor;


import java.util.HashMap;

public class ConnectionsProcessors {
    private static HashMap<String, ConnectionProcessor> processors = new HashMap<>();

    public static void addProcessor(ConnectionProcessor processor) {
        processors.put(processor.id(), processor);
    }

    public static ConnectionProcessor getProcessor(String id) {
        return processors.get(id);
    }

    public static void removeProcessor(String id) {
        processors.remove(id);
    }

    public static void removeProcessor(ConnectionProcessor processor) {
        removeProcessor(processor.id());
    }
}
