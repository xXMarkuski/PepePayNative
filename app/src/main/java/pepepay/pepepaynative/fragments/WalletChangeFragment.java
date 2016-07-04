package pepepay.pepepaynative.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.handler.qrCode.QRCreationHandler;
import pepepay.pepepaynative.backend.social31.handler.wifiDirect.WifiDirectBackend;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;

public class WalletChangeFragment extends DialogFragment {

    private Wallet wallet;

    public WalletChangeFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WalletChangeFragment.
     */
    public static WalletChangeFragment newInstance(Wallet wallet) {
        WalletChangeFragment fragment = new WalletChangeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setWallet(wallet);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = inflater.inflate(R.layout.fragment_wallet_change, null);
        final EditText nameSelector = (EditText) view.findViewById(R.id.nameSelector);
        final RelativeLayout qrlayout = (RelativeLayout) view.findViewById(R.id.qrLayout);
        nameSelector.setText(Wallets.getName(wallet));

        final TextView id = (TextView) view.findViewById(R.id.walletID);
        id.setText(Wallets.getSimple(wallet) + "");

        Button deleteWallet = (Button) view.findViewById(R.id.deleteWalletButton);
        deleteWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(view.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.delete_wallet)
                        .setMessage(v.getContext().getString(R.string.delete_wallet_Text, Wallets.getName(wallet)))
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Wallets.deleteWallet(wallet);
                                WalletChangeFragment.this.dismiss();
                            }

                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        builder.setTitle(R.string.selectDevice).setView(view);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Wallets.addName(wallet, nameSelector.getText() + "");
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setIcon(android.R.drawable.ic_menu_edit);

        ImageView imageView = (ImageView) view.findViewById(R.id.qrView);
        imageView.setImageBitmap(QRCreationHandler.createQR(WifiDirectBackend.generateWalletConnectionString(wallet, view.getContext())));

        return builder.create();
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
}
