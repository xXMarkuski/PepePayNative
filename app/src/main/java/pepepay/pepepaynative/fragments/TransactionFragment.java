package pepepay.pepepaynative.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.connection.Connection;
import pepepay.pepepaynative.backend.social31.packages.Parcel;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;
import pepepay.pepepaynative.utils.ObjectManager;

public class TransactionFragment extends DialogFragment {
    private static final String CONNECTION = "connection";
    private static final String TOWALLET = "towallet";
    private static final String FROMWALLET = "fromwallet";


    private Connection connection;
    private Wallet from;
    private Wallet to;

    public TransactionFragment() {
    }

    public static TransactionFragment newInstance(Connection connection, Wallet from, Wallet to) {
        TransactionFragment fragment = new TransactionFragment();
        Bundle args = new Bundle();
        args.putInt(CONNECTION, ObjectManager.add(connection));
        args.putString(TOWALLET, to.getIdentifier());
        args.putString(FROMWALLET, from.getIdentifier());
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle b;
        if(savedInstanceState != null){
            b=savedInstanceState;
        }else {
            b=getArguments();
        }

        connection = ObjectManager.getAndRemove(b.getInt(CONNECTION));
        to = Wallets.getWallet(b.getString(TOWALLET));
        from = Wallets.getWallet(b.getString(FROMWALLET));


        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_transaction, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.createTransaction).setView(view).setIcon(android.R.drawable.ic_menu_info_details);

        final TextView amountSelector = (TextView) view.findViewById(R.id.amountSelector);
        amountSelector.setText("0");
        final SeekBar amountSelectorBar = (SeekBar) view.findViewById(R.id.amountSelectorBar);
        final TextView pinSelector = (TextView) view.findViewById(R.id.pinSelector);
        final TextView purposeSelector = (TextView) view.findViewById(R.id.purposeSelector);
        final TextView error = (TextView) view.findViewById(R.id.errorText);
        final TextView pinText = (TextView) view.findViewById(R.id.pinText);

        pinSelector.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                error.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        builder.setPositiveButton(R.string.confirm, null);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface d) {
                Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (Wallets.isValidPassword(from, pinSelector.getText() + "")) {
                            final Transaction transaction = from.getSendTransaction(to, Wallets.getPrivateKey(from, pinSelector.getText() + ""), Float.parseFloat(amountSelector.getText() + ""), purposeSelector.getText() + "");
                            connection.send(Parcel.toParcel(PepePay.LOADER_MANAGER.save(transaction), Connection.REQ));
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    from.addTransaction(transaction);
                                }
                            }).start();
                            dialog.dismiss();
                        } else {
                            error.setText(R.string.wrongPassword);
                        }
                    }
                });
            }
        });

        amountSelectorBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                amountSelector.setText(progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
        if (!Wallets.isGodWallet(from)) {
            amountSelectorBar.setMax((int) from.getBalance());
        }

        if (Wallets.isValidPassword(from, "")) {
            pinSelector.setVisibility(View.GONE);
            pinText.setVisibility(View.GONE);
        }

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CONNECTION, ObjectManager.add(connection));
        outState.putString(TOWALLET, to.getIdentifier());
        outState.putString(FROMWALLET, from.getIdentifier());
        super.onSaveInstanceState(outState);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setFrom(Wallet from) {
        this.from = from;
    }

    public void setTo(Wallet to) {
        this.to = to;
    }
}
