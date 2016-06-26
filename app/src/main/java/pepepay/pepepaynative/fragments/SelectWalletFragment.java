package pepepay.pepepaynative.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.connection.Connection;
import pepepay.pepepaynative.backend.social31.packages.Parcel;
import pepepay.pepepaynative.backend.social31.receive.ReceiveHandler;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.utils.Function;
import pepepay.pepepaynative.utils.LongUtils;
import pepepay.pepepaynative.utils.StringUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link SelectWalletFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectWalletFragment extends DialogFragment {
    private Function<Void, Wallet> callback;
    private Connection connection;

    public SelectWalletFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectWalletFragment.
     */
    public static SelectWalletFragment newInstance(Connection connection, Function<Void, Wallet> callback) {
        SelectWalletFragment fragment = new SelectWalletFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setConnection(connection);
        fragment.setCallback(callback);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = inflater.inflate(R.layout.fragment_select_wallet, null);
        final LinearLayout layout = (LinearLayout) view.findViewById(R.id.selectWallet);
        builder.setTitle(R.string.selectWallet).setView(view);

        final Parcel parcel = new Parcel(Connection.requestWalletIDs, Connection.REQ, LongUtils.nextLong(Long.MAX_VALUE));
        connection.send(parcel);
        connection.addReceiveHandler(new ReceiveHandler() {
            private ArrayList<Parcel> walletsParcels = new ArrayList<Parcel>();
            private HashMap<Button, Wallet> buttonWalletHashMap = new HashMap<Button, Wallet>();

            @Override
            public Void eval(Parcel ans, Connection connection) {
                if (ans.isAnswerOf(parcel)) {
                    ArrayList<String> array = (ArrayList) PepePay.LOADER_MANAGER.load(ans.getData());
                    for (String walletID : array) {
                        Wallet wallet = Wallets.getWallet(walletID);
                        if (wallet == null) {
                            Parcel walletParcel = new Parcel(StringUtils.multiplex(Connection.getWallet, walletID), Connection.REQ, LongUtils.nextLong(Long.MAX_VALUE));
                            connection.send(walletParcel);
                            walletsParcels.add(walletParcel);
                        } else {
                            final Button button = new Button(SelectWalletFragment.this.getContext());
                            button.setText(Wallets.getName(wallet));
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    callback.eval(buttonWalletHashMap.get(button));
                                }
                            });
                            buttonWalletHashMap.put(button, wallet);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    layout.addView(button);
                                }
                            });
                        }
                    }
                }

                Iterator<Parcel> iter = walletsParcels.iterator();
                while (iter.hasNext()) {
                    Parcel walletParcel = iter.next();
                    if (ans.isAnswerOf(walletParcel)) {
                        Wallet wallet = (Wallet) PepePay.LOADER_MANAGER.load(walletParcel.getData());
                        Wallets.addWallet(wallet);
                        final Button button = new Button(SelectWalletFragment.this.getContext());
                        button.setText(Wallets.getName(wallet));
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                callback.eval(buttonWalletHashMap.get(button));
                            }
                        });
                        buttonWalletHashMap.put(button, wallet);
                        layout.addView(button);
                        iter.remove();
                    }
                }
                return null;
            }
        });

        return builder.create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setCallback(Function<Void, Wallet> callback) {
        this.callback = callback;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

}
