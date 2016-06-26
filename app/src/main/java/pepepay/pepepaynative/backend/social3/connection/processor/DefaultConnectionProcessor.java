package pepepay.pepepaynative.backend.social3.connection.processor;

public class DefaultConnectionProcessor implements ConnectionProcessor {

    private static DefaultConnectionProcessor instance = new DefaultConnectionProcessor();

    public static DefaultConnectionProcessor instance() {
        return instance;
    }

    @Override
    public String send(String data) {
        return data;
    }

    @Override
    public String receive(String data) {
        return data;
    }

    @Override
    public String id() {
        return "default";
    }

}
