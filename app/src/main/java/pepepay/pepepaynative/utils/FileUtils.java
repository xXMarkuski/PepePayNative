package pepepay.pepepaynative.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

public class FileUtils {

    public static void write(File file, String text) {
        try {
            if (!file.exists()) {
                Files.createParentDirs(file);
                Files.touch(file);
            }
            Files.write(text, file, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String read(File file) {
        String content = "";
        try {
            content = Files.toString(file, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public static File child(File file, String subpath) {
        return new File(file, subpath);
    }
}
