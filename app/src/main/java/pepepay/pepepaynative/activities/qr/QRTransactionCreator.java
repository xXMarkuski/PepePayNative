package pepepay.pepepaynative.activities.qr;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.handler.local.LocalConnectionHandler;
import pepepay.pepepaynative.backend.social31.handler.qrCode.QRCreationHandler;
import pepepay.pepepaynative.backend.social31.handler.wifiDirect.WifiDirectBackend;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.fragments.walletoverview.SelectWalletFragment;
import pepepay.pepepaynative.utils.function.Function;
import pepepay.pepepaynative.utils.function.Function2;

public class QRTransactionCreator extends Fragment {
    public QRTransactionCreator() {
    }

    public static QRTransactionCreator newInstance() {
        QRTransactionCreator fragment = new QRTransactionCreator();
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
        final ImageView qrView = (ImageView) view.findViewById(R.id.qrView);
        SelectWalletFragment.newInstance(PepePay.CONNECTION_MANAGER.connect(LocalConnectionHandler.device), new Function<Void, Wallet>() {
            @Override
            public Void eval(final Wallet wallet) {
                if (wallet == null) return null;
                QRTransactionHelper.newInstance(new Function2<Void, Float, String>() {
                    @Override
                    public Void eval(Float s, String s2) {
                        qrView.setImageBitmap(QRCreationHandler.createQR(WifiDirectBackend.generateTransactionConnectionString(wallet, getContext(), s, s2)));
                        return null;
                    }
                }).show(getFragmentManager(), "dialog");
                return null;
            }
        }).show(getFragmentManager(), null);
        return view;
    }
}
