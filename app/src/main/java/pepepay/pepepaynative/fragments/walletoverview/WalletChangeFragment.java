package pepepay.pepepaynative.fragments.walletoverview;


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
import android.widget.TextView;

import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;

public class WalletChangeFragment extends DialogFragment {

    private static final String WALLETID = "walletid";

    private Wallet wallet;

    public WalletChangeFragment() {
    }

    public static WalletChangeFragment newInstance(String walletID) {
        WalletChangeFragment fragment = new WalletChangeFragment();
        Bundle args = new Bundle();
        args.putString(WALLETID, walletID);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(savedInstanceState != null){
            wallet = Wallets.getWallet(savedInstanceState.getString(WALLETID));
        } else {
            wallet = Wallets.getWallet(getArguments().getString(WALLETID));
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = inflater.inflate(R.layout.fragment_wallet_change, null);
        final EditText nameSelector = (EditText) view.findViewById(R.id.nameSelector);
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

        builder.setTitle(R.string.walletChange).setView(view);
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

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(WALLETID, wallet.getIdentifier());
        super.onSaveInstanceState(outState);
    }
}
