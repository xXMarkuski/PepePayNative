package pepepay.pepepaynative.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import pepepay.pepepaynative.PepePay;


public class Options {

    public static final String IS_DEBUG_ENABLED = "debug";

    private HashMap<String, ? super Object> options;
    private ArrayList<Function2<Void, String, ? super Object>> callbacks;

    public Options(HashMap<String, ? super Object> options) {
        this.options = options;
        this.callbacks = new ArrayList<Function2<Void, String, ? super Object>>();
    }

    public static Options load(File file) {
        if (file.exists()) {
            return new Options((HashMap<String, ? super Object>) PepePay.LOADER_MANAGER.load(FileUtils.read(file)));
        } else {
            return new Options(new HashMap<String, Object>());
        }
    }

    public void addListener(Function2<Void, String, ? super Object> listener) {
        for (HashMap.Entry<String, ? super Object> option : options.entrySet()) {
            listener.eval(option.getKey(), option.getValue());
        }
        callbacks.add(listener);
    }

    public void removeListener(Function2<Void, String, ? super Object> listener) {
        callbacks.remove(listener);
    }

    public void set(String key, Object value) {
        for (Function2<Void, String, ? super Object> listener : callbacks) {
            listener.eval(key, value);
        }
        options.put(key, value);
    }

    public <T> T get(String key, T defaultVal) {
        if (options.get(key) == null) set(key, defaultVal);
        return (T) options.get(key);
    }

    public void save(File file) {
        FileUtils.write(file, PepePay.LOADER_MANAGER.save(options));
    }
}
