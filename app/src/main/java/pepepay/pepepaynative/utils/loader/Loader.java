package pepepay.pepepaynative.utils.loader;

public interface Loader<T> {
    String save(T t);

    T load(String data);

    T unsaveLoad(String data) throws Exception;

    Class<T> getHandledType();

    String id();
}
