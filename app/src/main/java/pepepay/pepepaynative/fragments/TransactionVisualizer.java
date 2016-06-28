package pepepay.pepepaynative.fragments;

import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;
import pepepay.pepepaynative.utils.Function2;

public class TransactionVisualizer {

    public TransactionVisualizer(Wallet wallet, final LinearLayout layout, final FragmentManager fm) {
        wallet.addTransactionListener(new Function2<Void, Wallet, Transaction>() {
            @Override
            public Void eval(final Wallet wallet, final Transaction transaction) {
                PepePay.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        final Button button = new Button(layout.getContext());
                        int color = Color.BLACK;
                        String text = "";
                        if (transaction.getReceiver().equals(wallet.getIdentifier())) {
                            color = layout.getContext().getResources().getColor(android.R.color.holo_green_dark);
                            text = Wallets.getName(transaction.getSender()) + " +" + transaction.getAmount();
                        } else if (transaction.getSender().equals(wallet.getIdentifier())) {
                            color = layout.getContext().getResources().getColor(android.R.color.holo_red_dark);
                            text = Wallets.getName(transaction.getReceiver()) + " -" + transaction.getAmount();
                        }
                        button.setTextColor(color);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TransactionInfoFragment.newInstance(transaction).show(fm, "dialog");
                            }
                        });
                        button.setText(text);
                        layout.addView(button, 0);
                    }
                });
                return null;
            }
        }, true);
    }

    public static void update(LinearLayout layout, final FragmentManager fm, final Transaction transaction, Wallet wallet) {
        final Button button = new Button(layout.getContext());
        int color = Color.BLACK;
        String text = "";
        if (transaction.getReceiver().equals(wallet.getIdentifier())) {
            color = layout.getContext().getResources().getColor(android.R.color.holo_green_dark);
            text = Wallets.getName(transaction.getSender()) + " +" + transaction.getAmount();
        } else if (transaction.getSender().equals(wallet.getIdentifier())) {
            color = layout.getContext().getResources().getColor(android.R.color.holo_red_dark);
            text = Wallets.getName(transaction.getReceiver()) + " -" + transaction.getAmount();
        }
        button.setTextColor(color);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransactionInfoFragment.newInstance(transaction).show(fm, "dialog");
            }
        });
        button.setText(text);
        layout.addView(button, 0);
    }
}
