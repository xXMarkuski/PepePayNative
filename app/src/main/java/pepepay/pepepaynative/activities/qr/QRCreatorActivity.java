package pepepay.pepepaynative.activities.qr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import pepepay.pepepaynative.R;

public class QRCreatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_creator);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.qrPlaceholder, QRSelectTypeFragment.newInstance()).commit();
    }
}
