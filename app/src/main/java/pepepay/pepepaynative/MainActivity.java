package pepepay.pepepaynative;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
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
import pepepay.pepepaynative.backend.social31.handler.wifiSalut.SalutConnectionHandler;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.fragments.AboutFragment;
import pepepay.pepepaynative.fragments.SettingsFragment;
import pepepay.pepepaynative.fragments.WalletCreateFragment;
import pepepay.pepepaynative.fragments.walletoverview.WalletOverview;
import pepepay.pepepaynative.utils.FileUtils;
import pepepay.pepepaynative.utils.Options;

public class MainActivity extends AppCompatActivity {

    private final PrimaryDrawerItem createWallet = new PrimaryDrawerItem().withName(R.string.createWallet).withIdentifier(0).withIcon(GoogleMaterial.Icon.gmd_add_box);
    private final PrimaryDrawerItem wallets = new PrimaryDrawerItem().withName("Wallets").withIdentifier(1).withIcon(GoogleMaterial.Icon.gmd_account_balance_wallet);
    private final PrimaryDrawerItem createQR = new PrimaryDrawerItem().withName(R.string.action_createQR).withIdentifier(2).withIcon(GoogleMaterial.Icon.gmd_attach_file);
    private final PrimaryDrawerItem settings = new PrimaryDrawerItem().withName(R.string.settings).withIdentifier(1000).withIcon(GoogleMaterial.Icon.gmd_settings);
    private final PrimaryDrawerItem about = new PrimaryDrawerItem().withName(R.string.about).withIdentifier(1001).withIcon(GoogleMaterial.Icon.gmd_info);
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private Drawer[] drawer;

    private RetainedFragment dataFragment;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println(sharedPref.getAll());
        setTheme(Options.getTheme(sharedPref.getString(Options.THEME, "light")));
        setContentView(R.layout.activity_main);

        dataFragment = (RetainedFragment) getSupportFragmentManager().findFragmentByTag("data");
        // create the fragment and data the first time
        if (dataFragment == null) {
            Fabric.with(this, new Crashlytics());

            // add the fragment
            dataFragment = new RetainedFragment();
            getSupportFragmentManager().beginTransaction().add(dataFragment, "data").commit();

            HandlerThread connThread = new HandlerThread("pepepay.connection");
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
            dataFragment.setConnThread(connThread);

            PepePay pepePay = new PepePay();
            //WifiDirectConnectionHandler wifiDirectConnectionHandler = new WifiDirectConnectionHandler(this);
            pepePay.create(Arrays.<IDeviceConnectionHandler>asList(/*wifiDirectConnectionHandler, new QRConnectionHandler(wifiDirectConnectionHandler, this),*/ new SalutConnectionHandler(this)),
                    new File(this.getFilesDir(), "godWallets"),
                    new File(this.getFilesDir(), "wallets"),
                    new File(this.getFilesDir(), "private"),
                    new File(this.getFilesDir(), "names"),
                    new File(this.getFilesDir(), "errols"));
            dataFragment.setPepePay(pepePay);
        }

        String defWalletId = sharedPref.getString(Options.DEFAULT_WALLET, "");
        Wallets.setDefaultWallet(defWalletId);

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(Options.THEME)) {
                    finish();
                    final Intent intent = getIntent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else if (key.equals(Options.DEFAULT_WALLET)) {
                    String defWalletId = sharedPreferences.getString(Options.DEFAULT_WALLET, "");
                    Wallets.setDefaultWallet(defWalletId);
                }
            }
        };
        sharedPref.registerOnSharedPreferenceChangeListener(listener);

        if (!sharedPref.getBoolean(Options.READ_AGB, false)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final Dialog[] greeting = new Dialog[]{null};
            greeting[0] = builder.setMessage(R.string.tosGreeting).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sharedPref.edit().putBoolean(Options.READ_AGB, true).apply();
                    //TODO:Implement username entering at first launch
                }
            }).setNeutralButton(R.string.showTos, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(FileUtils.readAsset(Options.READ_AGB, MainActivity.this)).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            greeting[0].show();
                        }
                    }).setCancelable(false).create().show();
                }
            }).setCancelable(false).create();
            greeting[0].show();
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TypedValue enabledTextColor = new TypedValue();
        getTheme().resolveAttribute(R.attr.material_drawer_primary_text, enabledTextColor, true);

        drawer = new Drawer[1];

        drawer[0] = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withHasStableIds(true)
                .withSavedInstance(savedInstanceState)
                .withCloseOnClick(true)
                .withFireOnInitialOnClick(true)
                .withGenerateMiniDrawer(true)
                .withSelectedItem(wallets.getIdentifier())
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Simple Pay").withIcon(R.mipmap.ic_launcher).withEnabled(false).withDisabledTextColor(enabledTextColor.data),
                        new DividerDrawerItem(),
                        createWallet, wallets,
                        new DividerDrawerItem(),
                        createQR,
                        new DividerDrawerItem(),
                        settings, about
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if(drawerItem == null) return true;
                        Fragment nextFragment = null;
                        if (drawerItem.equals(createWallet.getIdentifier())) {
                            nextFragment = WalletCreateFragment.newInstance();
                        } else if (drawerItem.equals(wallets.getIdentifier())) {
                            nextFragment = WalletOverview.newInstance();
                        } else if (drawerItem.equals(createQR.getIdentifier())) {
                            nextFragment = QRSelectTypeFragment.newInstance();
                        } else if (drawerItem.equals(settings.getIdentifier())) {
                            nextFragment = SettingsFragment.newInstance();
                        } else if (drawerItem.equals(about.getIdentifier())) {
                            nextFragment = AboutFragment.newInstance();
                        }

                        if (nextFragment != null) {
                            //TODO:Fix add to back stack
                            getSupportFragmentManager().beginTransaction()/*.addToBackStack(null)*/.replace(R.id.container, nextFragment).commit();
                            if (drawer[0] != null) drawer[0].closeDrawer();
                        }
                        return true;
                    }
                })
                .build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        drawer[0].getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PepePay.ERROL.saveErrols(dataFragment.getPepePay().errolFile);
        saveAll();
        PepePay.CONNECTION_MANAGER.onPause();
    }

    private void saveAll() {
        Wallets.saveNames(dataFragment.getPepePay().nameFile);
        Wallets.savePrivateKeys(dataFragment.getPepePay().privateFile);
        Wallets.saveWallets(dataFragment.getPepePay().walletFile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PepePay.CONNECTION_MANAGER.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        drawer[0].saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    public static class RetainedFragment extends Fragment {

        private HandlerThread connThread;
        private PepePay pepePay;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // retain this fragment
            setRetainInstance(true);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            connThread.quit();
        }

        public HandlerThread getConnThread() {
            return connThread;
        }

        public void setConnThread(HandlerThread connThread) {
            this.connThread = connThread;
        }

        public PepePay getPepePay() {
            return pepePay;
        }

        public void setPepePay(PepePay pepePay) {
            this.pepePay = pepePay;
        }
    }
}
