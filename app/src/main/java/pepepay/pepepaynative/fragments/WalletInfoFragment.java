package pepepay.pepepaynative.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.connection.Connection;
import pepepay.pepepaynative.backend.social31.handler.IDevice;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;
import pepepay.pepepaynative.utils.Function;

public class WalletInfoFragment extends Fragment implements Wallets.WalletsListener {
    private Button walletChangeButton;
    private Wallet wallet;
    private LinearLayout layout;

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
        Wallet wallet = Wallets.getOwnWallets().get(walletNumber);
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
        walletChangeButton = (Button) v.findViewById(R.id.nameButton);
        walletChangeButton.setText(Wallets.getName(wallet) + "(" + wallet.getBalance() + ")");
        walletChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WalletChangeFragment.newInstance(wallet).show(fm, "dialog");
            }
        });
        walletChangeButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Wallets.addGodWallet(wallet);
                return true;
            }
        });
        final Button sendMoneyButton = (Button) v.findViewById(R.id.sendMoney);
        sendMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectDeviceFragment deviceSelector = SelectDeviceFragment.newInstance(wallet, new Function<Void, IDevice>() {
                    @Override
                    public Void eval(IDevice iDevice) {
                        final Connection connection = PepePay.CONNECTION_MANAGER.connect(iDevice);
                        SelectWalletFragment walletSelector = SelectWalletFragment.newInstance(connection, new Function<Void, Wallet>() {
                            @Override
                            public Void eval(Wallet wallet) {
                                TransactionFragment transactionFragment = TransactionFragment.newInstance(connection, WalletInfoFragment.this.wallet, wallet);
                                transactionFragment.show(fm, "dialog");
                                return null;
                            }
                        });
                        walletSelector.show(fm, "dialog");
                        return null;
                    }
                });
                deviceSelector.show(fm, "dialog");
            }
        });

        layout = (LinearLayout) v.findViewById(R.id.transOverview);
        new TransactionVisualizer(wallet, layout, getFragmentManager());

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
            walletChangeButton.setText(newName);
        }
    }

    @Override
    public void balanceChange(String walletID, Transaction newTransaction) {
        if (walletID.equals(wallet.getIdentifier())) {
            walletChangeButton.setText(Wallets.getName(wallet) + "(" + wallet.getBalance() + ")");
            TransactionVisualizer.update(layout, getFragmentManager(), newTransaction, wallet);
        }
    }

    @Override
    public void walletDeleted(Wallet wallet) {

    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
}
