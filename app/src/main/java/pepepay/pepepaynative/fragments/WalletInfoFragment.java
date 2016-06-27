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
import pepepay.pepepaynative.utils.Function;

public class WalletInfoFragment extends Fragment implements Wallets.WalletsListener {
    private static final String WalletID = "walletid";
    private String walletID;
    private Button walletChangeButton;

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
        args.putString(WalletID, Wallets.getOwnWallets().get(walletNumber).getIdentifier());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wallet_info, container, false);
        final FragmentManager fm = WalletInfoFragment.this.getFragmentManager();
        walletID = getArguments().getString(WalletID);
        walletChangeButton = (Button) v.findViewById(R.id.nameButton);
        walletChangeButton.setText(Wallets.getName(walletID));
        walletChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WalletChangeFragment.newInstance(Wallets.getWallet(walletID)).show(fm, "dialog");
            }
        });
        final Button sendMoneyButton = (Button) v.findViewById(R.id.sendMoney);
        sendMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectDeviceFragment deviceSelector = SelectDeviceFragment.newInstance(walletID, new Function<Void, IDevice>() {
                    @Override
                    public Void eval(IDevice iDevice) {
                        final Connection connection = PepePay.CONNECTION_MANAGER.connect(iDevice);
                        SelectWalletFragment walletSelector = SelectWalletFragment.newInstance(connection, new Function<Void, Wallet>() {
                            @Override
                            public Void eval(Wallet wallet) {
                                TransactionFragment transactionFragment = TransactionFragment.newInstance(connection, Wallets.getWallet(walletID), wallet);
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

        LinearLayout layout = (LinearLayout) v.findViewById(R.id.transOverview);
        new TransactionVisualizer(Wallets.getWallet(walletID), layout);

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
        if (walletID.equals(this.walletID)) {
            walletChangeButton.setText(newName);
        }
    }

    @Override
    public void balanceChange(String walletID, float newBalance) {

    }

    @Override
    public void walletDeleted(Wallet wallet) {

    }
}
