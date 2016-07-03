package pepepay.pepepaynative.backend.social31.handler.qrCode;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Arrays;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import pepepay.pepepaynative.R;
import pepepay.pepepaynative.WalletOverview2;
import pepepay.pepepaynative.backend.social31.ConnectionManager;
import pepepay.pepepaynative.backend.social31.handler.wifiDirect.WifiDirectBackend;
import pepepay.pepepaynative.backend.social31.handler.wifiDirect.WifiDirectConnectionHandler;

public class QRConnectionHandler extends WifiDirectBackend<QRConnectionHandler> implements ZXingScannerView.ResultHandler {
    private static final String TAG = "QRConnectionHandler";
    private static QRConnectionHandler qr;
    private ConnectionManager manager;
    private WalletOverview2 activity;
    private QRConnectionHandlerActivity qrConnectionHandlerActivity;

    public QRConnectionHandler(WifiDirectConnectionHandler handler, WalletOverview2 activity) {
        super(handler);
        this.activity = activity;
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void connect(WifiDirectBackendDevice target) {
        activity.startActivity(QRConnectionHandlerActivity.class);
        qr = this;
    }

    @Override
    public boolean canSend() {
        return true;
    }

    @Override
    protected String getIDeviceName() {
        return activity.getString(R.string.scan_qr);
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        String contents = rawResult.getText();

        Log.v(TAG, contents); // Prints scan results

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);

        handleString(contents);
        if (qrConnectionHandlerActivity != null) {
            qrConnectionHandlerActivity.getmScannerView().resumeCameraPreview(null);
            final Dialog[] greeting = new Dialog[]{null};
            final AlertDialog.Builder builder = new AlertDialog.Builder(qrConnectionHandlerActivity);
            greeting[0] = builder.setMessage(R.string.aboutText).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    qrConnectionHandlerActivity.finish();
                }
            }).create();
            greeting[0].show();
        }
    }

    private void setQRConnectionHandlerActivity(QRConnectionHandlerActivity activity) {
        this.qrConnectionHandlerActivity = activity;
    }

    public static class QRConnectionHandlerActivity extends AppCompatActivity {

        private ZXingScannerView mScannerView;

        public QRConnectionHandlerActivity() {

        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mScannerView = new ZXingScannerView(this);    // Programmatically initialize the scanner view
            setContentView(mScannerView);
            mScannerView.setFormats(Arrays.asList(BarcodeFormat.QR_CODE));
            //System.out.println(generateWalletConnectionString(Wallets.getOwnWallet(0), this));
        }

        @Override
        public void onResume() {
            super.onResume();
            if (qr != null) {
                mScannerView.setResultHandler(qr); // Register ourselves as a handler for scan results.
                qr.setQRConnectionHandlerActivity(this);
            }
            mScannerView.startCamera();          // Start camera on resume
        }


        @Override
        public void onPause() {
            super.onPause();
            mScannerView.stopCamera();           // Stop camera on pause
        }

        public ZXingScannerView getmScannerView() {
            return mScannerView;
        }
    }

}
