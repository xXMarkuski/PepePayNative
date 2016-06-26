package pepepay.pepepaynative.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.handler.IDevice;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.utils.Function;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WalletInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WalletInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WalletInfoFragment extends Fragment {
    private static final String WalletID = "walletid";

    private OnFragmentInteractionListener mListener;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
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
                FragmentManager fm = WalletInfoFragment.this.getFragmentManager();
                SelectDeviceFragment newFragment = SelectDeviceFragment.newInstance(walletid, new Function<Void, IDevice>() {
                    @Override
                    public Void eval(IDevice iDevice) {
                        Log.d("asdsd", iDevice.getName());
                        return null;
                    }
                });
                newFragment.show(fm, "asdasd");
            }
        });

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
