package pepepay.pepepaynative.activities.qr;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.handler.qrCode.QRCreationHandler;
import pepepay.pepepaynative.backend.social31.handler.wifiDirect.WifiDirectBackend;

public class QRDeviceCreator extends Fragment {
    public QRDeviceCreator() {
    }

    public static QRDeviceCreator newInstance() {
        QRDeviceCreator fragment = new QRDeviceCreator();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qrdevice_creator, container, false);
        ImageView qrView = (ImageView) view.findViewById(R.id.qrView);
        qrView.setImageBitmap(QRCreationHandler.createQR(WifiDirectBackend.generateDeviceConnectionString(getContext())));
        return view;
    }
}
