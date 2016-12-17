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
import android.widget.TextView;

import java.util.ArrayList;

import pepepay.pepepaynative.PepePay;
import pepepay.pepepaynative.R;
import pepepay.pepepaynative.backend.social31.handler.IDevice;
import pepepay.pepepaynative.utils.function.Function;
import pepepay.pepepaynative.utils.function.Function2;
import pepepay.pepepaynative.utils.ObjectManager;

public class SelectDeviceFragment extends DialogFragment {

    private static final String CALLBACK = "callback";

    private Function<Void, IDevice> callback;
    private Function2<Void, ArrayList<? extends IDevice>, ArrayList<? extends IDevice>> listener;

    public SelectDeviceFragment() {
    }

    public static SelectDeviceFragment newInstance(Function<Void, IDevice> callback) {
        SelectDeviceFragment fragment = new SelectDeviceFragment();
        Bundle args = new Bundle();
        args.putInt(CALLBACK, ObjectManager.add(callback));
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(savedInstanceState != null){
            callback = ObjectManager.getAndRemove(savedInstanceState.getInt(CALLBACK));
        } else {
            callback = ObjectManager.getAndRemove(getArguments().getInt(CALLBACK));
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final ArrayList<IDevice> devices = new ArrayList<>();

        final ArrayAdapter<IDevice> adapter = new ArrayAdapter<IDevice>(this.getContext(), android.R.layout.select_dialog_singlechoice, devices) {
            @Override
            public int getCount() {
                return devices.size();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView)view).setText(devices.get(position).getName());
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CALLBACK, ObjectManager.add(callback));
        super.onSaveInstanceState(outState);
    }
}
