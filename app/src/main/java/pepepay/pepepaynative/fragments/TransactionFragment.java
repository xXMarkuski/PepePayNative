package pepepay.pepepaynative.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.connection.Connection;
import pepepay.pepepaynative.backend.social31.packages.Parcel;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link TransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionFragment extends DialogFragment {
    private Connection connection;
    private Wallet from;
    private Wallet to;

    public TransactionFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TransactionFragment.
     */
    public static TransactionFragment newInstance(Connection connection, Wallet from, Wallet to) {
        TransactionFragment fragment = new TransactionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setConnection(connection);
        fragment.setFrom(from);
        fragment.setTo(to);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_transaction, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.selectDevice).setView(view);

        final TextView amountSelector = (TextView) view.findViewById(R.id.amountSelector);
        final TextView pinSelector = (TextView) view.findViewById(R.id.pinSelector);
        final TextView purposeSelector = (TextView) view.findViewById(R.id.purposeSelector);
        Button button = (Button) view.findViewById(R.id.confirmButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Wallets.isValidPassword(from, pinSelector.getText() + "")) {
                    Transaction transaction = from.getSendTransaction(to, Wallets.getPrivateKey(from, pinSelector.getText() + ""), Float.parseFloat(amountSelector.getText() + ""), purposeSelector.getText() + "");
                    connection.send(Parcel.toParcel(PepePay.LOADER_MANAGER.save(transaction), "req"));
                    from.addTransaction(transaction);
                } else {
                    //error.setText("wrong password");
                }
            }
        });

        return builder.create();
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

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
