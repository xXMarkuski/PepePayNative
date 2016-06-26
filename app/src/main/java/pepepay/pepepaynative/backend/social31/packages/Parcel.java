package pepepay.pepepaynative.backend.social31.packages;

import java.util.HashMap;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.utils.LongUtils;
import pepepay.pepepaynative.utils.StringUtils;
import pepepay.pepepaynative.utils.loader.Loader;

public class Parcel {
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
        return this.header.get(HeaderOption.UID).equals(parcel.header.get(HeaderOption.UID)) && !this.header.get(HeaderOption.TYPE).equals(parcel.header.get(HeaderOption.TYPE));
    }

    public String getData() {
        return data;
    }

    public long getUid() {
        return Long.parseLong(header.get(HeaderOption.UID));
    }

    public Parcel getAnswer(String data) {
        return new Parcel(data, "ans", this.getUid());
    }

    enum HeaderOption {
        /*
        Type: can either be ans or req
        Uid: for Type req is has to be a random uid, for Type ans it has to be the uid of the req
        Processor: Deferments whith Connection Processor to use
         */
        TYPE, UID, PROCESSOR
    }

    public static class ParcelLoader implements Loader<Parcel> {

        @Override
        public String save(Parcel parcel) {
            return StringUtils.multiplex(PepePay.LOADER_MANAGER.save(parcel.header), parcel.data);
        }

        @Override
        public Parcel load(String data) {
            String[] dem = StringUtils.demultiplex(data);
            return new Parcel(dem[1], ((HashMap<HeaderOption, String>) PepePay.LOADER_MANAGER.load(dem[0])));
        }

        @Override
        public Parcel unsaveLoad(String data) throws Exception {
            return load(data);
        }

        @Override
        public Class<Parcel> getHandledType() {
            return Parcel.class;
        }

        @Override
        public String id() {
            return "P";
        }
    }

    public static class HeaderOptionLoader implements Loader<HeaderOption> {

        @Override
        public String save(HeaderOption headerOption) {
            if (headerOption.equals(HeaderOption.TYPE)) {
                return "type";
            } else if (headerOption.equals(HeaderOption.UID)) {
                return "uid";
            } else if (headerOption.equals(HeaderOption.PROCESSOR)) {
                return "pro";
            }
            return "";
        }

        @Override
        public HeaderOption load(String data) {
            if (data.equals("type")) {
                return HeaderOption.TYPE;
            } else if (data.equals("uid")) {
                return HeaderOption.UID;
            } else if (data.equals("pro")) {
                return HeaderOption.PROCESSOR;
            }
            return null;
        }

        @Override
        public HeaderOption unsaveLoad(String data) throws Exception {
            return load(data);
        }

        @Override
        public Class<HeaderOption> getHandledType() {
            return HeaderOption.class;
        }

        @Override
        public String id() {
            return "h";
        }
    }

}
