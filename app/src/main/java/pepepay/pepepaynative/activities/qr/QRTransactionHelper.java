package pepepay.pepepaynative.activities.qr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import pepepay.pepepaynative.R;
import pepepay.pepepaynative.utils.function.Function2;

public class QRTransactionHelper extends DialogFragment {

    private Function2<Void, Float, String> callback;

    public QRTransactionHelper() {
    }

    public static QRTransactionHelper newInstance(Function2<Void, Float, String> callback) {
        QRTransactionHelper fragment = new QRTransactionHelper();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setCallback(callback);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_qrtransaction_helper, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setTitle(R.string.createTransactionQRTitle).setIcon(android.R.drawable.ic_dialog_info);

        final TextView amountSelector = (TextView) view.findViewById(R.id.amountSelector);
        final SeekBar amountSelectorBar = (SeekBar) view.findViewById(R.id.amountSelectorBar);
        final TextView purposeSelector = (TextView) view.findViewById(R.id.purposeSelector);

        amountSelectorBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                amountSelector.setText(progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                callback.eval(Float.parseFloat(amountSelector.getText().toString()), purposeSelector.getText().toString());
            }
        });

        return builder.create();
    }

    public void setCallback(Function2<Void, Float, String> callback) {
        this.callback = callback;
    }
}
