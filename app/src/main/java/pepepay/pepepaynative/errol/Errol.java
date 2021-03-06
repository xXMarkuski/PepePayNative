package pepepay.pepepaynative.errol;

import java.io.File;
import java.util.ArrayList;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.utils.FileUtils;

public class Errol {
    private ArrayList<String> errols;

    public Errol() {
        errols = new ArrayList<>();
    }

    public void loadErrols(File file) {
        try {
            ArrayList<String> tmp = (ArrayList<String>) PepePay.LOADER_MANAGER.load(FileUtils.read(file));
            errols.addAll(tmp);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void saveErrols(File file) {
        try {
            FileUtils.write(file, PepePay.LOADER_MANAGER.save(errols));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void errol(String s) {
        //TODO: Rework Errol handeling, maybe add fragment
        /*final AlertDialog.Builder builder = new AlertDialog.Builder(PepePay.ACTIVITY);
        builder.setIcon(android.R.drawable.stat_notify_error).setTitle(R.string.errorOccurred).setMessage(s).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        try {
            PepePay.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    builder.create().show();
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        errols.add(s);*/
    }


}

