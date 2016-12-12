package pepepay.pepepaynative.utils.function;

public interface Function2<T, U, V> {
    T eval(U u, V v);
}