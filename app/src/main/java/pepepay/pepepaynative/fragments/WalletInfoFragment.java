package pepepay.pepepaynative.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.connection.Connection;
import pepepay.pepepaynative.backend.social31.handler.IDevice;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.utils.Function;

public class WalletInfoFragment extends Fragment {
    private static final String WalletID = "walletid";

    public WalletInfoFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param walletNumber The index of the wallet in Wallets.getOwnWallets()
     * @return A new instance of fragment WalletInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        final String walletid = getArguments().getString(WalletID);
        Button walletChangeButton = (Button) v.findViewById(R.id.nameButton);
        walletChangeButton.setText(Wallets.getName(walletid));
        final Button sendMoneyButton = (Button) v.findViewById(R.id.sendMoney);
        sendMoneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentManager fm = WalletInfoFragment.this.getFragmentManager();
                SelectDeviceFragment deviceSelector = SelectDeviceFragment.newInstance(walletid, new Function<Void, IDevice>() {
                    @Override
                    public Void eval(IDevice iDevice) {
                        final Connection connection = PepePay.CONNECTION_MANAGER.connect(iDevice);
                        SelectWalletFragment walletSelector = SelectWalletFragment.newInstance(connection, new Function<Void, Wallet>() {
                            @Override
                            public Void eval(Wallet wallet) {
                                TransactionFragment transactionFragment = TransactionFragment.newInstance(connection, Wallets.getWallet(walletid), wallet);
                                transactionFragment.show(fm, "asdasd");
                                return null;
                            }
                        });
                        walletSelector.show(fm, "asdasd");
                        return null;
                    }
                });
                deviceSelector.show(fm, "asdasd");
            }
        });

        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
