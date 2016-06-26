package pepepay.pepepaynative.backend.social3.connection.processor;

public interface ConnectionProcessor {

    String send(String data);

    String receive(String data);

    String id();
}
