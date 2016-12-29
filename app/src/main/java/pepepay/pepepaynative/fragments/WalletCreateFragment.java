package pepepay.pepepaynative.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;

public class WalletCreateFragment extends Fragment implements Wallets.WalletsListener {

    private static final String BUNDLE_NAME = "name";
    private static final String BUNDLE_PIN = "pin";
    private static final String BUNDLE_BUTTON_STATE = "buttonstate";

    private  String TAG = "WalletCreateFragment";

    private TextView nameSelector;
    private TextView pinSelector;
    private Button okButton;

    public WalletCreateFragment() {
    }

    public static WalletCreateFragment newInstance() {
        WalletCreateFragment fragment = new WalletCreateFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_wallet_create, container, false);

        nameSelector = (TextView) view.findViewById(R.id.nameSelector);
        pinSelector = (TextView) view.findViewById(R.id.pinSelector);
        okButton = (Button) view.findViewById(R.id.button4);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Wallets.generateAndAddWallet(12, nameSelector.getText() + "", pinSelector.getText() + "", WalletCreateFragment.this);
                nameSelector.setText("");
                pinSelector.setText("");
                Log.d(TAG, "unclickable");
                okButton.setEnabled(false);
            }
        });

        if(savedInstanceState != null){
            nameSelector.setText(savedInstanceState.getString(BUNDLE_NAME));
            pinSelector.setText(savedInstanceState.getString(BUNDLE_PIN));
            okButton.setEnabled(savedInstanceState.getBoolean(BUNDLE_BUTTON_STATE));
        }

        return view;
    }

    @Override
    public void privateWalletAdded(Wallet wallet) {
        Log.d(TAG, "clickable");
        if(getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                okButton.setEnabled(true);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(BUNDLE_NAME, nameSelector.getText() + "");
        outState.putString(BUNDLE_PIN, pinSelector.getText() + "");
        outState.putBoolean(BUNDLE_BUTTON_STATE, okButton.isEnabled());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void privateWalletGeneratingBegin() {
    }

    @Override
    public void nameChange(String walletID, String newName) {

    }

    @Override
    public void balanceChange(String walletID, Transaction newTransaction) {

    }

    @Override
    public void walletDeleted(Wallet wallet) {

    }
}
