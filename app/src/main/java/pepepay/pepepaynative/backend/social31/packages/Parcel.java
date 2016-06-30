package pepepay.pepepaynative.backend.social31.packages;

import java.io.Serializable;
import java.util.HashMap;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.backend.social31.connection.Connection;
import pepepay.pepepaynative.utils.LongUtils;

public class Parcel implements Serializable {

    public static final long serialVersionUID = 1L;

    //Can be anything
    private final String data;
    //Header
    private final HashMap<HeaderOption, String> header;

    /**
     * Id uid = 0 it will use a random uid
     *
     * @param data
     * @param type
     * @param uid
     */
    public Parcel(String data, String type, long uid) {
        this.data = data;
        this.header = new HashMap<HeaderOption, String>();
        if (uid == 0) uid = LongUtils.nextLong(Long.MAX_VALUE);
        this.header.put(HeaderOption.UID, uid + "");
        this.header.put(HeaderOption.TYPE, type);
    }

    public Parcel(String data, HashMap<HeaderOption, String> header) {
        this.data = data;
        this.header = header;
    }

    public static Parcel toParcel(String data, String type) {
        return new Parcel(data, type, LongUtils.nextLong(Long.MAX_VALUE));
    }

    public String toData() {
        return PepePay.LOADER_MANAGER.save(this);
    }

    public boolean isAnswerOf(Parcel parcel) {
        return this.header.get(HeaderOption.UID).equals(parcel.header.get(HeaderOption.UID)) && this.header.get(HeaderOption.TYPE).equals(Connection.ANS) && parcel.header.get(HeaderOption.TYPE).equals(Connection.REQ);
    }

    public String getData() {
        return data;
    }

    public long getUid() {
        return Long.parseLong(header.get(HeaderOption.UID));
    }

    public Parcel getAnswer(String data) {
        return new Parcel(data, Connection.ANS, this.getUid());
    }

    public Parcel getAnswer(Object obj) {
        return new Parcel(PepePay.LOADER_MANAGER.save(obj), Connection.ANS, this.getUid());
    }

    enum HeaderOption implements Serializable {
        /*
        Type: can either be ans or req
        Uid: for Type req is has to be a random uid, for Type ans it has to be the uid of the req
        Processor: Deferments whith Connection Processor to use
         */
        TYPE, UID, PROCESSOR;

        public static final long serialVersionUID = 1L;

    }
}
