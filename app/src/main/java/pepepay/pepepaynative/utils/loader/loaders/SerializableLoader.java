package pepepay.pepepaynative.utils.loader.loaders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import pepepay.pepepaynative.utils.types.StringUtils;
import pepepay.pepepaynative.utils.loader.Loader;


public class SerializableLoader implements Loader<Serializable> {

    private ByteArrayInputStream inStream;
    private ObjectInputStream inObjStream;

    @Override
    public String save(Serializable serializable) {
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            ObjectOutputStream outObjStream = new ObjectOutputStream(outStream);

            outObjStream.writeObject(serializable);
            outObjStream.close();
            return StringUtils.encode(outStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Serializable load(String data) {
        try {
            return unsaveLoad(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Serializable unsaveLoad(String data) throws Exception {
        byte[] bytes = StringUtils.decode(data);
        ObjectInputStream inObjStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object o = inObjStream.readObject();
        inObjStream.close();
        return (Serializable) o;
    }

    @Override
    public Class<Serializable> getHandledType() {
        return Serializable.class;
    }

    @Override
    public String id() {
        return "o";
    }
}
