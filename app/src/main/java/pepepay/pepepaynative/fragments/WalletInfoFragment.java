package pepepay.pepepaynative.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.connection.Connection;
import pepepay.pepepaynative.backend.social31.handler.IDevice;
import pepepay.pepepaynative.backend.social31.packages.Parcel;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;
import pepepay.pepepaynative.utils.Function;
import pepepay.pepepaynative.utils.StringUtils;

public class WalletInfoFragment extends Fragment implements Wallets.WalletsListener {
    private Wallet wallet;
    private LinearLayout transOverview;
    private TextView walletName;

    public WalletInfoFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param walletNumber The index of the wallet in Wallets.getOwnWallets()
     * @return A new instance of fragment WalletInfoFragment.
     */
    public static WalletInfoFragment newInstance(int walletNumber) {
        WalletInfoFragment fragment = new WalletInfoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        String id = Wallets.getOwnWalletID(walletNumber);
        Wallet wallet = Wallets.getWallet(id);
        if (wallet == null) throw new RuntimeException("wallet is null");
        fragment.setWallet(wallet);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (wallet == null) return null;

        View v = inflater.inflate(R.layout.fragment_wallet_info, container, false);
        final FragmentManager fm = WalletInfoFragment.this.getFragmentManager();
        walletName = (TextView) v.findViewById(R.id.walletName);
        walletName.setText(Wallets.getName(wallet) + ": " + wallet.getBalance());
        TextView walletChangeButton = (Button) v.findViewById(R.id.editButton);
        walletChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WalletChangeFragment.newInstance(wallet).show(fm, "dialog");
            }
        });
       /* walletChangeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (Wallets.isGodWallet(wallet)) {
                    Wallets.removeGodWallet(wallet);
                } else {
                    Wallets.addGodWallet(wallet);
                }

                System.out.println(PepePay.LOADER_MANAGER.save(Wallets.getGodWalletsIDs()));
                return true;
            }
        });*/
        final Button sendMoneyButton = (Button) v.findViewById(R.id.sendMoney);
        sendMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectDeviceFragment deviceSelector = SelectDeviceFragment.newInstance(wallet, new Function<Void, IDevice>() {
                    @Override
                    public Void eval(IDevice iDevice) {
                        final Connection connection = PepePay.CONNECTION_MANAGER.connect(iDevice);
                        if (connection.getTargetWalletID() != null) {
                            if (Wallets.getWallet(connection.getTargetWalletID()) == null) {
                                connection.send(Parcel.toParcel(StringUtils.multiplex(Connection.getWalletNoTransaction, connection.getTargetWalletID()), Connection.REQ), new Function<Void, String>() {
                                    @Override
                                    public Void eval(String s) {
                                        try {
                                            Wallets.addWallet((Wallet) PepePay.LOADER_MANAGER.load(s));
                                            connection.send(Parcel.toParcel(StringUtils.multiplex(Connection.getName, connection.getTargetWalletID()), Connection.REQ), new Function<Void, String>() {
                                                @Override
                                                public Void eval(String s) {
                                                    Wallets.addName(connection.getTargetWalletID(), s);
                                                    connection.send(Parcel.toParcel(StringUtils.multiplex(Connection.beginTransCheck, wallet.getIdentifier()), Connection.REQ));
                                                    TransactionFragment transactionFragment = TransactionFragment.newInstance(connection, WalletInfoFragment.this.wallet, wallet);
                                                    transactionFragment.show(fm, "dialog");
                                                    return null;
                                                }
                                            });
                                        } catch (Throwable throwable) {
                                            throwable.printStackTrace();
                                        }
                                        return null;
                                    }
                                });
                            }
                        } else {
                            SelectWalletFragment walletSelector = SelectWalletFragment.newInstance(connection, new Function<Void, Wallet>() {
                                @Override
                                public Void eval(Wallet wallet) {
                                    if (wallet == null) return null;
                                    connection.send(Parcel.toParcel(StringUtils.multiplex(Connection.beginTransCheck, wallet.getIdentifier()), Connection.REQ));
                                    TransactionFragment transactionFragment = TransactionFragment.newInstance(connection, WalletInfoFragment.this.wallet, wallet);
                                    transactionFragment.show(fm, "dialog");
                                    return null;
                                }
                            });
                            walletSelector.show(fm, "dialog");
                        }
                        return null;
                    }
                });
                deviceSelector.show(fm, "dialog");
            }
        });

        transOverview = (LinearLayout) v.findViewById(R.id.transOverview);

        ArrayList<Transaction> transactions = wallet.getTransactionsChronologically();
        for (Transaction transaction : transactions) {
            transOverview.addView(getView(transaction), 0);
        }

        Wallets.addWalletAddListener(this);

        return v;
    }

    @Override
    public void onDetach() {
        Wallets.removeWalletAddListener(this);
        super.onDetach();
    }

    @Override
    public void privateWalletAdded(Wallet wallet) {

    }

    @Override
    public void privateWalletGeneratingBegin() {

    }

    @Override
    public void nameChange(String walletID, String newName) {
        if (walletID.equals(wallet.getIdentifier())) {
            walletName.setText(Wallets.getName(wallet) + ": " + wallet.getBalance());
        }
    }

    @Override
    public void balanceChange(final String walletID, final Transaction newTransaction) {
        PepePay.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (walletID.equals(wallet.getIdentifier())) {
                    walletName.setText(Wallets.getName(wallet) + ": " + wallet.getBalance());
                    transOverview.addView(getView(newTransaction), 0);
                }
            }
        });
    }

    @Override
    public void walletDeleted(Wallet wallet) {

    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public View getView(final Transaction transaction) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.transaction_small, null);
        int color = Color.BLACK;
        String textWallet = "";
        String textAmount = "";
        if (transaction.getReceiver().equals(wallet.getIdentifier())) {
            color = this.getContext().getResources().getColor(android.R.color.holo_green_dark);
            textWallet = Wallets.getName(transaction.getSender());
            textAmount = "+" + transaction.getAmount();
        } else if (transaction.getSender().equals(wallet.getIdentifier())) {
            color = this.getContext().getResources().getColor(android.R.color.holo_red_dark);
            textWallet = Wallets.getName(transaction.getReceiver());
            textAmount = "-" + transaction.getAmount();
        }

        ((TextView)view.findViewById(R.id.walletName)).setText(textWallet);

        ((TextView)view.findViewById(R.id.amount)).setText(textAmount);
        ((TextView)view.findViewById(R.id.amount)).setTextColor(color);

        view.findViewById(R.id.cardClickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransactionInfoFragment.newInstance(transaction).show(getFragmentManager(), "dialog");
            }
        });

        return view;
    }

}
