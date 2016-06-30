package pepepay.pepepaynative.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import java.util.ArrayList;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.handler.IDevice;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.utils.Function;
import pepepay.pepepaynative.utils.Function2;

public class SelectDeviceFragment extends DialogFragment {
    private Function<Void, IDevice> callback;
    private Wallet wallet;
    private Function2<Void, ArrayList<? extends IDevice>, ArrayList<? extends IDevice>> listener;

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
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final ArrayList<IDevice> devices = new ArrayList<>();

        final ArrayAdapter<IDevice> adapter = new ArrayAdapter<IDevice>(this.getContext(), android.R.layout.select_dialog_singlechoice, devices) {
            @Override
            public int getCount() {
                return devices.size();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                CheckedTextView view = new CheckedTextView(this.getContext());
                view.setText(devices.get(position).getName());
                view.setCheckMarkDrawable(android.R.drawable.btn_radio);
                view.setTextAppearance(getContext(), android.R.style.TextAppearance_DeviceDefault_Large);
                return view;
            }
        };

        final int[] selectedItem = {0};

        builder.setTitle(R.string.selectDevice).setIcon(android.R.drawable.ic_menu_info_details).setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedItem[0] = which;
            }
        });

        listener = new Function2<Void, ArrayList<? extends IDevice>, ArrayList<? extends IDevice>>() {
            @Override
            public Void eval(ArrayList<? extends IDevice> iDevices, ArrayList<? extends IDevice> iDevices2) {
                devices.addAll(iDevices);
                devices.removeAll(iDevices2);
                PepePay.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
                return null;
            }
        };
        PepePay.CONNECTION_MANAGER.addDeviceChangeListener(listener);

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.eval(devices.get(selectedItem[0]));
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        PepePay.CONNECTION_MANAGER.removeDeviceChangeListener(listener);
    }

    public void setCallback(Function<Void, IDevice> callback) {
        this.callback = callback;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }
}
