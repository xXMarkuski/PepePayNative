package pepepay.pepepaynative.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.wallet2.Wallets;

public class WalletCreateFragment extends Fragment {
    private TextView nameSelector;
    private TextView pinSelector;
    private Button okButton;

    public WalletCreateFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WalletCreateFragment.
     */
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
                Wallets.generateAndAddWallet(11, nameSelector.getText() + "", pinSelector.getText() + "");
            }
        });

        return view;
    }
}
