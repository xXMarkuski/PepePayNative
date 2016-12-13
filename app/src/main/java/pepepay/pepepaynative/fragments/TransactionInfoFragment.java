package pepepay.pepepaynative.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;
import pepepay.pepepaynative.utils.StringUtils;

public class TransactionInfoFragment extends DialogFragment {

    public static final DateFormat format = new SimpleDateFormat("dd MMM, HH:mm:ss", Locale.getDefault());

    private Transaction transaction;

    public TransactionInfoFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TransactionInfoFragment.
     */
    public static TransactionInfoFragment newInstance(Transaction transaction) {
        TransactionInfoFragment fragment = new TransactionInfoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setTransaction(transaction);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view = inflater.inflate(R.layout.fragment_transaction_info, null);
        builder.setTitle(R.string.transactionInfo).setView(view).setIcon(android.R.drawable.ic_menu_info_details);

        TextView amount = (TextView) view.findViewById(R.id.amount);
        TextView sender = (TextView) view.findViewById(R.id.sender);
        TextView receiver = (TextView) view.findViewById(R.id.receiver);
        TextView time = (TextView) view.findViewById(R.id.time);
        TextView purpose = (TextView) view.findViewById(R.id.purpose);

        amount.setText(transaction.getAmount() + "");
        sender.setText(Wallets.getName(transaction.getSender()));
        receiver.setText(Wallets.getName(transaction.getReceiver()));

        Date date = new Date(transaction.getTime());
        time.setText(format.format(date));

        try {
            purpose.setText(StringUtils.demultiplex(transaction.getPurpose())[0]);
        } catch (Throwable throwable) {
            purpose.setText(transaction.getPurpose());
        }

        return builder.create();
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
