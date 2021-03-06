package pepepay.pepepaynative.utils;

import android.content.Context;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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

    public static String readAsset(String path, Context context) {
        InputStreamReader streamReader = null;
        try {
            streamReader = new InputStreamReader(context.getResources().getAssets().open(path), Charsets.UTF_8);
            return CharStreams.toString(streamReader);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                streamReader.close();
            } catch (Throwable e) {
                //die silent
            }
        }
        return "";
    }

    public static File child(File file, String subpath) {
        return new File(file, subpath);
    }

    public static String read(String path, Context context) {
        return read(getFile(path, context));
    }

    public static File getFile(String path, Context context) {
        return new File(context.getFilesDir(), path);
    }
}
