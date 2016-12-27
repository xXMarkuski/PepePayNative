package pepepay.pepepaynative.fragments.walletoverview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.connection.Connection;
import pepepay.pepepaynative.backend.social31.handler.IDevice;
import pepepay.pepepaynative.backend.social31.packages.Parcel;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;
import pepepay.pepepaynative.utils.ObjectManager;
import pepepay.pepepaynative.utils.types.StringUtils;
import pepepay.pepepaynative.utils.function.Function;

public class WalletInfoFragment extends Fragment implements Wallets.WalletsListener {
    private static final String WALLET = "wallet";
    private static final String FRAGMENTMANAGER = "fragmentmanager";

    private LinearLayout transOverview;
    private TextView walletName;

    private Wallet wallet;
    private int fragmentmanagerid;

    public WalletInfoFragment() {
    }

    public static WalletInfoFragment newInstance(Wallet wallet) {
        WalletInfoFragment fragment = new WalletInfoFragment();
        Bundle args = new Bundle();
        args.putString(WALLET, wallet.getIdentifier());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Wallet wallet = null;

        if(savedInstanceState != null){
            wallet = Wallets.getWallet(savedInstanceState.getString(WALLET));
            fragmentmanagerid = savedInstanceState.getInt(FRAGMENTMANAGER);
            ObjectManager.put(fragmentmanagerid, getFragmentManager());
        } else {
            wallet = Wallets.getWallet(getArguments().getString(WALLET));
            fragmentmanagerid = ObjectManager.add(getFragmentManager());
        }

        this.wallet = wallet;

        final Wallet finalWallet = wallet;

        if (wallet == null) throw new RuntimeException("wallet is null");

        View v = inflater.inflate(R.layout.fragment_wallet_info, container, false);
        final FragmentManager fm = WalletInfoFragment.this.getFragmentManager();
        walletName = (TextView) v.findViewById(R.id.walletName);
        walletName.setText(Wallets.getName(wallet) + ": " + wallet.getBalance());
        TextView walletChangeButton = (Button) v.findViewById(R.id.editButton);
        walletChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WalletChangeFragment.newInstance(finalWallet.getIdentifier()).show(fm, "dialog");
            }
        });

       walletChangeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (Wallets.isGodWallet(finalWallet)) {
                    Wallets.removeGodWallet(finalWallet);
                    System.out.println("removed God Wallet" + Wallets.getName(finalWallet));
                } else {
                    Wallets.addGodWallet(finalWallet);
                    System.out.println("added God Wallet" + Wallets.getName(finalWallet));
                }

                System.out.println(PepePay.LOADER_MANAGER.save(Wallets.getGodWalletsIDs()));
                return true;
            }
        });

        final Button sendMoneyButton = (Button) v.findViewById(R.id.sendMoney);
        sendMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMoney();
            }
        });

        transOverview = (LinearLayout) v.findViewById(R.id.transOverview);

        ArrayList<Transaction> transactions = wallet.getTransactionsChronologically();
        for (Transaction transaction : transactions) {
            transOverview.addView(getTransactionView(transaction, wallet.getIdentifier()), 0);
        }

        Wallets.addWalletAddListener(this);

        return v;
    }

    private void sendMoney(){
        SelectDeviceFragment deviceSelector = SelectDeviceFragment.newInstance(new Function<Void, IDevice>() {
            @Override
            public Void eval(IDevice iDevice) {
                final Connection connection = PepePay.CONNECTION_MANAGER.connect(iDevice);
                findWallet(connection);
                return null;
            }
        });

        deviceSelector.show((FragmentManager) ObjectManager.get(fragmentmanagerid), "dialog");
    }

    private void findWallet(final Connection connection){
        final Wallet finalWallet = wallet;
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

                                    connection.send(Parcel.toParcel(StringUtils.multiplex(Connection.beginTransCheck, finalWallet.getIdentifier()), Connection.REQ));
                                    TransactionFragment transactionFragment = TransactionFragment.newInstance(connection, WalletInfoFragment.this.wallet, finalWallet);
                                    transactionFragment.show((FragmentManager) ObjectManager.get(fragmentmanagerid), "dialog");

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
                    transactionFragment.show((FragmentManager) ObjectManager.get(fragmentmanagerid), "dialog");

                    return null;
                }
            });
            walletSelector.show((FragmentManager) ObjectManager.get(fragmentmanagerid), "dialog");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(WALLET, wallet.getIdentifier());
        outState.putInt(FRAGMENTMANAGER, fragmentmanagerid);
        super.onSaveInstanceState(outState);
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
                    transOverview.addView(getTransactionView(newTransaction, walletID), 0);
                }
            }
        });
    }

    @Override
    public void walletDeleted(Wallet wallet) {

    }

    private View getTransactionView(final Transaction transaction, String walletID) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.transaction_small, null);
        int color = Color.BLACK;
        String textWallet = "";
        String textAmount = "";
        if (transaction.getReceiver().equals(walletID)) {
            color = this.getContext().getResources().getColor(android.R.color.holo_green_dark);
            textWallet = Wallets.getName(transaction.getSender());
            textAmount = "+" + transaction.getAmount();
        } else if (transaction.getSender().equals(walletID)) {
            color = this.getContext().getResources().getColor(android.R.color.holo_red_dark);
            textWallet = Wallets.getName(transaction.getReceiver());
            textAmount = "-" + transaction.getAmount();
        }

        ((TextView) view.findViewById(R.id.walletName)).setText(textWallet);

        ((TextView) view.findViewById(R.id.amount)).setText(textAmount);
        ((TextView) view.findViewById(R.id.amount)).setTextColor(color);

        view.findViewById(R.id.cardClickable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransactionInfoFragment.newInstance(transaction).show(getFragmentManager(), "dialog");
            }
        });

        return view;
    }

}
