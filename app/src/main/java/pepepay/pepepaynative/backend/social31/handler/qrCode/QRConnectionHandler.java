package pepepay.pepepaynative.backend.social31.handler.qrCode;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Arrays;

import pepepay.pepepaynative.WalletOverview2;
import pepepay.pepepaynative.backend.social31.ConnectionManager;
import pepepay.pepepaynative.backend.social31.handler.IDeviceConnectionHandler;
import pepepay.pepepaynative.utils.Function;

public class QRConnectionHandler extends AppCompatActivity implements IDeviceConnectionHandler<QRConnectionHandler, QRDevice> {

    public static final QRDevice QR_DEVICE = new QRDevice();
    private ConnectionManager manager;
    private WalletOverview2 activity;

    public QRConnectionHandler() {
    }

    public static QRConnectionHandler newInstance(WalletOverview2 activity) {

        QRConnectionHandler fragment = new QRConnectionHandler();
        fragment.setActivity(activity);
        return fragment;
    }

    public void scanQR() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
                .setOrientationLocked(false)
                .setBeepEnabled(true)
                .initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scanQR();
    }

    @Override
    public boolean canInit() {
        return true;
    }

    @Override
    public void preInit(ConnectionManager manager) {
        this.manager = manager;
    }

    @Override
    public void init(ConnectionManager manager) {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void requestAvailableDevices(Function<Void, ArrayList<QRDevice>> callback) {
        callback.eval(new ArrayList<QRDevice>(Arrays.asList(QR_DEVICE)));
    }

    @Override
    public void connect(QRDevice target) {
        activity.startActivity(QRConnectionHandler.class);
    }

    @Override
    public void disconnect(QRDevice target) {

    }

    @Override
    public void send(QRDevice target, String data) {
        manager.disconnect(target);
    }

    @Override
    public boolean canSend() {
        return true;
    }

    @Override
    public Class<QRDevice> getIDeviceType() {
        return QRDevice.class;
    }

    public void setActivity(WalletOverview2 activity) {
        this.activity = activity;
    }
}
