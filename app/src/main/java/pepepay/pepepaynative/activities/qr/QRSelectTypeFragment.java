package pepepay.pepepaynative.activities.qr;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

import pepepay.pepepaynative.R;

public class QRSelectTypeFragment extends Fragment {
    public static QRSelectTypeFragment newInstance() {

        Bundle args = new Bundle();

        QRSelectTypeFragment fragment = new QRSelectTypeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_select_type, container, false);
        ListView selector = (ListView) view.findViewById(R.id.qrTypeSelector);
        ArrayList<String> types = new ArrayList<String>(Arrays.asList(getString(R.string.createDeviceQR), getString(R.string.createWalletQR), getString(R.string.createTransactionQR)));
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_item, types);
        selector.setAdapter(adapter);
        selector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                if (container != null) {
                    container.removeAllViews();
                }
                if (i == 0) {
                    transaction.replace(R.id.container, QRDeviceCreator.newInstance());
                } else if (i == 1) {
                    transaction.replace(R.id.container, QRWalletCreator.newInstance());
                } else if (i == 2) {
                    transaction.replace(R.id.container, QRTransactionCreator.newInstance());
                }
                transaction.addToBackStack(null).commit();
            }
        });

        return view;
    }
}
