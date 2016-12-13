package pepepay.pepepaynative;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.util.Arrays;

import io.fabric.sdk.android.Fabric;
import pepepay.pepepaynative.backend.social31.handler.IDeviceConnectionHandler;
import pepepay.pepepaynative.backend.social31.handler.wifiDirect.WifiDirectConnectionHandler;
import pepepay.pepepaynative.backend.wallet2.Wallet;
import pepepay.pepepaynative.backend.wallet2.Wallets;
import pepepay.pepepaynative.backend.wallet2.transaction.Transaction;
import pepepay.pepepaynative.fragments.WalletCreateFragment;
import pepepay.pepepaynative.fragments.WalletInfoFragment;
import pepepay.pepepaynative.utils.FileUtils;
import pepepay.pepepaynative.utils.Options;

public class WalletOverview2 extends AppCompatActivity implements Wallets.WalletsListener {

    private String TAG = "WalletOverview2";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private Thread updateThread;

    private Integer lastTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.LightTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());

        if (updateThread == null) {
            WifiDirectConnectionHandler wifiDirectConnectionHandler = new WifiDirectConnectionHandler(this);
            new PepePay(Arrays.<IDeviceConnectionHandler>asList(wifiDirectConnectionHandler)).create(new File(this.getFilesDir(), "godWallets"), new File(this.getFilesDir(), "wallets"), new File(this.getFilesDir(), "private"), new File(this.getFilesDir(), "names"), new File(this.getFilesDir(), "options"), new File(this.getFilesDir(), "errols"), this);

            updateThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.interrupted()) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        PepePay.CONNECTION_MANAGER.update();
                    }
                }
            });
            updateThread.start();

            Wallets.addWalletAddListener(this);
        }
        setContentView(R.layout.activity_wallet_overview2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if (!PepePay.OPTIONS.get(Options.STANDARD_FORM_CONTRACT, false)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final Dialog[] greeting = new Dialog[]{null};
            greeting[0] = builder.setMessage(R.string.tosGreeting).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PepePay.OPTIONS.set(Options.STANDARD_FORM_CONTRACT, true);

                    PepePay.OPTIONS.set(Options.USER_DEFINED_DEVICE_NAME, "");
                }
            }).setNeutralButton(R.string.showTos, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(WalletOverview2.this);
                    builder.setMessage(FileUtils.readAsset(Options.STANDARD_FORM_CONTRACT)).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            greeting[0].show();
                        }
                    }).setCancelable(false).create().show();
                }
            }).setCancelable(false).create();
            greeting[0].show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wallet_overview2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final Dialog[] greeting = new Dialog[]{null};
            greeting[0] = builder.setMessage(R.string.aboutText).setPositiveButton(R.string.confirm, null).setNeutralButton(R.string.showTos, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(WalletOverview2.this);
                    builder.setMessage(FileUtils.readAsset(Options.STANDARD_FORM_CONTRACT)).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            greeting[0].show();
                        }
                    }).create().show();
                }
            }).create();
            greeting[0].show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void privateWalletAdded(Wallet wallet) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSectionsPagerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void privateWalletGeneratingBegin() {

    }

    @Override
    public void nameChange(String walletID, String newName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshTabTitles();
            }
        });
    }

    @Override
    public void balanceChange(String walletID, Transaction newTransaction) {

    }

    @Override
    public void walletDeleted(Wallet wallet) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSectionsPagerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onPause() {
        lastTab = mViewPager.getCurrentItem();
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
        if (lastTab == null && mSectionsPagerAdapter.getCount() > 1) {
            mViewPager.setCurrentItem(1);
        } else if (mSectionsPagerAdapter.getCount() > 1){
            mViewPager.setCurrentItem(lastTab);
        } else {
            mViewPager.setCurrentItem(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateThread != null) {
            updateThread.interrupt();
        }
    }

    private void refreshTabTitles() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setText(mSectionsPagerAdapter.getPageTitle(i));
            }
        }
    }

    public void startActivity(Class<? extends Activity> clazz) {
        Intent myIntent = new Intent(this, clazz);
        this.startActivity(myIntent);
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return WalletCreateFragment.newInstance();
            } else {
                System.out.println(position + "  " + Wallets.getOwnWalletID(position - 1));
                return WalletInfoFragment.newInstance(Wallets.getOwnWalletID(position - 1));
            }
        }

        @Override
        public int getCount() {
            int size = Wallets.getOwnWalletsCount();
            return size + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.createWallet);
            } else {
                return Wallets.getName(Wallets.getOwnWalletID(position - 1));
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

}
