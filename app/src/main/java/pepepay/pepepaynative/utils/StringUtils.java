package pepepay.pepepaynative.utils;

import org.spongycastle.util.encoders.Base64;

import java.util.ArrayList;

public class StringUtils {

    public static final int radix = Character.MAX_RADIX;

    public static String multiplex(String... strings) {
        //format: strings[0].length(),strings[1].length()(strings[0]strings[1])
        //ex: 5,3(hallolol)
        String lengths = "";
        String data = "";
        for (String string : strings) {
            lengths += lengths.isEmpty() ? Integer.toString(string.length(), radix) : "," + Integer.toString(string.length(), radix);
            data += string;
        }

        return lengths + "(" + data + ")";
    }

    public static String multiplex(ArrayList<String> strings) {
        return multiplex((String[]) strings.toArray());
    }

    public static String[] demultiplex(String multiplexed) {
        if (multiplexed.equals("()")) return new String[0];
        String rawLengths = multiplexed.substring(0, multiplexed.indexOf("("));
        String[] lengths = rawLengths.split(",");

        String data = multiplexed.substring(multiplexed.indexOf("("), multiplexed.lastIndexOf(")") + 1);

        ArrayList<String> result = new ArrayList<String>(lengths.length);

        int pos = 1;
        for (String length : lengths) {
            int intLength = Integer.parseInt(length, radix);
            result.add(data.substring(pos, pos + intLength));
            pos += intLength;
        }
        return result.toArray(new String[0]);
    }

    public static boolean isMultiplexed(String s) {
        return s.matches(".*\\(.*\\)");
    }

    public static String encode(byte[] array) {
        return Base64.toBase64String(array);
    }

    public static byte[] decode(String string) {
        return Base64.decode(string);
    }

    public static int getSimple(String string) {
        int result = 0;
        for (char c : string.toCharArray()) {
            result += c;
        }

        return result;
    }
}
