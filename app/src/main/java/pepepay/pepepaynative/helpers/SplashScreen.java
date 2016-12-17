package pepepay.pepepaynative.helpers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pepepay.pepepaynative.WalletOverview2;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, WalletOverview2.class);
        startActivity(intent);
        finish();
    }
}
