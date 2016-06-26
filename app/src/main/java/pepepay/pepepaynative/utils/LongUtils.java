package pepepay.pepepaynative.utils;

import java.util.Random;

public class LongUtils {

    private static final Random rng = new Random();

    public static long nextLong(long n) {
        long bits, val;
        do {
            bits = (rng.nextLong() << 1) >>> 1;
            val = bits % n;
        } while (bits - val + (n - 1) < 0L);
        return val;
    }
}
