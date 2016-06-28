package pepepay.pepepaynative.utils.loader;


import java.util.HashMap;

import pepepay.pepepaynative.utils.StringUtils;

public class LoaderManager implements Loader<Object> {
    private HashMap<String, Loader> loaderMap = new HashMap<>();

    public void registerLoader(Loader<?> loader) {
        loaderMap.put(loader.id(), loader);
    }

    public void removeLoader(String id) {
        loaderMap.remove(id);
    }

    @Override
    public String save(Object object) {
        Loader l = null;
        for (Loader loader : loaderMap.values()) {
            if (loader.getHandledType().isInstance(object)) {
                l = loader;
                break;
            }
        }
        String saved = l.save(object);
        if (saved.isEmpty()) return l.id();
        String s = StringUtils.multiplex(l.id(), saved);
        return s;
    }

    @Override
    public Object load(String data) {
        try {
            return unsaveLoad(data);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object unsaveLoad(String data) throws ClassNotFoundException {
        Loader loader = loaderMap.get(data);
        if (loader != null) return loader.load("");

        String[] strings = StringUtils.demultiplex(data);
        if (strings.length < 2) {
            return null;
        }
        if (strings[0].equals("getWallet")) throw new RuntimeException();
        return loaderMap.get(strings[0]).load(strings[1]);
    }

    @Override
    public Class<Object> getHandledType() {
        return Object.class;
    }

    @Override
    public String id() {
        return null;
    }

}
