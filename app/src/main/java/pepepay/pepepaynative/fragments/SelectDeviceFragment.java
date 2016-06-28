package pepepay.pepepaynative.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.handler.IDevice;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.utils.Function;
import pepepay.pepepaynative.utils.Function2;

public class SelectDeviceFragment extends DialogFragment {
    private Function<Void, IDevice> callback;
    private Wallet wallet;

    public SelectDeviceFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectDeviceFragment.
     */
    public static SelectDeviceFragment newInstance(Wallet wallet, Function<Void, IDevice> callback) {
        SelectDeviceFragment fragment = new SelectDeviceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setCallback(callback);
        fragment.setWallet(wallet);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_select_device, null);
        final LinearLayout layout = (LinearLayout) view.findViewById(R.id.selectDevice);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.selectDevice).setView(view).setIcon(android.R.drawable.ic_menu_info_details);

        PepePay.CONNECTION_MANAGER.addDeviceChangeListener(new Function2<Void, ArrayList<? extends IDevice>, ArrayList<? extends IDevice>>() {
            HashMap<IDevice, Button> iDeviceButtonHashMap = new HashMap<IDevice, Button>();

            @Override
            public Void eval(ArrayList<? extends IDevice> iDevices, ArrayList<? extends IDevice> iDevices2) {
                for (final IDevice device : iDevices) {
                    Button button = new Button(SelectDeviceFragment.this.getContext());
                    button.setText(device.getName());
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callback.eval(device);
                        }
                    });
                    iDeviceButtonHashMap.put(device, button);
                    layout.addView(button);
                }
                for (IDevice device : iDevices2) {
                    layout.removeView(iDeviceButtonHashMap.get(device));
                    iDeviceButtonHashMap.remove(device);
                }
                return null;
            }
        });

        return builder.create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setCallback(Function<Void, IDevice> callback) {
        this.callback = callback;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
}
