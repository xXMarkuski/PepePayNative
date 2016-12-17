package pepepay.pepepaynative.utils;

import java.util.ArrayList;
import java.util.HashMap;

public class ObjectManager {
    private static HashMap<Integer, Object> list = new HashMap<>();
    private static int highestIndex = 0;

    public static <T> void put(int i, T obj){
        if(i>highestIndex) highestIndex = i;
        list.put(i, obj);
    }

    public static <T> int add(T obj){
        int i = highestIndex++;
        list.put(i, obj);
        return i;
    }

    public static <T extends Object> T get(int i){
        return (T) list.get(i);
    }

    public static <T> void remove(T obj){
        list.remove(obj);
    }

    public static <T extends Object> T getAndRemove(int i){
        T f = get(i);
        list.remove(i);
        return f;
    }
}
