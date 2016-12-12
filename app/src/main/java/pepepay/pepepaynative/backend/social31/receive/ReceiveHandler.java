package pepepay.pepepaynative.backend.social31.receive;


import pepepay.pepepaynative.backend.social31.connection.Connection;
import pepepay.pepepaynative.backend.social31.packages.Parcel;
import pepepay.pepepaynative.utils.function.Function2;

public interface ReceiveHandler extends Function2<Void, Parcel, Connection> {
    @Override
    Void eval(Parcel parcel, Connection connection);
}
