package pepepay.pepepaynative;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.File;
import java.util.Arrays;

import io.fabric.sdk.android.Fabric;
import pepepay.pepepaynative.activities.qr.QRSelectTypeFragment;
import pepepay.pepepaynative.backend.social31.handler.IDeviceConnectionHandler;
import pepepay.pepepaynative.backend.social31.handler.wifiDirect.WifiDirectConnectionHandler;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.fragments.SettingsFragment;
import pepepay.pepepaynative.fragments.WalletCreateFragment;
import pepepay.pepepaynative.fragments.walletoverview.WalletOverview;

public class MainActivity extends AppCompatActivity {

    private HandlerThread connThread;

    private final PrimaryDrawerItem createWallet = new PrimaryDrawerItem().withName(R.string.createWallet).withIdentifier(0).withIcon(GoogleMaterial.Icon.gmd_add_box);
    private final PrimaryDrawerItem wallets = new PrimaryDrawerItem().withName("Wallets").withIdentifier(1).withIcon(GoogleMaterial.Icon.gmd_account_balance_wallet);
    private final PrimaryDrawerItem createQR = new PrimaryDrawerItem().withName(R.string.action_createQR).withIdentifier(2).withIcon(GoogleMaterial.Icon.gmd_attach_file);
    private final PrimaryDrawerItem settings = new PrimaryDrawerItem().withName(R.string.settings).withIdentifier(1000).withIcon(GoogleMaterial.Icon.gmd_settings);
    private final PrimaryDrawerItem about = new PrimaryDrawerItem().withName(R.string.about).withIdentifier(1001);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.LightTheme_NoActionBar);
        setContentView(R.layout.activity_main);


        Fabric.with(this, new Crashlytics());

        WifiDirectConnectionHandler wifiDirectConnectionHandler = new WifiDirectConnectionHandler(this);
        PepePay.create(Arrays.<IDeviceConnectionHandler>asList(/*wifiDirectConnectionHandler, new QRConnectionHandler(wifiDirectConnectionHandler, this), new SalutConnectionHandler(this)*/),
                new File(this.getFilesDir(), "godWallets"),
                new File(this.getFilesDir(), "wallets"),
                new File(this.getFilesDir(), "private"),
                new File(this.getFilesDir(), "names"),
                new File(this.getFilesDir(), "options"),
                new File(this.getFilesDir(), "errols"), this);

        connThread = new HandlerThread("pepepay.connection");
        connThread.start();
        final Handler h = new Handler(connThread.getLooper());

        final Runnable[] connup = new Runnable[1];
        connup[0] = new Runnable() {
            @Override
            public void run() {
                PepePay.CONNECTION_MANAGER.update();
                h.postDelayed((connup[0]), 100);
            }
        };

        h.post(connup[0]);
        h.post(connup[0]);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Drawer s = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withCloseOnClick(true)
                .addDrawerItems(
                        createWallet, wallets,
                        new DividerDrawerItem(),
                        createQR,
                        new DividerDrawerItem(),
                        settings, about
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem.equals(createWallet.getIdentifier())) {
                            getSupportFragmentManager().beginTransaction().replace(R.id.container, WalletCreateFragment.newInstance()).commit();
                            return true;
                        } else if (drawerItem.equals(wallets.getIdentifier())) {
                            getSupportFragmentManager().beginTransaction().replace(R.id.container, WalletOverview.newInstance()).commit();
                            return true;
                        } else if (drawerItem.equals(createQR.getIdentifier())) {
                            getSupportFragmentManager().beginTransaction().replace(R.id.container, QRSelectTypeFragment.newInstance()).commit();
                            return true;
                        }else if (drawerItem.equals(settings.getIdentifier())) {
                            getSupportFragmentManager().beginTransaction().replace(R.id.container, SettingsFragment.newInstance()).commit();
                            return true;
                        } else if (drawerItem.equals(about.getIdentifier())) {
                            //getSupportFragmentManager().beginTransaction().replace(R.id.container, SettingsFragment.newInstance()).commit();
                            return true;
                        }
                        return false;
                    }
                })
                .build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        s.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wallets.saveAll();
        PepePay.ERROL.saveErrols(PepePay.errolFile);
        PepePay.OPTIONS.save(PepePay.optionsFile);
        PepePay.CONNECTION_MANAGER.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PepePay.CONNECTION_MANAGER.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connThread.quit();
    }
}
